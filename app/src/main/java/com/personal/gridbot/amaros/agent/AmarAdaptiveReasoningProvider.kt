package com.personal.gridbot.amaros.agent

/**
 * Item 8 adaptive reasoning provider.
 *
 * Capability, resource, quality and deterministic routing are centralized here.
 */
class AmarAdaptiveReasoningProvider(
    private val catalog: AmarModelProviderCatalog,
    private val audit: AmarModelRoutingAudit = AmarModelRoutingAudit()
) {
    fun matchCapabilities(
        task: AmarModelTask,
        providers: List<AmarModelProviderProfile> = catalog.providers()
    ): List<AmarModelProviderProfile> {
        val required = task.requiredCapabilities()
        if (required.isEmpty()) return emptyList()

        return providers.filter { profile ->
            profile.available && required.all { capability ->
                profile.capabilities.contains(capability)
            }
        }
    }

    fun selectProvider(
        task: AmarModelTask,
        complexity: AmarModelComplexity,
        maxContextTokens: Int,
        maxRamMb: Int,
        minimumQuality: AmarModelQuality
    ): AmarRoutingDecision {
        val candidates = matchCapabilities(task).filter { profile ->
            profile.maxContextTokens >= maxContextTokens &&
                profile.estimatedRamMb <= maxRamMb &&
                profile.quality.meets(minimumQuality)
        }

        val selected = candidates.sortedWith(
            compareByDescending<AmarModelProviderProfile> { it.quality.rank }
                .thenBy { it.cost.rank }
                .thenBy { it.estimatedRamMb }
                .thenBy { it.provider.id }
        ).firstOrNull()

        val decision = if (selected == null) {
            AmarRoutingDecision(
                selectedProvider = null,
                reason = "NO_COMPATIBLE_PROVIDER",
                fallbackPlan = listOf("SECONDARY_PROVIDER", "FAIL_CLOSED"),
                decisionState = "FAIL_CLOSED"
            )
        } else {
            AmarRoutingDecision(
                selectedProvider = selected,
                reason = "CAPABILITY_RESOURCE_QUALITY_MATCH",
                fallbackPlan = listOf("SECONDARY_PROVIDER", "FAIL_CLOSED"),
                decisionState = "SELECTED"
            )
        }

        audit.record(
            level = 2,
            authority = "ADAPTIVE_MODEL_ROUTING",
            complexity = complexity,
            selectedProvider = selected?.provider?.id,
            decisionState = decision.decisionState,
            reason = decision.reason
        )
        return decision
    }

    suspend fun generate(
        context: AmarAgentContext,
        task: AmarModelTask,
        complexity: AmarModelComplexity,
        maxContextTokens: Int,
        maxRamMb: Int,
        minimumQuality: AmarModelQuality
    ): AmarAgentResponse {
        val decision = selectProvider(task, complexity, maxContextTokens, maxRamMb, minimumQuality)
        val provider = decision.selectedProvider?.provider
            ?: return AmarAgentResponse(
                answer = "لم يتوفر نموذج متوافق مع متطلبات هذه المهمة.",
                status = AmarAgentResponse.Status.ERROR
            )

        val result = provider.generate(
            AmarGenerationRequest(
                systemPrompt = "AMAR adaptive reasoning. Do not invent unsupported facts.",
                userPrompt = context.userText,
                maxTokens = 512
            )
        )
        return AmarAgentResponse(result.text, AmarAgentResponse.Status.READY, context.tools.map { it.id })
    }
}

data class AmarRoutingDecision(
    val selectedProvider: AmarModelProviderProfile?,
    val reason: String,
    val fallbackPlan: List<String>,
    val decisionState: String
)

private val AmarModelQuality.rank: Int
    get() = when (this) {
        AmarModelQuality.UNKNOWN -> 0
        AmarModelQuality.BASIC -> 1
        AmarModelQuality.STANDARD -> 2
        AmarModelQuality.HIGH -> 3
    }

private val AmarModelQuality.meets: (AmarModelQuality) -> Boolean
    get() = { actual -> this.rank >= actual.rank }

private val AmarModelCost.rank: Int
    get() = when (this) {
        AmarModelCost.LOW -> 1
        AmarModelCost.MEDIUM -> 2
        AmarModelCost.HIGH -> 3
    }

enum class AmarModelTask {
    UNKNOWN,
    SIMPLE_EXPLANATION,
    MULTI_FACTOR_ANALYSIS,
    TEXT_GENERATION;

    fun requiredCapabilities(): Set<AmarModelCapability> = when (this) {
        UNKNOWN -> emptySet()
        SIMPLE_EXPLANATION -> setOf(AmarModelCapability.SIMPLE_EXPLANATION)
        MULTI_FACTOR_ANALYSIS -> setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS)
        TEXT_GENERATION -> setOf(AmarModelCapability.TEXT_GENERATION)
    }
}
