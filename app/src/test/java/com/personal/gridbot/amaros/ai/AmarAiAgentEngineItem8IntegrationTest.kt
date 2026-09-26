package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.agent.AmarModelCapability
import com.personal.gridbot.amaros.agent.AmarModelProvider
import com.personal.gridbot.amaros.agent.AmarModelProviderCatalog
import com.personal.gridbot.amaros.agent.AmarModelProviderProfile
import com.personal.gridbot.amaros.agent.AmarModelQuality
import com.personal.gridbot.amaros.agent.AmarModelCost
import com.personal.gridbot.amaros.agent.AmarModelInfo
import com.personal.gridbot.amaros.agent.AmarGenerationRequest
import com.personal.gridbot.amaros.agent.AmarGenerationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarAiAgentEngineItem8IntegrationTest {
    private class FakeProvider : AmarModelProvider {
        override val id = "integration-model"
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 8192, 256, true)
        override suspend fun generate(request: AmarGenerationRequest) = AmarGenerationResult("integration-provider")
        override suspend fun unload() = Unit
    }

    @Test fun reason_task_executor_reaches_router_adaptive_and_provider() = runBlocking {
        val provider = FakeProvider()
        val catalog = AmarModelProviderCatalog(
            listOf(
                AmarModelProviderProfile(
                    provider = provider,
                    capabilities = setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),
                    maxContextTokens = 8192,
                    estimatedRamMb = 256,
                    quality = AmarModelQuality.HIGH,
                    cost = AmarModelCost.LOW
                )
            )
        )
        val result = AmarAiAgentEngine(modelProviderCatalog = catalog)
            .ask("", "", "compare multiple factors in detail")
        assertEquals("integration-provider", result.answer)
    }
}
