package com.personal.gridbot.amaros.agent.admission

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test

class AmarEvidenceIntakeContractTest {
    @Test
    fun blank_provider_is_rejected() {
        val candidate = EvidenceCandidate("", "Title", "https://example.com", "Evidence", 1L)
        val rejected = IntakeRejectedCandidate(candidate, IntakeRejectionReason.BLANK_PROVIDER, "provider is blank")
        assertEquals(IntakeRejectionReason.BLANK_PROVIDER, rejected.reason)
    }

    @Test
    fun blank_title_is_rejected() {
        val candidate = EvidenceCandidate("provider", "", "https://example.com", "Evidence", 1L)
        val rejected = IntakeRejectedCandidate(candidate, IntakeRejectionReason.BLANK_TITLE, "title is blank")
        assertEquals(IntakeRejectionReason.BLANK_TITLE, rejected.reason)
    }

    @Test
    fun blank_evidence_is_rejected() {
        val candidate = EvidenceCandidate("provider", "Title", "https://example.com", "", 1L)
        val rejected = IntakeRejectedCandidate(candidate, IntakeRejectionReason.BLANK_EVIDENCE, "excerpt is blank")
        assertEquals(IntakeRejectionReason.BLANK_EVIDENCE, rejected.reason)
    }

    @Test
    fun invalid_url_is_rejected() {
        val candidate = EvidenceCandidate("provider", "Title", "not-a-url", "Evidence", 1L)
        val rejected = IntakeRejectedCandidate(candidate, IntakeRejectionReason.INVALID_URL, "url is invalid")
        assertEquals(IntakeRejectionReason.INVALID_URL, rejected.reason)
    }

    @Test
    fun valid_candidate_is_normalized() {
        val candidate = NormalizedCandidate(
            provider = "provider",
            title = "Title",
            canonicalUrl = "https://example.com",
            normalizedExcerpt = "Evidence",
            retrievedAtEpochMs = 1L,
            fingerprint = "a".repeat(64),
            normalizationFlags = emptySet()
        )
        assertTrue(candidate.provider.isNotBlank())
        assertEquals(64, candidate.fingerprint.length)
    }

    @Test
    fun normalized_candidate_rejects_short_fingerprint() {
        assertThrows(IllegalArgumentException::class.java) {
            NormalizedCandidate(
                provider = "provider",
                title = "Title",
                canonicalUrl = "https://example.com",
                normalizedExcerpt = "Evidence",
                retrievedAtEpochMs = 1L,
                fingerprint = "a".repeat(63),
                normalizationFlags = emptySet()
            )
        }
    }
}
