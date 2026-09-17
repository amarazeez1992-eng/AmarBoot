package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceFreshnessTest {
    private val analyzer = AmarEvidenceFreshnessAnalyzer(freshnessWindowMs = 1_000L)

    @Test
    fun evidence_inside_window_is_fresh_and_full_score() {
        val result = analyzer.assess(retrievedAtEpochMs = 9_500L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.FRESH, result.status)
        assertEquals(500L, result.ageMs)
        assertEquals(1.0, result.score, 0.0)
    }

    @Test
    fun evidence_outside_window_is_stale_and_score_decays_deterministically() {
        val result = analyzer.assess(retrievedAtEpochMs = 8_000L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.STALE, result.status)
        assertEquals(2_000L, result.ageMs)
        assertEquals(0.5, result.score, 0.0)
    }

    @Test
    fun future_dated_evidence_is_rejected_as_future() {
        val result = analyzer.assess(retrievedAtEpochMs = 10_001L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.FUTURE, result.status)
        assertEquals(0.0, result.score, 0.0)
        assertTrue(result.ageMs < 0)
    }

    @Test
    fun repeated_evaluation_is_deterministic() {
        val first = analyzer.assess(7_000L, 10_000L)
        val second = analyzer.assess(7_000L, 10_000L)
        assertEquals(first, second)
    }
}
