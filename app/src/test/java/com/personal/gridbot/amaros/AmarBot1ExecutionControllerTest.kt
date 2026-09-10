package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.AmarBot1ExecutionController
import com.personal.gridbot.amaros.broker.AmarBridgeConfig
import com.personal.gridbot.amaros.broker.AmarMt5CommandClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBot1ExecutionControllerTest {
    private fun controller(
        authenticated: Boolean = true,
        accountEnabled: Boolean = true,
        live: Boolean = false,
        connectorReady: Boolean = true,
    ) = AmarBot1ExecutionController(
        commandClient = AmarMt5CommandClient(
            AmarBridgeConfig("https://127.0.0.1:1", "test-token"),
            "test-secret",
        ),
        accountLogin = 123L,
        symbol = "XAUUSD",
        authenticated = { authenticated },
        accountEnabled = { accountEnabled },
        explicitLiveAuthorization = { live },
        connectorReady = { connectorReady },
        clockMs = { 1_000L },
    )

    @Test
    fun liveExecutionIsBlockedWithoutExplicitAuthorization() = runBlocking {
        val result = controller(live = false).buy(0.01)
        assertFalse(result.accepted)
        assertFalse(result.executed)
        assertTrue(result.message.contains("مقفول"))
    }

    @Test
    fun invalidQuantityIsRejectedBeforeTransport() = runBlocking {
        val result = controller(live = false).sell(0.0)
        assertFalse(result.accepted)
        assertFalse(result.executed)
        assertTrue(result.message.contains("غير صالح"))
    }
}
