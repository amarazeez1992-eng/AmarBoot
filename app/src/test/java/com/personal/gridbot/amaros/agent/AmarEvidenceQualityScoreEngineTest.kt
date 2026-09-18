package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceQualityScoreEngineTest {
    private val engine = AmarEvidenceQualityScoreEngine()

    @Test
    fun only_fully_verified_evidence_gets_one_hundred_percent() {
        val item = AmarEvidenceQualityItem("f", 1.0, 1.0, true, true)
        assertEquals(1.0, engine.score(item), 0.0)
        assertTrue(engine.isFullyVerified(item))
    }

    @Test
    fun any_unverified_dimension_blocks_one_hundred_percent() {
        val cases = listOf(
            AmarEvidenceQualityItem("f", 0.99, 1.0, true, true),
            AmarEvidenceQualityItem("f", 1.0, 0.99, true, true),
            AmarEvidenceQualityItem("f", 1.0, 1.0, false, true),
            AmarEvidenceQualityItem("f", 1.0, 1.0, true, false)
        )
        cases.forEach {
            assertEquals(0.0, engine.score(it), 0.0)
            assertFalse(engine.isFullyVerified(it))
        }
    }

    @Test
    fun non_finite_numeric_inputs_fail_closed() {
        val cases = listOf(
            AmarEvidenceQualityItem("nan-authority", Double.NaN, 1.0, true, true),
            AmarEvidenceQualityItem("inf-authority", Double.POSITIVE_INFINITY, 1.0, true, true),
            AmarEvidenceQualityItem("nan-freshness", 1.0, Double.NaN, true, true),
            AmarEvidenceQualityItem("inf-freshness", 1.0, Double.POSITIVE_INFINITY, true, true)
        )
        cases.forEach {
            assertEquals(0.0, engine.score(it), 0.0)
            assertFalse(engine.isFullyVerified(it))
        }
    }

    @Test
    fun aggregate_requires_every_item_to_be_fully_verified() {
        val verified = AmarEvidenceQualityItem("a", 1.0, 1.0, true, true)
        val unverified = AmarEvidenceQualityItem("b", 1.0, 1.0, true, false)
        assertEquals(1.0, engine.aggregate(listOf(verified)), 0.0)
        assertEquals(0.0, engine.aggregate(listOf(verified, unverified)), 0.0)
        assertEquals(0.0, engine.aggregate(emptyList()), 0.0)
    }

    @Test
    fun identical_inputs_are_deterministic() {
        val item = AmarEvidenceQualityItem("stable", 1.0, 1.0, true, true)
        repeat(100) {
            assertEquals(1.0, engine.score(item), 0.0)
            assertTrue(engine.isFullyVerified(item))
        }
    }

    @Test
    fun canonical_output_is_binary_only() {
        val verified = AmarEvidenceQualityItem("verified", 1.0, 1.0, true, true)
        val rejected = AmarEvidenceQualityItem("rejected", 0.5, 1.0, true, true)
        assertTrue(engine.score(verified) == 0.0 || engine.score(verified) == 1.0)
        assertTrue(engine.score(rejected) == 0.0 || engine.score(rejected) == 1.0)
        assertEquals(1.0, engine.score(verified), 0.0)
        assertEquals(0.0, engine.score(rejected), 0.0)
    }
}
