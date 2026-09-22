package com.personal.gridbot.amaros.agent.historical

enum class HistoricalValidationReason {
    VALID_COMPARABLE_CASES,
    INSUFFICIENT_COMPARABLE_CASES,
    NO_COMPARABLE_CASES,
    REGIME_MISMATCH,
    INVALID_INPUT,
    DATA_UNAVAILABLE
}
