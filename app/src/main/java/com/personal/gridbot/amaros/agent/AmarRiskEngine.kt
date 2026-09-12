package com.personal.gridbot.amaros.agent

/** Risk is deterministic and independent from language-model output. */
class AmarRiskEngine {
    fun evaluate(request: RiskRequest): RiskDecision {
        if (request.equity <= 0.0) return RiskDecision(false, "Invalid equity")
        if (request.riskPercent <= 0.0 || request.riskPercent > request.maxRiskPercent) {
            return RiskDecision(false, "Risk limit exceeded")
        }
        if (request.expectedLoss <= 0.0) return RiskDecision(false, "Expected loss is missing")
        val allowedLoss = request.equity * request.riskPercent / 100.0
        if (request.expectedLoss > allowedLoss) return RiskDecision(false, "Expected loss exceeds risk budget")
        return RiskDecision(true, "Within configured risk budget")
    }
}

data class RiskRequest(
    val equity: Double,
    val riskPercent: Double,
    val expectedLoss: Double,
    val maxRiskPercent: Double = 1.0
)

data class RiskDecision(val allowed: Boolean, val reason: String)
