package com.personal.gridbot.amaros.agent

/** Stage 2: structured multi-role deliberation over one shared evidence snapshot. */
class AmarStageTwoEngine(
    private val reasoningProvider: AmarReasoningProvider,
    private val directionEngine: AmarDecisionDirectionEngine = AmarDecisionDirectionEngine(),
    private val coordinator: AmarDeliberationCoordinator = AmarDeliberationCoordinator()
) {
    suspend fun deliberate(
        question: String,
        evidence: List<ResearchFinding>,
        marketSnapshot: AmarMarketSnapshot? = null
    ): AmarStageTwoResult {
        require(question.isNotBlank())

        val sanitizedEvidence = evidence
            .filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
            .distinctBy { evidenceKey(it) }
            .take(MAX_EVIDENCE)

        val context = AmarAnalysisContext(question.trim(), marketSnapshot, sanitizedEvidence)
        val roles = listOf(
            AmarReasoningAnalystRole("ANALYST", "حلّل المعطيات فنيًا ومنطقيًا وحدد الاتجاه الذي تدعمه الأدلة فقط."),
            AmarReasoningAnalystRole("ADVISOR", "راجع المعطيات بشكل مستقل، اختبر البدائل والافتراضات وحدد اتجاهًا واحدًا أو UNKNOWN."),
            AmarReasoningAnalystRole("RISK_GUARD", "ابحث عن المخاطر والتناقضات ونقاط الفشل وحدد الاتجاه فقط إذا كان مبررًا."),
            AmarReasoningAnalystRole("DECISION_CONFIRMATION", "تحقق من الأدلة والتعارضات؛ لا تؤكد اتجاهًا إلا إذا كان قابلًا للدفاع عنه.")
        )

        val base = coordinator.deliberate(context, roles)
        val independentSourceCount = sanitizedEvidence.mapNotNull(::sourceHost).distinct().size
        val evidenceGateConflict = if (independentSourceCount < MIN_INDEPENDENT_SOURCES) {
            "insufficient_independent_evidence"
        } else null
        val conflicts = (base.conflicts + listOfNotNull(evidenceGateConflict)).distinct()
        val approved = base.approvedForSimulation && evidenceGateConflict == null
        val deliberation = base.copy(conflicts = conflicts, approvedForSimulation = approved)
        val directionCounts = deliberation.reports
            .map { it.direction }
            .filter { it != AmarDecisionDirection.UNKNOWN }
            .groupingBy { it }
            .eachCount()

        return AmarStageTwoResult(
            deliberation = deliberation,
            chosenDirection = deliberation.consensusDirection,
            directionCounts = directionCounts,
            confidence = deliberation.confidence.coerceIn(0.0, 1.0),
            approvedForSimulation = approved,
            executionAllowed = false,
            brokerAccessAllowed = false
        )
    }

    private inner class AmarReasoningAnalystRole(
        override val id: String,
        private val mandate: String
    ) : AmarAnalystRole {
        override suspend fun analyze(context: AmarAnalysisContext): AmarRoleReport {
            val evidenceText = context.evidence.take(MAX_PROMPT_EVIDENCE).joinToString("\n") {
                "- ${it.sourceTitle} | ${it.sourceUri} | ${it.evidence} | stance=${it.stance} | authority=${it.authority}"
            }.ifBlank { "لا توجد أدلة خارجية متاحة." }
            val marketText = context.marketSnapshot?.let {
                "symbol=${it.symbol};timeframe=${it.timeframe};candles=${it.candles.size};quote=${it.quote}"
            } ?: "NO_MARKET_SNAPSHOT"
            val response = reasoningProvider.respond(
                AmarAgentContext(
                    userText = buildString {
                        appendLine("STAGE_2_ROLE=$id")
                        appendLine("MANDATE=$mandate")
                        appendLine("QUESTION=${context.question}")
                        appendLine("MARKET_SNAPSHOT=$marketText")
                        appendLine("EVIDENCE=")
                        appendLine(evidenceText)
                        appendLine("REQUIRED_OUTPUT_DIRECTION=BUY|SELL|HOLD|UNKNOWN")
                        appendLine("EXECUTION_ALLOWED=false")
                        appendLine("BROKER_ACCESS_ALLOWED=false")
                        appendLine("أعطِ استنتاجًا واضحًا، واذكر الاعتراضات والمخاطر، ولا تنفذ أي إجراء.")
                    },
                    tools = emptyList(),
                    executionAllowed = false,
                    brokerAccessAllowed = false,
                    requireCrossValidation = true,
                    requireBacktestWhenApplicable = true
                )
            )
            val answer = response.answer.trim()
            val direction = directionEngine.detect(answer)
            val confidence = if (answer.isBlank() || direction == AmarDecisionDirection.UNKNOWN) {
                0.0
            } else {
                confidenceFromEvidence(context.evidence, direction)
            }
            val support = context.evidence
                .filter { stanceSupports(it.stance, direction) }
                .map(::evidenceKey)
                .take(MAX_REPORTED_EVIDENCE)
            val opposition = context.evidence
                .filter { stanceOpposes(it.stance, direction) }
                .map(::evidenceKey)
                .take(MAX_REPORTED_EVIDENCE)

            return AmarRoleReport(
                roleId = id,
                conclusion = answer,
                confidence = confidence,
                direction = direction,
                supportingEvidence = support,
                opposingEvidence = opposition,
                risks = buildList {
                    if (direction == AmarDecisionDirection.UNKNOWN) add("unresolved_direction")
                    if (opposition.isNotEmpty()) add("opposing_evidence_present")
                    if (context.evidence.isEmpty()) add("no_external_evidence")
                    if (support.isEmpty()) add("no_direct_supporting_evidence")
                    add("execution_disabled")
                }
            )
        }

        private fun confidenceFromEvidence(
            evidence: List<ResearchFinding>,
            direction: AmarDecisionDirection
        ): Double {
            if (evidence.isEmpty()) return 0.0
            val relevant = evidence.filter { stanceSupports(it.stance, direction) || stanceOpposes(it.stance, direction) }
            if (relevant.isEmpty()) return 0.0
            val support = relevant.count { stanceSupports(it.stance, direction) }
            val oppose = relevant.count { stanceOpposes(it.stance, direction) }
            if (support == 0) return 0.0
            val weightedQuality = relevant.map { authorityWeight(it.authority) }.average()
            val balance = support.toDouble() / (support + oppose).toDouble()
            return (CONFIDENCE_SUPPORT_WEIGHT * balance + CONFIDENCE_QUALITY_WEIGHT * weightedQuality)
                .coerceIn(0.0, 1.0)
        }

        private fun stanceSupports(stance: EvidenceStance, direction: AmarDecisionDirection): Boolean = when (direction) {
            AmarDecisionDirection.HOLD -> stance == EvidenceStance.MIXED
            AmarDecisionDirection.BUY, AmarDecisionDirection.SELL -> stance == EvidenceStance.SUPPORTS
            AmarDecisionDirection.UNKNOWN -> false
        }

        private fun stanceOpposes(stance: EvidenceStance, direction: AmarDecisionDirection): Boolean = when (direction) {
            AmarDecisionDirection.HOLD -> false
            AmarDecisionDirection.BUY, AmarDecisionDirection.SELL -> stance == EvidenceStance.OPPOSES
            AmarDecisionDirection.UNKNOWN -> false
        }

        private fun authorityWeight(authority: Authority): Double = when (authority) {
            Authority.PRIMARY -> 1.0
            Authority.OFFICIAL -> 0.95
            Authority.PEER_REVIEWED -> 0.90
            Authority.REPUTABLE -> 0.75
            Authority.COMMUNITY -> 0.40
            Authority.UNKNOWN -> 0.15
        }
    }

    private fun evidenceKey(finding: ResearchFinding): String =
        finding.fingerprint.ifBlank { "${finding.sourceUri}|${finding.sourceTitle}|${finding.evidence}" }

    private fun sourceHost(uri: String): String? = runCatching {
        java.net.URI(uri).host?.lowercase()?.removePrefix("www.")
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private companion object {
        const val MAX_EVIDENCE = 100
        const val MAX_PROMPT_EVIDENCE = 40
        const val MAX_REPORTED_EVIDENCE = 10
        const val MIN_INDEPENDENT_SOURCES = 2
        const val CONFIDENCE_SUPPORT_WEIGHT = 0.55
        const val CONFIDENCE_QUALITY_WEIGHT = 0.45
    }
}

data class AmarStageTwoResult(
    val deliberation: AmarDeliberationResult,
    val chosenDirection: AmarDecisionDirection,
    val directionCounts: Map<AmarDecisionDirection, Int>,
    val confidence: Double,
    val approvedForSimulation: Boolean,
    val executionAllowed: Boolean,
    val brokerAccessAllowed: Boolean
)
