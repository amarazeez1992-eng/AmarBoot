package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceIntakeValidatorTest {
    private val validator = AmarEvidenceIntakeValidator()

    private fun finding(
        title: String = "Source",
        uri: String = "https://example.com/evidence",
        evidence: String = "Gold evidence",
        retrievedAt: Long = 1_000L
    ) = ResearchFinding(title, uri, evidence, retrievedAtEpochMs = retrievedAt)

    @Test
    fun valid_intake_is_accepted() {
        assertTrue(validator.validate(listOf(finding())).accepted)
    }

    @Test
    fun missing_required_intake_fields_are_rejected() {
        val report = validator.validate(listOf(finding(title = "", uri = "", evidence = "")))
        assertFalse(report.accepted)
        assertTrue(report.issues.single().reasons.contains("missing_source_title"))
        assertTrue(report.issues.single().reasons.contains("missing_source_uri"))
        assertTrue(report.issues.single().reasons.contains("missing_evidence"))
    }

    @Test
    fun negative_retrieval_timestamp_is_rejected() {
        assertFalse(validator.validate(listOf(finding(retrievedAt = -1L))).accepted)
    }

    @Test
    fun repeated_validation_is_deterministic() {
        val input = listOf(finding(), finding(evidence = "Second"))
        assertTrue(validator.validate(input) == validator.validate(input))
    }
}
