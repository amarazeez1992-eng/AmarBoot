package com.personal.gridbot.amaros.data

/**
 * Source boundary for market data. The Android app may publish normalized snapshots,
 * while MT5/TradingView connectors can be added later without changing UI contracts.
 */
interface AmarMarketDataProvider {
    fun start()
    fun stop()
    fun publish(snapshot: MarketSnapshot)
}

class AmarMarketDataPipeline : AmarMarketDataProvider {
    override fun start() = Unit
    override fun stop() = Unit

    override fun publish(snapshot: MarketSnapshot) {
        val invalid = !snapshot.bid.isFinite() || !snapshot.ask.isFinite() ||
            snapshot.bid <= 0.0 || snapshot.ask < snapshot.bid || snapshot.timestampEpochMs <= 0L
        if (invalid) {
            AmarMarketStateStore.publish(
                AmarMarketState(
                    symbol = snapshot.symbol,
                    timeframe = snapshot.timeframe,
                    quality = MarketDataQuality.INVALID
                )
            )
            return
        }
        AmarMarketStateStore.publish(
            AmarMarketState(
                symbol = snapshot.symbol,
                timeframe = snapshot.timeframe,
                bid = snapshot.bid,
                ask = snapshot.ask,
                source = "NORMALIZED_PROVIDER",
                timestampEpochMs = snapshot.timestampEpochMs,
                quality = MarketDataQuality.LIVE
            )
        )
    }
}
