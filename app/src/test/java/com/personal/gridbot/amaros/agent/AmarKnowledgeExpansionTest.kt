package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarKnowledgeExpansionTest {
    @Test fun rejected_capability_is_stored_without_trust_promotion() {
        val mcb = AmarMCB()
        val status = mcb.discover(
            AmarMCBCapabilityCandidate(
                "x", "Unverified", "engine", "src", 0.95,
                provenanceVerified = false,
                licenseVerified = false,
                securityReviewed = false,
                deterministicTestsPassed = false,
                domainValidated = false,
                benchmarked = false,
                regressionPassed = false,
                adversarialReviewed = false
            )
        )
        assertEquals(AmarAdmissionStatus.DISCOVERED, status)
        assertTrue(mcb.knowledge().all().single().status == AmarAdmissionStatus.DISCOVERED)
    }

    @Test fun fully_validated_capability_can_be_admitted() {
        val mcb = AmarMCB()
        val status = mcb.discover(
            AmarMCBCapabilityCandidate(
                "y", "Validated", "indicator", "src", 0.95,
                provenanceVerified = true,
                licenseVerified = true,
                securityReviewed = true,
                deterministicTestsPassed = true,
                domainValidated = true,
                benchmarked = true,
                regressionPassed = true,
                adversarialReviewed = true
            )
        )
        assertEquals(AmarAdmissionStatus.ADMITTED, status)
    }

    @Test fun incomplete_admission_gate_must_fail_closed() {
        val mcb = AmarMCB()
        val status = mcb.discover(
            AmarMCBCapabilityCandidate(
                "z", "Almost Valid", "engine", "src", 0.99,
                provenanceVerified = true,
                licenseVerified = true,
                securityReviewed = true,
                deterministicTestsPassed = true,
                domainValidated = true,
                benchmarked = true,
                regressionPassed = true,
                adversarialReviewed = false
            )
        )
        assertEquals(AmarAdmissionStatus.DISCOVERED, status)
    }
}
