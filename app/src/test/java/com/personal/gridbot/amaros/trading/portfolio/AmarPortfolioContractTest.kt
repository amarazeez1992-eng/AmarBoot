package com.personal.gridbot.amaros.trading.portfolio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarPortfolioContractTest {
    @Test
    fun trustedCompleteSnapshotIsUsable() {
        val snapshot = AmarPortfolioSnapshot(
            balance = 1000.0,
            equity = 1100.0,
            margin = 200.0,
            freeMargin = 900.0,
            floatingPnl = 100.0,
            exposure = 500.0,
            openPositions = 2,
            pendingOrders = 1,
            source = "TEST",
            isTrusted = true
        )
        assertTrue(snapshot.validate().isEmpty())
        assertTrue(snapshot.hasTrustedAccountEvidence)
    }

    @Test
    fun unavailableTrustedSourceIsNotUsable() {
        val snapshot = AmarPortfolioSnapshot(
            balance = 1000.0,
            equity = 1000.0,
            margin = 0.0,
            freeMargin = 1000.0,
            source = AmarPortfolioContract.UNAVAILABLE_SOURCE,
            isTrusted = true
        )
        assertTrue("SOURCE_UNAVAILABLE" in snapshot.validate())
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }

    @Test
    fun blankTrustedSourceIsNotUsable() {
        val snapshot = AmarPortfolioSnapshot(
            balance = 1000.0,
            equity = 1000.0,
            margin = 0.0,
            freeMargin = 1000.0,
            source = "   ",
            isTrusted = true
        )
        assertTrue("SOURCE_UNAVAILABLE" in snapshot.validate())
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }

    @Test
    fun invalidTrustedSnapshotIsNotUsable() {
        val snapshot = AmarPortfolioSnapshot(
            equity = Double.NaN,
            source = "TEST",
            isTrusted = true
        )
        assertTrue("EQUITY_INVALID" in snapshot.validate())
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }

    @Test
    fun allNumericBoundariesAreRejected() {
        val snapshot = AmarPortfolioSnapshot(
            balance = Double.POSITIVE_INFINITY,
            equity = Double.NEGATIVE_INFINITY,
            margin = Double.NaN,
            freeMargin = Double.POSITIVE_INFINITY,
            floatingPnl = Double.NaN,
            exposure = -1.0,
            openPositions = -1,
            pendingOrders = -1,
            source = "TEST",
            isTrusted = true
        )

        val errors = snapshot.validate()
        assertTrue("BALANCE_INVALID" in errors)
        assertTrue("EQUITY_INVALID" in errors)
        assertTrue("MARGIN_INVALID" in errors)
        assertTrue("FREE_MARGIN_INVALID" in errors)
        assertTrue("FLOATING_PNL_INVALID" in errors)
        assertTrue("EXPOSURE_INVALID" in errors)
        assertTrue("OPEN_POSITIONS_INVALID" in errors)
        assertTrue("PENDING_ORDERS_INVALID" in errors)
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }

    @Test
    fun incompleteSnapshotIsNotTrustedEvidence() {
        val snapshot = AmarPortfolioSnapshot(source = "TEST", isTrusted = true)
        assertTrue(snapshot.validate().isEmpty())
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }
}
