package com.personal.gridbot.amaros.intelligence.core

import com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine

/**
 * Stage 11 / Item 1 — deterministic intelligence foundation.
 *
 * This layer performs perception and a bounded reasoning scaffold only. It does
 * not call models, tools, networks, brokers, or mutate application state.
 */
object AmarIntelligenceCore {
    enum class InputKind { TEXT, IMAGE, FILE, MARKET_DATA, HISTORICAL_DATA, EXTERNAL_SIGNAL, UNKNOWN }
    enum class ObservationStatus { PRESENT, MISSING, MALFORMED }
    enum class AnalysisState { READY, DEGRADED, BLOCKED }

    data class InputObservation(
        val kind: InputKind,
        val content: String,
        val sourceId: String? = null,
        val freshnessScore: Double = 1.0,
        val qualityScore: Double = 1.0
    ) {
        init {
            require(freshnessScore.isFinite() && freshnessScore in 0.0..1.0) { "freshnessScore must be finite and in [0,1]" }
            require(qualityScore.isFinite() && qualityScore in 0.0..1.0) { "qualityScore must be finite and in [0,1]" }
        }

        val normalizedContent: String get() = content.trim()

        val status: ObservationStatus
            get() = when {
                normalizedContent.isEmpty() -> ObservationStatus.MISSING
                kind == InputKind.UNKNOWN -> ObservationStatus.MALFORMED
                else -> ObservationStatus.PRESENT
            }
    }

    data class PerceptionResult(
        val observations: List<InputObservation>,
        val presentCount: Int,
        val missingCount: Int,
        val malformedCount: Int,
        val completeness: Double
    )

    data class ReasoningResult(
        val conclusion: String,
        val supportingObservations: List<String>,
        val assumptions: List<String>,
        val alternatives: List<String>,
        val requiresMoreInput: Boolean
    )

    data class CoreResult(
        val perception: PerceptionResult,
        val reasoning: ReasoningResult,
        val confidence: AmarConfidenceEngine.Result,
        val state: AnalysisState
    )

    fun perceive(inputs: List<InputObservation>): PerceptionResult {
        val snapshot = inputs.toList()
        val malformed = snapshot.count { it.status == ObservationStatus.MALFORMED }
        val missing = snapshot.count { it.status == ObservationStatus.MISSING }
        val present = snapshot.count { it.status == ObservationStatus.PRESENT }
        val total = snapshot.size
        return PerceptionResult(
            observations = snapshot,
            presentCount = present,
            missingCount = missing,
            malformedCount = malformed,
            completeness = if (total == 0) 0.0 else present.toDouble() / total
        )
    }

    fun reason(perception: PerceptionResult): ReasoningResult {
        val usable = perception.observations.filter { it.status == ObservationStatus.PRESENT }
        val requiresMoreInput = usable.isEmpty() || perception.completeness <= 0.5
        val assumptions = buildList {
            if (usable.isEmpty()) add("No usable observations were supplied")
            if (perception.missingCount > 0) add("Some expected observations are missing")
            if (perception.malformedCount > 0) add("Some observations are malformed")
        }
        val conclusion = when {
            usable.isEmpty() -> "INSUFFICIENT_DATA"
            perception.completeness <= 0.5 -> "PARTIAL_DATA"
            else -> "SUFFICIENT_INPUT_FOR_NEXT_ANALYSIS_LAYER"
        }
        return ReasoningResult(
            conclusion = conclusion,
            supportingObservations = usable.map { "${it.kind}:${it.sourceId ?: "unspecified"}" },
            assumptions = assumptions,
            alternatives = listOf("Collect additional evidence before making a high-impact decision"),
            requiresMoreInput = requiresMoreInput
        )
    }

    /**
     * Item 2 is the sole confidence authority.
     * Item 1 prepares the five explicit dimensions and delegates scoring/labels
     * to AmarConfidenceEngine; it contains no parallel confidence algorithm.
     */
    fun confidence(perception: PerceptionResult, reasoning: ReasoningResult): AmarConfidenceEngine.Result {
        val present = perception.observations.filter { it.status == ObservationStatus.PRESENT }
        val quality = if (present.isEmpty()) 0.0 else present.map { it.qualityScore }.average()
        val freshness = if (present.isEmpty()) 0.0 else present.map { it.freshnessScore }.average()
        val agreement = when {
            present.isEmpty() -> 0.0
            reasoning.supportingObservations.isEmpty() -> 0.0
            reasoning.requiresMoreInput -> 0.6
            else -> 1.0
        }
        val sourceReliability = if (present.isEmpty()) 0.0
        else present.count { it.sourceId != null }.toDouble() / present.size
        return AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(
                quality = quality,
                completeness = perception.completeness.coerceIn(0.0, 1.0),
                freshness = freshness,
                agreement = agreement,
                sourceReliability = sourceReliability
            )
        )
    }

    fun analyze(inputs: List<InputObservation>): CoreResult {
        val perception = perceive(inputs)
        val reasoning = reason(perception)
        val confidence = confidence(perception, reasoning)
        val state = when {
            perception.presentCount == 0 -> AnalysisState.BLOCKED
            reasoning.requiresMoreInput -> AnalysisState.DEGRADED
            else -> AnalysisState.READY
        }
        return CoreResult(perception, reasoning, confidence, state)
    }

}
