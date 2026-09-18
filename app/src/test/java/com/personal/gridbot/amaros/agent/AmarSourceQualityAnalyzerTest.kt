package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarSourceQualityAnalyzerTest {
    private val analyzer = AmarSourceQualityAnalyzer()

    private fun finding(
        uri: String = "https://example.com/evidence",
        evidence: String = "Gold evidence",
        authority: Authority = Authority.PRIMARY
    ) = ResearchFinding("Source", uri, evidence, authority = authority, retrievedAtEpochMs = 1_000L)

    @Test
    fun complete_authoritative_evidence_is_usable() {
        assertTrue(analyzer.assess(finding()).usable)
    }

    @Test
    fun missing_source_or_evidence_is_not_usable() {
        assertFalse(analyzer.assess(finding(uri = "")).usable)
        assertFalse(analyzer.assess(finding(evidence = "")).usable)
    }

    @Test
    fun unknown_authority_is_not_usable() {
        assertFalse(analyzer.assess(finding(authority = Authority.UNKNOWN)).usable)
    }

    @Test
    fun repeated_assessment_is_deterministic() {
        val input = finding()
        assertTrue(analyzer.assess(input) == analyzer.assess(input))
    }
}
