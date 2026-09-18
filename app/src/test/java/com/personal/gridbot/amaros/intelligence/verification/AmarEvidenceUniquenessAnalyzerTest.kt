package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceUniquenessAnalyzerTest {
    private val analyzer = AmarEvidenceUniquenessAnalyzer()

    @Test
    fun same_content_from_different_sources_remains_unique_records() {
        val result = analyzer.analyze(
            listOf(
                finding("https://a.example/e", "Gold rises", 100L),
                finding("https://b.example/e", "Gold rises", 100L)
            )
        )

        assertTrue(result.unique)
        assertEquals(2, result.uniqueRecordCount)
        assertEquals(0, result.collisionGroupCount)
    }

    @Test
    fun same_source_content_and_timestamp_collide() {
        val result = analyzer.analyze(
            listOf(
                finding("https://a.example/e", "Gold rises", 100L),
                finding("https://a.example/e", "Gold rises", 100L)
            )
        )

        assertFalse(result.unique)
        assertEquals(1, result.uniqueRecordCount)
        assertEquals(1, result.collisionGroupCount)
        assertEquals(2, result.collidingFindingCount)
    }

    @Test
    fun changed_retrieval_time_creates_a_distinct_record_identity() {
        val result = analyzer.analyze(
            listOf(
                finding("https://a.example/e", "Gold rises", 100L),
                finding("https://a.example/e", "Gold rises", 101L)
            )
        )

        assertTrue(result.unique)
        assertEquals(2, result.uniqueRecordCount)
    }

    @Test
    fun per_finding_uniqueness_uses_the_same_canonical_record_identity() {
        val a = finding("https://a.example/e", "Gold rises", 100L)
        val b = finding("https://b.example/e", "Gold rises", 100L)
        val duplicate = finding("https://a.example/e", "Gold rises", 100L)
        assertTrue(analyzer.isUnique(a, listOf(a, b)))
        assertFalse(analyzer.isUnique(a, listOf(a, duplicate)))
        assertTrue(analyzer.isUnique(b, listOf(a, b)))
    }

    @Test
    fun missing_source_or_evidence_is_not_unique() {
        val result = analyzer.analyze(
            listOf(
                finding("", "Gold rises", 100L),
                finding("https://a.example/e", "", 100L)
            )
        )

        assertFalse(result.unique)
        assertEquals(0, result.eligibleFindings)
    }

    @Test
    fun analysis_is_deterministic_and_does_not_depend_on_supplied_fingerprint() {
        val findings = listOf(
            finding("https://a.example/e", "Gold rises", 100L, "tampered"),
            finding("https://b.example/e", "Gold falls", 100L, "")
        )

        assertEquals(analyzer.analyze(findings), analyzer.analyze(findings))
        assertTrue(analyzer.analyze(findings).unique)
    }

    private fun finding(
        uri: String,
        evidence: String,
        time: Long,
        fingerprint: String = "ignored"
    ) = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = time,
        fingerprint = fingerprint
    )
}
