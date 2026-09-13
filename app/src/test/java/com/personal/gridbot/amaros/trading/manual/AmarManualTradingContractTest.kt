package com.personal.gridbot.amaros.trading.manual

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarManualTradingContractTest {
    @Test
    fun validIntentIsAccepted() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.MARKET,
                volume = 0.01
            )
        )
        assertTrue(result.accepted)
    }

    @Test
    fun whitespaceOnlySymbolIsRejected() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "   ",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.MARKET,
                volume = 0.01
            )
        )
        assertFalse(result.accepted)
        assertTrue("SYMBOL_REQUIRED" in result.errors)
    }

    @Test
    fun invalidVolumeIsRejected() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.MARKET,
                volume = 0.0
            )
        )
        assertFalse(result.accepted)
    }

    @Test
    fun pendingOrderRequiresEntryPrice() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.PENDING,
                volume = 0.01
            )
        )
        assertFalse(result.accepted)
        assertTrue("ENTRY_PRICE_REQUIRED" in result.errors)
    }

    @Test
    fun marketOrderRejectsEntryPrice() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.MARKET,
                volume = 0.01,
                entryPrice = 2500.0
            )
        )
        assertFalse(result.accepted)
        assertTrue("ENTRY_PRICE_NOT_ALLOWED_FOR_MARKET" in result.errors)
    }

    @Test
    fun buyPendingRejectsStopLossAtOrAboveEntry() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.PENDING,
                volume = 0.01,
                entryPrice = 2500.0,
                stopLoss = 2500.0,
                takeProfit = 2510.0
            )
        )
        assertFalse(result.accepted)
        assertTrue("STOP_LOSS_SIDE_INVALID" in result.errors)
    }

    @Test
    fun sellPendingRejectsTakeProfitAtOrAboveEntry() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.SELL,
                orderType = AmarManualTradeIntent.OrderType.PENDING,
                volume = 0.01,
                entryPrice = 2500.0,
                stopLoss = 2510.0,
                takeProfit = 2500.0
            )
        )
        assertFalse(result.accepted)
        assertTrue("TAKE_PROFIT_SIDE_INVALID" in result.errors)
    }

    @Test
    fun validPendingProtectionIsAccepted() {
        val result = AmarManualTradeValidator.validate(
            AmarManualTradeIntent(
                symbol = "XAUUSD",
                side = AmarManualTradeIntent.Side.BUY,
                orderType = AmarManualTradeIntent.OrderType.PENDING,
                volume = 0.01,
                entryPrice = 2500.0,
                stopLoss = 2490.0,
                takeProfit = 2510.0
            )
        )
        assertTrue(result.accepted)
    }
}
