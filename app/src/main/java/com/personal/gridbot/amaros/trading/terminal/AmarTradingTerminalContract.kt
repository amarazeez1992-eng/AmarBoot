package com.personal.gridbot.amaros.trading.terminal

/** Trusted market snapshot exposed to the trading terminal. Empty fields mean unavailable data. */
data class AmarTradingTerminalSnapshot(
    val symbol: String = "",
    val bid: Double? = null,
    val ask: Double? = null,
    val spread: Double? = null,
    val timestampMs: Long? = null,
    val source: String = "UNAVAILABLE",
    val isTrusted: Boolean = false
) {
    val hasUsablePrice: Boolean
        get() = isTrusted && bid != null && ask != null && bid > 0.0 && ask > 0.0
}

/** Watchlist entry; it contains presentation metadata only and no fabricated market values. */
data class AmarWatchlistInstrument(
    val symbol: String,
    val displayName: String = symbol,
    val enabled: Boolean = true
)

object AmarTradingTerminalContract {
    const val VERSION = "1.0"
    const val UNAVAILABLE_SOURCE = "UNAVAILABLE"
}
