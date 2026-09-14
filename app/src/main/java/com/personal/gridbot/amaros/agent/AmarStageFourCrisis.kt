package com.personal.gridbot.amaros.agent

/** Detects abnormal market/data conditions before a decision can be promoted. */
class AmarStageFourCrisisEngine(private val limits: AmarCrisisLimits = AmarCrisisLimits()) {
    fun inspect(previous: AmarMarketCandle?, current: AmarMarketCandle, quote: AmarMarketQuote?, nowEpochMs: Long): AmarCrisisState {
        val reasons = mutableListOf<String>()
        val reference = current.close.coerceAtLeast(1e-12)
        if (quote != null && quote.ask - quote.bid > reference * limits.maxSpreadFraction) reasons += "spread_abnormal"
        if (current.high - current.low > reference * limits.maxBarRangeFraction) reasons += "bar_range_abnormal"
        if (previous != null && previous.close != 0.0 && kotlin.math.abs(current.open - previous.close) / kotlin.math.abs(previous.close) > limits.maxGapFraction) reasons += "price_gap_abnormal"
        if (limits.maxDataAgeMs > 0L && nowEpochMs - current.timestampEpochMs > limits.maxDataAgeMs) reasons += "market_data_stale"
        if (current.timestampEpochMs > nowEpochMs + limits.maxDataAgeMs) reasons += "market_data_from_future"
        return AmarCrisisState(reasons.isNotEmpty(), reasons.distinct())
    }
}
