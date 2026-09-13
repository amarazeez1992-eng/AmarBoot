package com.personal.gridbot.amaros.trading.charts

/** Deterministic chart-level calculations; no trading signal is generated here. */
object AmarChartAnalysis {
    fun support(series: AmarChartSeries): Double? =
        series.candles.minOfOrNull { it.low }

    fun resistance(series: AmarChartSeries): Double? =
        series.candles.maxOfOrNull { it.high }

    fun lastClose(series: AmarChartSeries): Double? =
        series.candles.lastOrNull()?.close
}
