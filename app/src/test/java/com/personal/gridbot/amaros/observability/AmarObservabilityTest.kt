package com.personal.gridbot.amaros.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarObservabilityTest {
    private class FakeClock : AmarObservabilityClock {
        var now = 1_000L
        override fun nowEpochMs(): Long = now
    }

    @Test fun record_isStructuredTraceableAndSanitized() {
        val clock = FakeClock()
        val observability = AmarObservability(clock = clock)
        val event = observability.record(
            component = "decision",
            event = "proposal_created",
            outcome = "accepted",
            traceId = "trace-1",
            details = mapOf("symbol" to "XAUUSD", "token" to "must-not-persist")
        )
        assertEquals("trace-1", event.traceId)
        assertEquals("XAUUSD", event.details["symbol"])
        assertFalse(event.details.containsKey("token"))
        assertEquals("trace-1", observability.auditSnapshot().single().traceId)
    }

    @Test fun metrics_areDeterministicAndCountErrors() {
        val clock = FakeClock()
        val observability = AmarObservability(clock = clock)
        observability.record("core", "heartbeat", "ok")
        observability.record("core", "failure", "rejected", AmarObservabilityEvent.Severity.ERROR)
        observability.record("core", "failure", "rejected", AmarObservabilityEvent.Severity.ERROR)
        val metrics = observability.metrics()
        assertEquals(3L, metrics.totalEvents)
        assertEquals(2L, metrics.errorEvents)
        assertEquals(2L, metrics.eventCounts["failure"])
    }

    @Test fun criticalEvent_failsHealthClosed() {
        val clock = FakeClock()
        val observability = AmarObservability(clock = clock)
        observability.record("agent", "unsafe_state", "blocked", AmarObservabilityEvent.Severity.CRITICAL)
        val health = observability.health()
        assertFalse(health.healthy)
        assertEquals(1_000L, health.lastCriticalEpochMs)
        assertEquals("critical_observability_event", health.reason)
    }

    @Test fun eventBuffer_isBoundedWithoutLosingCounters() {
        val clock = FakeClock()
        val observability = AmarObservability(clock = clock, capacity = 2)
        observability.record("core", "one", "ok")
        observability.record("core", "two", "ok")
        observability.record("core", "three", "ok")
        assertEquals(listOf("two", "three"), observability.snapshot().map { it.event })
        assertEquals(3L, observability.metrics().totalEvents)
    }

    @Test fun auditIntegrity_remainsValidAfterObservabilityEvents() {
        val observability = AmarObservability()
        observability.record("research", "started", "ok")
        observability.record("research", "completed", "ok")
        assertTrue(observability.verifyAuditIntegrity())
    }

    @Test(expected = IllegalArgumentException::class)
    fun capacity_mustBePositive() {
        AmarObservability(capacity = 0)
    }
}
