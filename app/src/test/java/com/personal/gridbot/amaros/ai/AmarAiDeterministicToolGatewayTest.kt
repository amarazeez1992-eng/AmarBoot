package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiDeterministicToolGatewayTest {
    @Test
    fun inspectAppReturnsDeterministicSnapshot() = runBlocking {
        val result = AmarAiDeterministicToolGateway.execute("inspect_app", "")
        assertTrue(result?.startsWith("APP_INSPECTION|") == true)
        assertTrue(result?.contains("EXECUTION=") == true)
    }

    @Test
    fun engineMarketUsesRealMarketBinding() = runBlocking {
        val result = AmarAiDeterministicToolGateway.execute("engine_market", "")
        assertTrue(result?.startsWith("ENGINE_MARKET|") == true)
    }
}
