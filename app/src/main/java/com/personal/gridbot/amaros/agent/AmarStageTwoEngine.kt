package com.personal.gridbot.amaros.agent

/**
 * Stage 2: structured multi-role deliberation over one shared evidence snapshot.
 * The current provider may be shared, but every role has a separate mandate and
 * produces a structured direction. No role receives execution authority.
 */
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
        val context = AmarAnalysisContext(
            question = question.trim(),
            marketSnapshot = marketSnapshot,
            evidence = evidence.distinctBy { it.fingerprint }.take(100)
        )
        val roles = listOf(
            AmarReasoningAnalystRole("ANALYST", "حلّل المعطيات فنيًا ومنطقيًا وحدد الاتجاه الذي تدعمه الأدلة فقط."),
            AmarReasoningAnalystRole("ADVISOR", "راجع المعطيات بشكل مستقل، اختبر البدائل والافتراضات وحدد اتجاهًا واحدًا أو UNKNOWN."),
            AmarReasoningAnalystRole("RISK_GUARD", "ابحث عن المخاطر والتناقضات ونقاط الفشل وحدد الاتجاه فقط إذا كان مبررًا."),
            AmarReasoningAnalystRole("DECISION_CONFIRMATION", "تحقق من الأدلة والتعارضات؛ لا تؤكد اتجاهًا إلا إذا كان قابلًا للدفاع عنه.")
        )

        val deliberation = coordinator.deliberate(context, roles)
        val directionCounts = deliberation.reports
            .map { it.direction }
            .filter { it != AmarDecisionDirection.UNKNOWN }
            .groupingBy { it }
            .eachCount()
        val chosenDirection = deliberation.consensusDirection
        val safeConfidence = deliberation.confidence.coerceIn(0.0, 1.0)

        return AmarStageTwoResult(
            deliberation = deliberation,
            chosenDirection = chosenDirection,
            directionCounts = directionCounts,
            confidence = safeConfidence,
            approvedForSimulation = deliberation.approvedForSimulation,
            executionAllowed = false,
            brokerAccessAllowed = false
        )
    }

    private inner class AmarReasoningAnalystRole(
        override val id: String,
        private val mandate: String
    ) : AmarAnalystRole {
        override suspend fun analyze(context: AmarAnalysisContext): AmarRoleReport {
            val evidenceText = context.evidence.take(40).joinToString("\n") {
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
            val confidence = when {
                answer.isBlank() -> 0.0
                direction == AmarDecisionDirection.UNKNOWN -> 0.0
                else -> confidenceFromEvidence(context.evidence, direction)
            }
            val support = context.evidence
                .filter { stanceSupports(it.stance, direction) }
                .map { it.fingerprint }
                .take(10)
            val opposition = context.evidence
                .filter { stanceOpposes(it.stance, direction) }
                .map { it.fingerprint }
                .take(10)
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
            val weighted = relevant.sumOf { authorityWeight(it.authority) }
            val support = relevant.count { stanceSupports(it.stance, direction) }
            val oppose = relevant.count { stanceOpposes(it.stance, direction) }
            val balance = support.toDouble() / (support + oppose).toDouble()
            val quality = (weighted / relevant.size.toDouble()).coerceIn(0.0, 1.0)
            return (0.55 * balance + 0.45 * quality).coerceIn(0.0, 1.0)
        }

        private fun stanceSupports(stance: EvidenceStance, direction: AmarDecisionDirection): Boolean =
            when (direction) {
                AmarDecisionDirection.HOLD -> stance == EvidenceStance.MIXED || stance == EvidenceStance.UNKNOWN
                AmarDecisionDirection.BUY,
                AmarDecisionDirection.SELL -> stance == EvidenceStance.SUPPORTS
                AmarDecisionDirection.UNKNOWN -> false
            }

        private fun stanceOpposes(stance: EvidenceStance, direction: AmarDecisionDirection): Boolean =
            when (direction) {
                AmarDecisionDirection.HOLD -> false
                AmarDecisionDirection.BUY,
                AmarDecisionDirection.SELL -> stance == EvidenceStance.OPPOSES
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
