package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRuntimeStateManagerTest {
    @Test
    fun botStateIsStoredAndRead() {
        val manager = AmarRuntimeStateManager()
        manager.setBotState(1, "RUNNING")
        assertEquals("RUNNING", manager.botState(1))
    }

    @Test
    fun commandLifecycleMustFollowGovernedOrder() {
        val manager = AmarRuntimeStateManager()
        manager.setCommandState(7, AmarBridgeContract.PENDING_MT5)

        assertFalse(manager.transitionCommand(7, AmarBridgeContract.VERIFIED))
        assertTrue(manager.transitionCommand(7, AmarBridgeContract.ACKNOWLEDGED))
        assertTrue(manager.transitionCommand(7, AmarBridgeContract.EXECUTED))
        assertTrue(manager.transitionCommand(7, AmarBridgeContract.VERIFIED))
        assertEquals(AmarBridgeContract.VERIFIED, manager.commandState(7))
    }

    @Test
    fun terminalCommandCannotTransitionAgain() {
        val manager = AmarRuntimeStateManager()
        manager.setCommandState(7, AmarBridgeContract.VERIFIED)
        assertFalse(manager.transitionCommand(7, AmarBridgeContract.EXECUTED))
        assertEquals(AmarBridgeContract.VERIFIED, manager.commandState(7))
    }

    @Test
    fun pendingCommandCanTerminateOnRejectOrFailure() {
        val manager = AmarRuntimeStateManager()
        manager.setCommandState(7, AmarBridgeContract.PENDING_MT5)
        assertTrue(manager.transitionCommand(7, AmarBridgeContract.REJECTED))
        assertFalse(manager.transitionCommand(7, AmarBridgeContract.ACKNOWLEDGED))

        manager.setCommandState(8, AmarBridgeContract.PENDING_MT5)
        assertTrue(manager.transitionCommand(8, AmarBridgeContract.FAILED))
        assertFalse(manager.transitionCommand(8, AmarBridgeContract.EXECUTED))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidBotIsRejected() {
        AmarRuntimeStateManager().setBotState(0, "RUNNING")
    }
}
