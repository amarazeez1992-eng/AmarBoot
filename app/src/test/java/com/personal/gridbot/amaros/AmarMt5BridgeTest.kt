package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarLiveExecutionGate
import com.personal.gridbot.amaros.broker.AmarLiveGateState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMt5BridgeTest {
    private val gate = AmarLiveExecutionGate()

    @Test fun unauthenticated_is_locked() {
        assertEquals(AmarLiveGateState.LOCKED, gate.state(false, true, true, true))
        assertFalse(gate.allowExecution(AmarLiveGateState.LOCKED))
    }

    @Test fun authenticated_but_not_explicitly_live_is_read_only() {
        assertEquals(AmarLiveGateState.AUTHENTICATED_READ_ONLY, gate.state(true, true, false, true))
        assertFalse(gate.allowExecution(AmarLiveGateState.AUTHENTICATED_READ_ONLY))
    }

    @Test fun missing_connector_stays_read_only() {
        assertEquals(AmarLiveGateState.AUTHENTICATED_READ_ONLY, gate.state(true, true, true, false))
    }

    @Test fun all_gates_are_required_for_live_state() {
        assertEquals(AmarLiveGateState.AUTHORIZED_LIVE, gate.state(true, true, true, true))
        assertTrue(gate.allowExecution(AmarLiveGateState.AUTHORIZED_LIVE))
    }
}
