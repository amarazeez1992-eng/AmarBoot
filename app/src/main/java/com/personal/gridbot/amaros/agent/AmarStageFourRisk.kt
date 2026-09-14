package com.personal.gridbot.amaros.agent

/** Central risk gate. A rejection is explicit and auditable. */
class AmarStageFourRiskEngine(private val limits: AmarRiskLimits = AmarRiskLimits()) {
    fun evaluate(snapshot: AmarRiskSnapshot, proposedLoss: Double): AmarRiskDecision {
        val reasons = mutableListOf<String>()
        if (snapshot.equity < limits.minimumEquity) reasons += "minimum_equity_breached"
        if (snapshot.openPositions >= limits.maxOpenPositions) reasons += "max_open_positions_reached"
        if (snapshot.initialEquity > 0.0 && -snapshot.dailyPnl / snapshot.initialEquity > limits.maxDailyLossFraction) reasons += "daily_loss_limit_breached"
        if (snapshot.peakEquity > 0.0 && (snapshot.peakEquity - snapshot.equity) / snapshot.peakEquity > limits.maxDrawdownFraction) reasons += "max_drawdown_breached"
        if (snapshot.initialEquity > 0.0 && proposedLoss / snapshot.initialEquity > limits.maxRiskPerTradeFraction) reasons += "per_trade_risk_limit_breached"
        return AmarRiskDecision(reasons.isEmpty(), reasons.distinct())
    }
}
