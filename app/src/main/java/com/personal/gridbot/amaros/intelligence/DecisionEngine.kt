package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot

/**
 * B7 decision layer. It produces an explainable strategy proposal only.
 * It has no broker dependency and cannot execute trades.
 */
class DecisionEngine {
    enum class Direction { LONG_BIAS, SHORT_BIAS, NEUTRAL }

    data class DecisionProposal(
        val direction: Direction,
        val score: Double,
        val confidence: Double,
        val rationale: String,
        val executable: Boolean = false
    )

    fun evaluate(data: AmarDataSnapshot, context: MarketContext): DecisionProposal {
        val score = context.directionalScore.coerceIn(-1.0, 1.0)
        val direction = when {
            score > 0.20 -> Direction.LONG_BIAS
            score < -0.20 -> Direction.SHORT_BIAS
            else -> Direction.NEUTRAL
        }
        val confidence = context.confidence.coerceIn(0.0, 1.0)
        val rationale = "${context.regime} • ${context.explanation} • ${data.market.symbol}/${data.market.timeframe}"
        return DecisionProposal(direction, score, confidence, rationale, executable = false)
    }
}
