package com.personal.gridbot.amaros.agent.claim

import com.personal.gridbot.amaros.agent.AmarClaimVerification
import com.personal.gridbot.amaros.agent.AmarClaimVerificationEngine
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.status.EvidenceStatus

class AmarClaimVerifier(
    private val engine: AmarClaimVerificationEngine = AmarClaimVerificationEngine()
) : AmarClaimVerificationContract {

    override fun verify(input: AmarClaimVerificationInput): ClaimVerificationResult {
        val claims = input.structuredClaims ?: extractClaims(input.answer)
        if (claims.isEmpty()) return ClaimVerificationResult(emptyList(), emptyList(), false)
        val verified = mutableListOf<VerifiedClaim>()
        val rejected = mutableListOf<RejectedClaim>()

        claims.forEach { claim ->
            val legacy = engine.verify(claim.text, input.findings).claims.firstOrNull()
            val matched = input.findings.filter { finding ->
                finding.evidence.isNotBlank() &&
                    overlap(tokens(claim.text), tokens(finding.evidence)) >= 0.25
            }
            val supportingIds = matched.filter {
                it.stance.name == "SUPPORTS" || it.stance.name == "MIXED"
            }.map { it.fingerprint }
            val opposingIds = matched.filter { it.stance.name == "OPPOSES" }.map { it.fingerprint }

            val conflictFromUpstream = matched.any {
                input.upstreamStates[it.fingerprint]?.status == EvidenceStatus.CONFLICTED
            }
            val conflictFromPoint14 = matched.any {
                it.fingerprint in input.conflictAwareness.conflictingFingerprints
            }
            val state = when {
                (supportingIds.isNotEmpty() && opposingIds.isNotEmpty()) ||
                    conflictFromUpstream || conflictFromPoint14 -> ClaimVerificationState.CONFLICTED
                opposingIds.isNotEmpty() -> ClaimVerificationState.OPPOSED
                supportingIds.isNotEmpty() -> ClaimVerificationState.SUPPORTED
                matched.isNotEmpty() -> ClaimVerificationState.NEUTRAL
                else -> ClaimVerificationState.INSUFFICIENT
            }

            val explanation = explain(state, legacy, matched.size)
            val processed = VerifiedClaim(claim, state, supportingIds, opposingIds, explanation)
            if (state == ClaimVerificationState.SUPPORTED || state == ClaimVerificationState.OPPOSED) {
                verified += processed
            }
            if (state != ClaimVerificationState.SUPPORTED) {
                rejected += RejectedClaim(claim, state, explanation)
            }
        }

        val ready = claims.isNotEmpty() &&
            rejected.none {
                it.state == ClaimVerificationState.INSUFFICIENT ||
                    it.state == ClaimVerificationState.CONFLICTED
            }
        return ClaimVerificationResult(verified, rejected, ready)
    }

    private fun extractClaims(answer: String): List<StructuredClaim> =
        answer.split(Regex("""(?<=[.!?؟])\s+|\n+"""))
            .map { it.trim() }
            .filter { it.length >= 20 }
            .mapIndexed { index, text -> StructuredClaim("answer-claim-" + index, text, "", "", null) }

    private fun explain(
        state: ClaimVerificationState,
        legacy: AmarClaimVerification?,
        matchedCount: Int
    ): String = when (state) {
        ClaimVerificationState.SUPPORTED ->
            "Claim supported by matched evidence (" + (legacy?.supportingEvidence ?: matchedCount) + " supporting)."
        ClaimVerificationState.OPPOSED ->
            "Claim opposed by evidence (" + (legacy?.opposingEvidence ?: matchedCount) + " opposing)."
        ClaimVerificationState.NEUTRAL -> "Matched evidence is neutral and does not establish support."
        ClaimVerificationState.INSUFFICIENT -> "Insufficient matched evidence for this claim."
        ClaimVerificationState.CONFLICTED -> "Supporting and opposing evidence or an upstream conflict affects this claim."
    }

    private fun tokens(text: String): Set<String> =
        text.lowercase().split(Regex("""[^\p{L}\p{N}]+""")).filter { it.length >= 4 }.toSet()

    private fun overlap(a: Set<String>, b: Set<String>): Double =
        if (a.isEmpty()) 0.0 else a.intersect(b).size.toDouble() / a.size
}
