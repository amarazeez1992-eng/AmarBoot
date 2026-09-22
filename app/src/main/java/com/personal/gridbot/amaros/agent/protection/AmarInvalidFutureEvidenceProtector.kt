package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.FreshnessStatus
import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.status.ConflictState

/**
 * Point 17 protection gate.
 *
 * Consumes upstream states only. It does not detect, recalculate, resolve,
 * rank, verify claims, or mutate evidence.
 */
class AmarInvalidFutureEvidenceProtector : AmarInvalidFutureEvidenceProtectionContract {

    override fun protect(input: InvalidFutureEvidenceProtectionInput): InvalidFutureEvidenceProtectionResult {
        if (input.evidence.isEmpty()) return InvalidFutureEvidenceProtectionResult.empty()

        val protected = mutableListOf<ProtectedEvidence>()
        val invalid = mutableListOf<InvalidEvidence>()
        val future = mutableListOf<FutureEvidence>()

        val globalConflicted =
            input.conflictAwareness.globalConflictState == ConflictState.CONFLICTED
        val claimRejected = input.claimVerification.rejectedClaims.isNotEmpty()

        for (evidence in input.evidence) {
            val fingerprint = evidence.candidate.candidate.fingerprint
            val freshness = input.freshnessStates[fingerprint]
            val tampering = input.tamperingStates[fingerprint]

            // Fail-closed: required freshness state must exist.
            if (freshness == null) {
                invalid += InvalidEvidence(
                    evidence = evidence,
                    reason = InvalidReason.UNKNOWN_INVALIDITY,
                    originalSource = fingerprint
                )
                continue
            }

            // Deterministic precedence:
            // FUTURE -> TAMPERED -> CONFLICTED -> CLAIM_REJECTED -> PROTECTED.
            when {
                freshness == FreshnessStatus.FUTURE -> {
                    future += FutureEvidence(
                        evidence = evidence,
                        // Point 4 derives FUTURE from retrievedAtEpochMs > nowEpochMs.
                        // Reuse that upstream timestamp; do not recalculate it here.
                        futureTimestamp = evidence.candidate.candidate.retrievedAtEpochMs,
                        reason = InvalidReason.FUTURE_TIMESTAMP.name
                    )
                }

                tampering == true -> {
                    invalid += InvalidEvidence(
                        evidence = evidence,
                        reason = InvalidReason.TAMPERED,
                        originalSource = fingerprint
                    )
                }

                globalConflicted -> {
                    invalid += InvalidEvidence(
                        evidence = evidence,
                        reason = InvalidReason.CONFLICTED_EVIDENCE,
                        originalSource = fingerprint
                    )
                }

                claimRejected -> {
                    invalid += InvalidEvidence(
                        evidence = evidence,
                        reason = InvalidReason.CLAIM_REJECTED,
                        originalSource = fingerprint
                    )
                }

                else -> {
                    protected += ProtectedEvidence(
                        evidence = evidence,
                        protectionReason = ProtectionReason.PASSED_ALL_GATES
                    )
                }
            }
        }

        return InvalidFutureEvidenceProtectionResult(
            protected = protected,
            invalid = invalid,
            future = future,
            isDownstreamReady = invalid.isEmpty() && future.isEmpty()
        )
    }
}
