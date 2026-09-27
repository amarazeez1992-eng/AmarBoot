package com.personal.gridbot.amaros.intelligence.confidence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun weak_evidence_produces_rejected_confidence_and_explanations() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.2, 0.3, 0.1, 0.2, 0.4)
        )
        assertTrue(result.score < 0.30)
        assertEquals(AmarConfidenceEngine.Label.REJECTED, result.label)
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

    @Test fun boundary_020_minus_001_is_rejected() = assertBoundary(0.199, AmarConfidenceEngine.Label.REJECTED)
    @Test fun boundary_020_exact_is_rejected() = assertBoundary(0.200, AmarConfidenceEngine.Label.REJECTED)
    @Test fun boundary_020_plus_001_is_rejected() = assertBoundary(0.201, AmarConfidenceEngine.Label.REJECTED)

    @Test fun boundary_040_minus_001_is_moderate() = assertBoundary(0.399, AmarConfidenceEngine.Label.LOW)
    @Test fun boundary_040_exact_is_moderate() = assertBoundary(0.400, AmarConfidenceEngine.Label.MODERATE)
    @Test fun boundary_040_plus_001_is_moderate() = assertBoundary(0.401, AmarConfidenceEngine.Label.MODERATE)

    @Test fun boundary_065_minus_001_is_moderate() = assertBoundary(0.649, AmarConfidenceEngine.Label.MODERATE)
    @Test fun boundary_065_exact_is_high() = assertBoundary(0.650, AmarConfidenceEngine.Label.HIGH)
    @Test fun boundary_065_plus_001_is_high() = assertBoundary(0.651, AmarConfidenceEngine.Label.HIGH)

    @Test fun boundary_085_minus_001_is_high() = assertBoundary(0.849, AmarConfidenceEngine.Label.HIGH)
    @Test fun boundary_085_exact_is_very_high() = assertBoundary(0.850, AmarConfidenceEngine.Label.VERY_HIGH)
    @Test fun boundary_085_plus_001_is_very_high() = assertBoundary(0.851, AmarConfidenceEngine.Label.VERY_HIGH)

    @Test
    fun rejection_threshold_exact_boundary_is_not_rejected() {
        val result = resultAtScore(0.300)
        assertEquals(0.300, result.score, 0.000001)
        assertEquals(AmarConfidenceEngine.Label.LOW, result.label)
    }

    @Test
    fun equation_is_exact_and_uses_all_weights() {
        val evidence = AmarConfidenceEngine.Evidence(0.8, 0.7, 0.6, 0.9, 0.8)
        val result = AmarConfidenceEngine.evaluate(evidence)
        val expected = 0.25 * 0.8 + 0.25 * 0.7 + 0.15 * 0.6 + 0.20 * 0.9 + 0.15 * 0.8
        assertEquals(expected, result.score, 0.0000001)
    }

    @Test
    fun each_of_five_dimensions_can_reduce_score_individually() {
        val baseline = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 1.0, 1.0)
        )
        val values = listOf(
            AmarConfidenceEngine.Evidence(0.0, 1.0, 1.0, 1.0, 1.0),
            AmarConfidenceEngine.Evidence(1.0, 0.0, 1.0, 1.0, 1.0),
            AmarConfidenceEngine.Evidence(1.0, 1.0, 0.0, 1.0, 1.0),
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 0.0, 1.0),
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 1.0, 0.0)
        )
        values.forEach { assertTrue(baseline.score > AmarConfidenceEngine.evaluate(it).score) }
    }

    @Test
    fun all_confidence_dimensions_acceptable_reason_is_emitted() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.8, 0.8, 0.8, 0.8, 0.8)
        )
        assertEquals(listOf("all_confidence_dimensions_acceptable"), result.reasons)
    }

    @Test
    fun every_dimension_rejects_nan_and_infinity() {
        val cases = listOf(
            { x: Double -> AmarConfidenceEngine.Evidence(x, 0.5, 0.5, 0.5, 0.5) },
            { x: Double -> AmarConfidenceEngine.Evidence(0.5, x, 0.5, 0.5, 0.5) },
            { x: Double -> AmarConfidenceEngine.Evidence(0.5, 0.5, x, 0.5, 0.5) },
            { x: Double -> AmarConfidenceEngine.Evidence(0.5, 0.5, 0.5, x, 0.5) },
            { x: Double -> AmarConfidenceEngine.Evidence(0.5, 0.5, 0.5, 0.5, x) }
        )
        cases.forEach {
            assertTrue(runCatching { it(Double.NaN) }.isFailure)
            assertTrue(runCatching { it(Double.POSITIVE_INFINITY) }.isFailure)
            assertTrue(runCatching { it(Double.NEGATIVE_INFINITY) }.isFailure)
        }
    }

    @Test
    fun result_contains_all_fields() {
        val evidence = AmarConfidenceEngine.Evidence(0.8, 0.7, 0.6, 0.9, 0.8)
        val result = AmarConfidenceEngine.evaluate(evidence)
        assertEquals(0.765, result.score, 0.000001)
        assertEquals(AmarConfidenceEngine.Label.HIGH, result.label)
        assertEquals(0.8, result.evidenceQuality, 0.0)
        assertEquals(0.7, result.completeness, 0.0)
        assertEquals(0.6, result.freshness, 0.0)
        assertEquals(0.9, result.agreement, 0.0)
        assertEquals(0.8, result.sourceReliability, 0.0)
        assertFalse(result.reasons.isEmpty())
    }

    @Test
    fun result_reasons_are_complete_and_ordered() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(0.2, 0.3, 0.1, 0.2, 0.4)
        )
        assertEquals(
            listOf(
                "low_evidence_quality",
                "incomplete_input",
                "stale_input",
                "evidence_disagreement",
                "low_source_reliability"
            ),
            result.reasons
        )
    }

    @Test
    fun no_execution_authority_module_has_no_broker_imports() {
        val source = java.io.File(
            "src/main/java/com/personal/gridbot/amaros/intelligence/confidence/AmarConfidenceEngine.kt"
        ).readText()
        assertFalse(Regex("""(?m)^\\s*import\\s+.*broker""").containsMatchIn(source))
    }

    @Test
    fun consumer_contract_is_implementable() {
        val consumer = object : AmarConfidenceConsumer {
            override fun requestConfidence(evidence: AmarConfidenceEngine.Evidence): AmarConfidenceEngine.Result =
                AmarConfidenceEngine.evaluate(evidence)
        }
        val result = consumer.requestConfidence(
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 1.0, 1.0)
        )
        assertEquals(AmarConfidenceEngine.Label.VERY_HIGH, result.label)
    }

    @Test
    fun staleness_ceiling_sets_score_to_zero_and_rejects() {
        val result = AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(1.0, 1.0, 1.0, 1.0, 1.0),
            maxAgeMs = 1_000L,
            ageMs = 1_000L
        )
        assertEquals(0.0, result.score, 0.0)
        assertEquals(AmarConfidenceEngine.Label.REJECTED, result.label)
        assertEquals(listOf("staleness_ceiling_exceeded"), result.reasons)
    }

    private fun assertBoundary(score: Double, label: AmarConfidenceEngine.Label) {
        val result = resultAtScore(score)
        assertEquals(score, result.score, 0.000001)
        assertEquals(label, result.label)
    }

    private fun resultAtScore(score: Double): AmarConfidenceEngine.Result =
        AmarConfidenceEngine.evaluate(
            AmarConfidenceEngine.Evidence(score, score, score, score, score)
        )
}
