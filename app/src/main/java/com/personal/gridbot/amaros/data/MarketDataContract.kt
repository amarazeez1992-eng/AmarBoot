package com.personal.gridbot.amaros.data

/** Normalized market snapshot shared by all UI/analysis engines. */
data class MarketSnapshot(
    val symbol: String = "XAUUSD",
    val timeframe: String = "M5",
    val bid: Double = 0.0,
    val ask: Double = 0.0,
    val timestampEpochMs: Long = 0L
) {
    val spread: Double get() = (ask - bid).coerceAtLeast(0.0)
    val mid: Double get() = if (bid > 0.0 && ask > 0.0) (bid + ask) / 2.0 else 0.0
}
