package com.personal.gridbot.amaros.agent

/**
 * Stage 3 reasoning: bounded planning/review around the existing provider contract.
 * It records audit metadata only; private chain-of-thought is never stored.
 */
class AmarStageThreeReasoningEngine(
    private val reasoningProvider: AmarReasoningProvider,
    private val critic: AmarAgentCritic = AmarAgentCritic(),
    private val maxRevisions: Int = 1,
    private val maxTraceEntries: Int = 32
) {
    init {
        require(maxRevisions in 0..1)
        require(maxTraceEntries in 8..64)
    }

    suspend fun reason(
        request: AmarAgentRequest,
        evidence: List<ResearchFinding> = emptyList(),
        tools: List<AmarAgentTool> = emptyList()
    ): AmarStageThreeReasoningResult {
        val trace = mutableListOf<AmarReasoningTraceEntry>()
        val boundedEvidence = evidence.take(MAX_EVIDENCE)

        append(trace, "plan", AmarReasoningStep.PLANNING, "Bounded Stage 3 review plan", 1.0)

        val facts = boundedEvidence.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        val inferences = mutableListOf<String>()
        val assumptions = if (facts.isEmpty()) listOf("No external evidence supplied") else emptyList()
        val evidenceConfidence = evidenceConfidence(facts)
        append(
            trace,
            "evidence",
            AmarReasoningStep.EVIDENCE_CLASSIFICATION,
            "Facts=${facts.size}; inferences=deferred; assumptions=${assumptions.size}",
            evidenceConfidence
        )

        val contradiction = hasContradiction(facts)
        val contradictionConfidence = if (contradiction) 0.0 else 1.0
        append(
            trace,
            "conflict",
            AmarReasoningStep.CONTRADICTION_CHECK,
            if (contradiction) "Supporting and opposing evidence detected" else "No direct evidence conflict detected",
            contradictionConfidence,
            conflict = contradiction
        )

        var prompt = request.text
        var response = respond(prompt, request, boundedEvidence, tools)
        var critique = critic.review(response.answer, boundedEvidence, requireEvidence = boundedEvidence.isNotEmpty())
        var revisions = 0
        var stepConfidence = confidenceFor(critique.score, evidenceConfidence, contradiction)
        if (response.answer.isNotBlank()) inferences += response.answer

        append(trace, "draft-1", AmarReasoningStep.DRAFT, "Draft evaluated by critic", stepConfidence, critique.recommendation)

        while (!critique.accepted && revisions < maxRevisions) {
            revisions++
            val revisionReason = critique.issues.distinct().joinToString(", ")
            append(
                trace,
                "revision-$revisions",
                AmarReasoningStep.REVISION,
                "Bounded revision after critic failure",
                stepConfidence,
                "REVISE",
                revisionReason = revisionReason
            )

            prompt = request.text +
                "\n\nStage 3 review issues: " + revisionReason +
                "\nRevise only the answer. Separate supported facts from inferences, state assumptions when evidence is insufficient, " +
                "resolve conflicts only when supported by evidence, and preserve uncertainty."
            response = respond(prompt, request, boundedEvidence, tools)
            critique = critic.review(response.answer, boundedEvidence, requireEvidence = boundedEvidence.isNotEmpty())
            stepConfidence = confidenceFor(critique.score, evidenceConfidence, contradiction)
            append(
                trace,
                "revised-$revisions",
                AmarReasoningStep.REVISED_DRAFT,
                "Revised draft evaluated by critic",
                stepConfidence,
                critique.recommendation,
                revisionReason = revisionReason
            )
        }

        val finalConfidence = confidenceFor(critique.score, evidenceConfidence, contradiction)
        val finalResponse = if (critique.accepted) response else response.copy(
            status = AmarAgentResponse.Status.ERROR,
            answer = "لم يتم اعتماد الإجابة بعد: ${critique.issues.distinct().joinToString(", ")}"
        )
        append(
            trace,
            "final",
            AmarReasoningStep.FINAL_STATE,
            if (critique.accepted) "Final answer accepted" else "Final answer blocked",
            finalConfidence,
            if (critique.accepted) "PASS" else "BLOCK"
        )

        return AmarStageThreeReasoningResult(
            response = finalResponse,
            critique = critique,
            revisionCount = revisions,
            trace = trace.toList(),
            finalConfidence = finalConfidence,
            facts = facts.map { it.evidence },
            inferences = inferences.distinct(),
            assumptions = assumptions
        )
    }

    private suspend fun respond(
        prompt: String,
        request: AmarAgentRequest,
        evidence: List<ResearchFinding>,
        tools: List<AmarAgentTool>
    ): AmarAgentResponse = reasoningProvider.respond(
        AmarAgentContext(
            userText = prompt,
            tools = tools,
            executionAllowed = false,
            brokerAccessAllowed = false,
            requestedSourceCount = request.requestedSourceCount,
            maximumSourceCount = request.maximumSourceCount,
            requireCrossValidation = request.requireCrossValidation,
            requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
        )
    )

    private fun append(
        trace: MutableList<AmarReasoningTraceEntry>,
        id: String,
        step: AmarReasoningStep,
        claim: String,
        confidence: Double,
        criticResult: String? = null,
        conflict: Boolean = false,
        revisionReason: String = ""
    ) {
        if (trace.size >= maxTraceEntries) return
        trace += AmarReasoningTraceEntry(
            id = id,
            step = step,
            claim = claim,
            confidenceBefore = confidence,
            confidenceAfter = confidence,
            criticResult = criticResult,
            conflict = conflict,
            revisionReason = revisionReason
        )
    }

    private fun evidenceConfidence(evidence: List<ResearchFinding>): Double {
        if (evidence.isEmpty()) return 0.0
        val sourceCount = evidence.map { it.sourceUri.trim() }.distinct().size
        val authoritativeCount = evidence.count {
            it.authority == Authority.PRIMARY ||
                it.authority == Authority.OFFICIAL ||
                it.authority == Authority.PEER_REVIEWED
        }
        return (
            0.35 +
                sourceCount.coerceAtMost(4) * 0.10 +
                authoritativeCount.coerceAtMost(3) * 0.08
            ).coerceIn(0.0, 1.0)
    }

    private fun hasContradiction(evidence: List<ResearchFinding>): Boolean =
        evidence.any { it.stance == EvidenceStance.SUPPORTS } &&
            evidence.any { it.stance == EvidenceStance.OPPOSES }

    private fun confidenceFor(
        criticScore: Double,
        evidenceScore: Double,
        contradiction: Boolean
    ): Double = if (contradiction) {
        (criticScore * 0.35 + evidenceScore * 0.15).coerceIn(0.0, 1.0)
    } else {
        (criticScore * 0.65 + evidenceScore * 0.35).coerceIn(0.0, 1.0)
    }

    private companion object { const val MAX_EVIDENCE = 64 }
}

enum class AmarReasoningStep {
    PLANNING,
    EVIDENCE_CLASSIFICATION,
    CONTRADICTION_CHECK,
    DRAFT,
    REVISION,
    REVISED_DRAFT,
    FINAL_STATE
}

data class AmarReasoningTraceEntry(
    val id: String,
    val step: AmarReasoningStep,
    val claim: String,
    val confidenceBefore: Double,
    val confidenceAfter: Double,
    val criticResult: String? = null,
    val conflict: Boolean = false,
    val revisionReason: String = "",
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class AmarStageThreeReasoningResult(
    val response: AmarAgentResponse,
    val critique: AmarCritique,
    val revisionCount: Int,
    val trace: List<AmarReasoningTraceEntry> = emptyList(),
    val finalConfidence: Double = 0.0,
    val facts: List<String> = emptyList(),
    val inferences: List<String> = emptyList(),
    val assumptions: List<String> = emptyList()
)
