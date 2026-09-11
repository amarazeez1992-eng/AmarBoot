package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.data.AmarMarketHealthGate
import com.personal.gridbot.amaros.data.AmarMarketState
import com.personal.gridbot.amaros.data.MarketDataQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMarketHealthGateTest {
    @Test fun live_market_is_executable_when_fresh() {
        val state = AmarMarketState(source = "MT5", timestampEpochMs = 10_000L, quality = MarketDataQuality.LIVE)
        val health = AmarMarketHealthGate.evaluate(state, 12_000L, 5_000L)
        assertTrue(health.canExecute)
        assertEquals(MarketDataQuality.LIVE, health.quality)
    }

    @Test fun stale_market_is_blocked() {
        val state = AmarMarketState(source = "MT5", timestampEpochMs = 10_000L, quality = MarketDataQuality.LIVE)
        val health = AmarMarketHealthGate.evaluate(state, 16_000L, 5_000L)
        assertFalse(health.canExecute)
        assertEquals(MarketDataQuality.STALE, health.quality)
    }

    @Test fun unavailable_market_is_blocked() {
        val state = AmarMarketState()
        val health = AmarMarketHealthGate.evaluate(state, 10_000L, 5_000L)
        assertFalse(health.canExecute)
        assertEquals(MarketDataQuality.UNAVAILABLE, health.quality)
    }
}
