package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmarAdaptiveReasoningProviderRoutingTest {
    private class FakeProvider(override val id: String) : AmarModelProvider {
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 8192, 256, true)
        override suspend fun generate(request: AmarGenerationRequest) = AmarGenerationResult("generated")
        override suspend fun unload() = Unit
    }

    private fun profile(
        id: String,
        quality: AmarModelQuality = AmarModelQuality.HIGH,
        ram: Int = 256,
        context: Int = 8192
    ) = AmarModelProviderProfile(
        provider = FakeProvider(id),
        capabilities = setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),
        maxContextTokens = context,
        estimatedRamMb = ram,
        quality = quality,
        cost = AmarModelCost.LOW
    )

    @Test fun resource_fail_rejects_provider() {
        val provider = profile("ram-heavy", ram = 4096)
        val adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(provider)))
        val decision = adaptive.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS, AmarModelComplexity.HIGH, 4096, 1024, AmarModelQuality.STANDARD)
        assertNull(decision.selectedProvider)
        assertEquals("FAIL_CLOSED", decision.decisionState)
    }

    @Test fun quality_threshold_cases() {
        val basic = profile("basic", quality = AmarModelQuality.BASIC)
        val high = profile("high", quality = AmarModelQuality.HIGH)
        val adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(basic, high)))
        val decision = adaptive.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS, AmarModelComplexity.HIGH, 4096, 4096, AmarModelQuality.STANDARD)
        assertEquals("high", decision.selectedProvider?.provider?.id)
    }

    @Test fun capability_mismatch_is_rejected() {
        val provider = AmarModelProviderProfile(
            provider = FakeProvider("text-only"),
            capabilities = setOf(AmarModelCapability.TEXT_GENERATION),
            maxContextTokens = 8192,
            estimatedRamMb = 256,
            quality = AmarModelQuality.HIGH,
            cost = AmarModelCost.LOW
        )
        val adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(provider)))
        val decision = adaptive.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS, AmarModelComplexity.HIGH, 4096, 4096, AmarModelQuality.STANDARD)
        assertNull(decision.selectedProvider)
    }

    @Test fun no_candidate_is_fail_closed() {
        val adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog())
        val decision = adaptive.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS, AmarModelComplexity.HIGH, 4096, 4096, AmarModelQuality.STANDARD)
        assertEquals("FAIL_CLOSED", decision.decisionState)
        assertEquals(listOf("SECONDARY_PROVIDER", "FAIL_CLOSED"), decision.fallbackPlan)
    }

    @Test fun selected_provider_is_deterministic() {
        val first = profile("b")
        val second = profile("a")
        val adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog(listOf(first, second)))
        val decision = adaptive.selectProvider(AmarModelTask.MULTI_FACTOR_ANALYSIS, AmarModelComplexity.HIGH, 4096, 4096, AmarModelQuality.STANDARD)
        assertEquals("a", decision.selectedProvider?.provider?.id)
    }

    @Test fun generation_uses_selected_provider() = runBlocking {
        val adaptive = AmarAdaptiveReasoningProvider(
            AmarModelProviderCatalog(listOf(profile("model")))
        )
        val response = adaptive.generate(
            context = AmarAgentContext("compare factors", emptyList(), false, false),
            task = AmarModelTask.MULTI_FACTOR_ANALYSIS,
            complexity = AmarModelComplexity.HIGH,
            maxContextTokens = 4096,
            maxRamMb = 4096,
            minimumQuality = AmarModelQuality.STANDARD
        )
        assertEquals("generated", response.answer)
    }
}
