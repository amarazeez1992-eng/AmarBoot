package com.personal.gridbot.amaros.alerts

/** Independent alert evaluation boundary. Read-only and never executes trades. */
object AmarAlertCenterPolicy {
    data class Snapshot(
        val price: Double,
        val profit: Double,
        val drawdownPct: Double,
        val spread: Double,
        val marginLevelPct: Double,
    )

    data class Thresholds(
        val priceAbove: Double = 0.0,
        val priceBelow: Double = 0.0,
        val profitAtLeast: Double = 0.0,
        val lossAtMost: Double = 0.0,
        val drawdownAtLeastPct: Double = 0.0,
        val spreadAtLeast: Double = 0.0,
        val marginLevelAtMostPct: Double = 0.0,
    )

    fun validate(snapshot: Snapshot, thresholds: Thresholds): List<String> {
        val errors = mutableListOf<String>()
        if (!snapshot.price.isFinite() || snapshot.price < 0.0) errors += "PRICE_INVALID"
        if (!snapshot.profit.isFinite()) errors += "PROFIT_INVALID"
        if (!snapshot.drawdownPct.isFinite() || snapshot.drawdownPct < 0.0) errors += "DRAWDOWN_INVALID"
        if (!snapshot.spread.isFinite() || snapshot.spread < 0.0) errors += "SPREAD_INVALID"
        if (!snapshot.marginLevelPct.isFinite() || snapshot.marginLevelPct < 0.0) errors += "MARGIN_LEVEL_INVALID"
        if (!thresholds.priceAbove.isFinite() || thresholds.priceAbove < 0.0) errors += "PRICE_ABOVE_INVALID"
        if (!thresholds.priceBelow.isFinite() || thresholds.priceBelow < 0.0) errors += "PRICE_BELOW_INVALID"
        if (!thresholds.profitAtLeast.isFinite()) errors += "PROFIT_THRESHOLD_INVALID"
        if (!thresholds.lossAtMost.isFinite() || thresholds.lossAtMost > 0.0) errors += "LOSS_THRESHOLD_INVALID"
        if (!thresholds.drawdownAtLeastPct.isFinite() || thresholds.drawdownAtLeastPct < 0.0) errors += "DRAWDOWN_THRESHOLD_INVALID"
        if (!thresholds.spreadAtLeast.isFinite() || thresholds.spreadAtLeast < 0.0) errors += "SPREAD_THRESHOLD_INVALID"
        if (!thresholds.marginLevelAtMostPct.isFinite() || thresholds.marginLevelAtMostPct < 0.0) errors += "MARGIN_THRESHOLD_INVALID"
        return errors
    }

    fun triggered(snapshot: Snapshot, thresholds: Thresholds): List<String> {
        require(validate(snapshot, thresholds).isEmpty())
        val alerts = mutableListOf<String>()
        if (thresholds.priceAbove > 0.0 && snapshot.price >= thresholds.priceAbove) alerts += "PRICE_ABOVE"
        if (thresholds.priceBelow > 0.0 && snapshot.price <= thresholds.priceBelow) alerts += "PRICE_BELOW"
        if (thresholds.profitAtLeast > 0.0 && snapshot.profit >= thresholds.profitAtLeast) alerts += "PROFIT_TARGET"
        if (thresholds.lossAtMost < 0.0 && snapshot.profit <= thresholds.lossAtMost) alerts += "LOSS_LIMIT"
        if (thresholds.drawdownAtLeastPct > 0.0 && snapshot.drawdownPct >= thresholds.drawdownAtLeastPct) alerts += "DRAWDOWN"
        if (thresholds.spreadAtLeast > 0.0 && snapshot.spread >= thresholds.spreadAtLeast) alerts += "SPREAD"
        if (thresholds.marginLevelAtMostPct > 0.0 && snapshot.marginLevelPct <= thresholds.marginLevelAtMostPct) alerts += "MARGIN_LEVEL"
        return alerts
    }
}