package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Point 6 regression: duplicate identity comes from normalized evidence content, not source metadata. */
class AmarDuplicateEvidenceDetectorTest {
    private val detector = AmarDuplicateEvidenceDetector()

    @Test
    fun same_content_from_different_sources_is_one_duplicate_group() {
        val result = detector.detect(
            listOf(
                finding("Source A", "  Gold is rising  today. "),
                finding("Source B", "gold   is rising today."),
                finding("Source C", "A different observation.")
            )
        )

        assertTrue(result.hasDuplicates)
        assertEquals(1, result.duplicateGroupCount)
        assertEquals(2, result.duplicateFindingCount)
    }

    @Test
    fun different_content_is_not_duplicate_even_when_source_is_the_same() {
        val result = detector.detect(
            listOf(
                finding("Source A", "Gold is rising."),
                finding("Source A", "Gold is falling.")
            )
        )

        assertFalse(result.hasDuplicates)
        assertEquals(0, result.duplicateFindingCount)
    }

    @Test
    fun duplicate_detection_is_deterministic() {
        val findings = listOf(
            finding("Source A", "Same evidence"),
            finding("Source B", " same   evidence "),
            finding("Source C", "Other evidence")
        )

        assertEquals(detector.detect(findings), detector.detect(findings))
    }

    private fun finding(sourceTitle: String, evidence: String) = ResearchFinding(
        sourceTitle = sourceTitle,
        sourceUri = "https://$sourceTitle.example/evidence",
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 10_000L,
        fingerprint = ""
    )
}
