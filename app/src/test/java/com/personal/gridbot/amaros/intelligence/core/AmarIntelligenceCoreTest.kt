package com.personal.gridbot.amaros.intelligence.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarIntelligenceCoreTest {
    @Test
    fun perception_classifies_present_missing_and_malformed_inputs() {
        val result = AmarIntelligenceCore.perceive(
            listOf(
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "hello"),
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "")
            )
        )
        assertEquals(1, result.presentCount)
        assertEquals(1, result.missingCount)
        assertEquals(0, result.malformedCount)
        assertEquals(0.5, result.completeness, 0.0001)
    }

    @Test
    fun reasoning_fails_closed_to_insufficient_data_when_nothing_is_usable() {
        val result = AmarIntelligenceCore.analyze(
            listOf(AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.UNKNOWN, ""))
        )
        assertEquals("INSUFFICIENT_DATA", result.reasoning.conclusion)
        assertEquals(AmarIntelligenceCore.ConfidenceLabel.VERY_LOW, result.confidence.label)
        assertTrue(result.confidence.score < 0.20)
    }

    @Test
    fun confidence_is_bounded_and_sensitive_to_quality_freshness_and_completeness() {
        val strong = AmarIntelligenceCore.analyze(
            listOf(
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.MARKET_DATA, "ohlc", "source-a", 1.0, 1.0),
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.HISTORICAL_DATA, "history", "source-b", 0.9, 0.9)
            )
        )
        val weak = AmarIntelligenceCore.analyze(
            listOf(
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.MARKET_DATA, "partial", freshnessScore = 0.2, qualityScore = 0.2),
                AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.FILE, "")
            )
        )
        assertTrue(strong.confidence.score > weak.confidence.score)
        assertTrue(strong.confidence.score in 0.0..1.0)
        assertTrue(weak.confidence.score in 0.0..1.0)
    }

    @Test
    fun analysis_is_deterministic_for_identical_inputs() {
        val inputs = listOf(
            AmarIntelligenceCore.InputObservation(AmarIntelligenceCore.InputKind.TEXT, "same", "user", 0.8, 0.9)
        )
        assertEquals(AmarIntelligenceCore.analyze(inputs), AmarIntelligenceCore.analyze(inputs))
    }
}
