package com.personal.gridbot.amaros.trading.terminal

/** Read-only terminal state derived from trusted application market state. */
data class AmarTradingTerminalState(
    val selectedSymbol: String = "",
    val snapshot: AmarTradingTerminalSnapshot = AmarTradingTerminalSnapshot(),
    val watchlist: List<AmarWatchlistInstrument> = emptyList(),
    val selectedTimeframe: String = "M1"
) {
    init {
        require(selectedTimeframe.isNotBlank())
        require(watchlist.distinctBy { it.symbol }.size == watchlist.size)
        require(snapshot.validate().isEmpty())
        require(selectedSymbol.isBlank() || snapshot.symbol.isBlank() || selectedSymbol == snapshot.symbol)
    }

    val dataStatus: DataStatus
        get() = if (snapshot.hasUsablePrice && (selectedSymbol.isBlank() || selectedSymbol == snapshot.symbol)) {
            DataStatus.LIVE
        } else {
            DataStatus.UNAVAILABLE
        }

    enum class DataStatus { LIVE, UNAVAILABLE }
}

/** Deterministic state holder; it never invents prices or broker state. */
class AmarTradingTerminalStateStore {
    @Volatile
    private var state = AmarTradingTerminalState()

    fun current(): AmarTradingTerminalState = state

    fun publish(next: AmarTradingTerminalState) {
        state = next
    }
}
