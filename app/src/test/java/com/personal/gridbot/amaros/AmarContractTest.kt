package com.personal.gridbot.amaros

import com.personal.gridbot.amaros.analytics.AmarAnalytics
import com.personal.gridbot.amaros.core.AmarEvent
import com.personal.gridbot.amaros.core.AmarEventSequence
import com.personal.gridbot.amaros.core.AmarRuntimeController
import com.personal.gridbot.amaros.memory.AmarMemoryRecord
import com.personal.gridbot.amaros.memory.InMemoryAmarMemoryRepository
import com.personal.gridbot.amaros.knowledge.AmarKnowledgeItem
import com.personal.gridbot.amaros.knowledge.InMemoryAmarKnowledgeRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarContractTest {
    @Test fun memoryRepository_isNamespaceSafeAndDeterministic() = runBlocking {
        val repo = InMemoryAmarMemoryRepository()
        repo.put(AmarMemoryRecord(namespace = "a", key = "k", value = "1", createdAtEpochMs = 1, importance = 0.5))
        repo.put(AmarMemoryRecord(namespace = "b", key = "k", value = "2", createdAtEpochMs = 2, importance = 1.0))
        assertEquals("1", repo.get("a", "k")?.value)
        assertEquals(1, repo.query("a").size)
        assertTrue(repo.delete("a", "k"))
        assertEquals(null, repo.get("a", "k"))
    }

    @Test fun knowledgeRepository_searchIsRanked() = runBlocking {
        val repo = InMemoryAmarKnowledgeRepository()
        repo.upsert(AmarKnowledgeItem(topic = "grid", title = "Grid basics", content = "grid grid", source = "test", confidence = 0.7, createdAtEpochMs = 1))
        repo.upsert(AmarKnowledgeItem(topic = "grid", title = "Grid", content = "grid", source = "test", confidence = 0.9, createdAtEpochMs = 2))
        assertEquals(2, repo.search("grid").size)
        assertEquals(0.7, repo.search("grid")[0].confidence, 0.0)
    }

    @Test fun events_haveMonotonicSequenceAndCorrelation() {
        val first = AmarEvent.SystemMessage("a")
        val second = AmarEvent.SystemMessage("b")
        assertTrue(second.sequence > first.sequence)
        assertNotEquals(first.correlationId, second.correlationId)
        assertTrue(AmarEventSequence.next() > second.sequence)
    }

    @Test fun runtimeCycle_isDemoGuardedAndAnalyticsRecordsIt() {
        val runtime = AmarRuntimeController()
        runtime.onRuntimeStarted()
        val state = runtime.advance()
        val analytics = AmarAnalytics()
        analytics.record(state)
        val snapshot = analytics.snapshot()
        assertEquals(1L, snapshot.samples)
        assertEquals(1L, snapshot.successfulCycles)
        assertEquals("DEMO_GUARDED", snapshot.executionModeCounts.keys.single())
        assertTrue(state.execution?.executed == false)
        runtime.onRuntimeStopped()
    }
}
