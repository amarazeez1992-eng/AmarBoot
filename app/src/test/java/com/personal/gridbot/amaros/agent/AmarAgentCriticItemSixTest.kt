package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAgentCriticItemSixTest {
    @Test fun accepts_clean_evidence_backed_answer() {
        val result = AmarAgentCritic().review(
            "النتيجة تدعمها المصادر.",
            listOf(
                ResearchFinding("A", "https://example.com/a", "support-a", stance = EvidenceStance.SUPPORTS),
                ResearchFinding("B", "https://example.org/b", "support-b", stance = EvidenceStance.SUPPORTS)
            ),
            requireEvidence = true
        )

        assertTrue(result.accepted)
        assertEquals("PASS", result.recommendation)
        assertEquals(2, result.evidenceCount)
        assertEquals(2, result.independentSourceCount)
        assertTrue(result.score >= 1.0)
    }

    @Test fun rejects_conflicting_evidence_instead_of_hiding_it() {
        val result = AmarAgentCritic().review(
            "النتيجة مؤكدة.",
            listOf(
                ResearchFinding("A", "https://example.com/a", "supports", stance = EvidenceStance.SUPPORTS),
                ResearchFinding("B", "https://example.org/b", "opposes", stance = EvidenceStance.OPPOSES)
            ),
            requireEvidence = true
        )

        assertFalse(result.accepted)
        assertTrue("evidence_conflict" in result.issues)
        assertTrue("unsupported_certainty" in result.issues)
        assertTrue(result.score < 1.0)
    }

    @Test fun duplicate_source_does_not_count_as_independent_evidence() {
        val result = AmarAgentCritic().review(
            "تحليل قائم على المصدر.",
            listOf(
                ResearchFinding("A1", "https://example.com/a", "one"),
                ResearchFinding("A2", "https://example.com/a", "two")
            ),
            requireEvidence = true
        )

        assertFalse(result.accepted)
        assertTrue("insufficient_independent_sources" in result.issues)
        assertEquals(1, result.independentSourceCount)
    }

    @Test fun invalid_evidence_is_not_treated_as_support() {
        val result = AmarAgentCritic().review(
            "النتيجة 95%.",
            listOf(
                ResearchFinding("invalid", "", "missing source"),
                ResearchFinding("invalid-2", "https://example.com/b", "")
            ),
            requireEvidence = true
        )

        assertFalse(result.accepted)
        assertTrue("no_evidence" in result.issues)
        assertTrue("invalid_evidence" in result.issues)
        assertTrue("unsupported_numeric_claim" in result.issues)
        assertEquals(0, result.evidenceCount)
    }

    @Test fun guarantee_language_remains_blocked_even_with_multiple_sources() {
        val result = AmarAgentCritic().review(
            "هذا مضمون.",
            listOf(
                ResearchFinding("A", "https://example.com/a", "support-a", stance = EvidenceStance.SUPPORTS),
                ResearchFinding("B", "https://example.org/b", "support-b", stance = EvidenceStance.SUPPORTS)
            ),
            requireEvidence = true
        )

        assertFalse(result.accepted)
        assertTrue("guarantee_language" in result.issues)
    }

    @Test fun empty_answer_is_always_rejected() {
        val result = AmarAgentCritic().review("   ", emptyList(), requireEvidence = false)
        assertFalse(result.accepted)
        assertEquals(0.0, result.score, 0.0)
        assertTrue("empty_answer" in result.issues)
    }
}
