package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarInMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource
import com.personal.gridbot.amaros.agent.memory.AmarMemoryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarMemoryConflictDetectorTest {

    private fun entry(
        id: String, text: String,
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
        return AmarMemoryConflictDetector(repo, clock = { 42L })
    }

    // ─── Addition 1: Detection ───
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

    // ─── Addition 2: Source Comparison ───
    @Test fun userSourceBeatsSystemSource() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", source = AmarMemorySource.SYSTEM, updated = 3_000L),
            entry("b", "gold rose sharply today", source = AmarMemorySource.USER, updated = 1_000L)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    // ─── Addition 3: Timestamp Comparison ───
    @Test fun newestWinsWhenNoHigherAuthority() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 9_000L)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    // ─── Addition 4: Authority Comparison ───
    @Test fun permanentBeatsNonPermanent() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 9_000L, permanent = false),
            entry("b", "gold rose sharply today", updated = 1_000L, permanent = true)
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    // ─── Addition 5: Confidence Comparison ───
    @Test fun richerTagsWin() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 5_000L, tags = emptyList()),
            entry("b", "gold rose sharply today", updated = 1_000L, tags = listOf("verified", "audited"))
        )
        assertEquals("b", d.scan().first().winnerId)
    }

    // ─── Addition 6: Resolution State ───
    @Test fun resolutionStateIsSet() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 5_000L)
        )
        val c = d.scan().first()
        assertTrue(c.resolution != AmarMemoryConflictDetector.ResolutionState.NO_CONFLICT)
        assertNotNull(c.winnerId)
    }

    // ─── Addition 7: Audit Trail ───
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

    // ─── Addition 8: Severity Levels ───
    @Test fun severityCriticalWhenSourceDiffers() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", source = AmarMemorySource.SYSTEM, updated = 1_000L),
            entry("b", "gold rose sharply today", source = AmarMemorySource.USER, updated = 1_000L)
        )
        assertEquals(AmarMemoryConflictDetector.ConflictSeverity.CRITICAL, d.scan().first().severity)
    }

    @Test fun severityHighWhenAuthorityDiffers() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L, permanent = false),
            entry("b", "gold rose sharply today", updated = 1_000L, permanent = true)
        )
        assertEquals(AmarMemoryConflictDetector.ConflictSeverity.HIGH, d.scan().first().severity)
    }

    @Test fun severityMediumWhenConfidenceDiffers() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L, tags = emptyList()),
            entry("b", "gold rose sharply today", updated = 1_000L, tags = listOf("verified", "audited"))
        )
        assertEquals(AmarMemoryConflictDetector.ConflictSeverity.MEDIUM, d.scan().first().severity)
    }

    @Test fun severityLowWhenOnlyTimeDiffers() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 5_000L)
        )
        assertEquals(AmarMemoryConflictDetector.ConflictSeverity.LOW, d.scan().first().severity)
    }

    // ─── Addition 9: Grouped Conflicts ───
    @Test fun groupedConflictsMergeChains() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L),
            entry("b", "gold rose sharply today", updated = 2_000L),
            entry("c", "gold rose sharply today", updated = 3_000L)
        )
        val groups = d.scanGrouped()
        assertEquals(1, groups.size)
        assertEquals(listOf("a", "b", "c"), groups.first().memberIds)
    }

    @Test fun groupedConflictsEmptyWhenNoConflicts() {
        val d = detectorWith(
            entry("a", "gold rose today strongly", updated = 1_000L),
            entry("b", "weather is sunny outside", updated = 2_000L)
        )
        assertEquals(0, d.scanGrouped().size)
    }

    // ─── Addition 10: Persistent Audit Sink ───
    @Test fun auditSinkReceivesConflicts() {
        val sink = object : AmarConflictAuditSink {
            val received = mutableListOf<AmarMemoryConflictDetector.Conflict>()
            override fun onConflictDetected(conflict: AmarMemoryConflictDetector.Conflict) { received += conflict }
        }
        val repo = AmarInMemoryRepository()
        repo.save(entry("a", "gold rose sharply today", updated = 1_000L))
        repo.save(entry("b", "gold rose sharply today", updated = 5_000L))
        val d = AmarMemoryConflictDetector(repo, clock = { 42L }, auditSink = sink)
        d.scan()
        assertEquals(1, sink.received.size)
    }

    @Test fun auditSinkReceivesEscalations() {
        val sink = object : AmarConflictAuditSink {
            val received = mutableListOf<AmarMemoryConflictDetector.Escalation>()
            override fun onEscalation(escalation: AmarMemoryConflictDetector.Escalation) { received += escalation }
        }
        val repo = AmarInMemoryRepository()
        repo.save(entry("a", "gold rose sharply today", updated = 1_000L, permanent = true))
        repo.save(entry("b", "gold rose sharply today", updated = 5_000L, permanent = true))
        val d = AmarMemoryConflictDetector(repo, clock = { 42L }, auditSink = sink)
        d.scan()
        assertEquals(1, sink.received.size)
    }

    // ─── Addition 11: Escalation ───
    @Test fun escalationWhenBothPermanent() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L, permanent = true),
            entry("b", "gold rose sharply today", updated = 5_000L, permanent = true)
        )
        val c = d.scan().first()
        assertEquals(AmarMemoryConflictDetector.ResolutionState.ESCALATED, c.resolution)
        assertNull(c.winnerId)
    }

    @Test fun escalationsListPopulated() {
        val d = detectorWith(
            entry("a", "gold rose sharply today", updated = 1_000L, permanent = true),
            entry("b", "gold rose sharply today", updated = 5_000L, permanent = true)
        )
        d.scan()
        assertEquals(1, d.escalations().size)
    }

    // ─── Guards ───
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
        val d = detectorWith(entry("a", "a b c"), entry("b", "a b c"))
        assertEquals(0, d.scan().size)
    }

    @Test fun scanRejectsNonPositiveLimit() {
        val d = detectorWith()
        assertTrue(runCatching { d.scan(0) }.isFailure)
    }
}
