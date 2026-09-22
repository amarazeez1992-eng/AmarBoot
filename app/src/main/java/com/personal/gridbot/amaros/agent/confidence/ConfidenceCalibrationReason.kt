package com.personal.gridbot.amaros.agent.confidence

enum class ConfidenceCalibrationReason {
    VALID_INPUT,
    MISSING_RAW_CONFIDENCE,
    MISSING_EVIDENCE_QUALITY,
    MISSING_CLAIM_VERIFICATION,
    INVALID_INPUT,
    INVALID_UPSTREAM_STATE,
    CALIBRATION_FAILED
}
