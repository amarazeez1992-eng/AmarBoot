package com.personal.gridbot.amaros.agent

/**
 * Item 8 adaptive reasoning provider.
 *
 * Capability, resource, quality and deterministic routing are centralized here.
 */
class AmarAdaptiveReasoningProvider(
    private val catalog: AmarModelProviderCatalog,
    private val audit: AmarModelRoutingAudit = AmarModelRoutingAudit(),
    private val resilience: AmarProviderFallbackPolicy = AmarProviderFallbackPolicy(
        health = AmarProviderHealthMonitor(),
        classifier = AmarProviderFailureClassifier(),
        integrity = AmarProviderResultIntegrity(),
        audit = AmarProviderRecoveryAudit()
    )
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
        minimumQuality: AmarModelQuality,
        maxCost: AmarModelCost? = null
    ): AmarRoutingDecision {
        val candidates = matchCapabilities(task).filter { profile ->
            profile.maxContextTokens >= maxContextTokens &&
                profile.estimatedRamMb <= maxRamMb &&
                profile.quality.meets(minimumQuality) &&
                profile.cost.within(maxCost)
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
    ): AmarAgentResponse = generateWithProvider(
        context, task, complexity, maxContextTokens, maxRamMb, minimumQuality, null
    ).response

    internal suspend fun generateWithProvider(
        context: AmarAgentContext,
        task: AmarModelTask,
        complexity: AmarModelComplexity,
        maxContextTokens: Int,
        maxRamMb: Int,
        minimumQuality: AmarModelQuality,
        maxCost: AmarModelCost?
    ): AdaptiveGenerationOutcome {
        val candidates = matchCapabilities(task)
            .filter { profile ->
                profile.maxContextTokens >= maxContextTokens &&
                    profile.estimatedRamMb <= maxRamMb &&
                    profile.quality.meets(minimumQuality) &&
                    profile.cost.within(maxCost)
            }
            .sortedWith(
                compareByDescending<AmarModelProviderProfile> { it.quality.rank }
                    .thenBy { it.cost.rank }
                    .thenBy { it.estimatedRamMb }
                    .thenBy { it.provider.id }
            )

        val request = AmarGenerationRequest(
            systemPrompt = "AMAR adaptive reasoning. Do not invent unsupported facts.",
            userPrompt = context.userText,
            maxTokens = 512
        )
        val recovery = resilience.generate(
            operationId = "adaptive:" + task.name + ":" + context.userText.hashCode(),
            providers = candidates,
            request = request
        )

        if (recovery.decisionState == "SUCCESS" && recovery.result != null && !recovery.providerId.isNullOrBlank()) {
            val providerId = recovery.providerId
            val response = AmarAgentResponse(
                recovery.result.text,
                AmarAgentResponse.Status.READY,
                context.tools.map { it.id }
            )
            audit.record(
                level = 2,
                authority = "ADAPTIVE_MODEL_ROUTING",
                complexity = complexity,
                selectedProvider = providerId,
                decisionState = "SELECTED",
                reason = "PROVIDER_GENERATION_SUCCESS"
            )
            return AdaptiveGenerationOutcome(response, providerId)
        }

        audit.record(
            level = 2,
            authority = "ADAPTIVE_MODEL_ROUTING",
            complexity = complexity,
            selectedProvider = null,
            decisionState = "FAIL_CLOSED",
            reason = "ALL_COMPATIBLE_PROVIDERS_FAILED"
        )
        return AdaptiveGenerationOutcome(
            AmarAgentResponse(
                answer = "لم يتوفر نموذج متوافق مع متطلبات هذه المهمة.",
                status = AmarAgentResponse.Status.ERROR
            ),
            null
        )
    }
}

internal data class AdaptiveGenerationOutcome(val response: AmarAgentResponse, val providerId: String?)

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
    get() = { actual ->
        this != AmarModelQuality.UNKNOWN &&
            actual != AmarModelQuality.UNKNOWN &&
            this.rank >= actual.rank
    }

private val AmarModelCost.rank: Int
    get() = when (this) {
        AmarModelCost.UNKNOWN -> 0
        AmarModelCost.LOW -> 1
        AmarModelCost.MEDIUM -> 2
        AmarModelCost.HIGH -> 3
    }

private fun AmarModelCost.within(maxCost: AmarModelCost?): Boolean =
    maxCost == null ||
        (this != AmarModelCost.UNKNOWN &&
            maxCost != AmarModelCost.UNKNOWN &&
            this.rank <= maxCost.rank)

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
