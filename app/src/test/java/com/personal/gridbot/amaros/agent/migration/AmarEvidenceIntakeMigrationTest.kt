package com.personal.gridbot.amaros.agent.migration

import com.personal.gridbot.amaros.agent.AmarCanonicalEvidenceQualityAssembler
import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.admission.AmarEvidenceIntake
import com.personal.gridbot.amaros.agent.admission.AmarFindingToCandidateConverter
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceIntakeMigrationTest {
    private val converter = AmarFindingToCandidateConverter()
    private val intake = AmarEvidenceIntake()
    private val verificationLayer = AmarVerificationLayer()
    private val assembler = AmarCanonicalEvidenceQualityAssembler()

    private fun finding(
        uri: String,
        publisher: String = "Publisher",
        evidence: String = "gold trend rising",
        retrievedAt: Long = 1_000L
    ) = ResearchFinding(
        sourceTitle = "Source title",
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        publisher = publisher,
        retrievedAtEpochMs = retrievedAt,
        fingerprint = AmarEvidence.fingerprintOf("$uri|$evidence")
    )

    @Test
    fun research_finding_maps_to_evidence_candidate() {
        val candidate = converter.toCandidate(finding("https://a.example/x"))
        assertEquals("Publisher", candidate.provider)
        assertEquals("Source title", candidate.title)
        assertEquals("https://a.example/x", candidate.url)
        assertEquals("gold trend rising", candidate.excerpt)
        assertEquals(1_000L, candidate.retrievedAtEpochMs)
    }

    @Test
    fun converter_is_deterministic_for_same_finding() {
        val finding = finding("https://a.example/x")
        assertEquals(converter.toCandidate(finding), converter.toCandidate(finding))
    }

    @Test
    fun converted_candidates_reach_real_evidence_intake() {
        val findings = listOf(
            finding("https://a.example/x", "Publisher A"),
            finding("https://b.example/x", "Publisher B", "gold momentum rising")
        )
        val result = intake.intake(
            "What is the evidence?",
            findings.map(converter::toCandidate)
        )
        assertEquals(2, result.candidates.size)
        assertTrue(result.rejectedCandidates.isEmpty())
        assertTrue(result.intakeVerified)
    }

    @Test
    fun invalid_intake_fails_closed_without_empty_fallback() {
        val candidate = converter.toCandidate(finding("https://a.example/x", publisher = ""))
        val result = intake.intake("What is the evidence?", listOf(candidate))
        assertTrue(result.candidates.isEmpty())
        assertEquals(1, result.rejectedCandidates.size)
        assertFalse(result.intakeVerified)
    }

    @Test
    fun real_intake_result_is_consumed_by_certify() {
        val findings = listOf(
            finding("https://a.example/x", "Publisher A"),
            finding("https://b.example/x", "Publisher B", "gold momentum rising")
        )
        val intakeResult = intake.intake("What is the evidence?", findings.map(converter::toCandidate))
        val verification = verificationLayer.verifyEvidenceOnly(findings, nowEpochMs = 1_500L)
        val certified = assembler.certify(findings, 1_500L, verification, intakeResult)

        assertTrue(certified.upstreamStates.all { it.point1EvidenceIntakeVerified })
        assertNotNull(certified)
    }

    @Test
    fun failed_intake_reaches_certify_as_failed_state() {
        val findings = listOf(
            finding("https://a.example/x", ""),
            finding("https://b.example/x", "")
        )
        val intakeResult = intake.intake("What is the evidence?", findings.map(converter::toCandidate))
        val verification = verificationLayer.verifyEvidenceOnly(findings, nowEpochMs = 1_500L)
        val certified = assembler.certify(findings, 1_500L, verification, intakeResult)

        assertFalse(intakeResult.intakeVerified)
        assertTrue(certified.upstreamStates.all { !it.point1EvidenceIntakeVerified })
        assertEquals(0.0, certified.certificationScore, 0.0)
    }
}
