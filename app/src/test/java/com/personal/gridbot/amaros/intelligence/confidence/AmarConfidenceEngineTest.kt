package com.personal.gridbot.amaros.intelligence.confidence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarConfidenceEngineTest {
    @Test
    fun score_is_bounded_and_high_for_strong_evidence() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 1.0, 1.0)
        )
        assertEquals(1.0, result.score, 0.0001)
        assertEquals(AmarConfidenceEngine.Label.VERY_HIGH, result.label)
    }

    @Test
    fun weak_evidence_produces_low_confidence_and_explanations() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.2, 0.3, 0.1, 0.2, 0.4)
        )
        assertTrue(result.score < 0.40)
        assertEquals(AmarConfidenceEngine.Label.LOW, result.label)
        assertTrue(result.reasons.contains("low_evidence_quality"))
        assertTrue(result.reasons.contains("incomplete_input"))
        assertTrue(result.reasons.contains("stale_input"))
        assertTrue(result.reasons.contains("evidence_disagreement"))
        assertTrue(result.reasons.contains("low_source_reliability"))
    }

    @Test
    fun each_dimension_can_reduce_confidence() {
        val strong = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.9, 0.9, 0.9, 0.9, 0.9)
        )
        val stale = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.9, 0.9, 0.0, 0.9, 0.9)
        )
        val incomplete = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.9, 0.0, 0.9, 0.9, 0.9)
        )
        assertTrue(strong.score > stale.score)
        assertTrue(strong.score > incomplete.score)
    }

    @Test
    fun invalid_dimensions_are_rejected() {
        assertTrue(runCatching {
            AmarConfidenceEngine.Evidence(-0.01, 0.5, 0.5, 0.5, 0.5)
        }.isFailure)
        assertTrue(runCatching {
            AmarConfidenceEngine.Evidence(0.5, 1.01, 0.5, 0.5, 0.5)
        }.isFailure)
        assertTrue(runCatching {
            AmarConfidenceEngine.Evidence(0.5, 0.5, Double.NaN, 0.5, 0.5)
        }.isFailure)
    }

    @Test
    fun evaluation_is_deterministic() {
        val evidence = AmarConfidenceEngine.Evidence(0.7, 0.8, 0.6, 0.9, 0.75)
        val first = AmarConfidenceEngine.evaluate(evidence)
        val second = AmarConfidenceEngine.evaluate(evidence)
        assertEquals(first, second)
    }
}
