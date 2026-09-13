package com.personal.gridbot.amaros.risk

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRiskManagerPolicyTest {
    @Test
    fun safeSnapshotPasses() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = 1000.0,
            balance = 1000.0,
            floatingProfit = 5.0,
            exposure = 100.0,
            openPositions = 2,
            totalLots = 0.03,
            dailyProfit = 5.0,
            dailyLoss = 0.0,
            drawdownPct = 0.0,
            isTrusted = true,
        )
        val limits = AmarRiskManagerPolicy.Limits(
            maxExposure = 500.0,
            maxOpenPositions = 10,
            maxLots = 1.0,
            maxDailyLoss = 100.0,
            maxDrawdownPct = 10.0,
        )

        assertTrue(AmarRiskManagerPolicy.validate(snapshot, limits).isEmpty())
        assertTrue(AmarRiskManagerPolicy.breaches(snapshot, limits).isEmpty())
        assertTrue(AmarRiskManagerPolicy.canTrade(snapshot, limits))
    }

    @Test
    fun untrustedSnapshotFailsClosed() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = 1000.0,
            balance = 1000.0,
            floatingProfit = 0.0,
            exposure = 0.0,
            openPositions = 0,
            totalLots = 0.0,
            dailyProfit = 0.0,
            dailyLoss = 0.0,
            drawdownPct = 0.0,
        )
        val limits = AmarRiskManagerPolicy.Limits()
        assertTrue("UNTRUSTED_RUNTIME_DATA" in AmarRiskManagerPolicy.validate(snapshot, limits))
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, limits))
    }

    @Test
    fun breachedLimitsFailClosed() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = 900.0,
            balance = 1000.0,
            floatingProfit = -100.0,
            exposure = 500.0,
            openPositions = 10,
            totalLots = 1.0,
            dailyProfit = -100.0,
            dailyLoss = 100.0,
            drawdownPct = 10.0,
            isTrusted = true,
        )
        val limits = AmarRiskManagerPolicy.Limits(500.0, 10, 1.0, 100.0, 10.0)

        assertTrue(
            AmarRiskManagerPolicy.breaches(snapshot, limits).containsAll(
                listOf("MAX_EXPOSURE", "MAX_OPEN_POSITIONS", "MAX_LOTS", "MAX_DAILY_LOSS", "MAX_DRAWDOWN")
            )
        )
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, limits))
    }

    @Test
    fun nonFiniteSnapshotValuesFailClosed() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = Double.NaN,
            balance = Double.POSITIVE_INFINITY,
            floatingProfit = Double.NEGATIVE_INFINITY,
            exposure = Double.POSITIVE_INFINITY,
            openPositions = 0,
            totalLots = Double.NaN,
            dailyProfit = Double.POSITIVE_INFINITY,
            dailyLoss = Double.NaN,
            drawdownPct = Double.POSITIVE_INFINITY,
            isTrusted = true,
        )
        val errors = AmarRiskManagerPolicy.validate(snapshot, AmarRiskManagerPolicy.Limits())

        assertTrue("EQUITY_INVALID" in errors)
        assertTrue("BALANCE_INVALID" in errors)
        assertTrue("FLOATING_PROFIT_INVALID" in errors)
        assertTrue("EXPOSURE_INVALID" in errors)
        assertTrue("TOTAL_LOTS_INVALID" in errors)
        assertTrue("DAILY_PROFIT_INVALID" in errors)
        assertTrue("DAILY_LOSS_INVALID" in errors)
        assertTrue("DRAWDOWN_INVALID" in errors)
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, AmarRiskManagerPolicy.Limits()))
    }

    @Test
    fun negativeSnapshotValuesFailClosed() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = -0.01,
            balance = -0.01,
            floatingProfit = -1.0,
            exposure = -0.01,
            openPositions = -1,
            totalLots = -0.01,
            dailyProfit = -1.0,
            dailyLoss = -0.01,
            drawdownPct = -0.01,
            isTrusted = true,
        )
        val errors = AmarRiskManagerPolicy.validate(snapshot, AmarRiskManagerPolicy.Limits())

        assertTrue("EQUITY_INVALID" in errors)
        assertTrue("BALANCE_INVALID" in errors)
        assertTrue("EXPOSURE_INVALID" in errors)
        assertTrue("OPEN_POSITIONS_INVALID" in errors)
        assertTrue("TOTAL_LOTS_INVALID" in errors)
        assertTrue("DAILY_LOSS_INVALID" in errors)
        assertTrue("DRAWDOWN_INVALID" in errors)
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, AmarRiskManagerPolicy.Limits()))
    }

    @Test
    fun invalidLimitsFailClosed() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = 1000.0,
            balance = 1000.0,
            floatingProfit = 0.0,
            exposure = 0.0,
            openPositions = 0,
            totalLots = 0.0,
            dailyProfit = 0.0,
            dailyLoss = 0.0,
            drawdownPct = 0.0,
            isTrusted = true,
        )
        val limits = AmarRiskManagerPolicy.Limits(
            maxExposure = Double.NaN,
            maxOpenPositions = -1,
            maxLots = Double.POSITIVE_INFINITY,
            maxDailyLoss = -1.0,
            maxDrawdownPct = Double.NEGATIVE_INFINITY,
        )
        val errors = AmarRiskManagerPolicy.validate(snapshot, limits)

        assertTrue("MAX_EXPOSURE_INVALID" in errors)
        assertTrue("MAX_POSITIONS_INVALID" in errors)
        assertTrue("MAX_LOTS_INVALID" in errors)
        assertTrue("MAX_DAILY_LOSS_INVALID" in errors)
        assertTrue("MAX_DRAWDOWN_INVALID" in errors)
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, limits))
    }

    @Test
    fun exactPositiveLimitsAreBreaches() {
        val snapshot = AmarRiskManagerPolicy.Snapshot(
            equity = 1000.0,
            balance = 1000.0,
            floatingProfit = 0.0,
            exposure = 100.0,
            openPositions = 2,
            totalLots = 0.5,
            dailyProfit = 0.0,
            dailyLoss = 50.0,
            drawdownPct = 5.0,
            isTrusted = true,
        )
        val limits = AmarRiskManagerPolicy.Limits(
            maxExposure = 100.0,
            maxOpenPositions = 2,
            maxLots = 0.5,
            maxDailyLoss = 50.0,
            maxDrawdownPct = 5.0,
        )

        assertTrue(
            AmarRiskManagerPolicy.breaches(snapshot, limits).containsAll(
                listOf("MAX_EXPOSURE", "MAX_OPEN_POSITIONS", "MAX_LOTS", "MAX_DAILY_LOSS", "MAX_DRAWDOWN")
            )
        )
        assertFalse(AmarRiskManagerPolicy.canTrade(snapshot, limits))
    }
}
