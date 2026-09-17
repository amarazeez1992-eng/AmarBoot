package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Point 7 regression: fingerprint integrity is tied to the exact source URI + evidence payload. */
class AmarFingerprintIntegrityVerifierTest {
    private val verifier = AmarFingerprintIntegrityVerifier()

    @Test
    fun matching_fingerprint_is_intact() {
        val finding = finding("https://alpha.example/evidence", "Gold is rising.")
        val result = verifier.verify(listOf(finding))

        assertTrue(result.intact)
        assertEquals(1, result.validFindings)
        assertTrue(result.items.single().fingerprintMatchesContent)
    }

    @Test
    fun modified_fingerprint_is_rejected() {
        val finding = finding(
            "https://alpha.example/evidence",
            "Gold is rising.",
            fingerprint = "tampered"
        )
        val result = verifier.verify(listOf(finding))

        assertFalse(result.intact)
        assertEquals(1, result.invalidFindings)
        assertFalse(result.items.single().valid)
    }

    @Test
    fun missing_fingerprint_is_rejected() {
        val finding = finding(
            "https://alpha.example/evidence",
            "Gold is rising.",
            fingerprint = ""
        )
        val result = verifier.verify(listOf(finding))

        assertFalse(result.intact)
        assertEquals(1, result.invalidFindings)
        assertFalse(result.items.single().fingerprintPresent)
    }

    @Test
    fun changing_source_or_evidence_breaks_integrity() {
        val original = finding("https://alpha.example/evidence", "Gold is rising.")
        val changedSource = original.copy(sourceUri = "https://beta.example/evidence")
        val changedEvidence = original.copy(evidence = "Gold is falling.")

        val result = verifier.verify(listOf(original, changedSource, changedEvidence))

        assertEquals(1, result.validFindings)
        assertEquals(2, result.invalidFindings)
    }

    @Test
    fun verification_is_deterministic() {
        val findings = listOf(
            finding("https://alpha.example/a", "A"),
            finding("https://beta.example/b", "B")
        )

        assertEquals(verifier.verify(findings), verifier.verify(findings))
    }

    private fun finding(uri: String, evidence: String, fingerprint: String? = null) = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 10_000L,
        fingerprint = fingerprint ?: AmarEvidence.fingerprintOf("$uri|$evidence")
    )
}
