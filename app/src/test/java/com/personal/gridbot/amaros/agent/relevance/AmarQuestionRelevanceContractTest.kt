package com.personal.gridbot.amaros.agent.relevance

import com.personal.gridbot.amaros.agent.admission.NormalizedCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarQuestionRelevanceContractTest {
    private val relevance = AmarQuestionRelevance()

    @Test
    fun relevance_adapter_admits_when_score_above_threshold() {
        val result = relevance.evaluate(
            "gold price",
            listOf(candidate(title = "Gold Price", excerpt = "Market data"))
        )

        assertEquals(1, result.admitted.size)
        assertTrue(result.rejected.isEmpty())
        assertTrue(
            result.admitted.single().relevanceScore >=
                com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE
        )
    }

    @Test
    fun relevance_adapter_rejects_when_score_below_threshold() {
        val result = relevance.evaluate(
            "gold price",
            listOf(candidate(title = "Weather Forecast", excerpt = "Rain tomorrow"))
        )

        assertTrue(result.admitted.isEmpty())
        assertEquals(1, result.rejected.size)
        assertTrue(
            result.rejected.single().relevanceScore <
                com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE
        )
    }

    @Test
    fun relevance_adapter_uses_candidate_title_and_excerpt() {
        val titleOnly = relevance.evaluate(
            "gold price",
            listOf(candidate(title = "Gold", excerpt = "Market update"))
        ).admitted.single().relevanceScore

        val excerptOnly = relevance.evaluate(
            "gold price",
            listOf(candidate(title = "Market Update", excerpt = "Gold price"))
        ).admitted.single().relevanceScore

        assertTrue(excerptOnly > titleOnly)
    }

    @Test
    fun relevance_adapter_is_deterministic() {
        val candidates = listOf(
            candidate(title = "Gold Price", excerpt = "Market data"),
            candidate(title = "Weather Forecast", excerpt = "Rain tomorrow")
        )

        val first = relevance.evaluate("gold price", candidates)
        val second = relevance.evaluate("gold price", candidates)

        assertEquals(first, second)
    }

    @Test
    fun relevance_adapter_handles_empty_candidates() {
        val result = relevance.evaluate("gold price", emptyList())

        assertEquals(RelevanceResult.empty(), result)
    }

    @Test
    fun relevance_result_partitions_correctly() {
        val result = relevance.evaluate(
            "gold price",
            listOf(
                candidate(title = "Gold Price", excerpt = "Market data"),
                candidate(title = "Weather Forecast", excerpt = "Rain tomorrow")
            )
        )

        assertEquals(1, result.admitted.size)
        assertEquals(1, result.rejected.size)
        assertEquals(
            "RELEVANCE_ACCEPTED",
            result.admitted.single().reason
        )
        assertEquals(
            "RELEVANCE_REJECTED",
            result.rejected.single().reason
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun relevant_candidate_rejects_invalid_score() {
        RelevantCandidate(candidate(), Double.NaN, "RELEVANCE_ACCEPTED")
    }

    @Test(expected = IllegalArgumentException::class)
    fun relevance_rejected_candidate_rejects_invalid_score() {
        RelevanceRejectedCandidate(candidate(), 1.5, "RELEVANCE_REJECTED")
    }

    private fun candidate(
        provider: String = "provider",
        title: String = "Title",
        canonicalUrl: String = "https://example.com/page",
        excerpt: String = "Evidence",
        retrievedAtEpochMs: Long = 1L,
        fingerprint: String = "a".repeat(64)
    ) = NormalizedCandidate(
        provider = provider,
        title = title,
        canonicalUrl = canonicalUrl,
        normalizedExcerpt = excerpt,
        retrievedAtEpochMs = retrievedAtEpochMs,
        fingerprint = fingerprint,
        normalizationFlags = emptySet()
    )
}
