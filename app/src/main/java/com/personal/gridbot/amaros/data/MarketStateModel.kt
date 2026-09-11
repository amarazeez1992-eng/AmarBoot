package com.personal.gridbot.amaros.data

/** Canonical, source-agnostic market state shared by every Amar interface. */
enum class MarketDirection { BUY, NEUTRAL, SELL, UNKNOWN }
enum class MarketStrength { WEAK, NORMAL, STRONG, UNKNOWN }
enum class CandleState { BULLISH, BEARISH, NEUTRAL, UNKNOWN }
enum class MarketSession { ASIA, LONDON, NEW_YORK, OVERLAP, CLOSED, UNKNOWN }
enum class MarketDataQuality { LIVE, STALE, INVALID, UNAVAILABLE }

data class AmarMarketState(
    val symbol: String = "XAUUSD",
    val timeframe: String = "M5",
    val bid: Double = 0.0,
    val ask: Double = 0.0,
    val direction: MarketDirection = MarketDirection.UNKNOWN,
    val strength: MarketStrength = MarketStrength.UNKNOWN,
    val candle: CandleState = CandleState.UNKNOWN,
    val session: MarketSession = MarketSession.UNKNOWN,
    val source: String = "NONE",
    val timestampEpochMs: Long = 0L,
    val quality: MarketDataQuality = MarketDataQuality.UNAVAILABLE
) {
    val spread: Double get() = if (bid.isFinite() && ask.isFinite() && bid > 0.0 && ask >= bid) ask - bid else 0.0
    val mid: Double get() = if (bid.isFinite() && ask.isFinite() && bid > 0.0 && ask > 0.0) (bid + ask) / 2.0 else 0.0
    val isLive: Boolean get() = quality == MarketDataQuality.LIVE && source.isNotBlank() && timestampEpochMs > 0L
}

/** Single in-process holder; UI reads state but cannot manufacture LIVE data. */
object AmarMarketStateStore {
    @Volatile private var state: AmarMarketState = AmarMarketState()
    fun current(): AmarMarketState = state
    fun publish(next: AmarMarketState) {
        val invalid = next.quality == MarketDataQuality.LIVE &&
            (!next.bid.isFinite() || !next.ask.isFinite() || next.bid <= 0.0 || next.ask < next.bid ||
                next.source.isBlank() || next.timestampEpochMs <= 0L)
        state = if (invalid) next.copy(quality = MarketDataQuality.INVALID) else next
    }
    fun reset() { state = AmarMarketState() }
}
