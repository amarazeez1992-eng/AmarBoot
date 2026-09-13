package com.personal.gridbot.bridge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBridgeContractTest {
    @Test
    fun pendingAcknowledgedAndExecutedAreNotTerminal() {
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.PENDING_MT5))
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.ACKNOWLEDGED))
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.EXECUTED))
    }

    @Test
    fun onlyFinalStatusesAreTerminal() {
        listOf(
            AmarBridgeContract.REJECTED,
            AmarBridgeContract.VERIFIED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE
        ).forEach { assertTrue(AmarBridgeContract.isTerminal(it)) }
    }

    @Test
    fun allLifecycleStatusesAreKnown() {
        listOf(
            AmarBridgeContract.PENDING_MT5,
            AmarBridgeContract.ACKNOWLEDGED,
            AmarBridgeContract.EXECUTED,
            AmarBridgeContract.REJECTED,
            AmarBridgeContract.VERIFIED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE
        ).forEach { assertTrue(AmarBridgeContract.isKnown(it)) }
        assertFalse(AmarBridgeContract.isKnown("UNKNOWN"))
    }
}
