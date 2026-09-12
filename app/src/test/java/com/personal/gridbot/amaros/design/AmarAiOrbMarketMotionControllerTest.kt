package com.personal.gridbot.amaros.design

import com.personal.gridbot.amaros.bots.AmarMarketDataQuality
import com.personal.gridbot.amaros.bots.AmarMarketDirection
import com.personal.gridbot.amaros.bots.AmarMarketSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiOrbMarketMotionControllerTest {
    @Test
    fun unavailable_market_keeps_orb_at_neutral_baseline() {
        val state = AmarAiOrbMarketMotionController.from(AmarMarketSnapshot())
        assertEquals(0.0, state.intensity, 0.0001)
        assertEquals(2.5, state.driftPx, 0.0001)
        assertEquals(7.0, state.durationSeconds, 0.0001)
    }

    @Test
    fun live_strength_is_bounded_and_increases_motion() {
        val state = AmarAiOrbMarketMotionController.from(
            AmarMarketSnapshot(
                direction = AmarMarketDirection.BUY,
                strength = 0.9,
                source = com.personal.gridbot.amaros.bots.AmarMarketSource.MT5,
                quality = AmarMarketDataQuality.LIVE
            )
        )
        assertEquals(0.9, state.intensity, 0.0001)
        assertTrue(state.driftPx > 2.5)
        assertTrue(state.durationSeconds < 7.0)
    }

    @Test
    fun stale_market_cannot_drive_full_intensity() {
        val state = AmarAiOrbMarketMotionController.from(
            AmarMarketSnapshot(
                direction = AmarMarketDirection.SELL,
                strength = 1.0,
                source = com.personal.gridbot.amaros.bots.AmarMarketSource.MT5,
                quality = AmarMarketDataQuality.STALE
            )
        )
        assertEquals(0.35, state.intensity, 0.0001)
    }
}
