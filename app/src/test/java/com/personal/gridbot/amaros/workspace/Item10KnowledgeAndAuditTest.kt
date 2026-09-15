package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10KnowledgeAndAuditTest {
    @Test
    fun knowledgeUpdateRequiresValidatedEvidence() {
        val gate = AmarKnowledgeUpdateGate()
        val valid = AmarKnowledgeUpdate(
            KnowledgeRecord("k1", "fact", "topic", setOf("m1"), true),
            null,
            listOf(EvidenceRecord("e1", "source", "fact"))
        )
        assertTrue(gate.allow(valid))
        assertFalse(gate.allow(valid.copy(newRecord = valid.newRecord.copy(validated = false))))
        assertFalse(gate.allow(valid.copy(evidence = emptyList())))
        assertFalse(gate.allow(valid.copy(replacesId = "k1")))
    }

    @Test
    fun sensitiveControllerAuditsDeniedAndAllowedActions() {
        val audit = AmarWorkspaceAuditLog()
        val controller = AmarSensitiveActionController(audit)
        assertFalse(controller.authorize("a1", "user", true, true, false, 1L))
        assertTrue(controller.authorize("a2", "user", true, true, true, 2L))
        val events = audit.snapshot()
        assertTrue(events.size == 2)
        assertFalse(events[0].allowed)
        assertTrue(events[1].allowed)
    }
}
