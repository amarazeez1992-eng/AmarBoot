package com.personal.gridbot.amaros.agent.protection

enum class InvalidReason {
    FUTURE_TIMESTAMP,
    TAMPERED,
    CONFLICTED_EVIDENCE,
    CLAIM_REJECTED,
    UNKNOWN_INVALIDITY
}
