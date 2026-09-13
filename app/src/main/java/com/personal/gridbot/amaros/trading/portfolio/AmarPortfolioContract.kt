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
    val source: String = AmarPortfolioContract.UNAVAILABLE_SOURCE,
    val isTrusted: Boolean = false
) {
    fun validate(): List<String> = buildList {
        if (isTrusted && (source.isBlank() || source == AmarPortfolioContract.UNAVAILABLE_SOURCE)) add("SOURCE_UNAVAILABLE")
        if (balance != null && (!balance.isFinite() || balance < 0.0)) add("BALANCE_INVALID")
        if (equity != null && (!equity.isFinite() || equity < 0.0)) add("EQUITY_INVALID")
        if (margin != null && (!margin.isFinite() || margin < 0.0)) add("MARGIN_INVALID")
        if (freeMargin != null && !freeMargin.isFinite()) add("FREE_MARGIN_INVALID")
        if (floatingPnl != null && !floatingPnl.isFinite()) add("FLOATING_PNL_INVALID")
        if (exposure != null && (!exposure.isFinite() || exposure < 0.0)) add("EXPOSURE_INVALID")
        if (openPositions != null && openPositions < 0) add("OPEN_POSITIONS_INVALID")
        if (pendingOrders != null && pendingOrders < 0) add("PENDING_ORDERS_INVALID")
    }

    val hasTrustedAccountEvidence: Boolean
        get() = isTrusted && validate().isEmpty() &&
            balance != null && equity != null && margin != null && freeMargin != null
}

object AmarPortfolioContract {
    const val VERSION = "1.2"
    const val UNAVAILABLE_SOURCE = "UNAVAILABLE"
}
