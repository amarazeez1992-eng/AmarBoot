package com.personal.gridbot.amaros.agent

/** Prevents a strategy from being considered strong from one metric alone. */
class AmarEvaluationEngine {
    fun evaluate(m: StrategyMetrics): EvaluationResult {
        val checks = listOf(
            "sample" to (m.trades >= 100),
            "outOfSample" to m.outOfSample,
            "profitFactor" to (m.profitFactor >= 1.20),
            "drawdown" to (m.maxDrawdownPercent <= 20.0),
            "expectancy" to (m.expectancy > 0.0),
            "costStress" to m.costStressPassed
        )
        val passed = checks.count { it.second }
        return EvaluationResult(passed, checks.size, passed == checks.size, checks.toMap())
    }
}

data class StrategyMetrics(
    val trades: Int,
    val profitFactor: Double,
    val expectancy: Double,
    val maxDrawdownPercent: Double,
    val outOfSample: Boolean,
    val costStressPassed: Boolean
)

data class EvaluationResult(
    val passedChecks: Int,
    val totalChecks: Int,
    val approvedForResearch: Boolean,
    val checks: Map<String, Boolean>
)
