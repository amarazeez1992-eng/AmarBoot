package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarAdaptiveReasoningProviderTest {
    private class FakeProvider(override val id: String) : AmarModelProvider {
        override suspend fun load(modelPath: String) =
            AmarModelInfo(id, "test", 4096, 256, true)

        override suspend fun generate(request: AmarGenerationRequest) =
            AmarGenerationResult("ok")

        override suspend fun unload() = Unit
    }

    private fun profile(
        id: String,
        capabilities: Set<AmarModelCapability>
    ) = AmarModelProviderProfile(
        provider = FakeProvider(id),
        capabilities = capabilities,
        maxContextTokens = 4096,
        estimatedRamMb = 256,
        quality = AmarModelQuality.STANDARD,
        cost = AmarModelCost.LOW
    )

    @Test
    fun match_returns_only_capability_compatible_providers() {
        val matching = profile(
            "matching",
            setOf(AmarModelCapability.SIMPLE_EXPLANATION)
        )
        val nonMatching = profile(
            "non-matching",
            setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS)
        )
        val router = AmarAdaptiveReasoningProvider(
            AmarModelProviderCatalog(listOf(matching, nonMatching))
        )

        assertEquals(
            listOf("matching"),
            router.matchCapabilities(AmarModelTask.SIMPLE_EXPLANATION).map { it.provider.id }
        )
    }

    @Test
    fun match_returns_empty_when_no_provider_matches() {
        val provider = profile(
            "analysis-only",
            setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS)
        )
        val router = AmarAdaptiveReasoningProvider(
            AmarModelProviderCatalog(listOf(provider))
        )

        assertEquals(
            emptyList<AmarModelProviderProfile>(),
            router.matchCapabilities(AmarModelTask.SIMPLE_EXPLANATION)
        )
    }

    @Test
    fun unknown_task_is_not_treated_as_supported() {
        val provider = profile(
            "text-only",
            setOf(AmarModelCapability.TEXT_GENERATION)
        )
        val router = AmarAdaptiveReasoningProvider(
            AmarModelProviderCatalog(listOf(provider))
        )

        assertEquals(
            emptyList<AmarModelProviderProfile>(),
            router.matchCapabilities(AmarModelTask.UNKNOWN)
        )
    }
}
