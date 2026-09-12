package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.max

/** Trade-level stress simulation using measured R outcomes; deterministic and reproducible. */
object AmarTradeLevelStressEngine {
    enum class Scenario { SPREAD, SLIPPAGE, EXECUTION_DELAY, ADVERSE_SELECTION, REGIME_SHOCK }

    data class Result(
        val scenario: Scenario,
        val baseNetR: Double,
        val stressedNetR: Double,
        val degradationR: Double,
        val stressedExpectancyR: Double,
        val profitable: Boolean
    )

    fun evaluate(tradesR: List<Double>, scenario: Scenario, magnitude: Double): Result {
        require(tradesR.isNotEmpty()) { "Measured trade outcomes are required" }
        require(magnitude >= 0.0) { "Stress magnitude cannot be negative" }
        val m = magnitude.coerceAtMost(1.0)
        val stressed = tradesR.map { r ->
            val penalty = when (scenario) {
                Scenario.SPREAD -> m * 0.08
                Scenario.SLIPPAGE -> m * 0.12
                Scenario.EXECUTION_DELAY -> m * 0.10
                Scenario.ADVERSE_SELECTION -> if (r > 0.0) m * 0.18 else m * 0.04
                Scenario.REGIME_SHOCK -> if (r > 0.0) -m * 0.25 else r * m * 0.10
            }
            when (scenario) {
                Scenario.REGIME_SHOCK -> r + penalty
                else -> r - penalty
            }
        }
        val base = tradesR.sum()
        val stressedNet = stressed.sum()
        val expectancy = stressedNet / max(1, stressed.size)
        return Result(scenario, base, stressedNet, base - stressedNet, expectancy, stressedNet > 0.0)
    }
}
