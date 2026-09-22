package com.personal.gridbot.amaros.agent.protection

import com.personal.gridbot.amaros.agent.claim.ClaimVerificationResult
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence
import com.personal.gridbot.amaros.agent.FreshnessStatus

data class InvalidFutureEvidenceProtectionInput(
    val evidence: List<ClassifiedEvidence>,
    val freshnessStates: Map<String, FreshnessStatus>,
    val tamperingStates: Map<String, Boolean>,
    val conflictAwareness: EvidenceConflictAwarenessResult,
    val claimVerification: ClaimVerificationResult
)

interface AmarInvalidFutureEvidenceProtectionContract {
    fun protect(input: InvalidFutureEvidenceProtectionInput): InvalidFutureEvidenceProtectionResult
}
