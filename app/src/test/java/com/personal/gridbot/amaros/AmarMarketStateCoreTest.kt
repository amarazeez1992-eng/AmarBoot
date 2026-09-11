package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.bots.AmarMarketDataQuality
import com.personal.gridbot.amaros.bots.AmarMarketDirection
import com.personal.gridbot.amaros.bots.AmarMarketSnapshot
import com.personal.gridbot.amaros.bots.AmarMarketSource
import com.personal.gridbot.amaros.chart.AmarTimeframe
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMarketStateCoreTest {
    @Test fun empty_snapshot_is_not_usable() {
        assertFalse(AmarMarketSnapshot().isUsable)
        assertFalse(AmarMarketSnapshot().hasPrice)
    }

    @Test fun live_mt5_snapshot_is_usable() {
        val snapshot = AmarMarketSnapshot(
            symbol = "XAUUSD",
            timeframe = AmarTimeframe.M5,
            bid = 2500.10,
            ask = 2500.15,
            spread = 0.05,
            direction = AmarMarketDirection.BUY,
            strength = 82.0,
            source = AmarMarketSource.MT5,
            quality = AmarMarketDataQuality.LIVE,
            sourceTimestampMillis = 1L,
            updatedAtMillis = 2L
        )
        assertTrue(snapshot.isUsable)
        assertTrue(snapshot.hasPrice)
    }

    @Test fun live_without_source_is_not_usable() {
        val snapshot = AmarMarketSnapshot(
            quality = AmarMarketDataQuality.LIVE,
            bid = 1.0,
            ask = 1.1
        )
        assertFalse(snapshot.isUsable)
    }
}
