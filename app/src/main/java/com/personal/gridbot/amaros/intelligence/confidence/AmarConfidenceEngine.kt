package com.personal.gridbot.amaros.intelligence.confidence

/**
 * Stage 11 / Item 2 — dedicated, deterministic confidence engine.
 *
 * Confidence is a bounded epistemic signal, not a trading decision and never
 * grants execution authority. The engine consumes explicit quality dimensions
 * and produces a reproducible score plus an explainable label.
 */
object AmarConfidenceEngine {
    enum class Label { VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH }

    data class Evidence(
        val quality: Double,
        val completeness: Double,
        val freshness: Double,
        val agreement: Double,
        val sourceReliability: Double
    ) {
        init {
            require(quality.isFinite() && quality in 0.0..1.0)
            require(completeness.isFinite() && completeness in 0.0..1.0)
            require(freshness.isFinite() && freshness in 0.0..1.0)
            require(agreement.isFinite() && agreement in 0.0..1.0)
            require(sourceReliability.isFinite() && sourceReliability in 0.0..1.0)
        }
    }

    data class Result(
        val score: Double,
        val label: Label,
        val evidenceQuality: Double,
        val completeness: Double,
        val freshness: Double,
        val agreement: Double,
        val sourceReliability: Double,
        val reasons: List<String>
    )

    fun evaluate(evidence: Evidence): Result {
        val score = (
            evidence.quality * 0.25 +
                evidence.completeness * 0.25 +
                evidence.freshness * 0.15 +
                evidence.agreement * 0.20 +
                evidence.sourceReliability * 0.15
            ).coerceIn(0.0, 1.0)

        val reasons = buildList {
            if (evidence.quality < 0.5) add("low_evidence_quality")
            if (evidence.completeness < 0.5) add("incomplete_input")
            if (evidence.freshness < 0.5) add("stale_input")
            if (evidence.agreement < 0.5) add("evidence_disagreement")
            if (evidence.sourceReliability < 0.5) add("low_source_reliability")
            if (isEmpty()) add("all_confidence_dimensions_acceptable")
        }

        return Result(
            score = score,
            label = labelFor(score),
            evidenceQuality = evidence.quality,
            completeness = evidence.completeness,
            freshness = evidence.freshness,
            agreement = evidence.agreement,
            sourceReliability = evidence.sourceReliability,
            reasons = reasons
        )
    }

    private fun labelFor(score: Double): Label = when {
        score < 0.20 -> Label.VERY_LOW
        score < 0.40 -> Label.LOW
        score < 0.65 -> Label.MODERATE
        score < 0.85 -> Label.HIGH
        else -> Label.VERY_HIGH
    }
}
