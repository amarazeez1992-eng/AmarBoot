package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerification
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarHallucinationFirewallGateTest {
    private fun gate() = AmarHallucinationFirewallGate()

    private fun claim(text: String, accepted: Boolean = true, matched: Int = 3) =
        AmarClaimVerification(claim = text, supportingEvidence = matched, opposingEvidence = 0, accepted = accepted, matchedEvidence = matched)

    private fun report(vararg claims: AmarClaimVerification) =
        AmarClaimVerificationReport(claims.toList(), accepted = claims.all { it.accepted })

    private fun finding(uri: String, evidence: String) = ResearchFinding(
        sourceTitle = "s", sourceUri = uri, evidence = evidence,
        authority = Authority.PRIMARY, stance = EvidenceStance.SUPPORTS
    )

    @Test fun evaluate_returns_all_eight_components() {
        val correlation = CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = false,
            reason = com.personal.gridbot.amaros.agent.correlation.CorrelationReason.INSUFFICIENT_DATA
        )
        val r = gate().evaluate(
            answer = "A well-supported factual statement.",
            claims = listOf("The market moved."),
            report = report(claim("The market moved.")),
            findings = listOf(finding("https://a.example/x", "market moved evidence")),
            correlation = correlation,
            nowEpochMs = 42L
        )
        assertNotNull(r.blocking); assertNotNull(r.attribution); assertNotNull(r.hallucination)
        assertNotNull(r.temporal); assertNotNull(r.agreement); assertNotNull(r.selfContradiction)
        assertNotNull(r.coverage); assertNotNull(r.decision)
    }

    @Test fun evaluate_returns_a_valid_decision() {
        val correlation = CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = false,
            reason = com.personal.gridbot.amaros.agent.correlation.CorrelationReason.INSUFFICIENT_DATA
        )
        val r = gate().evaluate(
            answer = "X.", claims = listOf("X."), report = report(claim("X.")),
            findings = listOf(finding("https://a.example/x", "x evidence")),
            correlation = correlation, nowEpochMs = 42L
        )
        assertTrue(r.decision.decision == FinalDecision.PASS || r.decision.decision == FinalDecision.BLOCK)
    }

    @Test fun evaluate_invokes_item3_evidence_gate() {
        val correlation = CrossSourceCorrelationResult(
            correlatedGroups = emptyList(),
            isDownstreamReady = false,
            reason = com.personal.gridbot.amaros.agent.correlation.CorrelationReason.INSUFFICIENT_DATA
        )
        val r = gate().evaluate(
            answer = "X.", claims = listOf("X."), report = report(claim("X.")),
            findings = listOf(finding("https://a.example/x", "x evidence")),
            correlation = correlation, nowEpochMs = 42L
        )
        assertNotNull(r.evidence)
        assertEquals(1, r.evidence.findingsCount)
        assertTrue(r.evidence.evaluatedAtEpochMs > 0L)
    }
}
