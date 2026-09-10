package com.personal.gridbot.amaros.core

/**
 * B12 runtime health snapshot. Purely observational: it never changes trading
 * permissions and never executes broker operations.
 */
enum class AmarRuntimeHealthStatus {
    STARTING,
    HEALTHY,
    DEGRADED,
    FAULTED,
    STOPPED
}

data class AmarRuntimeHealth(
    val status: AmarRuntimeHealthStatus = AmarRuntimeHealthStatus.STOPPED,
    val cycleCount: Long = 0L,
    val consecutiveFailures: Int = 0,
    val totalFailures: Long = 0L,
    val lastSuccessEpochMs: Long = 0L,
    val lastCycleDurationMs: Long = 0L,
    val lastError: String? = null,
    val updatedAtEpochMs: Long = 0L
)
