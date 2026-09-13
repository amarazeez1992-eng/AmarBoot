package com.personal.gridbot.amaros.trading.risk

/** App-side risk policy. Null limits mean the corresponding control is not configured. */
data class AmarRiskPolicy(
    val dailyLossLimit: Double? = null,
    val dailyProfitTarget: Double? = null,
    val maximumExposure: Double? = null,
    val maximumOpenPositions: Int? = null,
    val maximumLot: Double? = null,
    val maximumDrawdown: Double? = null,
    val emergencyStop: Boolean = false
)

data class AmarRiskSnapshot(
    val dailyPnl: Double? = null,
    val exposure: Double? = null,
    val openPositions: Int? = null,
    val totalLot: Double? = null,
    val drawdown: Double? = null,
    val dataTrusted: Boolean = false
)

object AmarRiskPolicyEvaluator {
    fun evaluate(policy: AmarRiskPolicy, snapshot: AmarRiskSnapshot): List<String> {
        if (!snapshot.dataTrusted) return listOf("RISK_DATA_UNAVAILABLE")
        if (policy.emergencyStop) return listOf("EMERGENCY_STOP")
        return buildList {
            if (policy.dailyLossLimit != null && snapshot.dailyPnl != null && snapshot.dailyPnl <= -policy.dailyLossLimit) add("DAILY_LOSS_LIMIT")
            if (policy.dailyProfitTarget != null && snapshot.dailyPnl != null && snapshot.dailyPnl >= policy.dailyProfitTarget) add("DAILY_PROFIT_TARGET")
            if (policy.maximumExposure != null && snapshot.exposure != null && snapshot.exposure >= policy.maximumExposure) add("MAX_EXPOSURE")
            if (policy.maximumOpenPositions != null && snapshot.openPositions != null && snapshot.openPositions >= policy.maximumOpenPositions) add("MAX_OPEN_POSITIONS")
            if (policy.maximumLot != null && snapshot.totalLot != null && snapshot.totalLot >= policy.maximumLot) add("MAX_LOT")
            if (policy.maximumDrawdown != null && snapshot.drawdown != null && snapshot.drawdown >= policy.maximumDrawdown) add("MAX_DRAWDOWN")
        }
    }
}
