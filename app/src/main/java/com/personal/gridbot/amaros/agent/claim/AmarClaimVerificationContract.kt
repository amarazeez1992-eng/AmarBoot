package com.personal.gridbot.amaros.agent.claim

import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class AmarClaimVerificationInput(
    val answer: String,
    val structuredClaims: List<StructuredClaim>? = null,
    val findings: List<ResearchFinding>,
    val upstreamStates: Map<String, ClassifiedEvidence> = emptyMap(),
    val conflictAwareness: EvidenceConflictAwarenessResult
)

interface AmarClaimVerificationContract {
    fun verify(input: AmarClaimVerificationInput): ClaimVerificationResult
}
