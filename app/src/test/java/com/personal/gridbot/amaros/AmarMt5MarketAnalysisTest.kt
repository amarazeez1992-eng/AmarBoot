package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.broker.AmarMt5Candle
import com.personal.gridbot.amaros.broker.AmarMt5MarketAnalysis
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarMt5MarketAnalysisTest {
    @Test fun insufficient_data_is_neutral() {
        val candles = (0 until 5).map { i ->
            AmarMt5Candle(i.toLong(), 100.0, 101.0, 99.0, 100.0, 10L)
        }
        assertEquals("محايد", AmarMt5MarketAnalysis.direction(candles).labelAr)
    }

    @Test fun rising_candles_are_bullish() {
        val candles = (0 until 20).map { i ->
            val open = 100.0 + i
            AmarMt5Candle(i.toLong(), open, open + 2.0, open - 0.2, open + 1.0, 100L)
        }
        assertEquals("صاعد", AmarMt5MarketAnalysis.direction(candles).labelAr)
    }

    @Test fun falling_candles_are_bearish() {
        val candles = (0 until 20).map { i ->
            val open = 120.0 - i
            AmarMt5Candle(i.toLong(), open, open + 0.2, open - 2.0, open - 1.0, 100L)
        }
        assertEquals("هابط", AmarMt5MarketAnalysis.direction(candles).labelAr)
    }
}
