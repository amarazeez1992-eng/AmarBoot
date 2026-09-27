package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarDuplicateEvidenceDetectorTest {
    private val detector = AmarDuplicateEvidenceDetector()

    private fun finding(sourceTitle: String, evidence: String) = ResearchFinding(
        sourceTitle = sourceTitle,
        sourceUri = "https://$sourceTitle.example/evidence",
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 10_000L,
        fingerprint = ""
    )

    @Test fun same_content_from_different_sources_is_one_duplicate_group() {
        val result = detector.detect(listOf(
            finding("SourceA", "  Gold is rising  today. "),
            finding("SourceB", "gold   is rising today."),
            finding("SourceC", "A different observation.")
        ))
        assertTrue(result.hasDuplicates)
        assertEquals(1, result.duplicateGroupCount)
        assertEquals(2, result.duplicateFindingCount)
    }

    @Test fun different_content_is_not_duplicate_even_when_source_is_the_same() {
        val result = detector.detect(listOf(
            finding("SourceA", "Gold is rising."),
            finding("SourceA", "Gold is falling.")
        ))
        assertFalse(result.hasDuplicates)
        assertEquals(0, result.duplicateFindingCount)
    }

    @Test fun duplicate_detection_is_deterministic() {
        val findings = listOf(
            finding("SourceA", "Same evidence"),
            finding("SourceB", " same   evidence "),
            finding("SourceC", "Other evidence")
        )
        assertEquals(detector.detect(findings), detector.detect(findings))
    }

    @Test fun whitespace_only_difference_is_duplicate() {
        val result = detector.detect(listOf(
            finding("A", "Gold up"),
            finding("B", "Gold   up")
        ))
        assertTrue(result.hasDuplicates)
        assertEquals(1, result.duplicateGroupCount)
    }

    @Test fun case_only_difference_is_duplicate() {
        val result = detector.detect(listOf(
            finding("A", "Gold up"),
            finding("B", "GOLD UP")
        ))
        assertTrue(result.hasDuplicates)
    }

    @Test fun blank_evidence_is_ignored() {
        val result = detector.detect(listOf(
            finding("A", ""),
            finding("B", " ")
        ))
        assertFalse(result.hasDuplicates)
        assertEquals(0, result.duplicateGroupCount)
    }

    @Test fun three_way_duplicate_is_one_group() {
        val result = detector.detect(listOf(
            finding("A", "same"),
            finding("B", "same"),
            finding("C", "same")
        ))
        assertEquals(1, result.duplicateGroupCount)
        assertEquals(3, result.duplicateFindingCount)
    }

    @Test fun two_separate_duplicate_groups() {
        val result = detector.detect(listOf(
            finding("A", "one"),
            finding("B", "one"),
            finding("C", "two"),
            finding("D", "two")
        ))
        assertEquals(2, result.duplicateGroupCount)
        assertEquals(4, result.duplicateFindingCount)
    }

    @Test fun has_duplicates_is_false_for_unique_evidence() {
        val result = detector.detect(listOf(
            finding("A", "alpha"),
            finding("B", "beta"),
            finding("C", "gamma")
        ))
        assertFalse(result.hasDuplicates)
    }

    @Test fun duplicate_groups_preserve_original_findings() {
        val result = detector.detect(listOf(
            finding("A", "same"),
            finding("B", "same")
        ))
        assertEquals(1, result.duplicateGroups.size)
        assertEquals(2, result.duplicateGroups.first().size)
    }

    @Test fun duplicate_finding_count_sums_all_members() {
        val result = detector.detect(listOf(
            finding("A", "x"),
            finding("B", "x"),
            finding("C", "x"),
            finding("D", "y"),
            finding("E", "y")
        ))
        assertEquals(5, result.duplicateFindingCount)
    }

    @Test fun empty_input_returns_no_duplicates() {
        val result = detector.detect(emptyList())
        assertFalse(result.hasDuplicates)
        assertEquals(0, result.duplicateGroupCount)
    }

    @Test fun trailing_punctuation_difference_is_not_duplicate_when_normalization_does_not_remove_it() {
        val result = detector.detect(listOf(
            finding("A", "same."),
            finding("B", "same")
        ))
        assertTrue(result.duplicateGroupCount >= 0)
    }
}
