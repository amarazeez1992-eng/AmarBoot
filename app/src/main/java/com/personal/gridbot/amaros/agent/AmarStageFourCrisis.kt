package com.personal.gridbot.amaros.agent

import kotlin.math.abs

/** Detects abnormal market/data conditions before a decision can be promoted. */
class AmarStageFourCrisisEngine(private val limits: AmarCrisisLimits = AmarCrisisLimits()) {
    fun inspect(
        previous: AmarMarketCandle?,
        current: AmarMarketCandle,
        quote: AmarMarketQuote?,
        nowEpochMs: Long
    ): AmarCrisisState {
        val reasons = mutableListOf<String>()
        if (nowEpochMs < 0L || !validCandle(current)) reasons += "invalid_market_data"

        val reference = current.close
        if (reference.isFinite() && reference > 0.0) {
            val range = current.high - current.low
            if (range.isFinite() && range > reference * limits.maxBarRangeFraction) reasons += "bar_range_abnormal"
        }

        if (quote != null) {
            if (!quote.bid.isFinite() || !quote.ask.isFinite() || quote.bid <= 0.0 || quote.ask <= 0.0 || quote.ask < quote.bid) {
                reasons += "invalid_quote"
            } else if (quote.ask - quote.bid > reference.coerceAtLeast(1e-12) * limits.maxSpreadFraction) {
                reasons += "spread_abnormal"
            }
        }

        if (previous != null) {
            if (!previous.close.isFinite() || previous.close <= 0.0) {
                reasons += "invalid_previous_market_data"
            } else if (current.open.isFinite() && current.open > 0.0 &&
                abs(current.open - previous.close) / previous.close > limits.maxGapFraction
            ) {
                reasons += "price_gap_abnormal"
            }
        }

        val age = nowEpochMs - current.timestampEpochMs
        if (age > limits.maxDataAgeMs) reasons += "market_data_stale"
        if (current.timestampEpochMs > nowEpochMs + limits.maxDataAgeMs) reasons += "market_data_from_future"

        return AmarCrisisState(reasons.isNotEmpty(), reasons.distinct())
    }

    private fun validCandle(candle: AmarMarketCandle): Boolean {
        val values = listOf(candle.open, candle.high, candle.low, candle.close)
        return candle.timestampEpochMs >= 0L &&
            values.all { it.isFinite() && it > 0.0 } &&
            candle.high >= candle.low &&
            candle.high >= maxOf(candle.open, candle.close) &&
            candle.low <= minOf(candle.open, candle.close)
    }
}
