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
}
