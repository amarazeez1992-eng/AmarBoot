package com.personal.gridbot.amaros.analytics

import com.personal.gridbot.amaros.core.AmarRuntimeController
import kotlin.math.max

/** B15 deterministic, read-only analytics over runtime snapshots. */
data class AmarAnalyticsSnapshot(
    val samples: Long,
    val successfulCycles: Long,
    val failedCycles: Long,
    val successRate: Double,
    val averageCycleDurationMs: Double,
    val maxCycleDurationMs: Long,
    val consecutiveFailures: Int,
    val lastCycleNumber: Long,
    val decisionCounts: Map<String, Long>,
    val riskAllowedCount: Long,
    val riskBlockedCount: Long,
    val executionModeCounts: Map<String, Long>
)

class AmarAnalytics(private val maxSamples: Int = 10_000) {
    private val samples = ArrayDeque<Sample>()

    fun record(state: AmarRuntimeController.RuntimeState, durationMs: Long? = null) {
        val decision = state.decision?.direction?.name ?: "UNKNOWN"
        val riskAllowed = state.risk?.allowed ?: false
        val executionMode = if (state.execution?.executed == true) "EXECUTED" else "DEMO_GUARDED"
        val duration = max(0L, durationMs ?: 0L)
        samples.addLast(Sample(state.cycleNumber, duration, state.health.totalFailures == 0L || state.decision != null, decision, riskAllowed, executionMode))
        while (samples.size > maxSamples) samples.removeFirst()
    }

    fun snapshot(): AmarAnalyticsSnapshot {
        if (samples.isEmpty()) return AmarAnalyticsSnapshot(0, 0, 0, 0.0, 0.0, 0, 0, emptyMap(), 0, 0, emptyMap())
        val successful = samples.count { it.success }
        val failed = samples.size - successful
        val durations = samples.map { it.durationMs }
        val decisions = samples.groupingBy { it.decision }.eachCount().mapValues { it.value.toLong() }
        val modes = samples.groupingBy { it.executionMode }.eachCount().mapValues { it.value.toLong() }
        val allowed = samples.count { it.riskAllowed }.toLong()
        return AmarAnalyticsSnapshot(samples.size.toLong(), successful.toLong(), failed.toLong(), successful.toDouble() / samples.size,
            durations.average(), durations.maxOrNull() ?: 0L, 0, samples.last().cycleNumber, decisions, allowed, samples.size - allowed, modes)
    }

    private data class Sample(val cycleNumber: Long, val durationMs: Long, val success: Boolean, val decision: String, val riskAllowed: Boolean, val executionMode: String)
}
