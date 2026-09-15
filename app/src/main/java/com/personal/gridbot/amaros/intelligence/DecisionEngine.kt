package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataSnapshot
import com.personal.gridbot.amaros.trading.quant.AmarQuantTradingMath

/**
 * B7 decision layer. It produces an explainable strategy proposal only.
 * It has no broker dependency and cannot execute trades.
 *
 * Quantitative risk is optional and evidence-driven. A statistic is ignored when
 * its input is invalid or too small to provide a minimally useful sample.
 */
class DecisionEngine {
    enum class Direction { LONG_BIAS, SHORT_BIAS, NEUTRAL }

    data class QuantitativeInput(
        val closes: List<Double> = emptyList(),
        val equityCurve: List<Double> = emptyList(),
        val losses: List<Double> = emptyList(),
        val varConfidence: Double = 0.95,
        val varScale: Double? = null,
        val winProbability: Double? = null,
        val payoffRatio: Double? = null,
        val riskFraction: Double? = null,
        val ruinFraction: Double? = null
    )

    data class QuantitativeRisk(
        val realizedVolatility: Double?,
        val maxDrawdown: Double?,
        val normalizedHistoricalVar: Double?,
        val riskOfRuin: Double?,
        val score: Double
    )

    data class DecisionProposal(
        val direction: Direction,
        val score: Double,
        val confidence: Double,
        val rationale: String,
        val executable: Boolean = false,
        val quantitativeRisk: QuantitativeRisk? = null
    )

    fun evaluate(data: AmarDataSnapshot, context: MarketContext): DecisionProposal =
        evaluate(data, context, QuantitativeInput())

    fun evaluate(
        data: AmarDataSnapshot,
        context: MarketContext,
        quantitativeInput: QuantitativeInput
    ): DecisionProposal {
        val baseScore = context.directionalScore.coerceIn(-1.0, 1.0)
        val risk = assessQuantitativeRisk(quantitativeInput)
        val riskPenalty = risk?.score?.coerceIn(0.0, 1.0) ?: 0.0
        val score = (baseScore * (1.0 - 0.50 * riskPenalty)).coerceIn(-1.0, 1.0)
        val direction = when {
            score > 0.20 -> Direction.LONG_BIAS
            score < -0.20 -> Direction.SHORT_BIAS
            else -> Direction.NEUTRAL
        }
        val confidence = (context.confidence.coerceIn(0.0, 1.0) * (1.0 - 0.60 * riskPenalty))
            .coerceIn(0.0, 1.0)
        val riskText = risk?.let { " • quantitative-risk=${"%.4f".format(java.util.Locale.US, it.score)}" } ?: ""
        val rationale = "${context.regime} • ${context.explanation} • ${data.market.symbol}/${data.market.timeframe}$riskText"
        return DecisionProposal(
            direction = direction,
            score = score,
            confidence = confidence,
            rationale = rationale,
            executable = false,
            quantitativeRisk = risk
        )
    }

    fun assessQuantitativeRisk(input: QuantitativeInput): QuantitativeRisk? {
        val volatility = if (input.closes.size >= MIN_CLOSES_FOR_VOLATILITY && input.closes.all { it.isFinite() && it > 0.0 }) {
            AmarQuantTradingMath.realizedVolatility(input.closes)
        } else null

        val drawdown = if (input.equityCurve.isNotEmpty() && input.equityCurve.all { it.isFinite() && it >= 0.0 }) {
            AmarQuantTradingMath.maxDrawdown(input.equityCurve)
        } else null

        val rawVar = if (
            input.losses.size >= MIN_LOSSES_FOR_HISTORICAL_VAR &&
            input.losses.all { it.isFinite() && it >= 0.0 } &&
            input.varConfidence.isFinite() && input.varConfidence in 0.0..1.0
        ) AmarQuantTradingMath.historicalVar(input.losses, input.varConfidence) else null

        val normalizedVar = if (
            rawVar != null && input.varScale != null &&
            input.varScale.isFinite() && input.varScale > 0.0
        ) (rawVar / input.varScale).coerceIn(0.0, 1.0) else null

        val ruin = if (
            input.winProbability != null && input.payoffRatio != null &&
            input.riskFraction != null && input.ruinFraction != null &&
            input.winProbability.isFinite() && input.winProbability > 0.0 && input.winProbability < 1.0 &&
            input.payoffRatio.isFinite() && input.payoffRatio > 0.0 &&
            input.riskFraction.isFinite() && input.riskFraction > 0.0 && input.riskFraction < 1.0 &&
            input.ruinFraction.isFinite() && input.ruinFraction > 0.0 && input.ruinFraction < 1.0
        ) AmarQuantTradingMath.riskOfRuin(
            input.winProbability,
            input.payoffRatio,
            input.riskFraction,
            input.ruinFraction
        ) else null

        val weighted = listOf(
            volatility?.coerceIn(0.0, 1.0) to 0.30,
            drawdown?.coerceIn(0.0, 1.0) to 0.25,
            normalizedVar to 0.20,
            ruin to 0.25
        ).filter { it.first != null }

        if (weighted.isEmpty()) return null
        val weight = weighted.sumOf { it.second }
        val score = weighted.sumOf { (it.first ?: 0.0) * it.second } / weight
        return QuantitativeRisk(volatility, drawdown, normalizedVar, ruin, score.coerceIn(0.0, 1.0))
    }

    private companion object {
        private const val MIN_CLOSES_FOR_VOLATILITY = 4
        private const val MIN_LOSSES_FOR_HISTORICAL_VAR = 5
    }
}
