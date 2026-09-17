package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Point 5 regression: verifies source independence without introducing a second verifier. */
class AmarSourceIndependenceBoundaryTest {
    private val verifier = AmarSourceVerifier()

    @Test
    fun www_prefix_does_not_create_a_second_independent_source() {
        val result = verifier.verify(
            listOf(
                finding("https://example.com/a"),
                finding("https://www.example.com/b"),
                finding("https://example.com/c")
            )
        )

        assertEquals(1, result.independentSources)
        assertFalse(result.accepted)
    }

    @Test
    fun distinct_hosts_are_independent_even_when_paths_differ() {
        val result = verifier.verify(
            listOf(
                finding("https://alpha.example/a"),
                finding("https://beta.example/b")
            )
        )

        assertEquals(2, result.independentSources)
        assertTrue(result.accepted)
    }

    @Test
    fun repeated_evaluation_is_deterministic() {
        val findings = listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b"),
            finding("https://alpha.example/c")
        )

        assertEquals(verifier.verify(findings), verifier.verify(findings))
    }

    private fun finding(uri: String) = ResearchFinding(
        sourceUri = uri,
        evidence = "verified evidence",
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 10_000L,
        fingerprint = ""
    )
}
