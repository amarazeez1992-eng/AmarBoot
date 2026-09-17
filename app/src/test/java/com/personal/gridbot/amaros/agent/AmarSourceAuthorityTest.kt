package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceAuthorityTest {
    @Test
    fun authority_scoring_is_deterministic_and_matches_defined_scale() {
        val expected = linkedMapOf(
            Authority.PRIMARY to 1.0,
            Authority.OFFICIAL to 0.95,
            Authority.PEER_REVIEWED to 0.90,
            Authority.REPUTABLE to 0.75,
            Authority.COMMUNITY to 0.40,
            Authority.UNKNOWN to 0.15
        )
        val verifier = AmarSourceVerifier()

        expected.forEach { (authority, weight) ->
            val result = verifier.verify(
                listOf(
                    ResearchFinding("a", "https://a.example/source", "evidence-a", authority = authority),
                    ResearchFinding("b", "https://b.example/source", "evidence-b", authority = authority)
                )
            )
            assertEquals(weight, result.authorityScore, 0.0)
            assertTrue(result.confidence in 0.0..1.0)
        }
    }

    @Test
    fun higher_authority_produces_higher_score_without_changing_source_count() {
        val verifier = AmarSourceVerifier()
        val primary = verifier.verify(
            listOf(
                ResearchFinding("a", "https://a.example/source", "evidence-a", authority = Authority.PRIMARY),
                ResearchFinding("b", "https://b.example/source", "evidence-b", authority = Authority.PRIMARY)
            )
        )
        val community = verifier.verify(
            listOf(
                ResearchFinding("a", "https://a.example/source", "evidence-a", authority = Authority.COMMUNITY),
                ResearchFinding("b", "https://b.example/source", "evidence-b", authority = Authority.COMMUNITY)
            )
        )

        assertEquals(2, primary.totalSources)
        assertEquals(2, community.totalSources)
        assertTrue(primary.authorityScore > community.authorityScore)
        assertTrue(primary.confidence > community.confidence)
    }
}
