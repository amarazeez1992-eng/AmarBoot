package com.personal.gridbot.amaros.portfolio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarPortfolioPolicyTest {
    @Test
    fun validSnapshotCalculatesPortfolioMetrics() {
        val snapshot = AmarPortfolioPolicy.Snapshot(
            balance = 1000.0,
            equity = 1100.0,
            margin = 200.0,
            freeMargin = 900.0,
            floatingProfit = 100.0,
            exposure = 500.0,
            openPositions = 2,
            pendingOrders = 3,
        )

        assertTrue(AmarPortfolioPolicy.validate(snapshot).isEmpty())
        assertEquals(550.0, AmarPortfolioPolicy.marginLevelPct(snapshot), 0.0001)
        assertEquals(100.0, AmarPortfolioPolicy.netAccountDelta(snapshot), 0.0001)
    }

    @Test
    fun zeroMarginProducesInfiniteMarginLevel() {
        val snapshot = AmarPortfolioPolicy.Snapshot(
            balance = 1000.0,
            equity = 1000.0,
            margin = 0.0,
            freeMargin = 1000.0,
            floatingProfit = 0.0,
            exposure = 0.0,
            openPositions = 0,
            pendingOrders = 0,
        )

        assertTrue(AmarPortfolioPolicy.marginLevelPct(snapshot).isInfinite())
    }

    @Test
    fun invalidSnapshotIsRejected() {
        val snapshot = AmarPortfolioPolicy.Snapshot(
            balance = -1.0,
            equity = Double.NaN,
            margin = -2.0,
            freeMargin = Double.NaN,
            floatingProfit = Double.POSITIVE_INFINITY,
            exposure = -3.0,
            openPositions = -1,
            pendingOrders = -2,
        )

        assertTrue(AmarPortfolioPolicy.validate(snapshot).isNotEmpty())
    }
}
