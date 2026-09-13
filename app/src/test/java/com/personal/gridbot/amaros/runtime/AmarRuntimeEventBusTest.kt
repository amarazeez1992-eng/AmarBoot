package com.personal.gridbot.amaros.runtime

import com.personal.gridbot.bridge.AmarBridgeContract
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRuntimeEventBusTest {
    @Test
    fun emitPublishesEvent() = runBlocking {
        val bus = AmarRuntimeEventBus()
        val event = AmarRuntimeStatusEvent(
            commandId = 42L,
            botNumber = 1,
            previousState = AmarBridgeContract.PENDING_MT5,
            state = AmarBridgeContract.VERIFIED,
            reason = "acknowledged",
        )

        val collector = kotlinx.coroutines.async { bus.events.first() }
        bus.emit(event)

        assertEquals(event, collector.await())
    }

    @Test
    fun tryEmitAcceptsValidEvent() {
        val bus = AmarRuntimeEventBus()
        val event = AmarRuntimeStatusEvent(
            botNumber = 2,
            state = "RUNNING",
        )

        assertTrue(bus.tryEmit(event))
    }

    @Test(expected = IllegalArgumentException::class)
    fun eventRejectsInvalidBotNumber() {
        AmarRuntimeStatusEvent(botNumber = 11, state = "RUNNING")
    }
}
