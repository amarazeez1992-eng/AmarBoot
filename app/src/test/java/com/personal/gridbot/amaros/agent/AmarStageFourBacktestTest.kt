package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Deterministic synthetic walk-forward-style regression for the Stage 4 simulator. */
class AmarStageFourBacktestTest {
    @Test
    fun deterministic_backtest_is_reproducible_and_reports_metrics() {
        val candles = listOf(
            candle(1, 100.0), candle(2, 102.0), candle(3, 104.0), candle(4, 103.0),
            candle(5, 101.0), candle(6, 99.0), candle(7, 100.0), candle(8, 103.0),
            candle(9, 106.0), candle(10, 105.0), candle(11, 102.0), candle(12, 100.0)
        )
        val signals = listOf(
            AmarStrategySignal(1, AmarSignalDirection.LONG),
            AmarStrategySignal(4, AmarSignalDirection.SHORT),
            AmarStrategySignal(7, AmarSignalDirection.LONG),
            AmarStrategySignal(10, AmarSignalDirection.SHORT),
            AmarStrategySignal(12, AmarSignalDirection.FLAT)
        )
        val config = AmarSimulationConfig(initialEquity = 1000.0, quantity = 1.0, feePerTrade = 0.25, slippagePerUnit = 0.10)
        val engine = AmarStageFourSimulationEngine()
        val first = engine.run(candles, signals, config)
        val second = engine.run(candles, signals, config)

        assertTrue(first.completed)
        assertEquals(first, second)
        assertEquals(4, first.trades.size)
        assertEquals(2.55, first.trades[0].netPnl, 0.000001)
        assertEquals(2.55, first.trades[1].netPnl, 0.000001)
        assertEquals(4.55, first.trades[2].netPnl, 0.000001)
        assertEquals(4.55, first.trades[3].netPnl, 0.000001)
        assertEquals(14.2, first.trades.sumOf { it.netPnl }, 0.000001)
        assertEquals(1014.2, first.finalEquity, 0.000001)
        assertEquals(1.0, first.winRate, 0.000001)
        assertEquals(Double.POSITIVE_INFINITY, first.profitFactor, 0.0)
        assertEquals(0.0, first.maxDrawdown, 0.000001)
    }

    private fun candle(time: Long, close: Double) = AmarMarketCandle(time, close, close, close, close)
}
