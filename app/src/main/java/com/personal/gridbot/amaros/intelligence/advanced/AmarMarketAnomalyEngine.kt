package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs

/** Deterministic price/spread anomaly detector used as a risk/research gate, never as a trade signal. */
object AmarMarketAnomalyEngine {
    data class Report(
        val abnormalityScore: Double,
        val extremeMove: Boolean,
        val spreadAnomaly: Boolean,
        val regimeBreak: Boolean,
        val reasons: List<String>
    )

    fun evaluate(candles: List<AmarOhlc>, spreadMultiplier: Double = 2.5): Report {
        if (candles.size < 12) return Report(0.0, false, false, false, listOf("insufficient candles"))
        val ranges = candles.map { (it.high - it.low).coerceAtLeast(0.0) }
        val recent = candles.takeLast(3)
        val baseline = ranges.dropLast(3).average().coerceAtLeast(1e-9)
        val recentMax = recent.maxOf { it.high - it.low }
        val extreme = recentMax / baseline >= 3.0
        val spreads = candles.map { it.spread }.filter { it > 0.0 }
        val spreadBase = if (spreads.isEmpty()) 0.0 else spreads.dropLast(minOf(3, spreads.size)).average()
        val recentSpread = recent.map { it.spread }.filter { it > 0.0 }.maxOrNull() ?: 0.0
        val spreadAnomaly = spreadBase > 0.0 && recentSpread >= spreadBase * spreadMultiplier
        val previousHigh = candles.dropLast(3).takeLast(8).maxOf { it.high }
        val previousLow = candles.dropLast(3).takeLast(8).minOf { it.low }
        val recentHigh = recent.maxOf { it.high }
        val recentLow = recent.minOf { it.low }
        val regimeBreak = recentHigh > previousHigh || recentLow < previousLow
        val score = (if (extreme) 0.45 else 0.0) + (if (spreadAnomaly) 0.35 else 0.0) + (if (regimeBreak) 0.20 else 0.0)
        val reasons = buildList {
            if (extreme) add("range expansion is extreme")
            if (spreadAnomaly) add("spread is anomalously wide")
            if (regimeBreak) add("recent price broke the prior local range")
            if (isAbruptMove(candles)) add("abrupt directional displacement detected")
        }
        return Report(score.coerceIn(0.0, 1.0), extreme, spreadAnomaly, regimeBreak, reasons)
    }

    private fun isAbruptMove(candles: List<AmarOhlc>): Boolean {
        val returns = candles.takeLast(6).map { abs(it.close - it.open) }
        val base = candles.dropLast(6).takeLast(20).map { abs(it.close - it.open) }.average().coerceAtLeast(1e-9)
        return returns.maxOrNull()?.let { it / base >= 3.5 } == true
    }
}
