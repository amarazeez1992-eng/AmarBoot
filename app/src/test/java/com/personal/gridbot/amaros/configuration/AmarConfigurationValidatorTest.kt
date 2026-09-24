package com.personal.gridbot.amaros.configuration

import org.junit.Assert.assertThrows
import org.junit.Test

class AmarConfigurationValidatorTest {
    @Test
    fun valid_bounds_pass() {
        AmarConfigurationValidator.validate(
            maxResearchSources = 10_000,
            minimumEvidenceConfidence = 1.0
        )
    }

    @Test
    fun max_research_sources_zero_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            AmarConfigurationValidator.validate(0, 0.5)
        }
    }

    @Test
    fun max_research_sources_too_high_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            AmarConfigurationValidator.validate(20_000, 0.5)
        }
    }

    @Test
    fun minimum_evidence_confidence_below_zero_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            AmarConfigurationValidator.validate(100, -0.1)
        }
    }

    @Test
    fun minimum_evidence_confidence_above_one_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            AmarConfigurationValidator.validate(100, 1.5)
        }
    }
}
