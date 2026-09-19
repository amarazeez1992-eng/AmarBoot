package com.personal.gridbot.amaros.intelligence.confidence

import com.personal.gridbot.amaros.agent.AmarCanonicalEvidenceQualityAssembler
import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityUpstreamState
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Test

class AmarConfidenceEvidenceAdapterTest {
    private fun finding(
        uri: String,
        evidence: String,
        stance: EvidenceStance = EvidenceStance.SUPPORTS
    ) = ResearchFinding(
        sourceTitle = "source",
        sourceUri = uri,
        evidence = evidence,
        authority = Authority.PRIMARY,
        retrievedAtEpochMs = 1_000L,
        fingerprint = AmarEvidence.fingerprintOf("$uri|$evidence"),
        stance = stance
    )

    private fun verified() = AmarEvidenceQualityUpstreamState(
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
    fun adapter_consumes_canonical_outputs_without_synthetic_dimensions() {
        val findings = listOf(
            finding("https://a.example/x", "gold trend rising"),
            finding("https://b.example/x", "gold momentum rising")
        )
        val quality = AmarCanonicalEvidenceQualityAssembler().certify(
            findings = findings,
            nowEpochMs = 1_500L,
            upstreamStates = listOf(verified(), verified())
        )

        val result = AmarConfidenceEvidenceAdapter.evaluate(findings, quality)

        assertEquals(1.0, result.score, 0.0)
        assertEquals(AmarConfidenceEngine.Label.VERY_HIGH, result.label)
        assertEquals(1.0, result.evidenceQuality, 0.0)
        assertEquals(1.0, result.completeness, 0.0)
        assertEquals(1.0, result.freshness, 0.0)
        assertEquals(1.0, result.agreement, 0.0)
        assertEquals(1.0, result.sourceReliability, 0.0)
    }

    @Test
    fun failed_upstream_certification_flows_closed_into_confidence_quality() {
        val findings = listOf(finding("https://a.example/x", "gold trend rising"))
        val quality = AmarCanonicalEvidenceQualityAssembler().certify(
            findings = findings,
            nowEpochMs = 1_500L,
            upstreamStates = listOf(verified().copy(point8TamperingIntegrityVerified = false))
        )

        val result = AmarConfidenceEvidenceAdapter.evaluate(findings, quality)

        assertEquals(0.0, result.evidenceQuality, 0.0)
        org.junit.Assert.assertTrue(result.score < 1.0)
    }
}
