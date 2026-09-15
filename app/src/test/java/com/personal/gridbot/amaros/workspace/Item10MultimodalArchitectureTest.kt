package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10MultimodalArchitectureTest {
    @Test
    fun unreadableVideoFailsClosed() {
        val result = AmarFailClosedMultimodalPerception().analyzeVideo(emptyList())
        assertFalse(result.evidenceBacked)
        assertTrue("insufficient_visual_evidence" in result.blockers)
    }

    @Test
    fun usableVideoProducesEvidenceBackedAnalysisAndTimestamps() {
        val frames = listOf(
            AmarMediaFrame(100L, visibleText = "button", confidence = 0.95),
            AmarMediaFrame(200L, description = "dialog opened", confidence = 0.9)
        )
        val result = AmarFailClosedMultimodalPerception().analyzeVideo(
            listOf(AmarVideoSegment(0L, 300L, frames, transcript = "confirmed event"))
        )
        assertTrue(result.evidenceBacked)
        assertTrue(100L in result.importantTimestampsMs)
    }

    @Test
    fun screenWorkspaceRequiresPermissionAndSupportsImmediateStop() {
        val workspace = AmarScreenWorkspace()
        assertFalse(workspace.start(AmarScreenShareSession("s1", 1L, "Test", false)))
        assertTrue(workspace.start(AmarScreenShareSession("s2", 1L, "Test", true)))
        assertTrue(workspace.active())
        assertTrue(workspace.stop())
        assertFalse(workspace.active())
    }

    @Test
    fun restrictedModeRejectsUnboundedParallelSearch() {
        assertFalse(AmarWebPolicyGuard().allow(AmarWebRequest("query", AmarWebMode.RESTRICTED_SEARCH, AmarSearchDepth.PARALLEL)))
        assertTrue(AmarWebPolicyGuard().allow(AmarWebRequest("query", AmarWebMode.OPEN_SEARCH, AmarSearchDepth.PARALLEL)))
    }

    @Test
    fun emptyWebQueryIsRejectedInBothModes() {
        val guard = AmarWebPolicyGuard()
        assertFalse(guard.allow(AmarWebRequest("", AmarWebMode.RESTRICTED_SEARCH, AmarSearchDepth.FAST)))
        assertFalse(guard.allow(AmarWebRequest("", AmarWebMode.OPEN_SEARCH, AmarSearchDepth.DEEP)))
    }

    @Test
    fun multiEngineFusionUsesDistinctEvidenceAndFailsClosedWithoutEvidence() {
        val fusion = AmarEvidenceFusionEngine()
        val orchestrator = AmarMultiEngineOrchestrator(fusion)
        val result = orchestrator.fuse("answer", listOf(
            AmarEngineFinding("a", "answer", listOf(EvidenceRecord("e1", "source-a", "fact")), 0.9),
            AmarEngineFinding("b", "answer", listOf(EvidenceRecord("e1", "source-a", "fact")), 0.8)
        ))
        assertFalse(result.blocked)
        assertTrue(result.evidence.size == 1)
        val blocked = orchestrator.fuse("answer", emptyList())
        assertTrue(blocked.blocked)
    }

    @Test
    fun openSourceRegistryRejectsDuplicateIds() {
        val registry = AmarOpenSourceIntegrationRegistry()
        assertTrue(registry.register(AmarOpenSourceAdapter("engine", setOf("search"))))
        assertFalse(registry.register(AmarOpenSourceAdapter("engine", setOf("search"))))
    }

    @Test
    fun uncertaintyBlocksInvalidConfidence() {
        assertTrue(AmarUncertaintyEngine().normalize(1.2, emptyList()) == 0.0)
        assertTrue(AmarUncertaintyEngine().normalize(0.8, listOf("conflict")) == 0.0)
        assertTrue(AmarUncertaintyEngine().normalize(0.8, emptyList()) == 0.8)
    }

    @Test
    fun ninePointNineGateRequiresScoreAndCurrentEvidenceAndAllRequiredGates() {
        val gate = AmarNinePointNineQualityGate()
        assertFalse(gate.evaluate(0.989, true, true).certified)
        assertFalse(gate.evaluate(0.99, false, true).certified)
        assertFalse(gate.evaluate(0.99, true, false).certified)
        assertTrue(gate.evaluate(0.99, true, true).certified)
    }
}
