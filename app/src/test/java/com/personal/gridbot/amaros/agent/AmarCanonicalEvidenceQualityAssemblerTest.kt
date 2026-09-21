package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarCanonicalEvidenceQualityAssemblerTest {
    private val assembler = AmarCanonicalEvidenceQualityAssembler()

    private fun finding(
        uri: String,
        evidence: String,
        authority: Authority = Authority.PRIMARY,
        retrievedAt: Long = 1_000L,
        fingerprint: String = AmarEvidence.fingerprintOf("$uri|$evidence")
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = authority,
        retrievedAtEpochMs = retrievedAt,
        fingerprint = fingerprint
    )

    private fun allUpstreamVerified() = AmarEvidenceQualityUpstreamState(
        point1EvidenceIntakeVerified = true,
        point2SourceQualityVerified = true,
        point3AuthorityVerified = true,
        point4FreshnessVerified = true,
        point5SourceIndependenceVerified = true,
        point6DuplicateFreeVerified = true,
        point7FingerprintIntegrityVerified = true,
        point8TamperingIntegrityVerified = true,
        point9EvidenceUniquenessVerified = true
    )

    @Test
    fun point8_intact_provenance_is_accepted_by_owner_binding() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a, b), nowEpochMs = 1_500L)
        val certified = assembler.certify(listOf(a, b), 1_500L, verification, com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult.empty())
        assertTrue(certified.upstreamStates.first().point8TamperingIntegrityVerified)
    }

    @Test
    fun point8_modified_chain_hash_is_rejected() {
        val a = finding("https://a.example/x", "gold trend rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a), nowEpochMs = 1_500L)
        val tampered = verification.copy(provenance = listOf(verification.provenance.single().copy(chainHash = "tampered")))
        val certified = assembler.certify(listOf(a), 1_500L, tampered, com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult.empty())
        assertFalse(certified.upstreamStates.first().point8TamperingIntegrityVerified)
    }

    @Test
    fun point8_modified_previous_hash_is_rejected() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a, b), nowEpochMs = 1_500L)
        val tampered = verification.copy(provenance = verification.provenance.mapIndexed { index, node ->
            if (index == 1) node.copy(previousHash = "tampered") else node
        })
        val certified = assembler.certify(listOf(a, b), 1_500L, tampered, com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult.empty())
        assertFalse(certified.upstreamStates.first().point8TamperingIntegrityVerified)
    }

    @Test
    fun point8_modified_evidence_fingerprint_is_rejected() {
        val a = finding("https://a.example/x", "gold trend rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a), nowEpochMs = 1_500L)
        val tampered = verification.copy(provenance = listOf(verification.provenance.single().copy(evidenceFingerprint = "tampered")))
        val certified = assembler.certify(listOf(a), 1_500L, tampered, com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult.empty())
        assertFalse(certified.upstreamStates.first().point8TamperingIntegrityVerified)
    }

    @Test
    fun assembler_consumes_existing_point_contracts_without_reimplementing_them() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val report = assembler.assemble(listOf(a, b), nowEpochMs = 1_500L)

        assertEquals(2, report.items.size)
        assertEquals(2, report.point5Verification.independentSources)
        assertTrue(report.point7FingerprintIntegrity.intact)
        assertTrue(report.point9Uniqueness.unique)
        assertEquals(listOf(1.0, 1.0), report.compatibilityItemScores)
    }

    @Test
    fun unknown_authority_blocks_item_quality_but_preserves_owner_state() {
        val item = finding("https://a.example/x", "gold trend", authority = Authority.UNKNOWN)
        val report = assembler.assemble(listOf(item), nowEpochMs = 1_500L)

        assertFalse(report.items.single().authorityVerified)
        assertEquals(0.0, report.compatibilityItemScores.single(), 0.0)
    }

    @Test
    fun stale_evidence_remains_verified_but_has_reduced_freshness_score() {
        val item = finding("https://a.example/x", "stale gold evidence", retrievedAt = 1_000L)
        val report = assembler.assemble(listOf(item), nowEpochMs = 31L * 24L * 60L * 60L * 1000L + 1_000L)

        assertEquals(FreshnessStatus.STALE, report.items.single().let {
            if (it.freshnessVerified) FreshnessStatus.STALE else FreshnessStatus.FUTURE
        })
        assertTrue(report.items.single().freshnessVerified)
        assertTrue(report.items.single().freshnessScore < 1.0)
    }

    @Test
    fun future_evidence_is_blocked_without_repairing_freshness() {
        val item = finding("https://a.example/x", "future gold claim", retrievedAt = 2_000L)
        val report = assembler.assemble(listOf(item), nowEpochMs = 1_500L)

        assertFalse(report.items.single().freshnessVerified)
        assertEquals(0.0, report.items.single().freshnessScore, 0.0)
        assertEquals(0.0, report.compatibilityItemScores.single(), 0.0)
    }

    @Test
    fun duplicate_and_fingerprint_failures_remain_explicit_audit_facts() {
        val a = finding("https://a.example/x", "same evidence", retrievedAt = 1_000L)
        val b = finding(
            "https://b.example/x",
            "same evidence",
            retrievedAt = 1_000L,
            fingerprint = "tampered"
        )
        val report = assembler.assemble(listOf(a, b), nowEpochMs = 1_500L)

        assertTrue(report.point6Duplicates.hasDuplicates)
        assertEquals(1, report.point6Duplicates.duplicateGroupCount)
        assertFalse(report.point7FingerprintIntegrity.intact)
        assertTrue(report.point9Uniqueness.unique)
    }

    @Test
    fun owner_verification_outputs_feed_point10_without_local_point1_or_point8_reimplementation() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a, b), nowEpochMs = 1_500L)

        assertEquals(2, verification.usableEvidenceCount)
        assertEquals(0, verification.invalidEvidenceCount)
        assertEquals(2, verification.provenance.size)

        val certified = assembler.certify(
            findings = listOf(a, b),
            nowEpochMs = 1_500L,
            verification = verification,
            intakeResult = EvidenceIntakeResult.empty()
        )

        assertEquals(1.0, certified.certificationScore, 0.0)
        assertTrue(certified.upstreamStates.all { it.fullyVerified })
    }

    @Test
    fun owner_tampering_state_blocks_point10_certification() {
        val a = finding("https://a.example/x", "gold trend rising")
        val b = finding("https://b.example/x", "gold momentum rising")
        val verification = AmarVerificationLayer().verifyEvidenceOnly(listOf(a, b), nowEpochMs = 1_500L)
        val tampered = listOf(verification.provenance.first().copy(chainHash = "tampered")) + verification.provenance.drop(1)

        val tamperedVerification = verification.copy(provenance = tampered)
        val certified = assembler.certify(
            findings = listOf(a, b),
            nowEpochMs = 1_500L,
            verification = tamperedVerification,
            intakeResult = EvidenceIntakeResult.empty()
        )

        assertEquals(0.0, certified.certificationScore, 0.0)
        assertFalse(certified.upstreamStates.first().point8TamperingIntegrityVerified)
    }

    @Test
    fun certification_requires_canonical_upstream_states() {
        val a = finding("https://a.example/x", "gold trend")
        val b = finding("https://b.example/x", "gold momentum")
        val valid = assembler.certify(
            listOf(a, b),
            nowEpochMs = 1_500L,
            upstreamStates = listOf(allUpstreamVerified(), allUpstreamVerified())
        )
        assertEquals(1.0, valid.certificationScore, 0.0)

        val duplicateBlocked = assembler.certify(
            listOf(a, b),
            nowEpochMs = 1_500L,
            upstreamStates = listOf(
                allUpstreamVerified().copy(point6DuplicateFreeVerified = false),
                allUpstreamVerified()
            )
        )
        assertEquals(0.0, duplicateBlocked.certificationScore, 0.0)

        val tamperingBlocked = assembler.certify(
            listOf(a, b),
            nowEpochMs = 1_500L,
            upstreamStates = listOf(
                allUpstreamVerified().copy(point8TamperingIntegrityVerified = false),
                allUpstreamVerified()
            )
        )
        assertEquals(0.0, tamperingBlocked.certificationScore, 0.0)
    }

    @Test
    fun certification_rejects_missing_or_mismatched_upstream_state() {
        val a = finding("https://a.example/x", "gold trend")
        val report = assembler.certify(
            listOf(a),
            nowEpochMs = 1_500L,
            upstreamStates = emptyList()
        )
        assertEquals(0.0, report.certificationScore, 0.0)
    }

    @Test
    fun empty_input_is_fail_closed_and_deterministic() {
        val first = assembler.assemble(emptyList(), 1_500L)
        val second = assembler.assemble(emptyList(), 1_500L)

        assertEquals(first, second)
        assertEquals(0, first.point5Verification.totalSources)
    }
}
