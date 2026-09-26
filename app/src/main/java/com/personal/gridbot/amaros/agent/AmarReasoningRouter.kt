package com.personal.gridbot.amaros.agent

class AmarReasoningRouter(
    private val classifier: AmarModelTaskClassifier = AmarModelTaskClassifier(),
    private val complexityEstimator: AmarModelComplexityEstimator = AmarModelComplexityEstimator(),
    private val local: AmarReasoningProvider = AmarLocalReasoning(),
    private val adaptive: AmarAdaptiveReasoningProvider,
    private val audit: AmarModelRoutingAudit = AmarModelRoutingAudit(),
    private val minimumQuality: AmarModelQuality = AmarModelQuality.STANDARD,
    private val maxContextTokens: Int = 4096,
    private val maxRamMb: Int = 4096
) : AmarReasoningProvider {

    override suspend fun respond(context: AmarAgentContext): AmarAgentResponse {
        val task = classifier.classify(context)
        val complexity = complexityEstimator.estimate(
            task,
            AmarModelComplexityContext(
                text = context.userText,
                contextTokens = estimateTokens(context.userText),
                evidenceCount = context.userText.count { it == '\n' },
                constraintCount = context.tools.count()
            )
        )

        if (complexity == AmarModelComplexity.LOW) {
            audit.record(
                level = 1,
                authority = "REASONING_PROVIDER",
                complexity = complexity,
                selectedProvider = "AmarLocalReasoning",
                decisionState = "SELECTED",
                reason = "LOW complexity"
            )
            return local.respond(context)
        }

        val response = adaptive.generate(
            context = context,
            task = task,
            complexity = complexity,
            maxContextTokens = maxContextTokens,
            maxRamMb = maxRamMb,
            minimumQuality = minimumQuality
        )
        audit.record(
            level = 1,
            authority = "REASONING_PROVIDER",
            complexity = complexity,
            selectedProvider = responseProviderId(response),
            decisionState = if (response.status == AmarAgentResponse.Status.ERROR) "FAIL_CLOSED" else "SELECTED",
            reason = if (response.status == AmarAgentResponse.Status.ERROR) "ADAPTIVE_UNAVAILABLE" else "ADAPTIVE_SELECTED"
        )
        return response
    }

    private fun estimateTokens(text: String): Int = (text.length / 4).coerceAtLeast(1)

    private fun responseProviderId(response: AmarAgentResponse): String? =
        if (response.status == AmarAgentResponse.Status.ERROR) null else "ADAPTIVE_MODEL_PROVIDER"
}
