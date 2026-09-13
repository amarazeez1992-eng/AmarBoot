package com.personal.gridbot.amaros.strategy.lab

/**
 * Deterministic strategy-research boundary. It validates definitions and compares
 * measured results; it never fabricates backtest or broker performance.
 *
 * Comparison is deliberately not an optimization score: it orders measured results
 * by net P/L, then lower maximum drawdown, then higher win rate. Profit factor is
 * validated and retained as a measured metric but is not silently weighted.
 */
object AmarStrategyLabPolicy {
    data class StrategyDefinition(
        val id: String,
        val name: String,
        val version: Int,
        val rules: List<String>,
    )

    data class MeasuredResult(
        val trades: Int,
        val netProfitLoss: Double,
        val winRate: Double,
        val maxDrawdownPct: Double,
        val profitFactor: Double,
    )

    fun validate(definition: StrategyDefinition): List<String> = buildList {
        if (definition.id.isBlank()) add("STRATEGY_ID_REQUIRED")
        if (definition.name.isBlank()) add("STRATEGY_NAME_REQUIRED")
        if (definition.version <= 0) add("STRATEGY_VERSION_INVALID")
        if (definition.rules.isEmpty()) add("STRATEGY_RULES_REQUIRED")
        if (definition.rules.any { it.isBlank() }) add("STRATEGY_RULE_INVALID")
    }

    fun validateResult(result: MeasuredResult): List<String> = buildList {
        if (result.trades < 0) add("RESULT_TRADES_INVALID")
        if (!result.netProfitLoss.isFinite()) add("RESULT_PNL_INVALID")
        if (!result.winRate.isFinite() || result.winRate !in 0.0..1.0) add("RESULT_WIN_RATE_INVALID")
        if (!result.maxDrawdownPct.isFinite() || result.maxDrawdownPct < 0.0) add("RESULT_DRAWDOWN_INVALID")
        if (result.profitFactor.isNaN() || result.profitFactor < 0.0) add("RESULT_PROFIT_FACTOR_INVALID")
    }

    fun compare(first: MeasuredResult, second: MeasuredResult): Int {
        require(validateResult(first).isEmpty())
        require(validateResult(second).isEmpty())
        return when {
            first.netProfitLoss > second.netProfitLoss -> 1
            first.netProfitLoss < second.netProfitLoss -> -1
            first.maxDrawdownPct < second.maxDrawdownPct -> 1
            first.maxDrawdownPct > second.maxDrawdownPct -> -1
            first.winRate > second.winRate -> 1
            first.winRate < second.winRate -> -1
            else -> 0
        }
    }
}
