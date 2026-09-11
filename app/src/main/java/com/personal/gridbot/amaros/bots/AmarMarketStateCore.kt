package com.personal.gridbot.amaros.bots

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.personal.gridbot.amaros.chart.AmarTimeframe

/**
 * Canonical market-state model shared by every Bot Lab interface.
 *
 * This is a data/state boundary only. It does not calculate trading signals,
 * place orders, modify the strategy, or manufacture market values.
 * A future MT5/market provider will publish verified snapshots here.
 */
enum class AmarMarketDirection { BUY, NEUTRAL, SELL, UNKNOWN }

enum class AmarMarketDataQuality { LIVE, DELAYED, STALE, UNAVAILABLE }

enum class AmarMarketSource { MT5, TRADING_VIEW, SIMULATION, NONE }

data class AmarMarketSnapshot(
    val symbol: String = "",
    val timeframe: AmarTimeframe = AmarTimeframe.M1,
    val bid: Double? = null,
    val ask: Double? = null,
    val spread: Double? = null,
    val direction: AmarMarketDirection = AmarMarketDirection.UNKNOWN,
    val strength: Double? = null,
    val candleOpen: Double? = null,
    val candleHigh: Double? = null,
    val candleLow: Double? = null,
    val candleClose: Double? = null,
    val session: String? = null,
    val source: AmarMarketSource = AmarMarketSource.NONE,
    val quality: AmarMarketDataQuality = AmarMarketDataQuality.UNAVAILABLE,
    val sourceTimestampMillis: Long? = null,
    val updatedAtMillis: Long? = null
) {
    val isUsable: Boolean
        get() = quality == AmarMarketDataQuality.LIVE && source != AmarMarketSource.NONE

    val hasPrice: Boolean
        get() = bid != null || ask != null
}

/**
 * Single in-process source of truth for market state in the Android UI layer.
 * The initial state is deliberately empty; no fake price/signal is exposed.
 */
object AmarMarketStateStore {
    var snapshot: AmarMarketSnapshot by mutableStateOf(AmarMarketSnapshot())
        private set

    fun publish(next: AmarMarketSnapshot) {
        snapshot = next
    }

    fun clear() {
        snapshot = AmarMarketSnapshot()
    }
}
