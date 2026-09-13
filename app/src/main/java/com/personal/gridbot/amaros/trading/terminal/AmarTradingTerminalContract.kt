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
        get() = isTrusted && symbol.isNotBlank() && source.isNotBlank() && bid != null && ask != null &&
            bid > 0.0 && ask > 0.0 && bid <= ask && (spread == null || spread >= 0.0)

    fun validate(): List<String> = buildList {
        if (isTrusted && symbol.isBlank()) add("SYMBOL_REQUIRED")
        if (isTrusted && source.isBlank()) add("SOURCE_REQUIRED")
        if (bid != null && (!bid.isFinite() || bid <= 0.0)) add("BID_INVALID")
        if (ask != null && (!ask.isFinite() || ask <= 0.0)) add("ASK_INVALID")
        if (bid != null && ask != null && bid.isFinite() && ask.isFinite() && bid > ask) add("QUOTE_INVERTED")
        if (spread != null && (!spread.isFinite() || spread < 0.0)) add("SPREAD_INVALID")
        if (bid != null && ask != null && spread != null && bid.isFinite() && ask.isFinite() && spread.isFinite()) {
            val expected = ask - bid
            if (kotlin.math.abs(expected - spread) > 1e-9) add("SPREAD_INCONSISTENT")
        }
        if (timestampMs != null && timestampMs < 0L) add("TIMESTAMP_INVALID")
    }
}

/** Watchlist entry; it contains presentation metadata only and no fabricated market values. */
data class AmarWatchlistInstrument(
    val symbol: String,
    val displayName: String = symbol,
    val enabled: Boolean = true
) {
    init {
        require(symbol.isNotBlank())
        require(displayName.isNotBlank())
    }
}

object AmarTradingTerminalContract {
    const val VERSION = "1.1"
    const val UNAVAILABLE_SOURCE = "UNAVAILABLE"
}
