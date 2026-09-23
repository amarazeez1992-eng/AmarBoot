package com.personal.gridbot.amaros.agent.change

enum class ChangeDetectionReason {
    VALID_CHANGE_DETECTION,
    NO_BASELINE_AVAILABLE,
    INVALID_INPUT,
    SYMBOL_MISMATCH,
    CHANGE_DETECTION_FAILED
}
