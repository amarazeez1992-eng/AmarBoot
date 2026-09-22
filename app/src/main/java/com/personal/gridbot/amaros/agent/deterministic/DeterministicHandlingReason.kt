package com.personal.gridbot.amaros.agent.deterministic

enum class DeterministicHandlingReason {
    CANONICAL_ORDER_APPLIED,
    DUPLICATE_CANONICAL_KEY,
    INVALID_FINGERPRINT,
    MISSING_EVIDENCE,
    INVALID_INPUT
}
