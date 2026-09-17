package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceQualityAnalyzerTest {
    private val analyzer = AmarSourceQualityAnalyzer()

    private fun finding(
        title: String = "Official source",
        uri: String = "https://example.com/source",
        evidence: String = "Observed evidence",
        publisher: String = "Example Publisher"
    ) = ResearchFinding(
        sourceTitle = title,
        sourceUri = uri,
        evidence = evidence,
        publisher = publisher
    )

    @Test
    fun complete_source_metadata_is_usable() {
        val result = analyzer.analyze(listOf(finding()))
        assertEquals(AmarSourceQualityStatus.ACCEPTABLE, result.status)
        assertEquals(1, result.usableSourceCount)
        assertEquals(1.0, result.score, 0.0)
        assertTrue(result.items.single().validHttpUri)
    }

    @Test
    fun missing_publisher_degrades_metadata_without_invalidating_source() {
        val result = analyzer.analyze(listOf(finding(publisher = "")))
        val item = result.items.single()
        assertEquals(AmarSourceQualityStatus.DEGRADED, result.status)
        assertEquals(0.75, item.score, 0.0)
        assertTrue(item.publisherPresent.not())
        assertTrue(item.issues.contains("missing_publisher"))
        assertEquals(1, result.usableSourceCount)
    }

    @Test
    fun malformed_uri_is_invalid() {
        val item = analyzer.analyze(listOf(finding(uri = "not-a-uri"))).items.single()
        assertFalse(item.validHttpUri)
        assertEquals(AmarSourceQualityLevel.INVALID, item.quality)
        assertTrue(item.issues.contains("invalid_source_uri"))
    }

    @Test
    fun missing_evidence_is_invalid() {
        val item = analyzer.analyze(listOf(finding(evidence = "   "))).items.single()
        assertFalse(item.evidencePresent)
        assertEquals(AmarSourceQualityLevel.INVALID, item.quality)
        assertTrue(item.issues.contains("missing_evidence"))
    }

    @Test
    fun empty_input_is_unverifiable() {
        val result = analyzer.analyze(emptyList())
        assertEquals(AmarSourceQualityStatus.UNVERIFIABLE, result.status)
        assertEquals(0, result.usableSourceCount)
        assertEquals(0.0, result.score, 0.0)
    }

    @Test
    fun point_two_does_not_apply_authority_or_freshness() {
        val primary = finding().copy(authority = Authority.PRIMARY, retrievedAtEpochMs = 1_000L)
        val unknown = finding().copy(authority = Authority.UNKNOWN, retrievedAtEpochMs = 9_000L)
        val result = analyzer.analyze(listOf(primary, unknown))
        assertEquals(result.items[0].score, result.items[1].score, 0.0)
    }
}
