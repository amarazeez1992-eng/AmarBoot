package com.personal.gridbot.amaros.trading.quant

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Deterministic, execution-free quantitative primitives for the trading layer.
 *
 * All methods validate finite inputs and fail closed. Statistical methods make
 * their assumptions explicit so their outputs cannot be mistaken for broker or
 * execution guarantees.
 */
object AmarQuantTradingMathV2 {
    fun trueRange(high: Double, low: Double, previousClose: Double): Double? {
        if (!high.isFinite() || !low.isFinite() || !previousClose.isFinite() || high < low) return null
        return max(high - low, max(abs(high - previousClose), abs(low - previousClose)))
    }

    /**
     * Wilder ATR (RMA) over an ordered true-range series.
     * The first ATR is the simple mean of the first [period] TR values, then
     * Wilder smoothing is applied through the remaining observations.
     */
    fun atr(trueRanges: List<Double>, period: Int): Double? {
        if (period <= 0 || trueRanges.size < period) return null
        if (trueRanges.any { !it.isFinite() || it < 0.0 }) return null

        var value = trueRanges.take(period).average()
        for (index in period until trueRanges.size) {
            value = ((value * (period - 1)) + trueRanges[index]) / period
        }
        return value
    }

    fun realizedVolatility(closes: List<Double>, annualization: Double = 1.0): Double? {
        if (closes.size < 2 || annualization <= 0.0 || !annualization.isFinite()) return null
        if (closes.any { !it.isFinite() || it <= 0.0 }) return null
        val returns = closes.zipWithNext { a, b -> ln(b / a) }
        val mean = returns.average()
        val variance = returns.sumOf { (it - mean) * (it - mean) } / returns.size
        return sqrt(variance) * sqrt(annualization)
    }

    fun maxDrawdown(equityCurve: List<Double>): Double? {
        if (equityCurve.isEmpty() || equityCurve.any { !it.isFinite() || it < 0.0 }) return null
        var peak = equityCurve.first()
        var maxDrawdown = 0.0
        for (equity in equityCurve) {
            peak = max(peak, equity)
            if (peak > 0.0) maxDrawdown = max(maxDrawdown, (peak - equity) / peak)
        }
        return maxDrawdown
    }

    /**
     * Historical VaR as an empirical loss quantile.
     *
     * [losses] are non-negative loss magnitudes (0 = no loss). [confidence]
     * is the non-exceedance probability, e.g. 0.95. Linear interpolation is
     * used between adjacent order statistics instead of truncating the index.
     */
    fun historicalVar(losses: List<Double>, confidence: Double): Double? {
        if (losses.isEmpty() || losses.any { !it.isFinite() || it < 0.0 }) return null
        if (!confidence.isFinite() || confidence !in 0.0..1.0) return null

        val sorted = losses.sorted()
        if (sorted.size == 1) return sorted.first()

        val position = confidence * (sorted.lastIndex.toDouble())
        val lowerIndex = floor(position).toInt()
        val upperIndex = ceil(position).toInt().coerceAtMost(sorted.lastIndex)
        if (lowerIndex == upperIndex) return sorted[lowerIndex]

        val weight = position - lowerIndex
        return sorted[lowerIndex] + weight * (sorted[upperIndex] - sorted[lowerIndex])
    }

    /** Kelly fraction from win probability and win/loss payoff ratio. */
    fun kellyFraction(winProbability: Double, payoffRatio: Double): Double? {
        if (!winProbability.isFinite() || !payoffRatio.isFinite()) return null
        if (winProbability !in 0.0..1.0 || payoffRatio <= 0.0) return null
        val q = 1.0 - winProbability
        return winProbability - q / payoffRatio
    }

    /** Position sizing from fixed fractional risk. */
    fun riskPositionSize(
        equity: Double,
        riskFraction: Double,
        stopDistance: Double,
        valuePerUnit: Double
    ): Double? {
        if (!equity.isFinite() || !riskFraction.isFinite() || !stopDistance.isFinite() || !valuePerUnit.isFinite()) return null
        if (equity <= 0.0 || riskFraction <= 0.0 || stopDistance <= 0.0 || valuePerUnit <= 0.0) return null
        return equity * riskFraction / (stopDistance * valuePerUnit)
    }

    /**
     * Approximate probability of reaching a specified equity drawdown barrier
     * under an IID fixed-fraction binary-outcome model.
     *
     * This is a first-passage diffusion approximation, not an exact universal
     * "risk of ruin" theorem. It assumes fixed fractional sizing, independent
     * outcomes, constant win probability/payoff, and no costs or regime shift.
     * A non-positive expected log-growth implies eventual barrier probability 1.
     */
    fun riskOfRuin(
        winProbability: Double,
        payoffRatio: Double,
        riskFraction: Double,
        ruinFraction: Double
    ): Double? {
        if (!winProbability.isFinite() || !payoffRatio.isFinite() || !riskFraction.isFinite() || !ruinFraction.isFinite()) return null
        if (winProbability <= 0.0 || winProbability >= 1.0) return null
        if (payoffRatio <= 0.0 || riskFraction <= 0.0 || riskFraction >= 1.0) return null
        if (ruinFraction <= 0.0 || ruinFraction >= 1.0) return null

        val winLogReturn = ln(1.0 + riskFraction * payoffRatio)
        val lossLogReturn = ln(1.0 - riskFraction)
        val meanLogReturn = winProbability * winLogReturn + (1.0 - winProbability) * lossLogReturn
        val variance =
            winProbability * (winLogReturn - meanLogReturn) * (winLogReturn - meanLogReturn) +
                (1.0 - winProbability) * (lossLogReturn - meanLogReturn) * (lossLogReturn - meanLogReturn)

        if (variance <= 0.0 || !variance.isFinite()) {
            return if (meanLogReturn <= 0.0) 1.0 else 0.0
        }
        if (meanLogReturn <= 0.0) return 1.0

        val barrier = -ln(1.0 - ruinFraction)
        return (-2.0 * meanLogReturn * barrier / variance).let { kotlin.math.exp(it) }.coerceIn(0.0, 1.0)
    }
}
