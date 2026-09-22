package com.personal.gridbot.amaros.agent.correlation

enum class CorrelationReason {
    VALID_CORRELATION,
    INSUFFICIENT_DATA,
    MISSING_INDEPENDENCE_STATE,
    INVALID_INPUT,
    CONFLICT_UPSTREAM
}
