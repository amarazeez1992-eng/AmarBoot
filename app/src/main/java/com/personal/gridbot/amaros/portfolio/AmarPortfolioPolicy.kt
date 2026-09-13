package com.personal.gridbot.amaros.portfolio

/** Independent portfolio/account boundary. Read-only calculations; no trade execution. */
object AmarPortfolioPolicy {
    data class Snapshot(
        val balance: Double,
        val equity: Double,
        val margin: Double,
        val freeMargin: Double,
        val floatingProfit: Double,
        val exposure: Double,
        val openPositions: Int,
        val pendingOrders: Int,
    )

    fun validate(snapshot: Snapshot): List<String> {
        val errors = mutableListOf<String>()
        if (!snapshot.balance.isFinite() || snapshot.balance < 0.0) errors += "BALANCE_INVALID"
        if (!snapshot.equity.isFinite() || snapshot.equity < 0.0) errors += "EQUITY_INVALID"
        if (!snapshot.margin.isFinite() || snapshot.margin < 0.0) errors += "MARGIN_INVALID"
        if (!snapshot.freeMargin.isFinite()) errors += "FREE_MARGIN_INVALID"
        if (!snapshot.floatingProfit.isFinite()) errors += "FLOATING_PROFIT_INVALID"
        if (!snapshot.exposure.isFinite() || snapshot.exposure < 0.0) errors += "EXPOSURE_INVALID"
        if (snapshot.openPositions < 0) errors += "OPEN_POSITIONS_INVALID"
        if (snapshot.pendingOrders < 0) errors += "PENDING_ORDERS_INVALID"
        return errors
    }

    fun marginLevelPct(snapshot: Snapshot): Double {
        require(validate(snapshot).isEmpty())
        if (snapshot.margin == 0.0) return Double.POSITIVE_INFINITY
        return snapshot.equity / snapshot.margin * 100.0
    }

    fun netAccountDelta(snapshot: Snapshot): Double {
        require(validate(snapshot).isEmpty())
        return snapshot.equity - snapshot.balance
    }
}
