package com.personal.gridbot.amaros.agent.explanation

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.ranking.RankedEvidence
import com.personal.gridbot.amaros.agent.ranking.RankingSignal
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AmarEvidenceExplanationContractTest {
    private val explainer = AmarEvidenceExplainer()

    private fun ranked(
        status: EvidenceStatus = EvidenceStatus.COMPLETE,
        excerpt: String = "The evidence reports a verified fact."
    ): RankedEvidence {
        val candidate = NormalizedCandidate(
            provider = "Provider",
            title = "Evidence title",
            canonicalUrl = "https://example.com/evidence",
            normalizedExcerpt = excerpt,
            retrievedAtEpochMs = 1L,
            fingerprint = "a".repeat(64),
            normalizationFlags = emptySet()
        )
        val relevant = RelevantCandidate(candidate, 0.8, "relevant")
        val classified = ClassifiedEvidence(relevant, status, null, "status explanation")
        val signals = RankingSignal.requiredKeys().associateWith {
            when (it) {
                "relevanceScore" -> 0.8
                "statusWeight" -> 1.0
                "authorityScore" -> 0.7
                "freshnessScore" -> 0.9
                else -> 0.79
            }
        }
        return RankedEvidence(classified, 1, signals, "ranking explanation")
    }

    @Test fun empty_input_returns_empty_result() {
        val result = explainer.explain(emptyList())
        assertTrue(result.explained.isEmpty())
        assertTrue(result.rejected.isEmpty())
        assertFalse(result.isDownstreamReady)
    }

    @Test fun single_evidence_produces_explanation() {
        assertEquals(1, explainer.explain(listOf(ranked())).explained.size)
    }

    @Test fun explanation_is_not_blank() {
        val e = explainer.explain(listOf(ranked())).explained.single().explanation
        assertNotNull(e)
        assertTrue(e.summary.isNotBlank())
    }

    @Test fun explanation_contains_source_info() {
        val e = explainer.explain(listOf(ranked())).explained.single().explanation!!
        assertTrue(e.sourceInfo.contains("Provider"))
        assertTrue(e.sourceInfo.contains("https://example.com/evidence"))
    }

    @Test fun explanation_contains_status_info() {
        val e = explainer.explain(listOf(ranked())).explained.single().explanation!!
        assertTrue(e.statusInfo.contains("COMPLETE"))
    }

    @Test fun explanation_is_deterministic() {
        assertEquals(explainer.explain(listOf(ranked())), explainer.explain(listOf(ranked())))
    }

    @Test fun explanation_is_not_llm_generated() {
        val e = explainer.explain(listOf(ranked())).explained.single().explanation!!
        assertEquals("EN", e.language)
        assertFalse(e.summary.contains("LLM", ignoreCase = true))
    }

    @Test fun explanation_does_not_recalculate_relevance() {
        val e = explainer.explain(listOf(ranked())).explained.single().explanation!!
        assertFalse(e.details.any { it.contains("relevanceScore") })
    }

    @Test fun explanation_preserves_evidence() {
        val input = ranked()
        val output = explainer.explain(listOf(input)).explained.single().evidence
        assertEquals(input, output)
    }

    @Test fun explanation_works_for_complete_status() {
        val e = explainer.explain(listOf(ranked(EvidenceStatus.COMPLETE))).explained.single().explanation!!
        assertTrue(e.statusInfo.contains("COMPLETE"))
    }

    @Test fun explanation_works_for_partial_status() {
        val e = explainer.explain(listOf(ranked(EvidenceStatus.PARTIAL))).explained.single().explanation!!
        assertTrue(e.statusInfo.contains("PARTIAL"))
    }

    @Test fun explanation_works_for_stale_status() {
        val e = explainer.explain(listOf(ranked(EvidenceStatus.STALE))).explained.single().explanation!!
        assertTrue(e.statusInfo.contains("STALE"))
    }

    @Test fun explanation_language_matches_context() {
        assertEquals("EN", explainer.explain(listOf(ranked())).explained.single().explanation!!.language)
    }

    @Test fun explanation_result_partitions_correctly() {
        val result = explainer.explain(listOf(ranked(), ranked(EvidenceStatus.PARTIAL)))
        assertEquals(2, result.explained.size)
        assertTrue(result.rejected.isEmpty())
        assertTrue(result.isDownstreamReady)
    }

    @Test fun explanation_fail_closed_when_evidence_missing() {
        val result = explainer.explain(listOf(ranked(excerpt = "")))
        assertTrue(result.explained.isEmpty())
        assertEquals(1, result.rejected.size)
        assertEquals(RejectionReason.MISSING_EVIDENCE, result.rejected.single().explanationReason)
        assertFalse(result.isDownstreamReady)
    }
}
