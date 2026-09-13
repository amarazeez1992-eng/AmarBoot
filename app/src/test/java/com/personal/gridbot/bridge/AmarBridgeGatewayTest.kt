package com.personal.gridbot.bridge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class AmarBridgeGatewayTest {
    private val gateway = AmarBridgeGateway()

    @Test
    fun validCommandRemainsPendingMt5() {
        assertEquals(AmarBridgeContract.PENDING_MT5, gateway.submitForMt5("cmd-1"))
    }

    @Test
    fun blankCommandIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { gateway.submitForMt5(" ") }
    }
}
