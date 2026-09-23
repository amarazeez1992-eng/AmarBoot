package com.personal.gridbot.amaros.agent.change

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceChangeDetectionTest {
    private val detector = AmarEvidenceChangeDetector()

    private fun entry(
        fingerprint: String = "fp-1",
        source: String = "https://example.com/a",
        content: String = "content",
        retrievedAt: Long = 100L
    ) = EvidenceSnapshotEntry(fingerprint, source, content, retrievedAt)

    private fun snapshot(
        symbol: String = "XAUUSD",
        capturedAt: Long = 100L,
        entries: List<EvidenceSnapshotEntry> = listOf(entry())
    ) = EvidenceSnapshot(symbol, capturedAt, entries)

    private fun detect(
        current: EvidenceSnapshot,
        previous: EvidenceSnapshot? = snapshot(),
        now: Long = 200L
    ) = detector.detect(EvidenceChangeDetectionInput(current, previous, now))

    @Test fun detects_content_added() {
        val result = detect(snapshot(entries = listOf(entry(), entry("fp-2", "https://example.com/b", "new"))))
        assertEquals(listOf(ChangeType.CONTENT_ADDED), result.changes.map { it.changeType })
        assertTrue(result.actionFlag)
        assertTrue(result.isDownstreamReady)
    }

    @Test fun detects_content_modified() {
        val result = detect(snapshot(entries = listOf(entry(content = "changed"))))
        assertEquals(ChangeType.CONTENT_MODIFIED, result.changes.single().changeType)
    }

    @Test fun detects_content_removed() {
        val result = detect(snapshot(entries = emptyList()))
        assertEquals(ChangeType.CONTENT_REMOVED, result.changes.single().changeType)
    }

    @Test fun detects_source_changed() {
        val result = detect(snapshot(entries = listOf(entry(source = "https://example.com/b"))))
        assertEquals(ChangeType.SOURCE_CHANGED, result.changes.single().changeType)
    }

    @Test fun no_change_produces_no_action() {
        val result = detect(snapshot())
        assertTrue(result.changes.isEmpty())
        assertFalse(result.actionFlag)
        assertTrue(result.isDownstreamReady)
    }

    @Test fun missing_previous_fails_closed() {
        val result = detect(snapshot(), previous = null)
        assertFalse(result.isDownstreamReady)
        assertEquals(ChangeDetectionReason.NO_BASELINE_AVAILABLE, result.reason)
    }

    @Test fun missing_symbol_fails_closed() {
        val result = detect(snapshot(symbol = " "))
        assertFalse(result.isDownstreamReady)
        assertEquals(ChangeDetectionReason.NO_BASELINE_AVAILABLE, result.reason)
    }

    @Test fun does_not_extract_symbol_from_content() {
        val result = detect(snapshot(symbol = " ", entries = listOf(entry(content = "XAUUSD changed"))))
        assertEquals(ChangeDetectionReason.NO_BASELINE_AVAILABLE, result.reason)
    }

    @Test fun mismatched_symbol_fails_closed() {
        val result = detect(snapshot(), previous = snapshot(symbol = "EURUSD"))
        assertFalse(result.isDownstreamReady)
        assertEquals(ChangeDetectionReason.SYMBOL_MISMATCH, result.reason)
    }

    @Test fun future_current_snapshot_fails_closed() {
        val result = detect(snapshot(capturedAt = 201L))
        assertEquals(ChangeDetectionReason.INVALID_INPUT, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun future_entry_fails_closed() {
        val result = detect(snapshot(entries = listOf(entry(retrievedAt = 201L))))
        assertEquals(ChangeDetectionReason.INVALID_INPUT, result.reason)
    }

    @Test fun future_previous_snapshot_fails_closed() {
        val result = detect(snapshot(), previous = snapshot(capturedAt = 201L))
        assertEquals(ChangeDetectionReason.INVALID_INPUT, result.reason)
    }

    @Test fun duplicate_source_entries_fail_closed() {
        val result = detect(snapshot(entries = listOf(entry(), entry("fp-2", "https://example.com/a", "other"))))
        assertEquals(ChangeDetectionReason.INVALID_INPUT, result.reason)
        assertFalse(result.isDownstreamReady)
    }

    @Test fun blank_fingerprint_fails_closed() {
        val result = detect(snapshot(entries = listOf(entry(fingerprint = " "))))
        assertEquals(ChangeDetectionReason.INVALID_INPUT, result.reason)
    }

    @Test fun deterministic_for_same_input() {
        val current = snapshot(entries = listOf(entry(), entry("fp-2", "https://example.com/b", "new")))
        val first = detector.detect(EvidenceChangeDetectionInput(current, snapshot(), 200L))
        val second = detector.detect(EvidenceChangeDetectionInput(current, snapshot(), 200L))
        assertEquals(first, second)
    }
}
