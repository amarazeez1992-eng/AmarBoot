package com.personal.gridbot.amaros.agent

/** Validation gates are evidence requirements, not promises of profitability. */
class AmarValidationEngine {
    fun validate(
        result: AmarBacktestResult,
        outOfSample: Boolean,
        costStressPassed: Boolean,
        robustnessScore: Double
    ): AmarValidationResult {
        val checks = listOf(
            result.trades >= 100,
            outOfSample,
            result.profitFactor >= 1.20,
            result.maxDrawdownPercent <= 20.0,
            result.expectancy > 0.0,
            costStressPassed,
            robustnessScore >= 0.70
        )
        return AmarValidationResult(checks.all { it }, checks.count { it }, checks.size)
    }
}

data class AmarValidationResult(val passed: Boolean, val passedChecks: Int, val totalChecks: Int)
