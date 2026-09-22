package com.personal.gridbot.amaros.agent.lifecycle

enum class LifecycleReason {
    VALID_LIFECYCLE,
    NO_BASELINE_AVAILABLE,
    INVALID_INPUT,
    MISSING_PROVENANCE,
    TRANSITION_FAILED
}
