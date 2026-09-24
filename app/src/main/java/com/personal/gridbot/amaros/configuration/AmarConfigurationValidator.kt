package com.personal.gridbot.amaros.configuration

object AmarConfigurationValidator {
    fun validate(maxResearchSources: Int, minimumEvidenceConfidence: Double) {
        require(maxResearchSources in 1..10_000) {
            "maxResearchSources out of bounds: $maxResearchSources"
        }
        require(minimumEvidenceConfidence in 0.0..1.0) {
            "minimumEvidenceConfidence out of bounds: $minimumEvidenceConfidence"
        }
    }
}
