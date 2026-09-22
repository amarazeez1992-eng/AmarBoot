package com.personal.gridbot.amaros.agent.confidence

interface AmarConfidenceCalibrationContract {
    fun calibrate(input: ConfidenceCalibrationInput): ConfidenceCalibrationResult
}
