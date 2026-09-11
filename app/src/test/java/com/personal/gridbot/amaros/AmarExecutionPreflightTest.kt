package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarBot1Health
import com.personal.gridbot.amaros.broker.AmarBot1HealthStatus
import com.personal.gridbot.amaros.broker.AmarExecutionPreflight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarExecutionPreflightTest {
    private val healthy = AmarBot1Health(AmarBot1HealthStatus.HEALTHY, "BOT1_HEALTHY", 10L)

    @Test fun allows_exact_identity_and_symbol() {
        val result = AmarExecutionPreflight.check(healthy, "XAUUSD", "XAUUSD", 20260908L, 20260908L)
        assertTrue(result.allowed)
        assertEquals("EXECUTION_PREFLIGHT_OK", result.reason)
    }

    @Test fun blocks_unhealthy_runtime() {
        val result = AmarExecutionPreflight.check(
            AmarBot1Health(AmarBot1HealthStatus.STALE, "BOT1_STATE_STALE"),
            "XAUUSD", "XAUUSD", 20260908L, 20260908L,
        )
        assertFalse(result.allowed)
        assertEquals("BOT1_STATE_STALE", result.reason)
    }

    @Test fun blocks_symbol_mismatch() {
        val result = AmarExecutionPreflight.check(healthy, "XAUUSD", "BTCUSD", 20260908L, 20260908L)
        assertFalse(result.allowed)
        assertEquals("TARGET_SYMBOL_MISMATCH", result.reason)
    }

    @Test fun blocks_magic_mismatch() {
        val result = AmarExecutionPreflight.check(healthy, "XAUUSD", "XAUUSD", 20260908L, 99L)
        assertFalse(result.allowed)
        assertEquals("MAGIC_MISMATCH", result.reason)
    }
}
