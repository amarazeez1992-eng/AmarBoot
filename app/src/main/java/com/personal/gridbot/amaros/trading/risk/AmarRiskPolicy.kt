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
        val policyErrors = buildList {
            if (policy.dailyLossLimit != null && (!policy.dailyLossLimit.isFinite() || policy.dailyLossLimit <= 0.0)) add("DAILY_LOSS_POLICY_INVALID")
            if (policy.dailyProfitTarget != null && (!policy.dailyProfitTarget.isFinite() || policy.dailyProfitTarget <= 0.0)) add("DAILY_PROFIT_POLICY_INVALID")
            if (policy.maximumExposure != null && (!policy.maximumExposure.isFinite() || policy.maximumExposure <= 0.0)) add("MAX_EXPOSURE_POLICY_INVALID")
            if (policy.maximumOpenPositions != null && policy.maximumOpenPositions <= 0) add("MAX_OPEN_POSITIONS_POLICY_INVALID")
            if (policy.maximumLot != null && (!policy.maximumLot.isFinite() || policy.maximumLot <= 0.0)) add("MAX_LOT_POLICY_INVALID")
            if (policy.maximumDrawdown != null && (!policy.maximumDrawdown.isFinite() || policy.maximumDrawdown <= 0.0)) add("MAX_DRAWDOWN_POLICY_INVALID")
        }
        if (policyErrors.isNotEmpty()) return policyErrors
        if (!snapshot.dataTrusted) return listOf("RISK_DATA_UNAVAILABLE")
        if (policy.emergencyStop) return listOf("EMERGENCY_STOP")

        val required = buildList {
            if (policy.dailyLossLimit != null) add(snapshot.dailyPnl?.takeIf { it.isFinite() } != null)
            if (policy.dailyProfitTarget != null) add(snapshot.dailyPnl?.takeIf { it.isFinite() } != null)
            if (policy.maximumExposure != null) add(snapshot.exposure?.takeIf { it.isFinite() && it >= 0.0 } != null)
            if (policy.maximumOpenPositions != null) add(snapshot.openPositions?.takeIf { it >= 0 } != null)
            if (policy.maximumLot != null) add(snapshot.totalLot?.takeIf { it.isFinite() && it >= 0.0 } != null)
            if (policy.maximumDrawdown != null) add(snapshot.drawdown?.takeIf { it.isFinite() && it >= 0.0 } != null)
        }
        if (required.any { !it }) return listOf("RISK_EVIDENCE_INCOMPLETE")

        return buildList {
            if (policy.dailyLossLimit != null && snapshot.dailyPnl!! <= -policy.dailyLossLimit) add("DAILY_LOSS_LIMIT")
            if (policy.dailyProfitTarget != null && snapshot.dailyPnl!! >= policy.dailyProfitTarget) add("DAILY_PROFIT_TARGET")
            if (policy.maximumExposure != null && snapshot.exposure!! >= policy.maximumExposure) add("MAX_EXPOSURE")
            if (policy.maximumOpenPositions != null && snapshot.openPositions!! >= policy.maximumOpenPositions) add("MAX_OPEN_POSITIONS")
            if (policy.maximumLot != null && snapshot.totalLot!! >= policy.maximumLot) add("MAX_LOT")
            if (policy.maximumDrawdown != null && snapshot.drawdown!! >= policy.maximumDrawdown) add("MAX_DRAWDOWN")
        }
    }
}
