package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.ai.AmarAdvancedStrategyTestEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAdvancedStrategyTestEngineTest {
    @Test
    fun measuredMetricsAreDeterministic() {
        val candles = (0 until 8).map { i ->
            AmarAdvancedStrategyTestEngine.Candle(i.toLong(), 100.0, 103.0, 99.0, 102.0 + i)
        }
        val signals = listOf(
            AmarAdvancedStrategyTestEngine.Signal(0, AmarAdvancedStrategyTestEngine.Side.BUY, 100.0, 99.0, 103.0),
            AmarAdvancedStrategyTestEngine.Signal(2, AmarAdvancedStrategyTestEngine.Side.SELL, 104.0, 105.0, 102.0)
        )
        val report = AmarAdvancedStrategyTestEngine.evaluate(candles, signals)
        assertTrue(report.verified)
        assertEquals(2, report.sampleSize)
        assertEquals(50.0, report.winRatePct, 0.0001)
    }
}
