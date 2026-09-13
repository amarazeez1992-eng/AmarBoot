package com.personal.gridbot.amaros.trading.portfolio

/** Trusted account snapshot. Null means the provider did not supply the value. */
data class AmarPortfolioSnapshot(
    val balance: Double? = null,
    val equity: Double? = null,
    val margin: Double? = null,
    val freeMargin: Double? = null,
    val floatingPnl: Double? = null,
    val exposure: Double? = null,
    val openPositions: Int? = null,
    val pendingOrders: Int? = null,
    val source: String = "UNAVAILABLE",
    val isTrusted: Boolean = false
)

object AmarPortfolioContract {
    const val VERSION = "1.0"
}
