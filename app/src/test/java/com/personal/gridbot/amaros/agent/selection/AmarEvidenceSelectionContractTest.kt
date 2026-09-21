package com.personal.gridbot.amaros.agent.selection

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import com.personal.gridbot.amaros.agent.relevance.RelevantCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceSelectionContractTest {
    private val selection = AmarEvidenceSelection()

    @Test
    fun empty_input_returns_empty_selection() {
        assertEquals(EvidenceSelectionResult.empty(), selection.select(emptyList()))
    }

    @Test
    fun fewer_than_n_candidates_all_selected() {
        val candidates = listOf(candidate(score = 0.9), candidate(score = 0.8))
        val result = selection.select(candidates, maxSelected = 10)

        assertEquals(2, result.selected.size)
        assertTrue(result.dropped.isEmpty())
    }

    @Test
    fun exactly_n_candidates_all_selected() {
        val candidates = (1..3).map { candidate(score = it / 10.0) }
        val result = selection.select(candidates, maxSelected = 3)

        assertEquals(3, result.selected.size)
        assertTrue(result.dropped.isEmpty())
    }

    @Test
    fun more_than_n_candidates_selects_top_n() {
        val candidates = listOf(
            candidate(score = 0.9, fingerprint = "c".repeat(64)),
            candidate(score = 0.8, fingerprint = "b".repeat(64)),
            candidate(score = 0.7, fingerprint = "a".repeat(64))
        )
        val result = selection.select(candidates, maxSelected = 2)

        assertEquals(2, result.selected.size)
        assertEquals(listOf(0.9, 0.8), result.selected.map { it.candidate.relevanceScore })
        assertEquals(1, result.dropped.size)
        assertEquals(0.7, result.dropped.single().relevanceScore, 0.0)
    }

    @Test
    fun highest_relevance_scores_are_selected_first() {
        val candidates = listOf(
            candidate(score = 0.2),
            candidate(score = 0.95),
            candidate(score = 0.6)
        )
        val result = selection.select(candidates, maxSelected = 2)

        assertEquals(listOf(0.95, 0.6), result.selected.map { it.candidate.relevanceScore })
    }

    @Test
    fun selected_candidates_preserve_original_relevance_score() {
        val candidates = listOf(
            candidate(score = 0.73, fingerprint = "b".repeat(64)),
            candidate(score = 0.91, fingerprint = "a".repeat(64))
        )
        val result = selection.select(candidates, maxSelected = 2)

        assertEquals(0.91, result.selected[0].candidate.relevanceScore, 0.0)
        assertEquals(0.73, result.selected[1].candidate.relevanceScore, 0.0)
    }

    @Test
    fun dropped_candidates_are_preserved() {
        val dropped = candidate(score = 0.4, fingerprint = "z".repeat(64))
        val result = selection.select(
            listOf(candidate(score = 0.9), dropped),
            maxSelected = 1
        )

        assertEquals(listOf(dropped), result.dropped)
    }

    @Test
    fun dropped_candidates_imply_selection_limit_reached_without_mutation() {
        val dropped = candidate(score = 0.4, reason = "RELEVANCE_ACCEPTED", fingerprint = "z".repeat(64))
        val result = selection.select(
            listOf(candidate(score = 0.9), dropped),
            maxSelected = 1
        )

        assertEquals(1, result.dropped.size)
        assertEquals("RELEVANCE_ACCEPTED", result.dropped.single().reason)
    }

    @Test
    fun equal_scores_have_deterministic_fingerprint_order() {
        val candidates = listOf(
            candidate(score = 0.8, fingerprint = "c".repeat(64)),
            candidate(score = 0.8, fingerprint = "a".repeat(64)),
            candidate(score = 0.8, fingerprint = "b".repeat(64))
        )
        val result = selection.select(candidates, maxSelected = 3)

        assertEquals(
            listOf("a".repeat(64), "b".repeat(64), "c".repeat(64)),
            result.selected.map { it.candidate.candidate.fingerprint }
        )
    }

    @Test
    fun repeated_execution_is_deterministic() {
        val candidates = listOf(
            candidate(score = 0.8, fingerprint = "b".repeat(64)),
            candidate(score = 0.8, fingerprint = "a".repeat(64)),
            candidate(score = 0.9, fingerprint = "c".repeat(64))
        )

        val first = selection.select(candidates, maxSelected = 2)
        val second = selection.select(candidates, maxSelected = 2)

        assertEquals(first, second)
    }

    @Test(expected = IllegalArgumentException::class)
    fun zero_or_negative_n_is_rejected() {
        selection.select(listOf(candidate(score = 0.9)), maxSelected = 0)
    }

    @Test
    fun selection_does_not_recalculate_relevance() {
        val candidates = listOf(
            candidate(score = 0.31, fingerprint = "b".repeat(64)),
            candidate(score = 0.82, fingerprint = "a".repeat(64))
        )
        val result = selection.select(candidates, maxSelected = 2)

        assertEquals(
            listOf(0.82, 0.31),
            result.selected.map { it.candidate.relevanceScore }
        )
    }

    private fun candidate(
        score: Double,
        provider: String = "provider",
        title: String = "Title",
        canonicalUrl: String = "https://example.com/page",
        excerpt: String = "Evidence",
        retrievedAtEpochMs: Long = 1L,
        fingerprint: String = "a".repeat(64),
        reason: String = "RELEVANCE_ACCEPTED"
    ) = RelevantCandidate(
        candidate = NormalizedCandidate(
            provider = provider,
            title = title,
            canonicalUrl = canonicalUrl,
            normalizedExcerpt = excerpt,
            retrievedAtEpochMs = retrievedAtEpochMs,
            fingerprint = fingerprint,
            normalizationFlags = emptySet()
        ),
        relevanceScore = score,
        reason = reason
    )
}
