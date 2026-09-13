package com.personal.gridbot.amaros.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAlertCenterPolicyTest {
    private val snapshot = AmarAlertCenterPolicy.Snapshot(
        price = 2505.0,
        profit = 25.0,
        drawdownPct = 8.0,
        spread = 0.8,
        marginLevelPct = 140.0,
    )

    @Test
    fun validSnapshotProducesExpectedAlerts() {
        val thresholds = AmarAlertCenterPolicy.Thresholds(
            priceAbove = 2500.0,
            profitAtLeast = 20.0,
            drawdownAtLeastPct = 8.0,
            spreadAtLeast = 0.5,
            marginLevelAtMostPct = 150.0,
        )
        assertTrue(AmarAlertCenterPolicy.validate(snapshot, thresholds).isEmpty())
        assertEquals(
            listOf("PRICE_ABOVE", "PROFIT_TARGET", "DRAWDOWN", "SPREAD", "MARGIN_LEVEL"),
            AmarAlertCenterPolicy.triggered(snapshot, thresholds)
        )
    }

    @Test
    fun lossLimitTriggersOnNegativeProfit() {
        val losing = snapshot.copy(profit = -15.0)
        val thresholds = AmarAlertCenterPolicy.Thresholds(lossLimit = 10.0)
        assertEquals(listOf("LOSS_LIMIT"), AmarAlertCenterPolicy.triggered(losing, thresholds))
    }

    @Test
    fun disabledThresholdsDoNotTrigger() {
        val thresholds = AmarAlertCenterPolicy.Thresholds()
        assertTrue(AmarAlertCenterPolicy.triggered(snapshot, thresholds).isEmpty())
    }

    @Test
    fun invalidSnapshotAndThresholdsAreRejected() {
        val invalidSnapshot = snapshot.copy(price = Double.NaN, spread = -1.0)
        val invalidThresholds = AmarAlertCenterPolicy.Thresholds(lossLimit = -1.0)
        val errors = AmarAlertCenterPolicy.validate(invalidSnapshot, invalidThresholds)
        assertTrue("PRICE_INVALID" in errors)
        assertTrue("SPREAD_INVALID" in errors)
        assertTrue("LOSS_THRESHOLD_INVALID" in errors)
        assertFalse(AmarAlertCenterPolicy.canTriggerSafely(invalidSnapshot, invalidThresholds))
    }

    @Test
    fun zeroMarginInfinityIsValidEvidence() {
        val zeroMargin = snapshot.copy(marginLevelPct = Double.POSITIVE_INFINITY)
        assertTrue(AmarAlertCenterPolicy.validate(zeroMargin, AmarAlertCenterPolicy.Thresholds()).isEmpty())
    }
}
