package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceQualityScoreEngineTest {
    private val engine = AmarEvidenceQualityScoreEngine()

    private fun item(
        authority: Double = 1.0,
        freshness: Double = 1.0,
        independent: Boolean = true,
        unique: Boolean = true,
        authorityVerified: Boolean = true,
        freshnessVerified: Boolean = true
    ) = AmarEvidenceQualityItem(
        "f",
        authority,
        freshness,
        independent,
        unique,
        authorityVerified,
        freshnessVerified
    )

    @Test
    fun only_fully_verified_evidence_gets_one_hundred_percent() {
        val verified = item()
        assertEquals(1.0, engine.score(verified), 0.0)
        assertTrue(engine.isFullyVerified(verified))
    }

    @Test
    fun any_unverified_dimension_blocks_one_hundred_percent() {
        val cases = listOf(
            item(authority = .99, authorityVerified = false),
            item(freshness = .99, freshnessVerified = false),
            item(independent = false),
            item(unique = false)
        )
        cases.forEach {
            assertEquals(0.0, engine.score(it), 0.0)
            assertFalse(engine.isFullyVerified(it))
        }
    }

    @Test
    fun non_finite_numeric_inputs_fail_closed() {
        val cases = listOf(
            item(authority = Double.NaN),
            item(authority = Double.POSITIVE_INFINITY),
            item(freshness = Double.NaN),
            item(freshness = Double.POSITIVE_INFINITY)
        )
        cases.forEach {
            assertEquals(0.0, engine.score(it), 0.0)
            assertFalse(engine.isFullyVerified(it))
        }
    }

    @Test
    fun future_evidence_is_not_verified_even_when_numeric_freshness_is_one() {
        val future = item(freshness = 1.0, freshnessVerified = false)
        assertEquals(0.0, engine.score(future), 0.0)
        assertFalse(engine.isFullyVerified(future))
    }

    @Test
    fun aggregate_requires_every_item_to_be_fully_verified() {
        val verified = item()
        val unverified = item(unique = false)
        assertEquals(1.0, engine.aggregate(listOf(verified)), 0.0)
        assertEquals(0.0, engine.aggregate(listOf(verified, unverified)), 0.0)
        assertEquals(0.0, engine.aggregate(emptyList()), 0.0)
    }

    @Test
    fun identical_inputs_are_deterministic() {
        val stable = item()
        repeat(100) {
            assertEquals(1.0, engine.score(stable), 0.0)
            assertTrue(engine.isFullyVerified(stable))
        }
    }

    @Test
    fun canonical_output_is_binary_only() {
        val verified = item()
        val rejected = item(authority = .5, authorityVerified = false)
        assertTrue(engine.score(verified) == 0.0 || engine.score(verified) == 1.0)
        assertTrue(engine.score(rejected) == 0.0 || engine.score(rejected) == 1.0)
        assertEquals(1.0, engine.score(verified), 0.0)
        assertEquals(0.0, engine.score(rejected), 0.0)
    }

    private fun upstreamVerified() = AmarEvidenceQualityUpstreamState(
        point1EvidenceIntakeVerified = true,
        point2SourceQualityVerified = true,
        point3AuthorityVerified = true,
        point4FreshnessVerified = true,
        point5SourceIndependenceVerified = true,
        point6DuplicateFreeVerified = true,
        point7FingerprintIntegrityVerified = true,
        point8TamperingIntegrityVerified = true,
        point9EvidenceUniquenessVerified = true
    )

    @Test
    fun canonical_composition_requires_all_point_1_to_9_states() {
        val state = upstreamVerified()
        assertEquals(1.0, engine.score(item(), state), 0.0)

        val failures = listOf<(AmarEvidenceQualityUpstreamState) -> AmarEvidenceQualityUpstreamState>(
            { it.copy(point1EvidenceIntakeVerified = false) },
            { it.copy(point2SourceQualityVerified = false) },
            { it.copy(point3AuthorityVerified = false) },
            { it.copy(point4FreshnessVerified = false) },
            { it.copy(point5SourceIndependenceVerified = false) },
            { it.copy(point6DuplicateFreeVerified = false) },
            { it.copy(point7FingerprintIntegrityVerified = false) },
            { it.copy(point8TamperingIntegrityVerified = false) },
            { it.copy(point9EvidenceUniquenessVerified = false) }
        )

        failures.forEach { mutate ->
            assertEquals(0.0, engine.score(item(), mutate(state)), 0.0)
        }
    }

    @Test
    fun canonical_aggregate_fails_on_empty_or_mismatched_upstream_state() {
        val state = upstreamVerified()
        assertEquals(0.0, engine.aggregate(emptyList(), emptyList()), 0.0)
        assertEquals(0.0, engine.aggregate(listOf(item()), emptyList()), 0.0)
        assertEquals(1.0, engine.aggregate(listOf(item()), listOf(state)), 0.0)
    }

}
