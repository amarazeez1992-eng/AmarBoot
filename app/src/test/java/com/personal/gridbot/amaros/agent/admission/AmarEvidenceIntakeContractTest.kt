package com.personal.gridbot.amaros.agent.admission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceIntakeContractTest {
    private val intake = AmarEvidenceIntake()

    @Test
    fun url_canonicalization_lowercases_scheme_and_host() {
        val result = intake.intake("question", listOf(candidate(url = "HTTPS://Example.COM/Page")))
        assertEquals("https://example.com/Page", result.candidates.single().canonicalUrl)
    }

    @Test
    fun url_canonicalization_removes_trailing_slash() {
        val result = intake.intake("question", listOf(candidate(url = "https://example.com/page/")))
        assertEquals("https://example.com/page", result.candidates.single().canonicalUrl)
    }

    @Test
    fun url_canonicalization_removes_fragment() {
        val result = intake.intake("question", listOf(candidate(url = "https://example.com/page#section")))
        assertEquals("https://example.com/page", result.candidates.single().canonicalUrl)
    }

    @Test
    fun text_normalization_collapses_whitespace() {
        val result = intake.intake(
            "question",
            listOf(candidate(title = "  A   title\nwith\tmultiple   spaces  ", excerpt = "  Some\n evidence\t text  "))
        )
        assertEquals("A title with multiple spaces", result.candidates.single().title)
        assertEquals("Some evidence text", result.candidates.single().normalizedExcerpt)
    }

    @Test
    fun text_normalization_trims_leading_trailing() {
        val result = intake.intake(
            "question",
            listOf(candidate(provider = "  Wikipedia  ", title = "  Title  ", excerpt = "  Evidence  "))
        )
        assertEquals("wikipedia", result.candidates.single().provider)
        assertEquals("Title", result.candidates.single().title)
        assertEquals("Evidence", result.candidates.single().normalizedExcerpt)
    }

    @Test
    fun fingerprint_is_sha256_hex_64_chars() {
        val result = intake.intake(
            "question",
            listOf(
                candidate(
                    provider = "wikipedia",
                    title = "Same Title",
                    excerpt = "Same excerpt content"
                )
            )
        )
        val fingerprint = result.candidates.single().fingerprint
        assertEquals("d26e527685675d359bb0afee1d3f72da4f5b14bca501f91c554c175cbbc71187", fingerprint)
        assertEquals(64, fingerprint.length)
        assertTrue(fingerprint.matches(Regex("[0-9a-f]{64}")))
    }

    @Test
    fun fingerprint_is_deterministic() {
        val candidate = candidate(provider = "Wikipedia", title = "Same Title", excerpt = "Same excerpt")
        val first = intake.intake("question", listOf(candidate)).candidates.single().fingerprint
        val second = intake.intake("question", listOf(candidate)).candidates.single().fingerprint
        assertEquals(first, second)
    }

    @Test
    fun different_urls_with_same_content_produce_same_fingerprint() {
        val first = intake.intake(
            "question",
            listOf(candidate(url = "https://example.com/a", title = "Same Title", excerpt = "Same excerpt"))
        ).candidates.single().fingerprint
        val second = intake.intake(
            "question",
            listOf(candidate(url = "https://example.com/b", title = "Same Title", excerpt = "Same excerpt"))
        ).candidates.single().fingerprint
        assertEquals(first, second)
    }

    @Test
    fun duplicate_url_is_rejected() {
        val result = intake.intake(
            "question",
            listOf(
                candidate(url = "https://example.com/page/"),
                candidate(url = "HTTPS://EXAMPLE.COM/page#section")
            )
        )
        assertEquals(1, result.candidates.size)
        assertEquals(1, result.rejectedCandidates.size)
        assertEquals(IntakeRejectionReason.DUPLICATE_URL, result.rejectedCandidates.single().reason)
    }

    @Test
    fun duplicate_fingerprint_is_rejected() {
        val candidate1 = candidate(
            provider = "wikipedia",
            title = "Same Title",
            url = "https://en.wikipedia.org/wiki/Page_A",
            excerpt = "Same excerpt content",
            retrievedAtEpochMs = 1000L
        )
        val candidate2 = candidate(
            provider = "wikipedia",
            title = "Same Title",
            url = "https://en.wikipedia.org/wiki/Page_B",
            excerpt = "Same excerpt content",
            retrievedAtEpochMs = 2000L
        )
        val result = intake.intake("question", listOf(candidate1, candidate2))
        assertEquals(1, result.candidates.size)
        assertEquals(1, result.rejectedCandidates.size)
        assertEquals(
            IntakeRejectionReason.DUPLICATE_FINGERPRINT,
            result.rejectedCandidates[0].reason
        )
    }

    @Test
    fun valid_unrelated_candidate_passes_intake() {
        val result = intake.intake(
            "completely unrelated question",
            listOf(candidate(title = "Unrelated Article", excerpt = "Technically valid evidence"))
        )
        assertEquals(1, result.candidates.size)
        assertTrue(result.rejectedCandidates.isEmpty())
    }

    @Test
    fun invalid_url_is_rejected() {
        val result = intake.intake(
            "question",
            listOf(candidate(url = "not-a-url"))
        )
        assertTrue(result.candidates.isEmpty())
        assertEquals(1, result.rejectedCandidates.size)
        assertEquals(IntakeRejectionReason.INVALID_URL, result.rejectedCandidates.single().reason)
    }

    private fun candidate(
        provider: String = "provider",
        title: String = "Title",
        url: String = "https://example.com",
        excerpt: String = "Evidence",
        retrievedAtEpochMs: Long = 1L
    ) = EvidenceCandidate(provider, title, url, excerpt, retrievedAtEpochMs)
}
