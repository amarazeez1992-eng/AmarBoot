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
}
