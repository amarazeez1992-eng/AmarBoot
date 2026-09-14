package com.personal.gridbot.amaros.agent

/** Central risk gate. Invalid state fails closed and every rejection is explicit and auditable. */
class AmarStageFourRiskEngine(private val limits: AmarRiskLimits = AmarRiskLimits()) {
    fun evaluate(snapshot: AmarRiskSnapshot, proposedLoss: Double): AmarRiskDecision {
        val reasons = mutableListOf<String>()
        val finiteSnapshot = listOf(
            snapshot.equity, snapshot.initialEquity, snapshot.dailyPnl, snapshot.peakEquity
        ).all { it.isFinite() }

        if (!finiteSnapshot || snapshot.initialEquity <= 0.0 || snapshot.equity < 0.0 || snapshot.peakEquity < 0.0 || snapshot.openPositions < 0) {
            reasons += "invalid_risk_snapshot"
        }
        if (!proposedLoss.isFinite() || proposedLoss < 0.0) reasons += "invalid_proposed_loss"

        if (snapshot.equity < limits.minimumEquity) reasons += "minimum_equity_breached"
        if (snapshot.openPositions >= limits.maxOpenPositions) reasons += "max_open_positions_reached"

        val initial = snapshot.initialEquity
        if (initial > 0.0 && (-snapshot.dailyPnl).coerceAtLeast(0.0) / initial > limits.maxDailyLossFraction) {
            reasons += "daily_loss_limit_breached"
        }

        if (snapshot.peakEquity > 0.0 &&
            (snapshot.peakEquity - snapshot.equity).coerceAtLeast(0.0) / snapshot.peakEquity > limits.maxDrawdownFraction
        ) {
            reasons += "max_drawdown_breached"
        }

        if (initial > 0.0 && proposedLoss.isFinite() &&
            proposedLoss / initial > limits.maxRiskPerTradeFraction
        ) {
            reasons += "per_trade_risk_limit_breached"
        }

        return AmarRiskDecision(reasons.isEmpty(), reasons.distinct())
    }
}
