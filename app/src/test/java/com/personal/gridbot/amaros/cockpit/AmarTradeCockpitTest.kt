package com.personal.gridbot.amaros.cockpit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AmarTradeCockpitTest {
    @Test
    fun emptySelectionFailsClosed() {
        val preview = AmarTradeCockpit.preview(emptyList(), AmarTradeCockpit.Action.CLOSE)
        assertFalse(preview.executable)
        assertEquals("NO_POSITIONS_SELECTED", preview.reason)
    }

    @Test
    fun selectedPositionsAreAggregatedWithoutExecutionAuthority() {
        val preview = AmarTradeCockpit.preview(
            listOf(
                AmarTradeCockpit.Position(1L, "XAUUSD", 0.10, 5.0, selected = true),
                AmarTradeCockpit.Position(2L, "XAUUSD", 0.20, -2.0, selected = true),
                AmarTradeCockpit.Position(3L, "EURUSD", 0.30, 1.0, selected = false),
            ),
            AmarTradeCockpit.Action.SET_STOP_LOSS,
        )
        assertFalse(preview.executable)
        assertEquals(2, preview.selectedCount)
        assertEquals(0.30, preview.selectedVolume, 1e-9)
        assertEquals(3.0, preview.selectedProfit, 1e-9)
        assertEquals("PREVIEW_ONLY", preview.reason)
    }

    @Test
    fun invalidSelectedPositionFailsClosed() {
        val preview = AmarTradeCockpit.preview(
            listOf(AmarTradeCockpit.Position(1L, "XAUUSD", Double.NaN, 0.0, selected = true)),
            AmarTradeCockpit.Action.TRAILING,
        )
        assertFalse(preview.executable)
        assertEquals("INVALID_POSITION_DATA", preview.reason)
    }
}
