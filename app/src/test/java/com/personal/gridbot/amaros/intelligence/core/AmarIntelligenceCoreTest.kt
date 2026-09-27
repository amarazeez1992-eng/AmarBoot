package com.personal.gridbot.amaros.intelligence.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntelligenceCoreTest {
    @Test
    fun perception_classifies_present_missing_and_malformed_inputs() {
        val result = AmarIntelligenceCore.perceive(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "hello"),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, ""),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.UNKNOWN, "unsupported payload")
        ))
        assertEquals(1, result.presentCount)
        assertEquals(1, result.missingCount)
        assertEquals(1, result.malformedCount)
        assertEquals(1.0 / 3.0, result.completeness, 0.0001)
    }

    @Test
    fun reasoning_fails_closed_to_insufficient_data_when_nothing_is_usable() {
        val result = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.UNKNOWN, "unsupported payload")
        ))
        assertEquals("INSUFFICIENT_DATA", result.reasoning.conclusion)
        assertTrue(result.reasoning.assumptions.any { it.contains("malformed") })
        assertTrue(result.reasoning.requiresMoreInput)
        assertEquals(AmarIntelligenceCore.AnalysisState.BLOCKED, result.state)
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.VERY_LOW, result.confidence.label)
        assertTrue(result.confidence.score < 0.20)
    }

    @Test
    fun partial_input_is_degraded_and_requires_more_input() {
        val result = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "usable"),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "")
        ))
        assertEquals("PARTIAL_DATA", result.reasoning.conclusion)
        assertTrue(result.reasoning.requiresMoreInput)
        assertEquals(AmarIntelligenceCore.AnalysisState.DEGRADED, result.state)
    }

    @Test
    fun confidence_is_bounded_and_sensitive_to_quality_freshness_and_completeness() {
        val strong = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.MARKET_DATA, "ohlc", "source-a", 1.0, 1.0),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.HISTORICAL_DATA, "history", "source-b", 0.9, 0.9)
        ))
        val weak = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.MARKET_DATA, "partial", freshnessScore = 0.2, qualityScore = 0.2),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "")
        ))
        assertTrue(strong.confidence.score > weak.confidence.score)
        assertTrue(strong.confidence.score in 0.0..1.0)
        assertTrue(weak.confidence.score in 0.0..1.0)
    }

    @Test
    fun invalid_quality_or_freshness_is_rejected() {
        assertTrue(runCatching {
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "x", freshnessScore = -0.1)
        }.isFailure)
        assertTrue(runCatching {
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "x", qualityScore = 1.1)
        }.isFailure)
    }

    @Test
    fun analysis_is_deterministic_and_does_not_mutate_input_order() {
        val inputs = listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "same", "user", 0.8, 0.9),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "file", "local", 0.7, 0.8)
        )
        val first = AmarIntelligenceCore.analyze(inputs)
        val second = AmarIntelligenceCore.analyze(inputs)
        assertEquals(first, second)
        assertEquals("same", inputs.first().content)
    }

    @Test
    fun empty_input_is_blocked_and_has_zero_completeness() {
        val result = AmarIntelligenceCore.analyze(emptyList())
        assertEquals(0, result.perception.presentCount)
        assertEquals(0.0, result.perception.completeness, 0.0)
        assertEquals(AmarIntelligenceCore.AnalysisState.BLOCKED, result.state)
    }

    @Test
    fun confidence_label_boundary_020_maps_to_moderate() {
        val perception = AmarIntelligenceCore.PerceptionResult(
            observations = listOf(
                AmarIntelligenceCore.InputObservation(
                    AmarIntelligenceCore.InputKind.TEXT, "x",
                    freshnessScore = 0.0, qualityScore = 0.0
                )
            ),
            presentCount = 1, missingCount = 0, malformedCount = 0,
            completeness = (0.20 - 0.15 + 1e-9) / 0.30
        )
        val reasoning = AmarIntelligenceCore.ReasoningResult(
            "x", listOf("TEXT:unspecified"), emptyList(), emptyList(), false
        )
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.MODERATE, AmarIntelligenceCore.confidence(perception, reasoning).label)
    }

    @Test
    fun confidence_label_boundary_040_maps_to_high() {
        val perception = AmarIntelligenceCore.PerceptionResult(
            observations = listOf(
                AmarIntelligenceCore.InputObservation(
                    AmarIntelligenceCore.InputKind.TEXT, "x",
                    freshnessScore = 0.0, qualityScore = 0.0
                )
            ),
            presentCount = 1, missingCount = 0, malformedCount = 0,
            completeness = (0.40 - 0.15 + 1e-9) / 0.30
        )
        val reasoning = AmarIntelligenceCore.ReasoningResult(
            "x", listOf("TEXT:unspecified"), emptyList(), emptyList(), false
        )
        val result = AmarIntelligenceCore.confidence(perception, reasoning)
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.HIGH, result.label)
        assertEquals(0.40, result.score, 0.000001)
    }

    @Test
    fun confidence_label_boundary_065_maps_to_high() {
        val perception = AmarIntelligenceCore.PerceptionResult(
            observations = listOf(
                AmarIntelligenceCore.InputObservation(
                    AmarIntelligenceCore.InputKind.TEXT, "x",
                    freshnessScore = 0.0, qualityScore = 0.0
                )
            ),
            presentCount = 1, missingCount = 0, malformedCount = 0,
            completeness = 5.0 / 3.0
        )
        val reasoning = AmarIntelligenceCore.ReasoningResult(
            "x", listOf("TEXT:unspecified"), emptyList(), emptyList(), false
        )
        val result = AmarIntelligenceCore.confidence(perception, reasoning)
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.HIGH, result.label)
        assertEquals(0.65, result.score, 0.000001)
    }

    @Test
    fun confidence_label_boundary_085_maps_to_very_high() {
        val perception = AmarIntelligenceCore.PerceptionResult(
            observations = listOf(
                AmarIntelligenceCore.InputObservation(
                    AmarIntelligenceCore.InputKind.TEXT, "x",
                    freshnessScore = 0.0, qualityScore = 0.0
                )
            ),
            presentCount = 1, missingCount = 0, malformedCount = 0,
            completeness = 7.0 / 3.0
        )
        val reasoning = AmarIntelligenceCore.ReasoningResult(
            "x", listOf("TEXT:unspecified"), emptyList(), emptyList(), false
        )
        val result = AmarIntelligenceCore.confidence(perception, reasoning)
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.VERY_HIGH, result.label)
        assertEquals(0.85, result.score, 0.000001)
    }

    @Test
    fun nan_quality_is_rejected() {
        assertTrue(runCatching {
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "x", qualityScore = Double.NaN)
        }.isFailure)
    }

    @Test
    fun infinite_freshness_is_rejected() {
        assertTrue(runCatching {
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "x", freshnessScore = Double.POSITIVE_INFINITY)
        }.isFailure)
    }

    @Test
    fun whitespace_is_normalized_before_status_classification() {
        val observation = AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "   ")
        assertEquals("", observation.normalizedContent)
        assertEquals(AmarIntelligenceCore.ObservationStatus.MISSING, observation.status)
    }

    @Test
    fun image_and_external_signal_inputs_are_present() {
        val result = AmarIntelligenceCore.perceive(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.IMAGE, "image"),
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.EXTERNAL_SIGNAL, "signal")
        ))
        assertEquals(2, result.presentCount)
        assertEquals(1.0, result.completeness, 0.0)
    }

    @Test
    fun reasoning_exposes_supporting_observations_and_alternatives() {
        val result = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "usable", "source-a")
        ))
        assertTrue(result.reasoning.supportingObservations.contains("TEXT:source-a"))
        assertTrue(result.reasoning.alternatives.isNotEmpty())
    }

    @Test
    fun complete_input_reaches_ready_state() {
        val result = AmarIntelligenceCore.analyze(listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "ready")
        ))
        assertEquals(AmarIntelligenceCore.AnalysisState.READY, result.state)
        assertTrue(!result.reasoning.requiresMoreInput)
    }

    @Test
    fun confidence_score_is_clamped_to_valid_range() {
        val perception = AmarIntelligenceCore.PerceptionResult(
            observations = listOf(
                AmarIntelligenceCore.InputObservation(
                    AmarIntelligenceCore.InputKind.TEXT, "x",
                    freshnessScore = 1.0, qualityScore = 1.0
                )
            ),
            presentCount = 1, missingCount = 0, malformedCount = 0,
            completeness = 2.0,
        )
        val reasoning = AmarIntelligenceCore.ReasoningResult(
            "x", listOf("TEXT:unspecified"), emptyList(), emptyList(), false
        )
        val result = AmarIntelligenceCore.confidence(perception, reasoning)
        assertEquals(1.0, result.score, 0.0)
        assertTrue(result.score in 0.0..1.0)
    }

    @Test
    fun zero_and_one_quality_and_freshness_boundaries_are_accepted() {
        val zero = AmarIntelligenceCore.InputObservation(
            AmarIntelligenceCore.InputKind.TEXT, "zero",
            freshnessScore = 0.0, qualityScore = 0.0
        )
        val one = AmarIntelligenceCore.InputObservation(
            AmarIntelligenceCore.InputKind.TEXT, "one",
            freshnessScore = 1.0, qualityScore = 1.0
        )
        assertEquals(0.0, zero.freshnessScore, 0.0)
        assertEquals(0.0, zero.qualityScore, 0.0)
        assertEquals(1.0, one.freshnessScore, 0.0)
        assertEquals(1.0, one.qualityScore, 0.0)
    }

    @Test
    fun perception_snapshot_is_immutable_against_source_list_changes() {
        val source = mutableListOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "first")
        )
        val snapshot = AmarIntelligenceCore.perceive(source)
        source.add(AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "second"))
        assertEquals(1, snapshot.observations.size)
        assertEquals("first", snapshot.observations.single().content)
    }

}
