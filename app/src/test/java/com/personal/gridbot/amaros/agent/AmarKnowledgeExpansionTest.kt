package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarKnowledgeExpansionTest {
    @Test fun rejected_capability_is_stored_without_trust_promotion() {
        val mcb = AmarMCB()
        val status = mcb.discover(
            AmarCapabilityCandidate("x", "Unverified", "engine", "src", 0.95, false, false, false, false)
        )
        assertEquals(AmarAdmissionStatus.DISCOVERED, status)
        assertTrue(mcb.knowledge().all().single().status == AmarAdmissionStatus.DISCOVERED)
    }

    @Test fun fully_validated_capability_can_be_admitted() {
        val mcb = AmarMCB()
        val status = mcb.discover(
            AmarCapabilityCandidate("y", "Validated", "indicator", "src", 0.95, true, true, true, true)
        )
        assertEquals(AmarAdmissionStatus.ADMITTED, status)
    }
}
