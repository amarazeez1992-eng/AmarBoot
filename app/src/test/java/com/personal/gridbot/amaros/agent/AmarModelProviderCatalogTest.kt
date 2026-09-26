package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarModelProviderCatalogTest {
    private class FakeProvider(override val id: String) : AmarModelProvider {
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 4096, 256, true)
        override suspend fun generate(request: AmarGenerationRequest) = AmarGenerationResult("ok")
        override suspend fun unload() = Unit
    }

    @Test
    fun catalog_registers_and_returns_runtime_provider() {
        val provider = FakeProvider("test-model")
        val catalog = AmarModelProviderCatalog()
            .register(
                AmarModelProviderProfile(
                    provider = provider,
                    capabilities = setOf(AmarModelCapability.TEXT_GENERATION),
                    maxContextTokens = 4096,
                    estimatedRamMb = 256,
                    quality = AmarModelQuality.STANDARD,
                    cost = AmarModelCost.LOW
                )
            )

        assertFalse(catalog.isEmpty())
        assertEquals(provider, catalog.get("test-model")?.provider)
        assertEquals(1, catalog.providers().size)
    }

    @Test
    fun unavailable_provider_remains_registered_but_is_not_marked_available() {
        val provider = FakeProvider("offline")
        val catalog = AmarModelProviderCatalog(
            listOf(
                AmarModelProviderProfile(
                    provider = provider,
                    capabilities = setOf(AmarModelCapability.TEXT_GENERATION),
                    maxContextTokens = 2048,
                    estimatedRamMb = 128,
                    quality = AmarModelQuality.BASIC,
                    cost = AmarModelCost.LOW,
                    available = false
                )
            )
        )

        assertTrue(catalog.get("offline")?.available == false)
    }

    @Test
    fun provider_contract_remains_suspendable() = runBlocking {
        val provider = FakeProvider("test")
        assertEquals("ok", provider.generate(AmarGenerationRequest("system", "user")).text)
    }
}
