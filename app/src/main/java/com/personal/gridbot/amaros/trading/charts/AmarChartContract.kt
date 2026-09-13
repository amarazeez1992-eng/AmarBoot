package com.personal.gridbot.amaros.trading.charts

/** One real candle supplied by a trusted market-data source. */
data class AmarCandle(
    val timestampMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double? = null
) {
    init {
        require(timestampMs >= 0L)
        require(open.isFinite() && high.isFinite() && low.isFinite() && close.isFinite())
        require(high >= maxOf(open, close, low))
        require(low <= minOf(open, close, high))
        require(volume == null || volume.isFinite() && volume >= 0.0)
    }
}

data class AmarChartSeries(
    val symbol: String,
    val timeframe: String,
    val candles: List<AmarCandle>,
    val source: String,
    val isTrusted: Boolean
)

object AmarChartContract {
    const val VERSION = "1.0"
}
