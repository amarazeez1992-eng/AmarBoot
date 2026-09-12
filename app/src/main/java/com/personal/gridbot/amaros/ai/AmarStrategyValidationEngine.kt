package com.personal.gridbot.amaros.ai

import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

/** Statistical validation over already-measured trade R results. It never fabricates market trades. */
object AmarStrategyValidationEngine {
    data class Report(
        val sampleSize: Int,
        val winRatePct: Double,
        val profitFactor: Double,
        val expectancyR: Double,
        val payoffRatio: Double,
        val maxDrawdownR: Double,
        val recoveryFactor: Double,
        val sharpeLike: Double,
        val sqn: Double,
        val longestWinStreak: Int,
        val longestLossStreak: Int,
        val profitConcentrationTop20Pct: Double,
        val monteCarloDrawdownP50: Double,
        val monteCarloDrawdownP95: Double,
        val verified: Boolean
    )

    fun analyze(pnlR: List<Double>, simulations: Int = 1000, seed: Int = 20260912): Report {
        val clean = pnlR.filter { it.isFinite() }
        if (clean.isEmpty()) return Report(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0.0, 0.0, 0.0, false)
        require(simulations > 0)
        val wins = clean.filter { it > 0.0 }
        val losses = clean.filter { it < 0.0 }
        val grossWin = wins.sum()
        val grossLoss = -losses.sum()
        val pf = if (grossLoss > 0) grossWin / grossLoss else if (grossWin > 0) Double.POSITIVE_INFINITY else 0.0
        val mean = clean.average()
        val variance = clean.map { (it - mean) * (it - mean) }.average()
        val sd = sqrt(variance)
        val sharpeLike = if (sd > 0) mean / sd * sqrt(clean.size.toDouble()) else 0.0
        val sqn = if (sd > 0) mean / sd * sqrt(clean.size.toDouble()) else 0.0
        val payoff = if (losses.isNotEmpty() && wins.isNotEmpty()) wins.average() / abs(losses.average()) else 0.0
        val equity = equityCurve(clean)
        val maxDd = maxDrawdown(equity)
        val recovery = if (maxDd > 0) clean.sum() / maxDd else if (clean.sum() > 0) Double.POSITIVE_INFINITY else 0.0
        val top20 = wins.sortedDescending().take(maxOf(1, (clean.size * 0.2).toInt())).sum()
        val concentration = if (grossWin > 0) top20 / grossWin * 100.0 else 0.0

        val dds = MutableList(simulations) { 0.0 }
        val rng = Random(seed)
        for (s in 0 until simulations) {
            var eq = 0.0
            var peak = 0.0
            var dd = 0.0
            repeat(clean.size) {
                eq += clean[rng.nextInt(clean.size)]
                peak = maxOf(peak, eq)
                dd = maxOf(dd, peak - eq)
            }
            dds[s] = dd
        }
        dds.sort()
        fun percentile(p: Double): Double = dds[((dds.size - 1) * p).toInt().coerceIn(0, dds.lastIndex)]

        return Report(
            sampleSize = clean.size,
            winRatePct = wins.size * 100.0 / clean.size,
            profitFactor = pf,
            expectancyR = mean,
            payoffRatio = payoff,
            maxDrawdownR = maxDd,
            recoveryFactor = recovery,
            sharpeLike = sharpeLike,
            sqn = sqn,
            longestWinStreak = streak(clean, true),
            longestLossStreak = streak(clean, false),
            profitConcentrationTop20Pct = concentration,
            monteCarloDrawdownP50 = percentile(0.50),
            monteCarloDrawdownP95 = percentile(0.95),
            verified = clean.size >= 2
        )
    }

    private fun equityCurve(values: List<Double>): List<Double> {
        var sum = 0.0
        return values.map { sum += it; sum }
    }

    private fun maxDrawdown(equity: List<Double>): Double {
        var peak = 0.0
        var dd = 0.0
        equity.forEach { value ->
            peak = maxOf(peak, value)
            dd = maxOf(dd, peak - value)
        }
        return dd
    }

    private fun streak(values: List<Double>, win: Boolean): Int {
        var best = 0
        var current = 0
        values.forEach { value ->
            val hit = if (win) value > 0 else value < 0
            current = if (hit) current + 1 else 0
            best = maxOf(best, current)
        }
        return best
    }
}
