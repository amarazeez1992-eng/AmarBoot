package com.personal.gridbot.amaros.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * B12 runtime monitor. Tracks liveness and cycle quality without owning any
 * trading capability. State is immutable and safe for presentation/services.
 */
class AmarRuntimeMonitor {
    private val _state = MutableStateFlow(AmarRuntimeHealth())
    val state: StateFlow<AmarRuntimeHealth> = _state.asStateFlow()

    fun markStarted(nowEpochMs: Long = System.currentTimeMillis()) {
        _state.value = _state.value.copy(
            status = AmarRuntimeHealthStatus.STARTING,
            updatedAtEpochMs = nowEpochMs
        )
    }

    fun recordSuccess(durationMs: Long, nowEpochMs: Long = System.currentTimeMillis()) {
        val current = _state.value
        _state.value = current.copy(
            status = AmarRuntimeHealthStatus.HEALTHY,
            cycleCount = current.cycleCount + 1L,
            consecutiveFailures = 0,
            lastSuccessEpochMs = nowEpochMs,
            lastCycleDurationMs = durationMs.coerceAtLeast(0L),
            lastError = null,
            updatedAtEpochMs = nowEpochMs
        )
    }

    fun recordFailure(error: Throwable, durationMs: Long, nowEpochMs: Long = System.currentTimeMillis()) {
        val current = _state.value
        val consecutive = current.consecutiveFailures + 1
        _state.value = current.copy(
            status = if (consecutive >= 3) AmarRuntimeHealthStatus.FAULTED else AmarRuntimeHealthStatus.DEGRADED,
            cycleCount = current.cycleCount + 1L,
            consecutiveFailures = consecutive,
            totalFailures = current.totalFailures + 1L,
            lastCycleDurationMs = durationMs.coerceAtLeast(0L),
            lastError = error.message ?: error::class.simpleName ?: "Runtime error",
            updatedAtEpochMs = nowEpochMs
        )
    }

    fun markStopped(nowEpochMs: Long = System.currentTimeMillis()) {
        _state.value = _state.value.copy(
            status = AmarRuntimeHealthStatus.STOPPED,
            updatedAtEpochMs = nowEpochMs
        )
    }
}
