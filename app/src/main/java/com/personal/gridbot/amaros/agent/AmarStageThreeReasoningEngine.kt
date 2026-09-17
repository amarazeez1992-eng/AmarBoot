package com.personal.gridbot.amaros.agent

/** Stage 3 reasoning loop: draft -> critic -> one bounded revision, never execution. */
class AmarStageThreeReasoningEngine(
    private val reasoningProvider: AmarReasoningProvider,
    private val critic: AmarAgentCritic = AmarAgentCritic(),
    private val maxRevisions: Int = 1
) {
    init { require(maxRevisions in 0..1) }

    suspend fun reason(
        request: AmarAgentRequest,
        evidence: List<ResearchFinding> = emptyList(),
        tools: List<AmarAgentTool> = emptyList()
    ): AmarStageThreeReasoningResult {
        var prompt = request.text
        var response = respond(prompt, request, evidence, tools)
        var critique = critic.review(response.answer, evidence, requireEvidence = evidence.isNotEmpty())
        var revisions = 0

        while (!critique.accepted && revisions < maxRevisions) {
            revisions++
            prompt = request.text + "\n\nStage 3 critique: " + critique.issues.joinToString(", ") +
                "\nRevise the answer, remove unsupported claims, and preserve uncertainty where evidence is insufficient."
            response = respond(prompt, request, evidence, tools)
            critique = critic.review(response.answer, evidence, requireEvidence = evidence.isNotEmpty())
        }

        val finalResponse = if (critique.accepted) response else response.copy(
            status = AmarAgentResponse.Status.ERROR,
            answer = "لم يتم اعتماد الإجابة بعد: ${critique.issues.distinct().joinToString(", ")}"
        )
        return AmarStageThreeReasoningResult(finalResponse, critique, revisions)
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
}

data class AmarStageThreeReasoningResult(
    val response: AmarAgentResponse,
    val critique: AmarCritique,
    val revisionCount: Int
)
