package com.personal.gridbot.amaros.agent.ranking

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.EvidenceStatus
import com.personal.gridbot.amaros.agent.status.QueryContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceRankingContractTest {
    private val ranker = AmarEvidenceRanker()

    @Test
    fun empty_input_returns_empty_ranking() {
        val result = rank(emptyList())
        assertTrue(result.ranked.isEmpty())
        assertTrue(result.unranked.isEmpty())
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun single_evidence_gets_rank_1() {
        val evidence = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(listOf(evidence))
        assertEquals(1, result.ranked.single().rank)
        assertEquals(evidence, result.ranked.single().evidence)
    }

    @Test
    fun complete_status_ranked_higher_than_partial() {
        val partial = evidence("a".repeat(64), EvidenceStatus.PARTIAL)
        val complete = evidence("b".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(listOf(partial, complete))
        assertEquals(EvidenceStatus.COMPLETE, result.ranked.first().evidence.status)
    }

    @Test
    fun higher_relevance_score_ranked_first() {
        val low = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val high = evidence("b".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(
            listOf(low, high),
            relevance = mapOf(low.fp() to 0.2, high.fp() to 0.9)
        )
        assertEquals(high, result.ranked.first().evidence)
    }

    @Test
    fun authority_breaks_relevance_tie() {
        val low = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val high = evidence("b".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(
            listOf(low, high),
            authority = mapOf(low.fp() to 0.2, high.fp() to 0.9)
        )
        assertEquals(high, result.ranked.first().evidence)
    }

    @Test
    fun fingerprint_breaks_authority_tie() {
        val low = evidence("0".repeat(64), EvidenceStatus.COMPLETE)
        val high = evidence("f".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(
            listOf(high, low),
            authority = mapOf(high.fp() to 0.8, low.fp() to 0.8),
            freshness = mapOf(high.fp() to 0.8, low.fp() to 0.8)
        )
        assertEquals(low, result.ranked.first().evidence)
    }

    @Test
    fun stale_evidence_ranked_lowest() {
        val stale = evidence("a".repeat(64), EvidenceStatus.STALE)
        val complete = evidence("b".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(
            listOf(stale, complete),
            relevance = mapOf(stale.fp() to 1.0, complete.fp() to 0.1)
        )
        assertEquals(EvidenceStatus.COMPLETE, result.ranked.first().evidence.status)
        assertEquals(EvidenceStatus.STALE, result.ranked.last().evidence.status)
    }

    @Test
    fun unranked_contains_insufficient() {
        val item = evidence("a".repeat(64), EvidenceStatus.INSUFFICIENT)
        val result = rank(listOf(item))
        assertEquals(listOf(item), result.unranked)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun unranked_contains_conflicted() {
        val item = evidence("a".repeat(64), EvidenceStatus.CONFLICTED)
        val result = rank(listOf(item))
        assertEquals(listOf(item), result.unranked)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun unranked_contains_unverified() {
        val item = evidence("a".repeat(64), EvidenceStatus.UNVERIFIED)
        val result = rank(listOf(item))
        assertEquals(listOf(item), result.unranked)
        assertFalse(result.isDownstreamReady)
    }

    @Test
    fun ranking_signals_are_recorded() {
        val item = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(
            listOf(item),
            relevance = mapOf(item.fp() to 0.8),
            authority = mapOf(item.fp() to 0.9),
            freshness = mapOf(item.fp() to 0.7)
        )
        val signals = result.ranked.single().rankingSignals
        assertEquals(0.8, signals[RankingSignal.RELEVANCE]!!, 0.000001)
        assertEquals(1.0, signals[RankingSignal.STATUS_WEIGHT]!!, 0.000001)
        assertEquals(0.9, signals[RankingSignal.AUTHORITY]!!, 0.000001)
        assertEquals(0.7, signals[RankingSignal.FRESHNESS]!!, 0.000001)
        assertEquals(0.855, signals[RankingSignal.FINAL_SCORE]!!, 0.000001)
    }

    @Test
    fun explanation_is_deterministic() {
        val item = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        assertEquals(
            rank(listOf(item)).ranked.single().explanation,
            rank(listOf(item)).ranked.single().explanation
        )
    }

    @Test
    fun ranking_is_deterministic() {
        val first = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val second = evidence("b".repeat(64), EvidenceStatus.PARTIAL)
        val input = input(listOf(first, second))
        assertEquals(ranker.rank(input), ranker.rank(input))
    }

    @Test
    fun is_downstream_ready_false_when_unranked() {
        val valid = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val invalid = evidence("b".repeat(64), EvidenceStatus.CONFLICTED)
        val result = rank(listOf(valid, invalid))
        assertFalse(result.isDownstreamReady)
        assertEquals(1, result.ranked.size)
        assertEquals(1, result.unranked.size)
    }

    @Test
    fun ranking_does_not_recalculate_relevance() {
        val item = evidence("a".repeat(64), EvidenceStatus.COMPLETE, candidateRelevance = 0.0)
        val result = rank(
            listOf(item),
            relevance = mapOf(item.fp() to 1.0)
        )
        assertEquals(
            1.0,
            result.ranked.single().rankingSignals[RankingSignal.RELEVANCE]!!,
            0.000001
        )
    }

    @Test
    fun missing_signal_is_fail_closed() {
        val item = evidence("a".repeat(64), EvidenceStatus.COMPLETE)
        val result = rank(listOf(item), authority = emptyMap())
        assertTrue(result.ranked.isEmpty())
        assertEquals(listOf(item), result.unranked)
        assertFalse(result.isDownstreamReady)
    }

    private fun rank(
        evidence: List<ClassifiedEvidence>,
        relevance: Map<String, Double> = evidence.associate { it.fp() to 0.8 },
        authority: Map<String, Double> = evidence.associate { it.fp() to 0.8 },
        freshness: Map<String, Double> = evidence.associate { it.fp() to 0.8 }
    ) = ranker.rank(input(evidence, relevance, authority, freshness))

    private fun input(
        evidence: List<ClassifiedEvidence>,
        relevance: Map<String, Double> = evidence.associate { it.fp() to 0.8 },
        authority: Map<String, Double> = evidence.associate { it.fp() to 0.8 },
        freshness: Map<String, Double> = evidence.associate { it.fp() to 0.8 }
    ) = AmarEvidenceRankingInput(
        classifiedEvidence = evidence,
        relevanceScores = relevance,
        authorityScores = authority,
        freshnessScores = freshness,
        userQuestion = "test question",
        queryContext = QueryContext.GENERAL
    )

    private fun evidence(
        fingerprint: String,
        status: EvidenceStatus,
        candidateRelevance: Double = 0.8
    ) = ClassifiedEvidence(
        candidate = RelevantCandidate(
            candidate = NormalizedCandidate(
                provider = "provider",
                title = "Evidence " + fingerprint,
                canonicalUrl = "https://example.com/" + fingerprint,
                normalizedExcerpt = "Evidence",
                retrievedAtEpochMs = 1L,
                fingerprint = fingerprint,
                normalizationFlags = emptySet()
            ),
            relevanceScore = candidateRelevance,
            reason = "TEST"
        ),
        status = status,
        reason = null,
        explanation = "TEST"
    )

    private fun ClassifiedEvidence.fp(): String = candidate.candidate.fingerprint
}
