package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAdvancedMemoryTest {
    private val memory = AmarAdvancedMemory()

    @Test fun classifyKind_returns_context_kind() {
        assertEquals(AmarMemoryKind.CONTEXT, memory.classifyKind("context: some context"))
    }

    @Test fun classifyKind_returns_task_kind() {
        assertEquals(AmarMemoryKind.TASK, memory.classifyKind("task: analyze gold"))
    }

    @Test fun classifyKind_returns_preference_kind() {
        assertEquals(AmarMemoryKind.PREFERENCE, memory.classifyKind("prefer concise answers"))
    }

    @Test fun classifyKind_returns_fact_kind() {
        assertEquals(AmarMemoryKind.FACT, memory.classifyKind("fact: gold rose 2%"))
    }

    @Test fun classifyKind_defaults_to_long_term() {
        assertEquals(AmarMemoryKind.LONG_TERM, memory.classifyKind("some random note"))
    }

    @Test fun lifecycle_expires_entry() {
        val entry = AmarMemoryEntry(
            id = "e1", kind = AmarMemoryKind.TASK, content = "task x",
            provenance = listOf("test"), createdAtEpochMs = 1_000L,
            expiresAtEpochMs = 2_000L
        )
        assertFalse(memory.isActive(entry, nowEpochMs = 3_000L))
        assertTrue(memory.isActive(entry, nowEpochMs = 1_500L))
    }

    @Test fun retrieval_returns_sorted_matches() {
        val entries = listOf(
            AmarMemoryEntry("a", AmarMemoryKind.FACT, "gold rose today", listOf("t"), 1_000L),
            AmarMemoryEntry("b", AmarMemoryKind.FACT, "gold fell yesterday", listOf("t"), 2_000L),
            AmarMemoryEntry("c", AmarMemoryKind.FACT, "silver is flat", listOf("t"), 3_000L)
        )
        val result = memory.retrieve(
            AmarMemoryQuery("gold", minScore = 0.0), entries
        )
        assertTrue(result.matches.isNotEmpty())
        assertTrue(result.matches.first().entry.content.contains("gold"))
    }

    @Test fun retrieval_respects_min_score() {
        val entries = listOf(
            AmarMemoryEntry("a", AmarMemoryKind.FACT, "nothing relevant", listOf("t"), 1_000L)
        )
        val result = memory.retrieve(
            AmarMemoryQuery("gold", minScore = 0.5), entries
        )
        assertEquals(0, result.matches.size)
        assertEquals("EMPTY", result.decisionState)
    }
}
