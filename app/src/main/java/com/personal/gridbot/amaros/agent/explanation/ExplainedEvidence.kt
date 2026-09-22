package com.personal.gridbot.amaros.agent.explanation

import com.personal.gridbot.amaros.agent.ranking.RankedEvidence

data class ExplainedEvidence(
    val evidence: RankedEvidence,
    val explanation: EvidenceExplanation?,
    val explanationReason: RejectionReason?
) {
    init {
        require((explanation == null) xor (explanationReason == null)) {
            "exactly one of explanation or explanationReason must be present"
        }
    }
}

enum class RejectionReason {
    MISSING_EVIDENCE,
    INVALID_EVIDENCE,
    MISSING_SOURCE,
    MISSING_STATUS,
    UNSUPPORTED_LANGUAGE,
    EXPLANATION_GENERATION_FAILED
}
