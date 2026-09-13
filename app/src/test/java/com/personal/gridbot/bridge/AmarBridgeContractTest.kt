package com.personal.gridbot.bridge

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AmarBridgeContractTest {
    @Test
    fun pendingIsNotTerminal() {
        assertFalse(AmarBridgeContract.isTerminal(AmarBridgeContract.PENDING_MT5))
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
