package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1CommandVerifier
import com.personal.gridbot.amaros.broker.AmarBot1HealthMonitor
import com.personal.gridbot.amaros.broker.AmarBot1HealthStatus
import com.personal.gridbot.amaros.broker.AmarBot1LifecycleFacade
import com.personal.gridbot.amaros.broker.AmarBot1RemoteState
import com.personal.gridbot.amaros.broker.AmarBridgeConfig
import com.personal.gridbot.amaros.broker.AmarMt5CommandClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AmarBot1LifecycleFacadeTest {
    @Test
    fun health_delegates_to_fail_closed_monitor() {
        val facade = AmarBot1LifecycleFacade(
            verifier = AmarBot1CommandVerifier(
                client = AmarMt5CommandClient(
                    AmarBridgeConfig("https://127.0.0.1:1", "test-token"),
                    "test-secret",
                ),
            ),
            healthMonitor = AmarBot1HealthMonitor(),
            accountLogin = 1L,
            botMagic = 20260908L,
            symbol = "XAUUSD",
        )
        val health = facade.health(
            AmarBot1RemoteState(
                available = true,
                fresh = true,
                ageMs = 1L,
                botId = "BOT_1",
                magic = 999L,
                strategyId = "STRATEGY_01",
                strategyVersion = "2.00",
                marketReady = true,
            )
        )
        assertFalse(health.safeForExecution)
        assertEquals(AmarBot1HealthStatus.IDENTITY_MISMATCH, health.status)
    }
}
