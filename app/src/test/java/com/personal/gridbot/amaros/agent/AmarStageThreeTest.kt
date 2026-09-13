package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarStageThreeTest {
    @Test
    fun memory_is_bounded_and_deduplicated() {
        val memory = AmarAgentMemoryStore(capacity = 2)
        val now = 1_000L
        memory.remember(record("a", "gold trend", now, now + 10_000))
        memory.remember(record("a", "gold trend updated", now + 1, now + 10_001))
        memory.remember(record("b", "silver trend", now + 2, now + 10_002))
        memory.remember(record("c", "oil trend", now + 3, now + 10_003))

        assertEquals(2, memory.size(now + 4))
        assertEquals(setOf("b", "c"), memory.snapshot(now + 4).map { it.key }.toSet())
    }

    @Test
    fun expired_memory_is_removed_before_retrieval() {
        val memory = AmarAgentMemoryStore()
        memory.remember(record("expired", "gold trend", 0L, 100L))
        assertTrue(memory.search("gold", 100L).isEmpty())
        assertEquals(0, memory.size(100L))
    }

    @Test
    fun stage_three_merges_new_and_memory_evidence_without_duplicates() {
        val memory = AmarAgentMemoryStore()
        val engine = AmarStageThreeEngine(memory, maxRetrievedMemories = 8, maxRetrievedEvidence = 20)
        val finding = ResearchFinding("source", "https://a.example", "gold trend is rising", authority = Authority.PRIMARY)

        val first = engine.synchronize("gold trend", listOf(finding), nowEpochMs = 1_000L)
        val second = engine.synchronize("gold trend", emptyList(), nowEpochMs = 1_500L)

        assertEquals(0, first.retrievedMemoryCount)
        assertEquals(1, first.unifiedEvidence.size)
        assertEquals(1, second.retrievedMemoryCount)
        assertEquals(1, second.unifiedEvidence.size)
        assertEquals(1, second.independentSourceCount)
    }

    @Test
    fun unrelated_memory_is_not_retrieved() {
        val memory = AmarAgentMemoryStore()
        memory.remember(record("silver", "silver mining", 1_000L, 9_000L))
        assertTrue(memory.search("gold market", 2_000L).isEmpty())
    }

    private fun record(key: String, content: String, created: Long, expires: Long) = AmarMemoryRecord(
        key = key,
        query = content,
        content = content,
        sourceUri = "https://$key.example",
        sourceTitle = key,
        fingerprint = key,
        createdAtEpochMs = created,
        expiresAtEpochMs = expires
    )
}
