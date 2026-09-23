package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceAttributionEnforcerTest {

    @Test
    fun all_findings_have_source_uri_pass() {
        val result = SourceAttributionEnforcer.evaluate(
            acceptedReport(),
            listOf(finding("https://example.com/a"), finding("https://example.com/b"))
        )

        assertTrue(result.attributed)
        assertEquals(AttributionReason.ALL_ATTRIBUTED, result.reason)
        assertTrue(result.unattributedFindings.isEmpty())
    }

    @Test
    fun one_finding_without_source_uri_blocks() {
        val result = SourceAttributionEnforcer.evaluate(
            acceptedReport(),
            listOf(finding("https://example.com/a"), finding(""))
        )

        assertFalse(result.attributed)
        assertEquals(AttributionReason.MISSING_SOURCE_URI, result.reason)
        assertEquals(1, result.unattributedFindings.size)
    }

    @Test
    fun all_findings_without_source_uri_blocks() {
        val result = SourceAttributionEnforcer.evaluate(
            acceptedReport(),
            listOf(finding(""), finding(" "))
        )

        assertFalse(result.attributed)
        assertEquals(AttributionReason.MISSING_SOURCE_URI, result.reason)
        assertEquals(2, result.unattributedFindings.size)
    }

    @Test
    fun empty_findings_blocks() {
        val result = SourceAttributionEnforcer.evaluate(
            acceptedReport(),
            emptyList()
        )

        assertFalse(result.attributed)
        assertEquals(AttributionReason.NO_FINDINGS, result.reason)
    }

    @Test
    fun blank_source_uri_blocks() {
        val result = SourceAttributionEnforcer.evaluate(
            acceptedReport(),
            listOf(finding("  "))
        )

        assertFalse(result.attributed)
        assertEquals(AttributionReason.MISSING_SOURCE_URI, result.reason)
        assertEquals(listOf("fingerprint"), result.unattributedFindings)
    }

    @Test
    fun no_reverification() {
        val report = AmarClaimVerificationReport(
            claims = listOf(
                AmarClaimVerification(
                    claim = "accepted claim",
                    supportingEvidence = 1,
                    opposingEvidence = 0,
                    accepted = true,
                    matchedEvidence = 1
                )
            ),
            accepted = true
        )

        val result = SourceAttributionEnforcer.evaluate(
            report,
            listOf(finding("https://example.com/source"))
        )

        assertTrue(result.attributed)
        assertTrue(report.accepted)
        assertTrue(report.claims.single().accepted)
    }

    @Test
    fun determinism() {
        val report = acceptedReport()
        val findings = listOf(finding("https://example.com/a"), finding(""))

        val first = SourceAttributionEnforcer.evaluate(report, findings)
        val second = SourceAttributionEnforcer.evaluate(report, findings)

        assertEquals(first, second)
    }

    @Test
    fun regression_claim_verification_is_not_recalculated() {
        val rejectedReport = AmarClaimVerificationReport(
            claims = listOf(
                AmarClaimVerification(
                    claim = "unsupported claim",
                    supportingEvidence = 0,
                    opposingEvidence = 1,
                    accepted = false,
                    matchedEvidence = 1
                )
            ),
            accepted = false
        )

        val result = SourceAttributionEnforcer.evaluate(
            rejectedReport,
            listOf(finding("https://example.com/source"))
        )

        assertFalse(result.attributed)
        assertEquals(AttributionReason.INVALID_INPUT, result.reason)
        assertFalse(rejectedReport.accepted)
        assertFalse(rejectedReport.claims.single().accepted)
    }

    private fun acceptedReport() = AmarClaimVerificationReport(
        claims = listOf(
            AmarClaimVerification(
                claim = "supported claim",
                supportingEvidence = 1,
                opposingEvidence = 0,
                accepted = true,
                matchedEvidence = 1
            )
        ),
        accepted = true
    )

    private fun finding(sourceUri: String) = ResearchFinding(
        sourceTitle = "Test Source",
        sourceUri = sourceUri,
        evidence = "Test evidence",
        fingerprint = "fingerprint"
    )
}
