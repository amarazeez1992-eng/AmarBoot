package com.personal.gridbot.amaros.ai

import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStrategyQualityEngineTest {
    @Test
    fun completeStrategyRanksHigherThanEmpty() {
        val weak = AmarStrategyQualityEngine.audit("buy gold")
        val strong = AmarStrategyQualityEngine.audit(
            "symbol XAUUSD timeframe M1 market session regime trend setup condition entry trigger stop loss invalidation target take profit risk lot spread slippage execution volatility news session failure fallback cooldown backtest forward walk-forward oos overfit leak repaint"
        )
        assertTrue(strong.scorePct > weak.scorePct)
        assertTrue(strong.level >= 5)
    }
}
