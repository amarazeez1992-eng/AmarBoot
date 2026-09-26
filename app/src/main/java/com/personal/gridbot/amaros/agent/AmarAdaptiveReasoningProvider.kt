package com.personal.gridbot.amaros.agent

/**
 * Adaptive reasoning provider.
 *
 * Addition 2: capability matching only. Provider selection remains fail-closed
 * until later Item 8 additions supply the remaining routing constraints.
 */
class AmarAdaptiveReasoningProvider(
    private val catalog: AmarModelProviderCatalog
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
