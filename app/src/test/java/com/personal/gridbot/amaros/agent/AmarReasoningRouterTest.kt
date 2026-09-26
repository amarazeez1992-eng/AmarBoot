package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarReasoningRouterTest {
    private class FakeProvider(override val id: String) : AmarModelProvider {
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 8192, 256, true)
        override suspend fun generate(request: AmarGenerationRequest) = AmarGenerationResult("adaptive")
        override suspend fun unload() = Unit
    }

    @Test fun low_routes_to_local() = runBlocking {
        val router = AmarReasoningRouter(
            adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog())
        )
        val response = router.respond(AmarAgentContext("hello", emptyList(), false, false))
        assertEquals(AmarAgentResponse.Status.READY, response.status)
    }

    @Test fun high_without_provider_fails_closed() = runBlocking {
        val router = AmarReasoningRouter(
            adaptive = AmarAdaptiveReasoningProvider(AmarModelProviderCatalog())
        )
        val response = router.respond(
            AmarAgentContext("compare multiple factors in detail", emptyList(), false, false)
        )
        assertEquals(AmarAgentResponse.Status.ERROR, response.status)
    }

    @Test fun high_routes_to_adaptive_provider() = runBlocking {
        val catalog = AmarModelProviderCatalog(
            listOf(
                AmarModelProviderProfile(
                    provider = FakeProvider("adaptive"),
                    capabilities = setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),
                    maxContextTokens = 8192,
                    estimatedRamMb = 256,
                    quality = AmarModelQuality.HIGH,
                    cost = AmarModelCost.LOW
                )
            )
        )
        val response = AmarReasoningRouter(
            adaptive = AmarAdaptiveReasoningProvider(catalog)
        ).respond(AmarAgentContext("compare multiple factors in detail", emptyList(), false, false))
        assertEquals("adaptive", response.answer)
    }
}
