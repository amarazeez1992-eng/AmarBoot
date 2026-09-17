package com.personal.gridbot.amaros.agent

/**
 * Stage 3 reasoning: bounded, auditable decision stages around the existing provider contract.
 * Only safe summaries are recorded; private chain-of-thought is never stored.
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
        val validEvidence = boundedEvidence.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }

        // 1) Plan: explicit bounded stages, without exposing private reasoning.
        val planConfidence = if (request.text.isNotBlank()) 1.0 else 0.0
        append(trace, "plan", AmarReasoningStep.PLANNING, "Bounded plan: classify evidence -> draft -> independent confidence -> conflict check -> critic -> final", planConfidence)

        // 2) Evidence classification: facts are directly supplied valid findings.
        val facts = validEvidence.map { it.evidence }.distinct()
        val assumptions = if (validEvidence.isEmpty()) listOf("No external evidence supplied; answer must preserve uncertainty") else emptyList()
        val evidenceConfidence = evidenceConfidence(validEvidence)
        append(
            trace,
            "evidence",
            AmarReasoningStep.EVIDENCE_CLASSIFICATION,
            "Facts=${facts.size}; assumptions=${assumptions.size}; invalid=${boundedEvidence.size - validEvidence.size}",
            evidenceConfidence,
            criticResult = "FACTS_SEPARATED"
        )

        // 3) Conflict check is independent from the critic score.
        val contradiction = hasContradiction(validEvidence)
        val contradictionConfidence = if (contradiction) 0.0 else 1.0
        append(
            trace,
            "conflict",
            AmarReasoningStep.CONTRADICTION_CHECK,
            if (contradiction) "Supporting and opposing evidence detected" else "No direct evidence conflict detected",
            contradictionConfidence,
            conflict = contradiction,
            criticResult = if (contradiction) "CONFLICT" else "CLEAR"
        )

        var prompt = request.text
        var response = respond(prompt, request, boundedEvidence, tools)
        var critique = critic.review(response.answer, boundedEvidence, requireEvidence = boundedEvidence.isNotEmpty())
        var revisions = 0

        // 4) Independent confidence is calculated from evidence quality and critic outcome separately.
        var independentConfidence = independentConfidence(validEvidence, contradiction)
        var criticConfidence = critique.score.coerceIn(0.0, 1.0)
        var finalConfidence = combineConfidence(independentConfidence, criticConfidence, contradiction)
        append(trace, "draft-1", AmarReasoningStep.DRAFT, "Draft evaluated without storing private reasoning", criticConfidence, critique.recommendation)
        if (response.answer.isNotBlank()) {
            append(trace, "inference-1", AmarReasoningStep.INFERENCE_CLASSIFICATION, "Provider answer recorded as inference, not fact", independentConfidence, "INFERENCE")
        }

        // Re-reason only after critic failure and never beyond the configured bound.
        while (!critique.accepted && revisions < maxRevisions) {
            revisions++
            val revisionReason = critique.issues.distinct().joinToString(", ")
            append(trace, "revision-$revisions", AmarReasoningStep.REVISION, "Bounded revision after critic failure", criticConfidence, "REVISE", revisionReason = revisionReason)

            prompt = request.text +
                "\n\nStage 3 review issues: " + revisionReason +
                "\nRevise only the answer. Separate supported facts from inferences, state assumptions when evidence is insufficient, " +
                "resolve conflicts only when supported by evidence, and preserve uncertainty."
            response = respond(prompt, request, boundedEvidence, tools)
            critique = critic.review(response.answer, boundedEvidence, requireEvidence = boundedEvidence.isNotEmpty())
            criticConfidence = critique.score.coerceIn(0.0, 1.0)
            independentConfidence = independentConfidence(validEvidence, contradiction)
            finalConfidence = combineConfidence(independentConfidence, criticConfidence, contradiction)
            append(trace, "revised-$revisions", AmarReasoningStep.REVISED_DRAFT, "Revised draft independently re-evaluated", finalConfidence, critique.recommendation, revisionReason = revisionReason)
        }

        // 5) Final state: acceptance is critic-gated; contradiction remains a first-class conflict.
        finalConfidence = combineConfidence(independentConfidence, critique.score, contradiction)
        val finalResponse = if (critique.accepted) response else response.copy(
            status = AmarAgentResponse.Status.ERROR,
            answer = "لم يتم اعتماد الإجابة بعد: ${critique.issues.distinct().joinToString(", ")}"
        )
        append(trace, "final", AmarReasoningStep.FINAL_STATE, if (critique.accepted) "Final answer accepted" else "Final answer blocked", finalConfidence, if (critique.accepted) "PASS" else "BLOCK", conflict = contradiction)

        return AmarStageThreeReasoningResult(
            response = finalResponse,
            critique = critique,
            revisionCount = revisions,
            trace = trace.toList(),
            finalConfidence = finalConfidence,
            facts = facts,
            inferences = if (response.answer.isBlank()) emptyList() else listOf(response.answer),
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
            confidenceBefore = confidence.coerceIn(0.0, 1.0),
            confidenceAfter = confidence.coerceIn(0.0, 1.0),
            criticResult = criticResult,
            conflict = conflict,
            revisionReason = revisionReason
        )
    }

    private fun evidenceConfidence(evidence: List<ResearchFinding>): Double {
        if (evidence.isEmpty()) return 0.0
        val independentSources = evidence.map { it.sourceUri.trim() }.distinct().size
        val authority = evidence.map { authorityScore(it.authority) }.average()
        val independence = (independentSources.toDouble() / 4.0).coerceAtMost(1.0)
        return (authority * 0.6 + independence * 0.4).coerceIn(0.0, 1.0)
    }

    private fun authorityScore(authority: Authority): Double = when (authority) {
        Authority.PRIMARY -> 1.0
        Authority.OFFICIAL -> 0.95
        Authority.PEER_REVIEWED -> 0.9
        Authority.REPUTABLE -> 0.75
        Authority.COMMUNITY -> 0.45
        Authority.UNKNOWN -> 0.2
    }

    private fun independentConfidence(evidence: List<ResearchFinding>, contradiction: Boolean): Double {
        if (evidence.isEmpty()) return 0.0
        val sources = evidence.map { it.sourceUri.trim() }.filter { it.isNotBlank() }.distinct().size
        val uniqueness = evidence.map { it.fingerprint.ifBlank { "${it.sourceUri}|${it.evidence}" } }.distinct().size
        val sourceScore = (sources.toDouble() / 3.0).coerceAtMost(1.0)
        val evidenceScore = (uniqueness.toDouble() / 4.0).coerceAtMost(1.0)
        val conflictPenalty = if (contradiction) 0.45 else 0.0
        return (sourceScore * 0.55 + evidenceScore * 0.45 - conflictPenalty).coerceIn(0.0, 1.0)
    }

    private fun combineConfidence(independent: Double, criticScore: Double, contradiction: Boolean): Double {
        val conflictPenalty = if (contradiction) 0.20 else 0.0
        return (independent * 0.55 + criticScore.coerceIn(0.0, 1.0) * 0.45 - conflictPenalty).coerceIn(0.0, 1.0)
    }

    private fun hasContradiction(evidence: List<ResearchFinding>): Boolean =
        evidence.any { it.stance == EvidenceStance.SUPPORTS } && evidence.any { it.stance == EvidenceStance.OPPOSES }

    private companion object { const val MAX_EVIDENCE = 64 }
}

enum class AmarReasoningStep {
    PLANNING,
    EVIDENCE_CLASSIFICATION,
    CONTRADICTION_CHECK,
    DRAFT,
    INFERENCE_CLASSIFICATION,
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
