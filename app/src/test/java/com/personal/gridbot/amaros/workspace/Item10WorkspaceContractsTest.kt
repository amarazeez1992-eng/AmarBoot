package com.personal.gridbot.amaros.workspace

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Item10WorkspaceContractsTest {
    private val store = AmarConversationMemoryStore()

    @Test
    fun saveCommandPersistsMemoryForLaterRetrieval() {
        val memory = MemoryRecord("m1", "c1", "important project decision", "project", 1L, "conversation:c1", true)
        assertTrue(AmarMemoryCommandRouter(store).execute("احفظ هذا", memory = memory))
        assertTrue(store.memory("m1") != null)
    }

    @Test
    fun deleteCommandTombstonesMemoryAndPreventsKnowledgeReappearance() {
        val memory = MemoryRecord("m2", "c2", "remove me", "project", 1L, "conversation:c2", true)
        store.saveMemory(memory)
        assertTrue(store.promoteToKnowledge(KnowledgeRecord("k2", "remove me", "project", setOf("m2"), true)))
        assertTrue(AmarMemoryCommandRouter(store).execute("احذف هذا", memoryId = "m2"))
        assertTrue(store.memory("m2") == null)
        assertTrue(store.knowledge("k2") == null)
    }

    @Test
    fun unvalidatedKnowledgeCannotBePromoted() {
        store.saveMemory(MemoryRecord("m3", "c3", "unverified", "project", 1L, "conversation:c3", false))
        assertFalse(store.promoteToKnowledge(KnowledgeRecord("k3", "unverified", "project", setOf("m3"), false)))
        assertTrue(store.knowledge("k3") == null)
    }

    @Test
    fun emptyEvidenceFailsClosed() {
        val result = AmarEvidenceFusionEngine().fuse("answer", emptyList(), 0.8)
        assertFalse(result.blocked)
        assertTrue(result.evidence.isEmpty())
    }

    @Test
    fun invalidEvidenceIsRejected() {
        val result = AmarEvidenceFusionEngine().fuse(
            "answer",
            listOf(EvidenceRecord("e1", "", "", valid = false)),
            0.8
        )
        assertTrue(result.blocked)
        assertTrue("no_valid_evidence" in result.blockers)
    }

    @Test
    fun sensitiveActionRequiresExplicitConfirmation() {
        val gate = AmarActionSafetyGate()
        assertFalse(gate.authorize(AmarActionAuthorization(true, false, true)))
        assertTrue(gate.authorize(AmarActionAuthorization(true, true, true)))
    }

    @Test
    fun permissionIsRequiredForAnyAction() {
        assertFalse(AmarActionSafetyGate().authorize(AmarActionAuthorization(false, true, false)))
    }
}
