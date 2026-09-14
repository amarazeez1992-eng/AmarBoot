package com.personal.gridbot.amaros.intelligence.core

/** Stage 11 / 1 — deterministic perception, reasoning and bounded confidence. */
object AmarIntelligenceCore {
    enum class InputKind { TEXT, IMAGE, FILE, MARKET_DATA, HISTORICAL_DATA, EXTERNAL_SIGNAL, UNKNOWN }
    enum class ObservationStatus { PRESENT, MISSING, MALFORMED }

    data class InputObservation(
        val kind: InputKind,
        val content: String,
        val sourceId: String? = null,
        val freshnessScore: Double = 1.0,
        val qualityScore: Double = 1.0
    ) {
        init {
            require(freshnessScore in 0.0..1.0) { "freshnessScore must be in [0,1]" }
            require(qualityScore in 0.0..1.0) { "qualityScore must be in [0,1]" }
        }

        val status: ObservationStatus
            get() = when {
                content.isBlank() -> ObservationStatus.MISSING
                kind == InputKind.UNKNOWN -> ObservationStatus.MALFORMED
                else -> ObservationStatus.PRESENT
            }
    }

    data class PerceptionResult(val observations: List<InputObservation>, val presentCount: Int, val missingCount: Int, val malformedCount: Int, val completeness: Double)
    data class ReasoningResult(val conclusion: String, val supportingObservations: List<String>, val assumptions: List<String>, val alternatives: List<String>)
    data class ConfidenceResult(val score: Double, val evidenceQuality: Double, val completeness: Double, val freshness: Double, val reasoningCoverage: Double, val label: ConfidenceLabel)
    enum class ConfidenceLabel { VERY_LOW, LOW, MODERATE, HIGH, VERY_HIGH }
    data class CoreResult(val perception: PerceptionResult, val reasoning: ReasoningResult, val confidence: ConfidenceResult)

    fun perceive(inputs: List<InputObservation>): PerceptionResult {
        val malformed = inputs.count { it.status == ObservationStatus.MALFORMED }
        val missing = inputs.count { it.status == ObservationStatus.MISSING }
        val present = inputs.count { it.status == ObservationStatus.PRESENT }
        val total = inputs.size
        return PerceptionResult(inputs.toList(), present, missing, malformed, if (total == 0) 0.0 else present.toDouble() / total)
    }

    fun reason(perception: PerceptionResult): ReasoningResult {
        val usable = perception.observations.filter { it.status == ObservationStatus.PRESENT }
        val assumptions = buildList {
            if (usable.isEmpty()) add("No usable observations were supplied")
            if (perception.missingCount > 0) add("Some expected observations are missing")
            if (perception.malformedCount > 0) add("Some observations are malformed")
        }
        val conclusion = when {
            usable.isEmpty() -> "INSUFFICIENT_DATA"
            perception.completeness < 0.5 -> "PARTIAL_DATA"
            else -> "SUFFICIENT_INPUT_FOR_NEXT_ANALYSIS_LAYER"
        }
        return ReasoningResult(conclusion, usable.map { "${it.kind}:${it.sourceId ?: "unspecified"}" }, assumptions, listOf("Collect additional evidence before making a high-impact decision"))
    }

    fun confidence(perception: PerceptionResult, reasoning: ReasoningResult): ConfidenceResult {
        val present = perception.observations.filter { it.status == ObservationStatus.PRESENT }
        val quality = if (present.isEmpty()) 0.0 else present.map { it.qualityScore }.average()
        val freshness = if (present.isEmpty()) 0.0 else present.map { it.freshnessScore }.average()
        val coverage = when {
            reasoning.supportingObservations.isEmpty() -> 0.0
            reasoning.assumptions.any { it.contains("missing", ignoreCase = true) || it.contains("malformed", ignoreCase = true) } -> 0.6
            else -> 1.0
        }
        val score = (quality * 0.35 + perception.completeness * 0.30 + freshness * 0.20 + coverage * 0.15).coerceIn(0.0, 1.0)
        return ConfidenceResult(score, quality, perception.completeness, freshness, coverage, labelFor(score))
    }

    fun analyze(inputs: List<InputObservation>): CoreResult {
        val perception = perceive(inputs)
        val reasoning = reason(perception)
        return CoreResult(perception, reasoning, confidence(perception, reasoning))
    }

    private fun labelFor(score: Double): ConfidenceLabel = when {
        score < 0.20 -> ConfidenceLabel.VERY_LOW
        score < 0.40 -> ConfidenceLabel.LOW
        score < 0.65 -> ConfidenceLabel.MODERATE
        score < 0.85 -> ConfidenceLabel.HIGH
        else -> ConfidenceLabel.VERY_HIGH
    }
}
