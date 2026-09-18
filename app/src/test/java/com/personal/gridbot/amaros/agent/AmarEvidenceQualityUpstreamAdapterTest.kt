package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarEvidenceQualityUpstreamAdapterTest {
    private val adapter = AmarEvidenceQualityUpstreamAdapter()

    private fun item() = AmarEvidenceQualityItem(
        fingerprint = "upstream-fingerprint",
        authorityScore = 0.42,
        freshnessScore = 0.33,
        independentSource = true,
        uniqueEvidence = true,
        authorityVerified = true,
        freshnessVerified = true
    )

    private fun state() = AmarEvidenceQualityUpstreamState(
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
    fun canonical_bridge_delegates_verified_state_to_score_engine() {
        assertEquals(1.0, adapter.certify(item(), state()), 0.0)
    }

    @Test
    fun any_failed_upstream_state_blocks_canonical_certification() {
        val failing = state().copy(point8TamperingIntegrityVerified = false)
        assertEquals(0.0, adapter.certify(item(), failing), 0.0)
    }

    @Test
    fun aggregate_requires_matching_non_empty_items_and_states() {
        val passingItem = item()
        val passingState = state()
        val failingState = state().copy(point9EvidenceUniquenessVerified = false)
        assertEquals(1.0, adapter.certifyAll(listOf(passingItem), listOf(passingState)), 0.0)
        assertEquals(0.0, adapter.certifyAll(listOf(passingItem), listOf(failingState)), 0.0)
        assertEquals(0.0, adapter.certifyAll(emptyList(), emptyList()), 0.0)
        assertEquals(0.0, adapter.certifyAll(listOf(passingItem), emptyList()), 0.0)
    }
}
