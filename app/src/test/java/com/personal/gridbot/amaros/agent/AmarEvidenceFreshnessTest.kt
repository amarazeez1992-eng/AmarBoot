package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceFreshnessTest {
    private val analyzer = AmarEvidenceFreshnessAnalyzer(freshnessWindowMs = 1_000L)

    @Test fun evidence_inside_window_is_fresh_and_full_score() {
        val result = analyzer.assess(retrievedAtEpochMs = 9_500L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.FRESH, result.status)
        assertEquals(500L, result.ageMs)
        assertEquals(1.0, result.score, 0.0)
    }

    @Test fun evidence_outside_window_is_stale_and_score_decays_deterministically() {
        val result = analyzer.assess(retrievedAtEpochMs = 8_000L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.STALE, result.status)
        assertEquals(2_000L, result.ageMs)
        assertEquals(0.5, result.score, 0.0)
    }

    @Test fun future_dated_evidence_is_rejected_as_future() {
        val result = analyzer.assess(retrievedAtEpochMs = 10_001L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.FUTURE, result.status)
        assertEquals(0.0, result.score, 0.0)
        assertTrue(result.ageMs < 0)
    }

    @Test fun repeated_evaluation_is_deterministic() {
        val first = analyzer.assess(7_000L, 10_000L)
        val second = analyzer.assess(7_000L, 10_000L)
        assertEquals(first, second)
    }

    @Test fun age_exactly_at_window_boundary_is_fresh() {
        val result = analyzer.assess(retrievedAtEpochMs = 9_000L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.FRESH, result.status)
        assertEquals(1_000L, result.ageMs)
        assertEquals(1.0, result.score, 0.0)
    }

    @Test fun age_just_beyond_window_is_stale() {
        val result = analyzer.assess(retrievedAtEpochMs = 8_999L, nowEpochMs = 10_000L)
        assertEquals(FreshnessStatus.STALE, result.status)
        assertEquals(1_001L, result.ageMs)
    }

    @Test fun score_never_negative_for_stale() {
        val result = analyzer.assess(retrievedAtEpochMs = 0L, nowEpochMs = 10_000_000L)
        assertTrue(result.score >= 0.0)
    }

    @Test fun score_never_exceeds_one() {
        val result = analyzer.assess(retrievedAtEpochMs = 9_999L, nowEpochMs = 10_000L)
        assertTrue(result.score <= 1.0)
    }

    @Test fun age_ms_positive_for_past_evidence() {
        val result = analyzer.assess(retrievedAtEpochMs = 5_000L, nowEpochMs = 10_000L)
        assertTrue(result.ageMs > 0L)
    }

    @Test fun future_evidence_score_is_zero_regardless_of_age() {
        val result = analyzer.assess(retrievedAtEpochMs = 20_000L, nowEpochMs = 10_000L)
        assertEquals(0.0, result.score, 0.0)
    }

    @Test fun score_decays_monotonically_as_age_grows() {
        val a = analyzer.assess(9_500L, 10_000L).score
        val b = analyzer.assess(9_000L, 10_000L).score
        val c = analyzer.assess(5_000L, 10_000L).score
        assertTrue(a >= b)
        assertTrue(b >= c)
    }

    @Test fun huge_age_produces_zero_or_close_score() {
        val result = analyzer.assess(0L, 10_000_000_000L)
        assertTrue(result.score >= 0.0)
        assertEquals(FreshnessStatus.STALE, result.status)
    }

    @Test fun analyzer_rejects_non_positive_window() {
        assertTrue(runCatching { AmarEvidenceFreshnessAnalyzer(freshnessWindowMs = 0L) }.isFailure)
        assertTrue(runCatching { AmarEvidenceFreshnessAnalyzer(freshnessWindowMs = -1L) }.isFailure)
    }

    @Test fun analyze_rejects_negative_now() {
        assertTrue(runCatching { analyzer.assess(1_000L, -1L) }.isFailure)
    }
}
