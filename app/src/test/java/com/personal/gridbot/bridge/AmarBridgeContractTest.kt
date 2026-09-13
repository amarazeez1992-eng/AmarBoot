package com.personal.gridbot.bridge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarBridgeContractTest {
    @Test
    fun pendingAndAcknowledgedAreNotTerminal() {
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.PENDING_MT5))
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.ACKNOWLEDGED))
    }

    @Test
    fun terminalStatusesAreTerminal() {
        listOf(
            AmarBridgeContract.EXECUTED,
            AmarBridgeContract.REJECTED,
            AmarBridgeContract.VERIFIED,
            AmarBridgeContract.FAILED,
            AmarBridgeContract.STALE
        ).forEach { assertTrue(AmarBridgeContract.isTerminal(it)) }
    }
}
