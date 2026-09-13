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
        if (errors.isEmpty()) {
            val expectedFreeMargin = snapshot.equity - snapshot.margin
            if (!expectedFreeMargin.isFinite()) errors += "FREE_MARGIN_OVERFLOW"
            else {
                val tolerance = maxOf(1e-9, kotlin.math.abs(expectedFreeMargin) * 1e-9)
                if (kotlin.math.abs(snapshot.freeMargin - expectedFreeMargin) > tolerance) {
                    errors += "FREE_MARGIN_INCONSISTENT"
                }
            }
        }
        return errors
    }

    fun marginLevelPct(snapshot: Snapshot): Double {
        require(validate(snapshot).isEmpty())
        if (snapshot.margin == 0.0) return Double.POSITIVE_INFINITY
        val result = snapshot.equity / snapshot.margin * 100.0
        require(!result.isNaN()) { "MARGIN_LEVEL_INVALID" }
        return result
    }

    fun netAccountDelta(snapshot: Snapshot): Double {
        require(validate(snapshot).isEmpty())
        return snapshot.equity - snapshot.balance
    }
}
