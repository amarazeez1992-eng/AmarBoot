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
}
