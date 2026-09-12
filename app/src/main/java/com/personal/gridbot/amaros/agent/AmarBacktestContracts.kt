package com.personal.gridbot.amaros.agent

/** Deterministic simulation boundary. No broker or live account access. */
interface AmarBacktestEngine {
    fun run(strategy: AmarStrategySpec, data: List<AmarCandle>, costs: AmarTradingCosts): AmarBacktestResult
}

data class AmarCandle(
    val timestampEpochMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
) {
    init { require(high >= maxOf(open, close)); require(low <= minOf(open, close)) }
}

data class AmarTradingCosts(
    val spreadPoints: Double,
    val commissionPerUnit: Double,
    val slippagePoints: Double,
    val latencyMs: Long = 0L
) {
    init { require(spreadPoints >= 0); require(commissionPerUnit >= 0); require(slippagePoints >= 0); require(latencyMs >= 0) }
}

data class AmarBacktestResult(
    val trades: Int,
    val netProfit: Double,
    val profitFactor: Double,
    val maxDrawdownPercent: Double,
    val expectancy: Double,
    val totalCosts: Double
)
