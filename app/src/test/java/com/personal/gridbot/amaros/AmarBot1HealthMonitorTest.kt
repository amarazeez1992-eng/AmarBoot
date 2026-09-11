package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1HealthMonitor
import com.personal.gridbot.amaros.broker.AmarBot1HealthStatus
import com.personal.gridbot.amaros.broker.AmarBot1RemoteState
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarBot1HealthMonitorTest {
    private fun healthy() = AmarBot1RemoteState(
        available = true,
        fresh = true,
        ageMs = 100L,
        botId = "BOT_1",
        magic = 20260908L,
        strategyId = "STRATEGY_01",
        strategyVersion = "2.00",
        targetSymbol = "XAUUSD",
        marketReady = true,
    )

    @Test fun healthyStateIsAllowed() {
        val health = AmarBot1HealthMonitor().evaluate(healthy())
        assertEquals(AmarBot1HealthStatus.HEALTHY, health.status)
    }

    @Test fun staleStateIsBlocked() {
        val health = AmarBot1HealthMonitor().evaluate(healthy().copy(fresh = false))
        assertEquals(AmarBot1HealthStatus.STALE, health.status)
    }

    @Test fun identityMismatchIsBlocked() {
        val health = AmarBot1HealthMonitor().evaluate(healthy().copy(strategyVersion = "9.99"))
        assertEquals(AmarBot1HealthStatus.IDENTITY_MISMATCH, health.status)
    }
}
