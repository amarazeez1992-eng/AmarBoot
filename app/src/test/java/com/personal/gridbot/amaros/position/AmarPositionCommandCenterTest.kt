package com.personal.gridbot.amaros.position

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarPositionCommandCenterTest {
    private val positions = listOf(
        AmarPositionCommandCenter.Position(1L, "XAUUSD", 0.10, 2500.0),
        AmarPositionCommandCenter.Position(2L, "XAUUSD", 0.20, 2495.0),
    )

    @Test
    fun batchStopLossPreviewIsAccepted() {
        val preview = AmarPositionCommandCenter.preview(
            AmarPositionCommandCenter.Command(
                AmarPositionCommandCenter.Action.SET_STOP_LOSS,
                listOf(1L, 2L),
                price = 2480.0
            ),
            positions
        )
        assertTrue(preview.accepted)
        assertEquals(listOf(1L, 2L), preview.affectedTickets)
    }

    @Test
    fun partialCloseCannotConsumeWholePosition() {
        val preview = AmarPositionCommandCenter.preview(
            AmarPositionCommandCenter.Command(
                AmarPositionCommandCenter.Action.PARTIAL_CLOSE,
                listOf(1L),
                closeVolume = 0.10
            ),
            positions
        )
        assertFalse(preview.accepted)
        assertTrue("PARTIAL_VOLUME_MUST_BE_LESS_THAN_POSITION" in preview.errors)
    }

    @Test
    fun unknownAndDuplicateTicketsAreRejected() {
        val preview = AmarPositionCommandCenter.preview(
            AmarPositionCommandCenter.Command(
                AmarPositionCommandCenter.Action.CLOSE,
                listOf(1L, 1L, 99L)
            ),
            positions
        )
        assertFalse(preview.accepted)
        assertTrue("TICKETS_DUPLICATE" in preview.errors)
        assertTrue("POSITION_NOT_FOUND" in preview.errors)
    }
}
