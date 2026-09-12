package com.personal.gridbot.amaros.intelligence.trading

import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.max

/** Deterministic research primitives. No broker execution and no fabricated market results. */
object AmarQuantResearchPlatform {
    data class Bar(val timestampMs: Long, val open: Double, val high: Double, val low: Double, val close: Double)
    data class LeakageReport(val valid: Boolean, val reasons: List<String>)
    data class DatasetFingerprint(val algorithm: String, val value: String, val sampleSize: Int, val firstTimestamp: Long?, val lastTimestamp: Long?)
    data class Window(val train: IntRange, val validation: IntRange, val oos: IntRange)
    data class WalkForwardResult(val windows: List<Window>, val trainScores: List<Double>, val validationScores: List<Double>, val oosScores: List<Double>, val leakageFree: Boolean)
    data class StressResult(val scenario: String, val trades: Int, val netR: Double, val maxDrawdownR: Double, val passed: Boolean)
    data class Mutation(val id: String, val parentVersion: String, val rule: String, val complexityDelta: Int)
    data class MutationResult(val mutation: Mutation, val acceptedAsChallenger: Boolean, val reason: String)
    data class DriftReport(val drifted: Boolean, val baseline: Double, val current: Double, val delta: Double, val threshold: Double)

    fun validateNoLeakage(bars: List<Bar>, decisionCutoffMs: Long? = null): LeakageReport {
        val reasons = mutableListOf<String>()
        if (bars.isEmpty()) reasons += "EMPTY_DATASET"
        if (bars.any { listOf(it.open, it.high, it.low, it.close).any { v -> !v.isFinite() } }) reasons += "NON_FINITE_OHLC"
        if (bars.zipWithNext().any { it.first.timestampMs >= it.second.timestampMs }) reasons += "TIMESTAMPS_NOT_STRICTLY_INCREASING"
        if (bars.any { it.high < max(it.open, it.close) || it.low > minOf(it.open, it.close) }) reasons += "INVALID_OHLC_RANGE"
        if (decisionCutoffMs != null && bars.any { it.timestampMs > decisionCutoffMs }) reasons += "FUTURE_DATA_AFTER_CUTOFF"
        return LeakageReport(reasons.isEmpty(), reasons)
    }

    fun fingerprint(bars: List<Bar>): DatasetFingerprint {
        val canonical = bars.joinToString("\n") { "${it.timestampMs}|${it.open}|${it.high}|${it.low}|${it.close}" }
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray())
        val hex = digest.joinToString("") { "%02x".format(it) }
        return DatasetFingerprint("SHA-256", hex, bars.size, bars.firstOrNull()?.timestampMs, bars.lastOrNull()?.timestampMs)
    }

    fun walkForward(bars: List<Bar>, trainSize: Int, validationSize: Int, oosSize: Int, step: Int = oosSize, scorer: (List<Bar>) -> Double): WalkForwardResult {
        require(trainSize > 0 && validationSize > 0 && oosSize > 0 && step > 0)
        val windows = mutableListOf<Window>()
        val trainScores = mutableListOf<Double>()
        val validationScores = mutableListOf<Double>()
        val oosScores = mutableListOf<Double>()
        var start = 0
        while (start + trainSize + validationSize + oosSize <= bars.size) {
            val tr = start until start + trainSize
            val va = tr.last + 1..tr.last + validationSize
            val oo = va.last + 1..va.last + oosSize
            windows += Window(tr, va, oo)
            trainScores += scorer(bars.slice(tr))
            validationScores += scorer(bars.slice(va))
            oosScores += scorer(bars.slice(oo))
            start += step
        }
        return WalkForwardResult(windows, trainScores, validationScores, oosScores, windows.isNotEmpty())
    }

    fun stressTrades(baseR: List<Double>, spreadR: Double, slippageR: Double, newsMultiplier: Double = 1.0, regimeMultiplier: Double = 1.0, scenario: String = "BASELINE"): StressResult {
        require(spreadR >= 0 && slippageR >= 0 && newsMultiplier >= 0 && regimeMultiplier >= 0)
        val stressed = baseR.map { (it - spreadR - slippageR) * newsMultiplier * regimeMultiplier }
        var equity = 0.0
        var peak = 0.0
        var maxDd = 0.0
        stressed.forEach { r -> equity += r; peak = max(peak, equity); maxDd = max(maxDd, peak - equity) }
        val net = stressed.sum()
        return StressResult(scenario, stressed.size, net, maxDd, stressed.isNotEmpty() && net > 0.0 && maxDd < max(1.0, stressed.size * 0.25))
    }

    fun mutate(parentVersion: String, rules: List<String>): List<Mutation> = rules.filter { it.isNotBlank() }.mapIndexed { i, rule ->
        Mutation("M${i + 1}", parentVersion, rule.trim(), 1)
    }

    fun gateMutation(mutation: Mutation, oosImprovementR: Double, drawdownDeltaR: Double, sampleSize: Int, leakageFree: Boolean, stressPassed: Boolean): MutationResult {
        if (!leakageFree) return MutationResult(mutation, false, "LEAKAGE_GATE_FAILED")
        if (!stressPassed) return MutationResult(mutation, false, "STRESS_GATE_FAILED")
        if (sampleSize < 30) return MutationResult(mutation, false, "INSUFFICIENT_OOS_SAMPLE")
        if (oosImprovementR <= 0.0) return MutationResult(mutation, false, "NO_OOS_IMPROVEMENT")
        if (drawdownDeltaR > 0.0) return MutationResult(mutation, false, "DRAWDOWN_WORSE")
        return MutationResult(mutation, true, "CHALLENGER_ONLY_HUMAN_APPROVAL_REQUIRED")
    }

    fun drift(baseline: Double, current: Double, threshold: Double = 0.15): DriftReport {
        require(threshold >= 0)
        val delta = abs(current - baseline)
        return DriftReport(delta > threshold, baseline, current, delta, threshold)
    }

    fun strategyDna(rules: List<String>): String {
        val canonical = rules.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.sorted().joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
