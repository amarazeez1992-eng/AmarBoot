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
        require(open > 0.0 && high > 0.0 && low > 0.0 && close > 0.0)
        require(high >= maxOf(open, close, low))
        require(low <= minOf(open, close, high))
        require(volume == null || (volume.isFinite() && volume >= 0.0))
    }
}

data class AmarChartSeries(
    val symbol: String,
    val timeframe: String,
    val candles: List<AmarCandle>,
    val source: String,
    val isTrusted: Boolean
) {
    init {
        require(symbol.isNotBlank())
        require(timeframe.isNotBlank())
        require(source.isNotBlank())
        require(candles.zipWithNext().all { (left, right) -> right.timestampMs > left.timestampMs })
    }
}

object AmarChartContract {
    const val VERSION = "1.1"
}
