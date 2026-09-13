package com.personal.gridbot.amaros.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AmarTrackingPolicyTest {
    @Test
    fun validPositionsProduceDeterministicTotals() {
        val positions = listOf(
            AmarTrackingPolicy.PositionSnapshot(1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L, isTrusted = true),
            AmarTrackingPolicy.PositionSnapshot(2L, "XAUUSD", 0.02, 2500.0, 2495.0, -3.0, 100L, isTrusted = true),
        )

        assertTrue(AmarTrackingPolicy.validateAll(positions).isEmpty())
        assertEquals(2.0, AmarTrackingPolicy.totalProfit(positions), 0.0)
        assertEquals(74.95, AmarTrackingPolicy.exposure(positions), 0.0)
    }

    @Test
    fun contractSizeIsAppliedToNotionalExposure() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L, contractSize = 100.0, isTrusted = true
        )
        assertEquals(2505.0, AmarTrackingPolicy.exposure(listOf(position)), 0.0)
    }

    @Test
    fun invalidContractSizeIsRejected() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L,
            contractSize = 0.0,
            isTrusted = true,
        )
        assertEquals(
            listOf("POSITION_0_CONTRACT_SIZE_INVALID"),
            AmarTrackingPolicy.validateAll(listOf(position))
        )
    }

    @Test
    fun exposureOverflowIsRejectedAtValidationBoundary() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            1L, "XAUUSD", Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 100L,
            isTrusted = true,
        )
        assertEquals(
            listOf("POSITION_0_EXPOSURE_OVERFLOW"),
            AmarTrackingPolicy.validateAll(listOf(position))
        )
    }

    @Test
    fun aggregateProfitOverflowIsRejected() {
        val positions = listOf(
            AmarTrackingPolicy.PositionSnapshot(1L, "XAUUSD", 1.0, 1.0, 1.0, Double.MAX_VALUE, 100L, isTrusted = true),
            AmarTrackingPolicy.PositionSnapshot(2L, "XAUUSD", 1.0, 1.0, 1.0, Double.MAX_VALUE, 100L, isTrusted = true),
        )

        try {
            AmarTrackingPolicy.totalProfit(positions)
            fail("Expected aggregate profit overflow to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertEquals("PROFIT_TOTAL_OVERFLOW", expected.message)
        }
    }

    @Test
    fun aggregateExposureOverflowIsRejected() {
        val positions = listOf(
            AmarTrackingPolicy.PositionSnapshot(1L, "XAUUSD", Double.MAX_VALUE, 1.0, 1.0, 0.0, 100L, isTrusted = true),
            AmarTrackingPolicy.PositionSnapshot(2L, "XAUUSD", Double.MAX_VALUE, 1.0, 1.0, 0.0, 100L, isTrusted = true),
        )

        try {
            AmarTrackingPolicy.exposure(positions)
            fail("Expected aggregate exposure overflow to be rejected")
        } catch (expected: IllegalArgumentException) {
            assertEquals("EXPOSURE_OVERFLOW", expected.message)
        }
    }

    @Test
    fun untrustedRuntimeDataIsRejected() {
        val position = AmarTrackingPolicy.PositionSnapshot(
            1L, "XAUUSD", 0.01, 2500.0, 2505.0, 5.0, 100L
        )
        assertEquals(listOf("POSITION_0_UNTRUSTED_RUNTIME_DATA"), AmarTrackingPolicy.validateAll(listOf(position)))
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
            isTrusted = true,
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
