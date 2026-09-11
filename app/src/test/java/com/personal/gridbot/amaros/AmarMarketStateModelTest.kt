package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.data.AmarMarketDataPipeline
import com.personal.gridbot.amaros.data.AmarMarketState
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

    @Test fun non_finite_snapshot_cannot_become_live() {
        AmarMarketStateStore.reset()
        AmarMarketDataPipeline().publish(MarketSnapshot("XAUUSD", "M5", Double.NaN, 2500.2, 123456789L))
        assertFalse(AmarMarketStateStore.current().isLive)
        assertEquals(MarketDataQuality.INVALID, AmarMarketStateStore.current().quality)
    }

    @Test fun direct_live_publish_is_fail_closed_for_invalid_prices() {
        AmarMarketStateStore.reset()
        AmarMarketStateStore.publish(
            AmarMarketState(
                symbol = "XAUUSD",
                timeframe = "M5",
                bid = 2500.2,
                ask = 2500.1,
                source = "TEST",
                timestampEpochMs = 123456789L,
                quality = MarketDataQuality.LIVE
            )
        )
        assertFalse(AmarMarketStateStore.current().isLive)
        assertEquals(MarketDataQuality.INVALID, AmarMarketStateStore.current().quality)
    }
}
