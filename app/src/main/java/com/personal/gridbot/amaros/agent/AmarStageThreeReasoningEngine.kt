package com.personal.gridbot.amaros.agent

/**
 * Stage 3 reasoning loop: bounded multi-step review with structured audit trace.
 * The trace contains audit metadata only; it never stores private chain-of-thought.
 */
class AmarStageThreeReasoningEngine(
    private val reasoningProvider: AmarReasoningProvider,
    private val critic: AmarAgentCritic = AmarAgentCritic(),
    private val maxRevisions: Int = 1,
    private val maxTraceSteps: Int = 32
) {
    init {
        require(maxRevisions in 0..1)
        require(maxTraceSteps in 8..128)
    }

    suspend fun reason(
        request: AmarAgentRequest,
        evidence: List<ResearchFinding> = emptyList(),
        tools: List<AmarAgentTool> = emptyList()
    ): AmarStageThreeReasoningResult {
        val trace = mutableListOf<AmarReasoningTraceStep>()
        var prompt = request.text
        var response = respond(prompt, request, evidence, tools)
        var critique = critic.review(response.answer, evidence, requireEvidence = evidence.isNotEmpty())
        var revisions = 0

        appendTrace(trace, traceStep(
            id = "draft-1",
            type = AmarReasoningStepType.DRAFT,
            claim = response.answer,
            evidence = evidence,
            confidence = confidenceFor(critique, evidence),
            criticResult = critique.recommendation
        ))
        appendTrace(trace, traceStep(
            id = "facts-1",
            type = AmarReasoningStepType.FACTS,
            claim = "Validated evidence: ${validEvidenceCount(evidence)}; independent sources: ${independentSourceCount(evidence)}",
            evidence = evidence,
            confidence = evidenceConfidence(evidence),
            factCount = validEvidenceCount(evidence)
        ))
        appendTrace(trace, traceStep(
            id = "conflict-1",
            type = AmarReasoningStepType.CONTRADICTION_CHECK,
            claim = "Evidence contradiction status",
            evidence = evidence,
            confidence = contradictionConfidence(evidence),
            conflict = hasContradiction(evidence),
            criticResult = if (hasContradiction(evidence)) "CONFLICT" else "CLEAR"
        ))
        appendTrace(trace, traceStep(
            id = "inference-1",
            type = AmarReasoningStepType.INFERENCE,
            claim = if (response.answer.isBlank()) "No supported inference" else "Response evaluated against available evidence",
            evidence = evidence,
            confidence = confidenceFor(critique, evidence),
            assumption = evidence.isEmpty()
        ))

        while (!critique.accepted && revisions < maxRevisions) {
            revisions++
            val revisionReason = critique.issues.distinct().joinToString(", ")
            appendTrace(trace, AmarReasoningTraceStep(
                id = "revision-$revisions",
                type = AmarReasoningStepType.REVISION,
                evidenceIds = evidenceIds(evidence),
                claim = "Bounded revision requested",
                confidenceBefore = confidenceFor(critique, evidence),
                confidenceAfter = null,
                conflict = hasContradiction(evidence),
                criticResult = "REVISE",
                revisionReason = revisionReason
            ))
            prompt = request.text +
                "\n\nStage 3 review issues: " + revisionReason +
                "\nRevise the answer, remove unsupported claims, separate facts from inferences, " +
                "state assumptions where evidence is insufficient, and preserve uncertainty."
            response = respond(prompt, request, evidence, tools)
            critique = critic.review(response.answer, evidence, requireEvidence = evidence.isNotEmpty())
            appendTrace(trace, traceStep(
                id = "draft-revised-$revisions",
                type = AmarReasoningStepType.REVISED_DRAFT,
                claim = response.answer,
                evidence = evidence,
                confidence = confidenceFor(critique, evidence),
                criticResult = critique.recommendation,
                revisionReason = revisionReason,
                confidenceBefore = trace.firstOrNull { it.id == "revision-$revisions" }?.confidenceBefore
            ))
        }

        val finalConfidence = confidenceFor(critique, evidence)
        val finalResponse = if (critique.accepted) response else response.copy(
            status = AmarAgentResponse.Status.ERROR,
            answer = "لم يتم اعتماد الإجابة بعد: ${critique.issues.distinct().joinToString(", ")}"
        )
        appendTrace(trace, AmarReasoningTraceStep(
            id = "final",
            type = AmarReasoningStepType.FINAL_STATE,
            evidenceIds = evidenceIds(evidence),
            claim = if (critique.accepted) "Final answer accepted" else "Final answer blocked",
            confidenceBefore = finalConfidence,
            confidenceAfter = finalConfidence,
            conflict = hasContradiction(evidence),
            criticResult = if (critique.accepted) "PASS" else "BLOCK",
            revisionReason = if (critique.accepted) "" else critique.issues.distinct().joinToString(", ")
        ))

        return AmarStageThreeReasoningResult(
            response = finalResponse,
            critique = critique,
            revisionCount = revisions,
            trace = trace.toList(),
            finalConfidence = finalConfidence,
            facts = evidence.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }.map { it.evidence },
            inferences = if (finalResponse.answer.isBlank()) emptyList() else listOf(finalResponse.answer),
            assumptions = if (evidence.isEmpty()) listOf("No external evidence supplied") else emptyList()
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

    private fun traceStep(
        id: String,
        type: AmarReasoningStepType,
        claim: String,
        evidence: List<ResearchFinding>,
        confidence: Double,
        criticResult: String? = null,
        conflict: Boolean = false,
        assumption: Boolean = false,
        factCount: Int = 0,
        revisionReason: String = "",
        confidenceBefore: Double? = null
    ) = AmarReasoningTraceStep(
        id = id,
        type = type,
        evidenceIds = evidenceIds(evidence),
        claim = claim,
        confidenceBefore = confidenceBefore ?: confidence,
        confidenceAfter = confidence,
        conflict = conflict,
        criticResult = criticResult,
        revisionReason = revisionReason,
        assumption = assumption,
        factCount = factCount
    )

    private fun appendTrace(trace: MutableList<AmarReasoningTraceStep>, step: AmarReasoningTraceStep) {
        if (trace.size < maxTraceSteps) trace += step
    }

    private fun evidenceIds(evidence: List<ResearchFinding>): List<String> = evidence
        .mapIndexed { index, finding -> finding.fingerprint.ifBlank { "evidence-$index" } }
        .distinct()

    private fun validEvidenceCount(evidence: List<ResearchFinding>): Int = evidence.count {
        it.sourceUri.isNotBlank() && it.evidence.isNotBlank()
    }

    private fun independentSourceCount(evidence: List<ResearchFinding>): Int = evidence
        .filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        .map { it.sourceUri.trim() }
        .distinct()
        .size

    private fun hasContradiction(evidence: List<ResearchFinding>): Boolean =
        evidence.any { it.stance == EvidenceStance.SUPPORTS } &&
            evidence.any { it.stance == EvidenceStance.OPPOSES }

    private fun evidenceConfidence(evidence: List<ResearchFinding>): Double {
        val valid = validEvidenceCount(evidence)
        if (valid == 0) return 0.0
        val independent = independentSourceCount(evidence)
        val authority = evidence.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
            .count { it.authority == Authority.PRIMARY || it.authority == Authority.OFFICIAL || it.authority == Authority.PEER_REVIEWED }
        val base = (valid.coerceAtMost(4) * 0.12) + (independent.coerceAtMost(4) * 0.10) + (authority.coerceAtMost(3) * 0.08)
        return (0.20 + base - if (hasContradiction(evidence)) 0.25 else 0.0).coerceIn(0.0, 1.0)
    }

    private fun contradictionConfidence(evidence: List<ResearchFinding>): Double =
        if (hasContradiction(evidence)) 0.0 else if (evidence.isEmpty()) 0.5 else 1.0

    private fun confidenceFor(critique: AmarCritique, evidence: List<ResearchFinding>): Double =
        ((critique.score * 0.70) + (evidenceConfidence(evidence) * 0.30)).coerceIn(0.0, 1.0)
}

enum class AmarReasoningStepType {
    DRAFT,
    FACTS,
    INFERENCE,
    CONTRADICTION_CHECK,
    REVISION,
    REVISED_DRAFT,
    FINAL_STATE
}

data class AmarReasoningTraceStep(
    val id: String,
    val type: AmarReasoningStepType,
    val evidenceIds: List<String> = emptyList(),
    val claim: String,
    val confidenceBefore: Double,
    val confidenceAfter: Double?,
    val conflict: Boolean = false,
    val criticResult: String? = null,
    val revisionReason: String = "",
    val assumption: Boolean = false,
    val factCount: Int = 0,
    val timestampEpochMs: Long = System.currentTimeMillis()
)

data class AmarStageThreeReasoningResult(
    val response: AmarAgentResponse,
    val critique: AmarCritique,
    val revisionCount: Int,
    val trace: List<AmarReasoningTraceStep> = emptyList(),
    val finalConfidence: Double = 0.0,
    val facts: List<String> = emptyList(),
    val inferences: List<String> = emptyList(),
    val assumptions: List<String> = emptyList()
)
