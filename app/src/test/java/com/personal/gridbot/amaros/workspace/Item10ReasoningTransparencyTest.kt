package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10ReasoningTransparencyTest {
    @Test
    fun progressShowsSafeStagesAndEngineDialogue() {
        val trace = AmarReasoningTransparency()
        val controller = AmarReasoningProgressController(trace)
        controller.start()
        controller.plan("Select research and evidence engines", 120)
        controller.queryEngine("research", "Ask for independently sourced findings", 900)
        trace.engineDialogue("research", "research request", "2 supported findings", 2100, listOf("e1", "e2"))
        controller.checkEvidence("Validate source-backed evidence", 2600)
        controller.checkConflicts("Compare independent findings", 3000)
        controller.synthesize("Produce one supported result", 3600)
        controller.complete(4000)
        assertEquals(7, trace.snapshot().size)
        assertEquals("research", trace.engineSnapshot().single().engineId)
        assertTrue(trace.snapshot().all { it.detail.isNotBlank() })
    }

    @Test
    fun finalAnswerReportsElapsedTimeAndSelectedDetailLevel() {
        val result = AmarEvidenceFusionEngine().fuse("confirmed result", listOf(EvidenceRecord("e1", "source", "fact")), 0.99)
        val view = AmarReasoningTransparency().finalAnswer(result, AmarAnswerDetailLevel.DETAILED, 40000)
        assertEquals(40000L, view.elapsedMs)
        assertEquals(AmarAnswerDetailLevel.DETAILED, view.detailLevel)
        assertTrue(view.confirmed)
        assertFalse(view.evidence.isEmpty())
    }

    @Test
    fun blockedResultCannotBeMarkedConfirmed() {
        val result = AmarEvidenceFusionEngine().fuse("", emptyList(), 0.0)
        val view = AmarReasoningTransparency().finalAnswer(result, AmarAnswerDetailLevel.SHORT, 10)
        assertFalse(view.confirmed)
        assertTrue(view.blockers.contains("empty_answer"))
    }
}
