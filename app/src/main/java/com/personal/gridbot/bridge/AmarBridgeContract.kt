package com.personal.gridbot.bridge

/**
 * Boundary contract between the Android command layer and a future laptop/MT5 bridge.
 * This module is transport-agnostic and never executes trading operations itself.
 *
 * Lifecycle authority:
 * PENDING_MT5 -> ACKNOWLEDGED -> EXECUTED -> VERIFIED
 * Failure/termination may occur through REJECTED, FAILED, or STALE where permitted
 * by the command lifecycle coordinator.
 */
object AmarBridgeContract {
    const val VERSION = "1.2"
    const val PENDING_MT5 = "PENDING_MT5"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val EXECUTED = "EXECUTED"
    const val REJECTED = "REJECTED"
    const val VERIFIED = "VERIFIED"
    const val FAILED = "FAILED"
    const val STALE = "STALE"

    /** Only final states are terminal. EXECUTED is deliberately non-terminal because it must be verified. */
    fun isTerminal(status: String): Boolean = when (status) {
        REJECTED, VERIFIED, FAILED, STALE -> true
        else -> false
    }

    fun isKnown(status: String): Boolean = when (status) {
        PENDING_MT5, ACKNOWLEDGED, EXECUTED, REJECTED, VERIFIED, FAILED, STALE -> true
        else -> false
    }
}
