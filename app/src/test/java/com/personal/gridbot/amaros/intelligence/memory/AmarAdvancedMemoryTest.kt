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
    private fun entry(id: String, text: String, updatedAt: Long, tags: List<String> = emptyList(), type: AmarMemoryType = AmarMemoryType.FACT) = AmarMemoryEntry(
        id = id, type = type, text = text, tags = tags, source = AmarMemorySource.USER,
        createdAtEpochMs = updatedAt, updatedAtEpochMs = updatedAt, permanent = true
    )

    @Test
    fun recall_ranks_relevant_memory_and_filters_unrelated_recent_records() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("relevant", "Gold risk policy uses strict position sizing", 9_000L, listOf("risk")))
        repository.save(entry("unrelated", "Weather forecast for tomorrow is clear", 10_000L))
        val results = AmarAdvancedMemory(repository).recall("gold risk", 10_000L)
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
    fun consolidation_keeps_newest_duplicate_within_type() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("old", "Same project decision", 1_000L, type = AmarMemoryType.FACT))
        repository.save(entry("new", "  Same   project decision ", 2_000L, type = AmarMemoryType.FACT))
        repository.save(entry("different-type", "Same project decision", 3_000L, type = AmarMemoryType.DECISION))
        repository.save(entry("other", "Different memory", 4_000L))
        val report = AmarAdvancedMemory(repository).consolidate()
        assertEquals(4, report.inspected)
        assertEquals(3, report.groups)
        assertEquals(1, report.removedDuplicates)
        assertEquals(null, repository.get("old"))
        assertNotEquals(null, repository.get("new"))
        assertNotEquals(null, repository.get("different-type"))
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

    @Test(expected = IllegalArgumentException::class)
    fun supersession_cannot_reuse_the_old_id() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("old", "Old decision", 1_000L))
        AmarAdvancedMemory(repository).supersede("old", entry("old", "Replacement", 2_000L))
    }
    @Test
    fun taskMemory_isRetrievedAsTaskType() {
        val repository = AmarInMemoryRepository()
        val memory = AmarAdvancedMemory(repository)
        memory.remember(entry("task", "Review pending trade task", 5_000L), AmarAdvancedMemoryType.TASK)
        val results = memory.recall("pending trade task", 5_000L, AmarAdvancedMemoryType.TASK)
        assertEquals(listOf("task"), results.map { it.entry.id })
        assertEquals(AmarMemoryType.PROJECT, results.first().entry.type)
        assertTrue(results.first().entry.tags.contains("adv:task"))
    }

    @Test
    fun preferenceMemory_isRetrievedAsPreferenceType() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("preference", "User prefers strict risk controls", 5_000L, type = AmarMemoryType.PREFERENCE))
        val memory = AmarAdvancedMemory(repository)
        val preferred = memory.recall("strict risk controls", 5_000L, AmarMemoryType.PREFERENCE)
        val facts = memory.recall("strict risk controls", 5_000L, AmarMemoryType.FACT)
        assertEquals(listOf("preference"), preferred.map { it.entry.id })
        assertTrue(facts.none { it.entry.id == "preference" })
        assertEquals(AmarMemoryType.PREFERENCE, preferred.first().entry.type)
    }

    @Test
    fun contextMemory_isRetrievedAsContextType() {
        val repository = AmarInMemoryRepository()
        val memory = AmarAdvancedMemory(repository)
        memory.remember(entry("context", "Current trading session context", 6_000L), AmarAdvancedMemoryType.CONTEXT)
        val results = memory.recall("trading session context", 6_000L, AmarAdvancedMemoryType.CONTEXT)
        assertEquals(listOf("context"), results.map { it.entry.id })
        assertEquals(AmarMemoryType.CONVERSATION, results.first().entry.type)
        assertTrue(results.first().entry.tags.contains("adv:context"))
    }

    @Test
    fun factMemory_isDistinctFromOtherTypes() {
        val repository = AmarInMemoryRepository()
        repository.save(entry("fact", "Amar uses evidence", 7_000L, type = AmarMemoryType.FACT))
        repository.save(entry("decision", "Amar uses evidence", 8_000L, type = AmarMemoryType.DECISION))
        val memory = AmarAdvancedMemory(repository)
        val facts = memory.recall("Amar uses evidence", 8_000L, AmarMemoryType.FACT)
        assertEquals(listOf("fact"), facts.map { it.entry.id })
        assertTrue(facts.all { it.entry.type == AmarMemoryType.FACT })
        assertTrue(facts.none { it.entry.type == AmarMemoryType.DECISION })
    }

    @Test
    fun longTermMemory_isDistinctFromFacts() {
        val repository = AmarInMemoryRepository()
        val memory = AmarAdvancedMemory(repository)
        memory.remember(entry("long-term", "Long term trading principle", 1_000L), AmarAdvancedMemoryType.LONG_TERM)
        memory.remember(entry("fact", "Long term trading principle", 2_000L), AmarAdvancedMemoryType.FACT)
        val longTerm = memory.recall("Long term trading principle", 2_000L, AmarAdvancedMemoryType.LONG_TERM)
        val facts = memory.recall("Long term trading principle", 2_000L, AmarAdvancedMemoryType.FACT)
        assertEquals(listOf("long-term"), longTerm.map { it.entry.id })
        assertEquals(listOf("fact"), facts.map { it.entry.id })
        assertTrue(longTerm.first().entry.permanent)
        assertTrue(longTerm.first().entry.tags.contains("adv:long_term"))
        assertTrue(facts.first().entry.tags.contains("adv:fact").not())
    }

    @Test
    fun allMemoryTypes_areStorable() {
        val repository = AmarInMemoryRepository()
        val memory = AmarAdvancedMemory(repository)
        AmarMemoryType.values().forEachIndexed { index, memoryType ->
            repository.save(entry("type-$index", "Memory type " + memoryType.name, 10_000L + index, type = memoryType))
        }
        AmarMemoryType.values().forEachIndexed { index, memoryType ->
            val results = memory.recall("Memory type " + memoryType.name, 20_000L, memoryType)
            assertEquals(listOf("type-$index"), results.map { it.entry.id })
            assertEquals(memoryType, results.first().entry.type)
        }
    }
}
