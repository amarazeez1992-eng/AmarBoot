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
}
