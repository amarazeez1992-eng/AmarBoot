package com.personal.gridbot.amaros.trading.terminal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradingTerminalContractTest {
    @Test
    fun trustedSnapshotRequiresConsistentQuote() {
        val invalid = AmarTradingTerminalSnapshot(
            symbol = "XAUUSD",
            bid = 2505.0,
            ask = 2504.0,
            spread = 1.0,
            timestampMs = 1L,
            source = "TEST",
            isTrusted = true
        )
        assertTrue("QUOTE_INVERTED" in invalid.validate())
        assertFalse(invalid.hasUsablePrice)
    }

    @Test
    fun unavailableSnapshotDoesNotClaimLiveData() {
        val snapshot = AmarTradingTerminalSnapshot()
        assertTrue(snapshot.validate().isEmpty())
        assertFalse(snapshot.hasUsablePrice)
    }

    @Test
    fun trustedConsistentSnapshotIsUsable() {
        val snapshot = AmarTradingTerminalSnapshot(
            symbol = "XAUUSD",
            bid = 2504.0,
            ask = 2505.0,
            spread = 1.0,
            timestampMs = 1L,
            source = "TEST",
            isTrusted = true
        )
        assertTrue(snapshot.validate().isEmpty())
        assertTrue(snapshot.hasUsablePrice)
    }

    @Test
    fun trustedSnapshotCannotOmitQuoteEvidence() {
        val snapshot = AmarTradingTerminalSnapshot(
            symbol = "XAUUSD",
            timestampMs = 1L,
            source = "TEST",
            isTrusted = true
        )
        val errors = snapshot.validate()
        assertTrue("BID_REQUIRED" in errors)
        assertTrue("ASK_REQUIRED" in errors)
        assertTrue("SPREAD_REQUIRED" in errors)
        assertFalse(snapshot.hasUsablePrice)
    }

    @Test
    fun spreadToleranceIsRelativeForLargeQuotes() {
        val snapshot = AmarTradingTerminalSnapshot(
            symbol = "XAUUSD",
            bid = 250000000.0,
            ask = 250000001.0,
            spread = 1.0000000001,
            timestampMs = 1L,
            source = "TEST",
            isTrusted = true
        )
        assertTrue(snapshot.validate().isEmpty())
    }
}
