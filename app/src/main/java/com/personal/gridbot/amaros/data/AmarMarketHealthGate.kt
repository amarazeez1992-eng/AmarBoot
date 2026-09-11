package com.personal.gridbot.amaros.data

/** Fail-closed safety gate for execution decisions. No order should rely on stale/invalid market data. */
data class AmarMarketHealth(
    val quality: MarketDataQuality,
    val ageMs: Long,
    val reason: String
) {
    val canExecute: Boolean get() = quality == MarketDataQuality.LIVE
}

object AmarMarketHealthGate {
    fun evaluate(
        state: AmarMarketState,
        nowEpochMs: Long,
        maxAgeMs: Long
    ): AmarMarketHealth {
        if (maxAgeMs <= 0L) return AmarMarketHealth(MarketDataQuality.INVALID, Long.MAX_VALUE, "INVALID_MAX_AGE")
        if (state.quality != MarketDataQuality.LIVE) {
            return AmarMarketHealth(state.quality, Long.MAX_VALUE, "MARKET_NOT_LIVE")
        }
        val age = (nowEpochMs - state.timestampEpochMs).coerceAtLeast(0L)
        if (age > maxAgeMs) {
            return AmarMarketHealth(MarketDataQuality.STALE, age, "MARKET_STALE")
        }
        return AmarMarketHealth(MarketDataQuality.LIVE, age, "MARKET_LIVE")
    }
}
