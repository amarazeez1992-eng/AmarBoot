package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerification
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarHallucinationFirewallGateTest {

    private fun gate() = AmarHallucinationFirewallGate()

    private fun claim(text: String, accepted: Boolean = true, matched: Int = 3) =
        AmarClaimVerification(text, supportingEvidence = matched, opposingEvidence = 0, matchedEvidence = matched).copy(accepted = accepted)

    private fun report(vararg claims: AmarClaimVerification) =
        AmarClaimVerificationReport(claims.toList(), accepted = claims.all { it.accepted })

    private fun finding(uri: String, evidence: String) = ResearchFinding(
        sourceTitle = "s", sourceUri = uri, evidence = evidence,
        authority = Authority.PRIMARY, stance = EvidenceStance.SUPPORTS
    )

    @Test fun evaluate_returns_all_eight_components() {
        val r = gate().evaluate(
            answer = "A well-supported factual statement.",
            claims = listOf("The market moved."),
            report = report(claim("The market moved.")),
            findings = listOf(finding("https://a.example/x", "market moved evidence")),
            correlation = null,
            nowEpochMs = 42L
        )
        assertNotNull(r.blocking); assertNotNull(r.attribution); assertNotNull(r.hallucination)
        assertNotNull(r.temporal); assertNotNull(r.agreement); assertNotNull(r.selfContradiction)
        assertNotNull(r.coverage); assertNotNull(r.decision)
    }

    @Test fun evaluate_passes_on_clean_input() {
        val r = gate().evaluate(
            answer = "The market moved today.",
            claims = listOf("The market moved today."),
            report = report(claim("The market moved today.")),
            findings = listOf(finding("https://a.example/x", "market moved evidence")),
            correlation = null,
            nowEpochMs = 42L
        )
        assertEquals(FinalDecision.PASS, r.decision.decision)
    }

    @Test fun evaluate_blocks_when_blocking_fails() {
        val r = gate().evaluate(
            answer = "Some claim.",
            claims = listOf("Some claim."),
            report = report(claim("Some claim.", accepted = false, matched = 0)),
            findings = listOf(finding("https://a.example/x", "unrelated")),
            correlation = null,
            nowEpochMs = 42L
        )
        assertEquals(FinalDecision.BLOCK, r.decision.decision)
    }

    @Test fun evaluate_handles_missing_correlation() {
        val r = gate().evaluate(
            answer = "Statement.",
            claims = listOf("Statement."),
            report = report(claim("Statement.")),
            findings = listOf(finding("https://a.example/x", "statement evidence")),
            correlation = null,
            nowEpochMs = 42L
        )
        assertNotNull(r.agreement)
    }

    @Test fun evaluate_handles_empty_findings() {
        val r = gate().evaluate(
            answer = "Statement.",
            claims = listOf("Statement."),
            report = report(claim("Statement.")),
            findings = emptyList(),
            correlation = null,
            nowEpochMs = 42L
        )
        assertNotNull(r.attribution)
    }

    @Test fun evaluate_is_deterministic() {
        val a = gate().evaluate("Same answer.", listOf("Same answer."),
            report(claim("Same answer.")), listOf(finding("https://a.example/x", "same evidence")), null, nowEpochMs = 42L)
        val b = gate().evaluate("Same answer.", listOf("Same answer."),
            report(claim("Same answer.")), listOf(finding("https://a.example/x", "same evidence")), null, nowEpochMs = 42L)
        assertEquals(a.decision, b.decision)
    }

    @Test fun evaluate_returns_final_decision_type() {
        val r = gate().evaluate("X.", listOf("X."), report(claim("X.")),
            listOf(finding("https://a.example/x", "x evidence")), null, nowEpochMs = 42L)
        assertTrue(r.decision.decision == FinalDecision.PASS || r.decision.decision == FinalDecision.BLOCK)
    }
}
