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
    fun incompleteSnapshotIsNotTrustedEvidence() {
        val snapshot = AmarPortfolioSnapshot(source = "TEST", isTrusted = true)
        assertTrue(snapshot.validate().isEmpty())
        assertFalse(snapshot.hasTrustedAccountEvidence)
    }
}
