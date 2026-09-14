package com.personal.gridbot.amaros.trading.quant

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Deterministic, execution-free quantitative primitives for the trading layer.
 * All functions are fail-closed: invalid/non-finite inputs return null or a
 * conservative bounded result rather than manufacturing a trading signal.
 */
object AmarQuantTradingMath {
    fun trueRange(high: Double, low: Double, previousClose: Double): Double? {
        if (!high.isFinite() || !low.isFinite() || !previousClose.isFinite() || high < low) return null
        return max(high - low, max(abs(high - previousClose), abs(low - previousClose)))
    }

    fun atr(trueRanges: List<Double>, period: Int): Double? {
        if (period <= 0 || trueRanges.size < period || trueRanges.any { !it.isFinite() || it < 0.0 }) return null
        return trueRanges.takeLast(period).average()
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

    /** Historical Value-at-Risk at the supplied percentile (e.g. 0.95). */
    fun historicalVar(losses: List<Double>, confidence: Double): Double? {
        if (losses.isEmpty() || losses.any { !it.isFinite() } || confidence !in 0.0..1.0) return null
        val sorted = losses.sorted()
        val index = min(sorted.lastIndex, max(0, ((sorted.size - 1) * confidence).toInt()))
        return sorted[index]
    }

    /** Kelly fraction from win probability and win/loss payoff ratio. */
    fun kellyFraction(winProbability: Double, payoffRatio: Double): Double? {
        if (!winProbability.isFinite() || !payoffRatio.isFinite() || winProbability !in 0.0..1.0 || payoffRatio <= 0.0) return null
        val q = 1.0 - winProbability
        return winProbability - q / payoffRatio
    }

    /** Position sizing from fixed fractional risk. */
    fun riskPositionSize(equity: Double, riskFraction: Double, stopDistance: Double, valuePerUnit: Double): Double? {
        if (!equity.isFinite() || !riskFraction.isFinite() || !stopDistance.isFinite() || !valuePerUnit.isFinite()) return null
        if (equity <= 0.0 || riskFraction <= 0.0 || stopDistance <= 0.0 || valuePerUnit <= 0.0) return null
        return equity * riskFraction / (stopDistance * valuePerUnit)
    }

    /** Bounded risk-of-ruin proxy from win probability and payoff ratio. */
    fun riskOfRuin(winProbability: Double, payoffRatio: Double, riskFraction: Double): Double? {
        if (!winProbability.isFinite() || !payoffRatio.isFinite() || !riskFraction.isFinite()) return null
        if (winProbability <= 0.0 || winProbability >= 1.0 || payoffRatio <= 0.0 || riskFraction <= 0.0 || riskFraction >= 1.0) return null
        val lossProbability = 1.0 - winProbability
        val edgeRatio = lossProbability / (winProbability * payoffRatio)
        if (edgeRatio >= 1.0) return 1.0
        return edgeRatio.pow(1.0 / riskFraction).coerceIn(0.0, 1.0)
    }

    private fun Double.pow(exponent: Double): Double = kotlin.math.exp(exponent * ln(this))
}
