package com.personal.gridbot.amaros.trading.charts

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarChartAnalysisTest {
    @Test
    fun derivesSupportResistanceFromSuppliedCandles() {
        val series = AmarChartSeries(
            symbol = "XAUUSD",
            timeframe = "M1",
            source = "TEST",
            isTrusted = true,
            candles = listOf(
                AmarCandle(1L, 100.0, 105.0, 99.0, 103.0),
                AmarCandle(2L, 103.0, 108.0, 101.0, 107.0)
            )
        )
        assertEquals(99.0, AmarChartAnalysis.support(series), 0.0)
        assertEquals(108.0, AmarChartAnalysis.resistance(series), 0.0)
        assertEquals(107.0, AmarChartAnalysis.lastClose(series), 0.0)
    }
}
