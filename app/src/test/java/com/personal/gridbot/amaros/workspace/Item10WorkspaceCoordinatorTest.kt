package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10WorkspaceCoordinatorTest {
    private fun coordinator(): AmarWorkspaceCoordinator = AmarWorkspaceCoordinator(
        store = AmarConversationMemoryStore(),
        perception = AmarFailClosedMultimodalPerception(),
        fusion = AmarEvidenceFusionEngine(),
        qualityGate = AmarNinePointNineQualityGate(),
        audit = AmarWorkspaceAuditLog()
    )

    @Test
    fun unreadableVideoFailsClosed() {
        val result = coordinator().analyzeVideo(
            listOf(AmarVideoSegment(0L, 1000L, listOf(AmarMediaFrame(500L, confidence = 0.0))))
        )
        assertTrue(result.blocked)
        assertTrue(result.blockers.contains("no_evidence"))
    }

    @Test
    fun readableVideoProducesTraceableEvidence() {
        val result = coordinator().analyzeVideo(
            listOf(AmarVideoSegment(0L, 1000L, listOf(AmarMediaFrame(500L, description = "button visible", confidence = 0.95))))
        )
        assertFalse(result.blocked)
        assertTrue(result.evidence.single().source.startsWith("multimodal:"))
    }

    @Test
    fun qualityGateRequiresAllConditions() {
        assertTrue(coordinator().certifyQuality(0.99, true, true).certified)
        assertFalse(coordinator().certifyQuality(0.989, true, true).certified)
        assertFalse(coordinator().certifyQuality(1.0, false, true).certified)
        assertFalse(coordinator().certifyQuality(1.0, true, false).certified)
    }

    @Test
    fun screenStopIsAuditable() {
        assertTrue(coordinator().recordStop("user", 42L))
    }
}
