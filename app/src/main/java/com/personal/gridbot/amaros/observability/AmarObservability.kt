package com.personal.gridbot.amaros.observability

import com.personal.gridbot.amaros.audit.AmarAuditLog
import com.personal.gridbot.amaros.audit.AmarAuditRecord
import java.util.UUID

/** Stage 11 Item 8: bounded, structured, provider-agnostic runtime observability. */
data class AmarObservabilityEvent(
    val timestampEpochMs: Long,
    val traceId: String,
    val component: String,
    val event: String,
    val outcome: String,
    val severity: Severity,
    val details: Map<String, String> = emptyMap()
) {
    enum class Severity { INFO, WARN, ERROR, CRITICAL }

    init {
        require(timestampEpochMs >= 0L)
        require(traceId.isNotBlank())
        require(component.isNotBlank())
        require(event.isNotBlank())
        require(outcome.isNotBlank())
    }
}

data class AmarMetricSnapshot(
    val totalEvents: Long,
    val errorEvents: Long,
    val criticalEvents: Long,
    val eventCounts: Map<String, Long>
)

data class AmarHealthSnapshot(
    val healthy: Boolean,
    val checkedAtEpochMs: Long,
    val lastErrorEpochMs: Long?,
    val lastCriticalEpochMs: Long?,
    val reason: String?
)

fun interface AmarObservabilityClock {
    fun nowEpochMs(): Long
}

class AmarObservability(
    private val auditLog: AmarAuditLog = AmarAuditLog(),
    private val clock: AmarObservabilityClock = AmarObservabilityClock { System.currentTimeMillis() },
    private val capacity: Int = 2_000
) {
    private val lock = Any()
    private val events = ArrayDeque<AmarObservabilityEvent>()
    private val counts = linkedMapOf<String, Long>()
    private var totalEvents = 0L
    private var errorEvents = 0L
    private var criticalEvents = 0L
    private var lastErrorEpochMs: Long? = null
    private var lastCriticalEpochMs: Long? = null

    init { require(capacity > 0) }

    fun record(
        component: String,
        event: String,
        outcome: String,
        severity: AmarObservabilityEvent.Severity = AmarObservabilityEvent.Severity.INFO,
        traceId: String = UUID.randomUUID().toString(),
        details: Map<String, String> = emptyMap()
    ): AmarObservabilityEvent = synchronized(lock) {
        val timestamp = clock.nowEpochMs().also { require(it >= 0L) }
        val safeDetails = sanitize(details)
        val observed = AmarObservabilityEvent(timestamp, traceId, component, event, outcome, severity, safeDetails)
        events.addLast(observed)
        while (events.size > capacity) events.removeFirst()
        totalEvents++
        if (severity == AmarObservabilityEvent.Severity.ERROR || severity == AmarObservabilityEvent.Severity.CRITICAL) {
            errorEvents++
            lastErrorEpochMs = timestamp
        }
        if (severity == AmarObservabilityEvent.Severity.CRITICAL) criticalEvents++
        if (severity == AmarObservabilityEvent.Severity.CRITICAL) lastCriticalEpochMs = timestamp
        counts[event] = (counts[event] ?: 0L) + 1L
        auditLog.append(
            AmarAuditRecord(
                epochMs = timestamp,
                category = "observability",
                action = event,
                outcome = outcome,
                traceId = traceId,
                details = safeDetails + ("severity" to severity.name)
            )
        )
        observed
    }

    fun snapshot(): List<AmarObservabilityEvent> = synchronized(lock) { events.toList() }

    fun metrics(): AmarMetricSnapshot = synchronized(lock) {
        AmarMetricSnapshot(totalEvents, errorEvents, criticalEvents, counts.toMap())
    }

    fun health(): AmarHealthSnapshot = synchronized(lock) {
        AmarHealthSnapshot(
            healthy = lastCriticalEpochMs == null,
            checkedAtEpochMs = clock.nowEpochMs(),
            lastErrorEpochMs = lastErrorEpochMs,
            lastCriticalEpochMs = lastCriticalEpochMs,
            reason = lastCriticalEpochMs?.let { "critical_observability_event" }
        )
    }

    fun verifyAuditIntegrity(): Boolean = auditLog.verifyIntegrity()

    fun auditSnapshot(): List<AmarAuditRecord> = auditLog.snapshot()

    private fun sanitize(details: Map<String, String>): Map<String, String> = details
        .filterKeys { key ->
            !key.contains("password", true) &&
                !key.contains("token", true) &&
                !key.contains("secret", true) &&
                !key.contains("credential", true)
        }
        .toSortedMap()
}
