package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs
import kotlin.math.sqrt

/** Detects distribution drift and quantifies uncertainty without converting it into profit probability. */
object AmarDriftAndUncertaintyEngine {
    data class DriftReport(
        val meanDelta: Double,
        val volatilityDelta: Double,
        val distributionShift: Double,
        val severe: Boolean,
        val reasons: List<String>
    )

    data class UncertaintyReport(
        val uncertaintyPct: Double,
        val confidencePct: Double,
        val samplePenalty: Double,
        val instabilityPenalty: Double,
        val reasons: List<String>
    )

    fun drift(reference: List<Double>, recent: List<Double>): DriftReport {
        if (reference.size < 5 || recent.size < 5) {
            return DriftReport(0.0, 0.0, 1.0, true, listOf("insufficient samples for drift detection"))
        }
        val rm = mean(reference)
        val nm = mean(recent)
        val rs = stdev(reference, rm)
        val ns = stdev(recent, nm)

        // Mean drift is measured against pooled dispersion, not absolute price/value scale.
        // This prevents a large regime translation (e.g. 1 -> 4) from being diluted simply
        // because the raw values themselves are numerically large.
        val pooledStdev = sqrt((rs * rs + ns * ns) / 2.0).coerceAtLeast(1e-9)
        val rawMeanEffect = abs(nm - rm) / pooledStdev
        val meanDelta = (rawMeanEffect / 3.0).coerceIn(0.0, 1.0)

        // Volatility drift is a relative change between the two distributions.
        val volatilityDelta = (abs(ns - rs) / maxOf(rs, ns, 1e-9)).coerceIn(0.0, 1.0)
        val shift = (meanDelta * 0.65 + volatilityDelta * 0.35).coerceIn(0.0, 1.0)
        val severe = shift >= 0.50
        val reasons = buildList {
            if (meanDelta >= 0.25) add("mean shifted materially")
            if (volatilityDelta >= 0.25) add("volatility regime shifted materially")
            if (severe) add("revalidation required before trusting prior results")
        }
        return DriftReport(meanDelta, volatilityDelta, shift, severe, reasons)
    }

    fun uncertainty(sampleSize: Int, expectancyStdev: Double, oosWindows: Int, failedWindows: Int, drift: Double): UncertaintyReport {
        val samplePenalty = when {
            sampleSize < 30 -> 0.45
            sampleSize < 100 -> 0.25
            sampleSize < 300 -> 0.12
            else -> 0.05
        }
        val instabilityPenalty = (expectancyStdev.coerceAtLeast(0.0) * 0.20 +
            if (oosWindows <= 0) 0.35 else (failedWindows.toDouble() / oosWindows).coerceIn(0.0, 1.0) * 0.30 +
            drift.coerceIn(0.0, 1.0) * 0.35).coerceIn(0.0, 0.90)
        val uncertainty = (samplePenalty + instabilityPenalty).coerceIn(0.0, 0.95)
        val confidence = (1.0 - uncertainty).coerceIn(0.0, 1.0)
        return UncertaintyReport(uncertainty * 100.0, confidence * 100.0, samplePenalty, instabilityPenalty, buildList {
            if (samplePenalty >= 0.25) add("sample size is weak")
            if (failedWindows > 0) add("some OOS windows failed")
            if (drift >= 0.50) add("market distribution drift is severe")
            add("confidence is evidence quality, not probability of profit")
        })
    }

    private fun mean(values: List<Double>) = values.average()
    private fun stdev(values: List<Double>, mean: Double): Double = sqrt(values.map { (it - mean) * (it - mean) }.average())
}
