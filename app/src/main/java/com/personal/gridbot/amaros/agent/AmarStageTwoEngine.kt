package com.personal.gridbot.amaros.agent

/**
 * Stage 2: real multi-role deliberation over one shared evidence snapshot.
 * Roles are isolated, receive role-specific instructions, and have no execution authority.
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
        val context = AmarAnalysisContext(
            question = question,
            marketSnapshot = marketSnapshot,
            evidence = evidence
        )
        val roles = listOf(
            AmarReasoningAnalystRole("ANALYST", "حلّل المعطيات فنيًا ومنطقيًا وحدد ما تدعمه الأدلة فقط."),
            AmarReasoningAnalystRole("ADVISOR", "راجع رأي المحلل بزاوية مستقلة وابحث عن بدائل واعتراضات."),
            AmarReasoningAnalystRole("RISK_GUARD", "هاجم الاستنتاج وابحث عن المخاطر والتناقضات وأي سبب يمنع الاعتماد."),
            AmarReasoningAnalystRole("DECISION_CONFIRMATION", "تحقق من توافق النتيجة مع الأدلة ولا تعتمدها عند وجود تعارض جوهري.")
        )
        val deliberation = coordinator.deliberate(context, roles)
        val directionalReports = deliberation.reports.map { report ->
            directionEngine.detect(report.conclusion)
        }
        val directionCounts = directionalReports
            .filter { it != AmarDecisionDirection.UNKNOWN }
            .groupingBy { it }
            .eachCount()
        val chosenDirection = directionCounts.entries
            .sortedWith(compareByDescending<Map.Entry<AmarDecisionDirection, Int>> { it.value }.thenBy { it.key.name })
            .firstOrNull()?.key ?: AmarDecisionDirection.UNKNOWN
        val directionConflict = directionCounts.keys.size > 1
        val safeConfidence = deliberation.confidence.coerceIn(0.0, 1.0)
        val approved = deliberation.approvedForSimulation && !directionConflict
        return AmarStageTwoResult(
            deliberation = deliberation,
            chosenDirection = chosenDirection,
            directionCounts = directionCounts,
            confidence = safeConfidence,
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
            val evidenceText = context.evidence.take(40).joinToString("\n") {
                "- ${it.sourceTitle} | ${it.sourceUri} | ${it.evidence} | stance=${it.stance}"
            }.ifBlank { "لا توجد أدلة خارجية متاحة." }
            val response = reasoningProvider.respond(
                AmarAgentContext(
                    userText = buildString {
                        appendLine("STAGE_2_ROLE=$id")
                        appendLine("MANDATE=$mandate")
                        appendLine("QUESTION=${context.question}")
                        appendLine("EVIDENCE=")
                        appendLine(evidenceText)
                        appendLine("EXECUTION_ALLOWED=false")
                        appendLine("BROKER_ACCESS_ALLOWED=false")
                        appendLine("أعطِ استنتاجًا واضحًا مع ذكر الاعتراضات والمخاطر، دون تنفيذ أي إجراء.")
                    },
                    tools = emptyList(),
                    executionAllowed = false,
                    brokerAccessAllowed = false,
                    requireCrossValidation = true,
                    requireBacktestWhenApplicable = true
                )
            )
            val answer = response.answer.trim()
            val hasOpposition = context.evidence.any { it.stance == EvidenceStance.OPPOSES }
            val baseConfidence = if (answer.isBlank()) 0.0 else 0.85
            val confidence = if (hasOpposition) baseConfidence - 0.15 else baseConfidence
            return AmarRoleReport(
                roleId = id,
                conclusion = answer,
                confidence = confidence.coerceIn(0.0, 1.0),
                supportingEvidence = context.evidence.filter { it.stance == EvidenceStance.SUPPORTS }.map { it.fingerprint }.take(10),
                opposingEvidence = context.evidence.filter { it.stance == EvidenceStance.OPPOSES }.map { it.fingerprint }.take(10),
                risks = buildList {
                    if (hasOpposition) add("opposing_evidence_present")
                    add("execution_disabled")
                }
            )
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
