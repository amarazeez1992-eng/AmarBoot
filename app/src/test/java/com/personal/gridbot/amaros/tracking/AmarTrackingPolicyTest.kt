package com.personal.gridbot.amaros.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTrackingPolicyTest {
    @Test
    fun validPositionsProduceDeterministicTotals() {
        val positions = listOf(
            AmarTrackingPolicy.PositionSnapshot(1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L),
            AmarTrackingPolicy.PositionSnapshot(2L, "XAUUSD", 0.02, 2500.0, 2495.0, -3.0, 100L),
        )

        assertTrue(AmarTrackingPolicy.validateAll(positions).isEmpty())
        assertEquals(2.0, AmarTrackingPolicy.totalProfit(positions), 0.0)
        assertEquals(74.95, AmarTrackingPolicy.exposure(positions), 0.0)
    }

    @Test
    fun contractSizeIsAppliedToNotionalExposure() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L, contractSize = 100.0
        )
        assertEquals(2505.0, AmarTrackingPolicy.exposure(listOf(position)), 0.0)
    }

    @Test
    fun invalidPositionIsRejectedWithoutGuessing() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            ticket = 0L,
            symbol = "",
            volume = 0.0,
            priceOpen = -1.0,
            priceCurrent = Double.NaN,
            profit = Double.POSITIVE_INFINITY,
            magic = -1L,
        )

        assertEquals(
            listOf(
                "POSITION_0_TICKET_INVALID",
                "POSITION_0_SYMBOL_REQUIRED",
                "POSITION_0_VOLUME_INVALID",
                "POSITION_0_OPEN_PRICE_INVALID",
                "POSITION_0_CURRENT_PRICE_INVALID",
                "POSITION_0_PROFIT_INVALID",
                "POSITION_0_MAGIC_INVALID",
            ),
            AmarTrackingPolicy.validateAll(listOf(position))
        )
    }
}
