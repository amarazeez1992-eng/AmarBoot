package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.core.AmarCircuitBreaker
import com.personal.gridbot.amaros.core.AmarIdempotencyStore
import com.personal.gridbot.amaros.core.AmarRuntimeConfig
import com.personal.gridbot.amaros.core.amarWithRetry
import com.personal.gridbot.amaros.security.AmarSecurityRuntime
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AmarResilienceTest {
    @Test fun idempotencyStore_isBoundedAndStable() {
        val store = AmarIdempotencyStore(capacity = 2)
        store.put("a", "A")
        store.put("b", "B")
        store.put("c", "C")
        assertFalse(store.contains("a"))
        assertEquals("B", store.get("b"))
        assertEquals("C", store.get("c"))
    }

    @Test fun circuitBreaker_opensAfterThresholdAndResetsOnSuccess() {
        var now = 0L
        val breaker = AmarCircuitBreaker(failureThreshold = 2, openDurationMs = 1000L) { now }
        breaker.recordFailure()
        assertFalse(breaker.isOpen())
        breaker.recordFailure()
        assertTrue(breaker.isOpen())
        now = 1001L
        assertFalse(breaker.isOpen())
        breaker.recordSuccess()
        assertFalse(breaker.isOpen())
    }

    @Test fun retryPolicy_retriesDeterministically() = runBlocking {
        var attempts = 0
        val result = amarWithRetry(
            AmarRuntimeConfig(maxRetries = 2, initialBackoffMs = 0L, maxBackoffMs = 0L, jitterRatio = 0.0),
            Random(1)
        ) {
            attempts++
            if (attempts < 3) error("transient")
            "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, attempts)
    }

    @Test fun securityRuntime_isFailClosed() {
        val security = AmarSecurityRuntime()
        assertFalse(security.canExecuteLive())
        security.authorizeReadOnly()
        assertFalse(security.canExecuteLive())
        security.lock()
        assertTrue(security.snapshot().emergencyLock)
        assertFalse(security.snapshot().executionAuthorized)
        assertFalse(security.canExecuteLive())
    }
}
