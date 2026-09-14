package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarInMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource
import com.personal.gridbot.amaros.agent.memory.AmarMemoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAdvancedMemoryTest {
    private fun entry(
        id: String,
        text: String,
        updatedAt: Long,
        tags: List<String> = emptyList(),
        type: AmarMemoryType = AmarMemoryType.FACT
    ) = AmarMemoryEntry(
        id = id,
        type = type,
        text = text,
        tags = tags,
        source = AmarMemorySource.USER,
        createdAtEpochMs = updatedAt,
        updatedAtEpochMs = updatedAt,
        permanent = true
    )

    @Test
    fun recall_ranks_relevant_memory_and_filters_unrelated_recent_records() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("relevant", "Gold risk policy uses strict position sizing", 9_000L, listOf("risk")))
        repository.save(entry("unrelated", "Weather forecast for tomorrow is clear", 10_000L))
        val memory = AmarAdvancedMemory(repository)

        val results = memory.recall("gold risk", nowEpochMs = 10_000L)

        assertEquals("relevant", results.first().entry.id)
        assertTrue(results.none { it.entry.id == "unrelated" })
        assertTrue(results.first().score in 0.0..1.0)
    }

    @Test
    fun type_filter_and_fail_closed_empty_query_are_deterministic() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("fact", "Amar uses evidence", 1_000L, type = AmarMemoryType.FACT))
        repository.save(entry("decision", "Amar uses evidence", 2_000L, type = AmarMemoryType.DECISION))
        val memory = AmarAdvancedMemory(repository)

        assertTrue(memory.recall("   ", 2_000L).isEmpty())
        assertEquals(listOf("decision"), memory.recall("evidence", 2_000L, AmarMemoryType.DECISION).map { it.entry.id })
    }

    @Test
    fun consolidation_keeps_newest_exact_duplicate_and_reports_removal() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("old", "Same project decision", 1_000L))
        repository.save(entry("new", "  Same   project decision ", 2_000L))
        repository.save(entry("other", "Different memory", 3_000L))
        val report = AmarAdvancedMemory(repository).consolidate()

        assertEquals(3, report.inspected)
        assertEquals(2, report.groups)
        assertEquals(1, report.removedDuplicates)
        assertEquals(null, repository.get("old"))
        assertNotEquals(null, repository.get("new"))
    }

    @Test
    fun snapshot_is_reproducible_and_supersession_preserves_history() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("old", "Old decision", 1_000L))
        val memory = AmarAdvancedMemory(repository)
        val replacement = entry("new", "New decision", 2_000L, type = AmarMemoryType.DECISION)

        val first = memory.snapshot()
        val superseded = memory.supersede("old", replacement)
        val second = memory.snapshot()

        assertTrue(superseded.tags.contains("supersedes:old"))
        assertNotEquals(first.digest, second.digest)
        assertNotEquals(null, repository.get("old"))
        assertNotEquals(null, repository.get("new"))
    }
}