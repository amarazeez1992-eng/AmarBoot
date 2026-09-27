package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceIndependenceBoundaryTest {
    private val verifier = AmarSourceVerifier()

    private fun finding(uri: String, evidence: String = "verified evidence") = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 10_000L,
        fingerprint = ""
    )

    @Test fun www_prefix_does_not_create_a_second_independent_source() {
        val result = verifier.verify(listOf(
            finding("https://example.com/a"),
            finding("https://www.example.com/b"),
            finding("https://example.com/c")
        ))
        assertEquals(1, result.independentSources)
        assertFalse(result.accepted)
    }

    @Test fun distinct_hosts_are_independent_even_when_paths_differ() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b")
        ))
        assertEquals(2, result.independentSources)
        assertTrue(result.accepted)
    }

    @Test fun repeated_evaluation_is_deterministic() {
        val findings = listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b"),
            finding("https://alpha.example/c")
        )
        assertEquals(verifier.verify(findings), verifier.verify(findings))
    }

    @Test fun same_host_different_path_is_one_independent_source() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a"),
            finding("https://alpha.example/b")
        ))
        assertEquals(1, result.independentSources)
    }

    @Test fun case_insensitive_host_matching() {
        val result = verifier.verify(listOf(
            finding("https://EXAMPLE.com/a"),
            finding("https://example.com/b"),
            finding("https://other.example/c")
        ))
        assertEquals(2, result.independentSources)
    }

    @Test fun uri_without_host_falls_back_to_full_uri() {
        val result = verifier.verify(listOf(
            finding("not-a-uri-a"),
            finding("not-a-uri-b")
        ))
        assertEquals(2, result.independentSources)
    }

    @Test fun verify_returns_total_source_count() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a"),
            finding("https://beta.example/b"),
            finding("https://gamma.example/c")
        ))
        assertEquals(3, result.totalSources)
    }

    @Test fun verify_rejects_empty_findings() {
        val result = verifier.verify(emptyList())
        assertFalse(result.accepted)
        assertEquals(0, result.totalSources)
    }

    @Test fun verify_rejects_blank_evidence() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a", evidence = " "),
            finding("https://beta.example/b")
        ))
        assertEquals(1, result.totalSources)
    }

    @Test fun three_hosts_all_independent() {
        val result = verifier.verify(listOf(
            finding("https://a.example/x"),
            finding("https://b.example/y"),
            finding("https://c.example/z")
        ))
        assertEquals(3, result.independentSources)
    }

    @Test fun subdomain_is_its_own_host() {
        val result = verifier.verify(listOf(
            finding("https://api.example/a"),
            finding("https://www.example/b")
        ))
        assertEquals(2, result.independentSources)
    }

    @Test fun confidence_bounded_when_strong_authority_single_host() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a"),
            finding("https://alpha.example/b")
        ))
        assertTrue(result.confidence in 0.0..1.0)
    }

    @Test fun duplicate_uri_list_treated_as_one_independent_source() {
        val result = verifier.verify(listOf(
            finding("https://alpha.example/a"),
            finding("https://alpha.example/a")
        ))
        assertEquals(1, result.independentSources)
    }
}
