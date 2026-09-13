package com.personal.gridbot.amaros.strategy.lab

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStrategyLabPolicyTest {
    @Test
    fun validDefinitionIsAccepted() {
        val definition = AmarStrategyLabPolicy.StrategyDefinition(
            id = "grid-gold",
            name = "Gold Grid",
            version = 1,
            rules = listOf("BUY_GRID", "BASKET_TP")
        )
        assertTrue(AmarStrategyLabPolicy.validate(definition).isEmpty())
    }

    @Test
    fun invalidMeasuredResultIsRejected() {
        val result = AmarStrategyLabPolicy.MeasuredResult(
            trades = -1,
            netProfitLoss = Double.NaN,
            winRate = 1.2,
            maxDrawdownPct = -1.0,
            profitFactor = -1.0
        )
        assertTrue(AmarStrategyLabPolicy.validateResult(result).isNotEmpty())
    }

    @Test
    fun comparisonUsesMeasuredMetricsOnly() {
        val first = AmarStrategyLabPolicy.MeasuredResult(10, 100.0, 0.6, 8.0, 1.8)
        val second = AmarStrategyLabPolicy.MeasuredResult(10, 90.0, 0.8, 5.0, 2.0)
        assertEquals(1, AmarStrategyLabPolicy.compare(first, second))
    }
}
