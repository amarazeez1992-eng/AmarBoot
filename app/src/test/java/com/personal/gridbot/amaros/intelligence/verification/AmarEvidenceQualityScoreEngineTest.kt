package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarEvidenceQualityItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceQualityScoreEngineTest {
    private val engine = AmarEvidenceQualityScoreEngine()

    @Test
    fun perfect_dimensions_produce_one() {
        val item = AmarEvidenceQualityItem("f", 1.0, 1.0, true, true)
        assertEquals(1.0, engine.score(item), 0.000001)
    }

    @Test
    fun weak_dimensions_reduce_score_deterministically() {
        val item = AmarEvidenceQualityItem("f", 0.15, 0.0, false, false)
        val score = engine.score(item)
        assertTrue(score >= 0.0 && score <= 1.0)
        assertEquals(score, engine.score(item), 0.0)
    }

    @Test
    fun invalid_dimension_ranges_are_clamped() {
        val item = AmarEvidenceQualityItem("f", 7.0, -3.0, true, true)
        assertEquals(0.925, engine.score(item), 0.000001)
    }

    @Test
    fun aggregate_is_empty_safe_and_bounded() {
        assertEquals(0.0, engine.aggregate(emptyList()), 0.0)
        val items = listOf(
            AmarEvidenceQualityItem("a", 1.0, 1.0, true, true),
            AmarEvidenceQualityItem("b", 0.0, 0.0, false, false)
        )
        val score = engine.aggregate(items)
        assertTrue(score >= 0.0 && score <= 1.0)
        assertEquals((1.0 + engine.score(items[1])) / 2.0, score, 0.000001)
    }
}
