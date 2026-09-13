package com.personal.gridbot.bridge

/**
 * Boundary contract between the Android command layer and a future laptop/MT5 bridge.
 * This module is transport-agnostic and never executes trading operations itself.
 */
object AmarBridgeContract {
    const val VERSION = "1.1"
    const val PENDING_MT5 = "PENDING_MT5"
    const val ACKNOWLEDGED = "ACKNOWLEDGED"
    const val EXECUTED = "EXECUTED"
    const val REJECTED = "REJECTED"
    const val VERIFIED = "VERIFIED"
    const val FAILED = "FAILED"
    const val STALE = "STALE"

    fun isTerminal(status: String): Boolean = when (status) {
        EXECUTED, REJECTED, VERIFIED, FAILED, STALE -> true
        else -> false
    }
}
