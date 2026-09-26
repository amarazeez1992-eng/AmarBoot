package com.personal.gridbot.amaros.agent

class AmarReasoningRouter(
    private val local: AmarReasoningProvider,
    private val adaptive: AmarAdaptiveReasoningProvider,
    private val classifier: AmarModelTaskClassifier,
    private val complexity: AmarModelComplexityEstimator,
    private val audit: AmarModelRoutingAudit,
    private val minimumQuality: AmarModelQuality = AmarModelQuality.STANDARD,
    private val maxContextTokens: Int = 4096,
    private val maxRamMb: Int = 4096,
    private val maxCost: AmarModelCost? = null
) : AmarReasoningProvider {
    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
        val task = classifier.classify(context)
        val level = complexity.estimate(task, AmarModelComplexityContext(
            context.userText, (context.userText.length / 4).coerceAtLeast(1),
            context.userText.count { it == '\n' }, context.tools.size
        ))
        if (level == AmarModelComplexity.UNKNOWN) {
            audit.record(1, "REASONING_PROVIDER", level, null, "FAIL_CLOSED", "UNKNOWN_TASK")
            return AmarAgentResponse("لم يتم تصنيف متطلبات المهمة بصورة كافية.", AmarAgentResponse.Status.ERROR)
        }
        if (level == AmarModelComplexity.LOW) {
            audit.record(1, "REASONING_PROVIDER", level, "AmarLocalReasoning", "SELECTED", "LOW complexity")
            return local.respond(context)
        }
        val outcome = adaptive.generateWithProvider(context, task, level, maxContextTokens, maxRamMb, minimumQuality, maxCost)
        audit.record(1, "REASONING_PROVIDER", level, outcome.providerId,
            if (outcome.response.status == AmarAgentResponse.Status.ERROR) "FAIL_CLOSED" else "SELECTED",
            if (outcome.response.status == AmarAgentResponse.Status.ERROR) "ADAPTIVE_UNAVAILABLE" else "ADAPTIVE_SELECTED")
        return outcome.response
    }
}
