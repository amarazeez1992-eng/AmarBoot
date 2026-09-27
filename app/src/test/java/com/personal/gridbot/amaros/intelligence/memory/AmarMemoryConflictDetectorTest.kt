package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarInMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource
import com.personal.gridbot.amaros.agent.memory.AmarMemoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMemoryConflictDetectorTest {

    private fun entry(
        id: String,
        text: String,
        source: AmarMemorySource = AmarMemorySource.SYSTEM,
        updated: Long = 1_000L,
        permanent: Boolean = false,
        tags: List<String> = emptyList(),
        type: AmarMemoryType = AmarMemoryType.FACT
    ) = AmarMemoryEntry(
        id = id, type = type, text = text, tags = tags, source = source,
        createdAtEpochMs = updated, updatedAtEpochMs = updated, permanent = permanent
    )

    private fun detectorWith(vararg entries: AmarMemoryEntry): AmarMemoryConflictDetector {
        val repo = AmarInMemoryRepository()
        entries.forEach { repo.save(it) }
        return AmarMemoryConflictDetector(repo) { 42L }
    }

    @Test fun noConflictWhenTextsUnrelated() {
        val d = detectorWith(
            entry("a", "gold rose today strongly", updated = 1_000L),
            entry("b", "weather is sunny outside", updated = 2_000L)
        )
        assertEquals(0, d.scan().size)
    }

    @Test fun conflictWhenOverlappingSameType() {
        val d = detectorWith(
            entry("a", "gold price rose sharply in asian session", updated = 1_000L),
            entry("b", "gold price rose sharply in london session", updated = 2_000L)
        )
        assertEquals(1, d.scan().size)
    }

    @Test fun noConflictWhenDifferentTypes() {
        val d = detectorWith(
            entry("a", "gold price rose sharply", updated = 1_000L, type = AmarMemoryType.FACT),
            entry("b", "gold price rose sharply", updated = 2_000L, type = AmarMemoryType.DECISION)
        )
        assertEquals(0, d.scan().size)
    }

    @Test fun userSourceBeatsSystemSource() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", source = AmarMemorySource.SYSTEM, updated = 3_000L),
            entry("b", "gold rose sharply today", source = AmarMemorySource.USER, updated = 1_000L)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    @Test fun newestWinsWhenNoHigherAuthority() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 9_000L)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    @Test fun permanentBeatsNonPermanent() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 9_000L, permanent = false),
            entry("b", "gold rose sharply today", updated = 1_000L, permanent = true)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    @Test fun richerTagsWin() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 5_000L, tags = emptyList()),
            entry("b", "gold rose sharply today", updated = 1_000L, tags = listOf("verified", "audited"))
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    @Test fun resolutionStateIsSet() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 5_000L)
        )
        val c = d.scan().first()
        assertTrue(c.resolution != AmarMemoryConflictDetector.ResolutionState.NO_CONFLICT)
        assertNotNull(c.winnerId)
    }

    @Test fun auditTrailIsRecorded() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 5_000L)
        )
        d.scan()
        assertEquals(1, d.auditTrail().size)
        d.clearAudit()
        assertEquals(0, d.auditTrail().size)
    }

    @Test fun sameIdIsNotConflict() {
        val e = entry("a", "gold rose sharply today")
        val d = detectorWith(e)
        assertNull(d.detectBetween(e, e))
    }

    @Test fun emptyRepositoryYieldsNoConflicts() {
        val d = detectorWith()
        assertEquals(0, d.scan().size)
    }

    @Test fun shortTokensAreIgnored() {
        val d = detectorWith(
            entry("a", "a b c"),
            entry("b", "a b c")
        )
        assertEquals(0, d.scan().size)
    }

    @Test fun scanRejectsNonPositiveLimit() {
        val d = detectorWith()
        assertTrue(runCatching { d.scan(0) }.isFailure)
    }
}
