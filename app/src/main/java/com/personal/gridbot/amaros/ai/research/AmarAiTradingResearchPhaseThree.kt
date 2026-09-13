package com.personal.gridbot.amaros.ai.research

import kotlin.math.abs

/** Phase 3 research contracts. Read-only research; no broker execution. */

data class AmarOhlcv(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
) {
    init {
        require(timestamp >= 0)
        require(open.isFinite() && high.isFinite() && low.isFinite() && close.isFinite())
        require(volume.isFinite() && volume >= 0.0)
        require(high >= maxOf(open, close, low))
        require(low <= minOf(open, close, high))
    }
}

object AmarOhlcvNormalizer {
    fun normalize(rows: List<AmarOhlcv>): List<AmarOhlcv> = rows
        .sortedBy { it.timestamp }
        .distinctBy { it.timestamp }
}

data class AmarIndicatorValue(val name: String, val timestamp: Long, val value: Double)

interface AmarTechnicalIndicator {
    val name: String
    fun calculate(data: List<AmarOhlcv>): List<AmarIndicatorValue>
}

data class AmarStrategyCondition(val expression: String) {
    init { require(expression.isNotBlank()) }
}

data class AmarStrategyDefinition(
    val id: String,
    val name: String,
    val entry: List<AmarStrategyCondition>,
    val exit: List<AmarStrategyCondition>,
    val riskRules: List<AmarStrategyCondition> = emptyList()
) {
    init {
        require(id.isNotBlank() && name.isNotBlank())
        require(entry.isNotEmpty())
        require(exit.isNotEmpty())
    }
}

data class AmarBacktestTrade(
    val entryTime: Long,
    val exitTime: Long,
    val side: Int,
    val entryPrice: Double,
    val exitPrice: Double,
    val netPnl: Double
) {
    init {
        require(side == 1 || side == -1)
        require(entryPrice.isFinite() && exitPrice.isFinite() && netPnl.isFinite())
    }
}

data class AmarBacktestResult(
    val trades: List<AmarBacktestTrade>,
    val netPnl: Double,
    val maxDrawdown: Double,
    val winRate: Double,
    val profitFactor: Double
) {
    init {
        require(netPnl.isFinite() && maxDrawdown >= 0.0 && maxDrawdown.isFinite())
        require(winRate in 0.0..1.0)
        require(profitFactor.isFinite() && profitFactor >= 0.0)
    }
}

interface AmarBacktestEngine {
    fun run(data: List<AmarOhlcv>, strategy: AmarStrategyDefinition): AmarBacktestResult
}

data class AmarTradingCostModel(
    val spread: Double,
    val slippage: Double,
    val commission: Double,
    val latencyMs: Long
) {
    init {
        require(spread >= 0.0 && slippage >= 0.0 && commission >= 0.0)
        require(latencyMs >= 0)
    }
}

data class AmarRobustnessScore(
    val baseScore: Double,
    val walkForwardScore: Double,
    val monteCarloScore: Double,
    val stressScore: Double
) {
    init {
        listOf(baseScore, walkForwardScore, monteCarloScore, stressScore).forEach {
            require(it.isFinite() && it in 0.0..1.0)
        }
    }

    val composite: Double
        get() = (baseScore + walkForwardScore + monteCarloScore + stressScore) / 4.0
}

enum class AmarMarketRegime { TRENDING, RANGING, VOLATILE, QUIET, UNKNOWN }

data class AmarRegimeSnapshot(
    val timestamp: Long,
    val regime: AmarMarketRegime,
    val confidence: Double,
    val driftScore: Double
) {
    init {
        require(timestamp >= 0)
        require(confidence in 0.0..1.0)
        require(driftScore.isFinite() && driftScore >= 0.0)
    }
}

data class AmarStrategyCandidate(val id: String, val score: AmarRobustnessScore)

class AmarChampionChallengerSelector {
    fun select(candidates: List<AmarStrategyCandidate>): AmarStrategyCandidate? =
        candidates.maxByOrNull { it.score.composite }

    fun separation(champion: AmarStrategyCandidate, challenger: AmarStrategyCandidate): Double =
        abs(champion.score.composite - challenger.score.composite)
}
