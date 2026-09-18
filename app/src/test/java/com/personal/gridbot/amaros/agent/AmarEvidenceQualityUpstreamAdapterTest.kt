package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarEvidenceQualityUpstreamAdapterTest {
    private val adapter = AmarEvidenceQualityUpstreamAdapter()

    @Test
    fun canonical_bridge_consumes_upstream_states_without_recomputing_them() {
        val state = AmarEvidenceQualityUpstreamState(
            fingerprint = "upstream-fingerprint",
            authorityScore = 0.42,
            freshnessScore = 0.33,
            authorityVerified = true,
            freshnessVerified = true,
            independentSource = true,
            uniqueEvidence = true
        )
        assertEquals(1.0, adapter.certifyOne(state), 0.0)
    }

    @Test
    fun any_failed_upstream_state_blocks_canonical_certification() {
        val state = AmarEvidenceQualityUpstreamState(
            fingerprint = "x",
            authorityScore = 1.0,
            freshnessScore = 1.0,
            authorityVerified = true,
            freshnessVerified = true,
            independentSource = false,
            uniqueEvidence = true
        )
        assertEquals(0.0, adapter.certifyOne(state), 0.0)
    }

    @Test
    fun aggregate_requires_non_empty_all_verified_upstream_states() {
        val passing = AmarEvidenceQualityUpstreamState("a", 1.0, 1.0, true, true, true, true)
        val failing = passing.copy(fingerprint = "b", uniqueEvidence = false)
        assertEquals(1.0, adapter.certify(listOf(passing)), 0.0)
        assertEquals(0.0, adapter.certify(listOf(passing, failing)), 0.0)
        assertEquals(0.0, adapter.certify(emptyList()), 0.0)
    }
}
