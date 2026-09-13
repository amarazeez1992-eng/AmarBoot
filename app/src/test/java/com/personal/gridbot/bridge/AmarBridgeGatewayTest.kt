package com.personal.gridbot.bridge

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AmarBridgeGatewayTest {
    private val gateway = AmarBridgeGateway()

    @Test
    fun validCommandRemainsPendingMt5() {
        assertEquals(AmarBridgeContract.PENDING_MT5, gateway.submitForMt5("cmd-1"))
    }

    @Test
    fun blankCommandIsRejected() {
        assertFailsWith<IllegalArgumentException> { gateway.submitForMt5(" ") }
    }
}
