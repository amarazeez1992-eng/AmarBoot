package com.personal.gridbot.amaros.agent.admission

data class IntakeRejectedCandidate(
    val candidate: EvidenceCandidate,
    val reason: IntakeRejectionReason,
    val details: String
)

enum class IntakeRejectionReason {
    BLANK_PROVIDER,
    BLANK_TITLE,
    BLANK_EVIDENCE,
    INVALID_URL,
    DUPLICATE_URL,
    DUPLICATE_FINGERPRINT
}
