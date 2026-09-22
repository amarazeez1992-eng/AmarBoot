package com.personal.gridbot.amaros.agent.confidence

data class ConfidenceCalibrationResult(
    val calibratedConfidence: Double?,
    val inputSummary: Map<String, Double>,
    val isDownstreamReady: Boolean,
    val reason: ConfidenceCalibrationReason
) {
    companion object {
        fun failed(reason: ConfidenceCalibrationReason): ConfidenceCalibrationResult =
            ConfidenceCalibrationResult(null, emptyMap(), false, reason)
    }
}
