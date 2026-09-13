package com.personal.gridbot.amaros.risk.simulator

/**
 * Deterministic what-if calculations over caller-supplied trusted account values.
 * It never reads or changes broker state and never presents scenarios as live results.
 */
object AmarRiskSimulator {
    data class AccountState(
        val equity: Double,
        val margin: Double,
        val exposure: Double,
        val drawdownPct: Double,
    )

    data class Scenario(
        val estimatedLoss: Double = 0.0,
        val additionalMargin: Double = 0.0,
        val additionalExposure: Double = 0.0,
    )

    data class Result(
        val equityAfterLoss: Double,
        val marginAfter: Double,
        val freeMarginAfter: Double,
        val exposureAfter: Double,
        val estimatedEquityImpactPct: Double,
        val isScenarioOnly: Boolean = true,
    )

    fun validate(account: AccountState, scenario: Scenario): List<String> = buildList {
        if (!account.equity.isFinite() || account.equity < 0.0) add("EQUITY_INVALID")
        if (!account.margin.isFinite() || account.margin < 0.0) add("MARGIN_INVALID")
        if (!account.exposure.isFinite() || account.exposure < 0.0) add("EXPOSURE_INVALID")
        if (!account.drawdownPct.isFinite() || account.drawdownPct < 0.0) add("DRAWDOWN_INVALID")
        if (!scenario.estimatedLoss.isFinite() || scenario.estimatedLoss < 0.0) add("LOSS_INVALID")
        if (!scenario.additionalMargin.isFinite() || scenario.additionalMargin < 0.0) add("ADDITIONAL_MARGIN_INVALID")
        if (!scenario.additionalExposure.isFinite() || scenario.additionalExposure < 0.0) add("ADDITIONAL_EXPOSURE_INVALID")
    }

    fun simulate(account: AccountState, scenario: Scenario): Result {
        require(validate(account, scenario).isEmpty())
        val equityAfterLoss = account.equity - scenario.estimatedLoss
        val marginAfter = account.margin + scenario.additionalMargin
        val freeMarginAfter = equityAfterLoss - marginAfter
        val exposureAfter = account.exposure + scenario.additionalExposure
        val impactPct = if (account.equity == 0.0) 0.0 else scenario.estimatedLoss / account.equity * 100.0
        return Result(
            equityAfterLoss = equityAfterLoss,
            marginAfter = marginAfter,
            freeMarginAfter = freeMarginAfter,
            exposureAfter = exposureAfter,
            estimatedEquityImpactPct = impactPct,
        )
    }
}
