package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarEvidenceQualityUpstreamStateTest {
    private fun verified() = AmarEvidenceQualityUpstreamState(
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
    fun all_upstream_points_must_be_verified() {
        assertTrue(verified().fullyVerified)
    }

    @Test
    fun every_individual_upstream_failure_blocks_certification_input() {
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
            assertFalse(mutate(verified()).fullyVerified)
        }
    }

    @Test
    fun identical_state_is_deterministic() {
        val state = verified()
        repeat(100) {
            assertTrue(state.fullyVerified)
        }
    }
}
