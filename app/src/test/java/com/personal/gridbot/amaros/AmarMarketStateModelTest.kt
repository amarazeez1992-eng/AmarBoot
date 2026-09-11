package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.data.AmarMarketDataPipeline
import com.personal.gridbot.amaros.data.AmarMarketStateStore
import com.personal.gridbot.amaros.data.MarketDataQuality
import com.personal.gridbot.amaros.data.MarketSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMarketStateModelTest {
    @Test fun default_state_is_not_live() {
        AmarMarketStateStore.reset()
        assertFalse(AmarMarketStateStore.current().isLive)
        assertEquals(MarketDataQuality.UNAVAILABLE, AmarMarketStateStore.current().quality)
    }

    @Test fun valid_snapshot_becomes_live_normalized_state() {
        AmarMarketStateStore.reset()
        AmarMarketDataPipeline().publish(MarketSnapshot("XAUUSD", "M5", 2500.0, 2500.2, 123456789L))
        val state = AmarMarketStateStore.current()
        assertTrue(state.isLive)
        assertEquals(2500.1, state.mid, 1e-9)
        assertEquals(0.2, state.spread, 1e-9)
    }

    @Test fun invalid_snapshot_cannot_become_live() {
        AmarMarketStateStore.reset()
        AmarMarketDataPipeline().publish(MarketSnapshot("XAUUSD", "M5", 0.0, 2500.2, 123456789L))
        assertFalse(AmarMarketStateStore.current().isLive)
        assertEquals(MarketDataQuality.INVALID, AmarMarketStateStore.current().quality)
    }
}
