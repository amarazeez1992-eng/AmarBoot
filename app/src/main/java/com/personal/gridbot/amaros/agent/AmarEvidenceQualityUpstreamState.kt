package com.personal.gridbot.amaros.agent

/**
 * Canonical Point 10 input contract.
 *
 * Each flag is owned by the corresponding upstream Evidence Engine point.
 * Point 10 consumes these states; it does not recreate their methodology.
 */
data class AmarEvidenceQualityUpstreamState(
    val point1EvidenceIntakeVerified: Boolean,
    val point2SourceQualityVerified: Boolean,
    val point3AuthorityVerified: Boolean,
    val point4FreshnessVerified: Boolean,
    val point5SourceIndependenceVerified: Boolean,
    val point6DuplicateFreeVerified: Boolean,
    val point7FingerprintIntegrityVerified: Boolean,
    val point8TamperingIntegrityVerified: Boolean,
    val point9EvidenceUniquenessVerified: Boolean
) {
    val fullyVerified: Boolean
        get() = point1EvidenceIntakeVerified &&
            point2SourceQualityVerified &&
            point3AuthorityVerified &&
            point4FreshnessVerified &&
            point5SourceIndependenceVerified &&
            point6DuplicateFreeVerified &&
            point7FingerprintIntegrityVerified &&
            point8TamperingIntegrityVerified &&
            point9EvidenceUniquenessVerified
}
