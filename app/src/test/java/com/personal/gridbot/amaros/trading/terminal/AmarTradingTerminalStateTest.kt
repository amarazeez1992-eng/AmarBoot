package com.personal.gridbot.amaros.trading.terminal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarTradingTerminalStateTest {
    @Test
    fun selectedSymbolMustMatchTrustedSnapshot() {
        val snapshot = AmarTradingTerminalSnapshot(
            symbol = "XAUUSD",
            bid = 2504.0,
            ask = 2505.0,
            spread = 1.0,
            timestampMs = 1L,
            source = "TEST",
            isTrusted = true
        )
        val state = AmarTradingTerminalState(selectedSymbol = "XAUUSD", snapshot = snapshot)
        assertEquals(AmarTradingTerminalState.DataStatus.LIVE, state.dataStatus)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mismatchedSelectedSymbolIsRejected() {
        AmarTradingTerminalState(
            selectedSymbol = "EURUSD",
            snapshot = AmarTradingTerminalSnapshot(
                symbol = "XAUUSD",
                bid = 2504.0,
                ask = 2505.0,
                spread = 1.0,
                timestampMs = 1L,
                source = "TEST",
                isTrusted = true
            )
        )
    }
}
