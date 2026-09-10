package com.personal.gridbot.amaros.analytics

import com.personal.gridbot.amaros.core.AmarRuntimeController
import kotlin.math.max

/** B15 deterministic, bounded, read-only analytics over runtime snapshots. */
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

    init {
        require(maxSamples > 0) { "maxSamples must be positive" }
    }

    fun record(state: AmarRuntimeController.RuntimeState, durationMs: Long? = null) {
        val duration = max(0L, durationMs ?: state.health.lastCycleDurationMs)
        val failed = state.health.lastError != null
        val decision = state.decision?.direction?.name ?: "UNKNOWN"
        val riskAllowed = state.risk?.allowed == true
        val executionMode = if (state.execution?.executed == true) "EXECUTED" else "DEMO_GUARDED"
        samples.addLast(
            Sample(
                cycleNumber = state.cycleNumber,
                durationMs = duration,
                success = !failed,
                decision = decision,
                riskAllowed = riskAllowed,
                executionMode = executionMode,
                consecutiveFailures = state.health.consecutiveFailures
            )
        )
        while (samples.size > maxSamples) samples.removeFirst()
    }

    fun snapshot(): AmarAnalyticsSnapshot {
        if (samples.isEmpty()) {
            return AmarAnalyticsSnapshot(
                samples = 0L,
                successfulCycles = 0L,
                failedCycles = 0L,
                successRate = 0.0,
                averageCycleDurationMs = 0.0,
                maxCycleDurationMs = 0L,
                consecutiveFailures = 0,
                lastCycleNumber = 0L,
                decisionCounts = emptyMap(),
                riskAllowedCount = 0L,
                riskBlockedCount = 0L,
                executionModeCounts = emptyMap()
            )
        }
        val successful = samples.count { it.success }.toLong()
        val total = samples.size.toLong()
        val failed = total - successful
        val durations = samples.map { it.durationMs }
        val decisions = samples.groupingBy { it.decision }.eachCount().mapValues { it.value.toLong() }
        val modes = samples.groupingBy { it.executionMode }.eachCount().mapValues { it.value.toLong() }
        val allowed = samples.count { it.riskAllowed }.toLong()
        return AmarAnalyticsSnapshot(
            samples = total,
            successfulCycles = successful,
            failedCycles = failed,
            successRate = successful.toDouble() / total.toDouble(),
            averageCycleDurationMs = durations.average(),
            maxCycleDurationMs = durations.maxOrNull() ?: 0L,
            consecutiveFailures = samples.last().consecutiveFailures,
            lastCycleNumber = samples.last().cycleNumber,
            decisionCounts = decisions,
            riskAllowedCount = allowed,
            riskBlockedCount = total - allowed,
            executionModeCounts = modes
        )
    }

    private data class Sample(
        val cycleNumber: Long,
        val durationMs: Long,
        val success: Boolean,
        val decision: String,
        val riskAllowed: Boolean,
        val executionMode: String,
        val consecutiveFailures: Int
    )
}
