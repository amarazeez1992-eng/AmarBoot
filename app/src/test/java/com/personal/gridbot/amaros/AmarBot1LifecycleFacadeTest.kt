package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1Health
import com.personal.gridbot.amaros.broker.AmarBot1HealthMonitor
import com.personal.gridbot.amaros.broker.AmarBot1HealthStatus
import com.personal.gridbot.amaros.broker.AmarBot1LifecycleFacade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AmarBot1LifecycleFacadeTest {
    @Test fun health_delegates_to_fail_closed_monitor() {
        val facade = AmarBot1LifecycleFacade(
            verifier = throw UnsupportedOperationException("not used"),
            healthMonitor = AmarBot1HealthMonitor(),
            accountLogin = 1L,
            botMagic = 20260908L,
            symbol = "XAUUSD",
        )
        val health = facade.health(
            AmarBot1Health(
                status = AmarBot1HealthStatus.IDENTITY_MISMATCH,
                reason = "BOT1_IDENTITY_MISMATCH",
                ageMs = 1L,
            )
        )
        assertFalse(health.safeForExecution)
        assertEquals(AmarBot1HealthStatus.IDENTITY_MISMATCH, health.status)
    }
}
