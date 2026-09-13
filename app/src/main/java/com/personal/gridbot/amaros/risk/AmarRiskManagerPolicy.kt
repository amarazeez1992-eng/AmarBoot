package com.personal.gridbot.amaros.risk

/** Independent risk hardening boundary. It evaluates trusted account/position risk without executing trades. */
object AmarRiskManagerPolicy {
    data class Snapshot(
        val equity: Double,
        val balance: Double,
        val floatingProfit: Double,
        val exposure: Double,
        val openPositions: Int,
        val totalLots: Double,
        val dailyProfit: Double,
        val dailyLoss: Double,
        val drawdownPct: Double,
        val isTrusted: Boolean = false,
    )

    data class Limits(
        val maxExposure: Double = 0.0,
        val maxOpenPositions: Int = 0,
        val maxLots: Double = 0.0,
        val maxDailyLoss: Double = 0.0,
        val maxDrawdownPct: Double = 0.0,
    )

    fun validate(snapshot: Snapshot, limits: Limits): List<String> {
        val errors = mutableListOf<String>()
        if (!snapshot.isTrusted) errors += "UNTRUSTED_RUNTIME_DATA"
        if (!snapshot.equity.isFinite() || snapshot.equity < 0.0) errors += "EQUITY_INVALID"
        if (!snapshot.balance.isFinite() || snapshot.balance < 0.0) errors += "BALANCE_INVALID"
        if (!snapshot.floatingProfit.isFinite()) errors += "FLOATING_PROFIT_INVALID"
        if (!snapshot.exposure.isFinite() || snapshot.exposure < 0.0) errors += "EXPOSURE_INVALID"
        if (snapshot.openPositions < 0) errors += "OPEN_POSITIONS_INVALID"
        if (!snapshot.totalLots.isFinite() || snapshot.totalLots < 0.0) errors += "TOTAL_LOTS_INVALID"
        if (!snapshot.dailyProfit.isFinite()) errors += "DAILY_PROFIT_INVALID"
        if (!snapshot.dailyLoss.isFinite() || snapshot.dailyLoss < 0.0) errors += "DAILY_LOSS_INVALID"
        if (!snapshot.drawdownPct.isFinite() || snapshot.drawdownPct < 0.0) errors += "DRAWDOWN_INVALID"
        if (!limits.maxExposure.isFinite() || limits.maxExposure < 0.0) errors += "MAX_EXPOSURE_INVALID"
        if (limits.maxOpenPositions < 0) errors += "MAX_POSITIONS_INVALID"
        if (!limits.maxLots.isFinite() || limits.maxLots < 0.0) errors += "MAX_LOTS_INVALID"
        if (!limits.maxDailyLoss.isFinite() || limits.maxDailyLoss < 0.0) errors += "MAX_DAILY_LOSS_INVALID"
        if (!limits.maxDrawdownPct.isFinite() || limits.maxDrawdownPct < 0.0) errors += "MAX_DRAWDOWN_INVALID"
        return errors
    }

    fun breaches(snapshot: Snapshot, limits: Limits): List<String> {
        require(validate(snapshot, limits).isEmpty())
        val breaches = mutableListOf<String>()
        if (limits.maxExposure > 0.0 && snapshot.exposure >= limits.maxExposure) breaches += "MAX_EXPOSURE"
        if (limits.maxOpenPositions > 0 && snapshot.openPositions >= limits.maxOpenPositions) breaches += "MAX_OPEN_POSITIONS"
        if (limits.maxLots > 0.0 && snapshot.totalLots >= limits.maxLots) breaches += "MAX_LOTS"
        if (limits.maxDailyLoss > 0.0 && snapshot.dailyLoss >= limits.maxDailyLoss) breaches += "MAX_DAILY_LOSS"
        if (limits.maxDrawdownPct > 0.0 && snapshot.drawdownPct >= limits.maxDrawdownPct) breaches += "MAX_DRAWDOWN"
        return breaches
    }

    fun canTrade(snapshot: Snapshot, limits: Limits): Boolean =
        validate(snapshot, limits).isEmpty() && breaches(snapshot, limits).isEmpty()
}
