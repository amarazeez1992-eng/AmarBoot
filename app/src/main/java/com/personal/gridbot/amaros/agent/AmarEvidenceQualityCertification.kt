package com.personal.gridbot.amaros.agent

/**
 * Canonical Stage 11 / Item 3 / Point 10 input contract.
 *
 * This is an adapter boundary, not a reimplementation of Points 1-9.
 * Each boolean must be produced by the already-owned upstream point.
 * Point 10 only certifies the supplied verified state.
 */
data class AmarEvidenceQualityCertificationInput(
    val evidenceIntakeValid: Boolean,
    val sourceQualityVerified: Boolean,
    val authorityVerified: Boolean,
    val freshnessVerified: Boolean,
    val sourceIndependenceVerified: Boolean,
    val duplicateFree: Boolean,
    val fingerprintIntegrityVerified: Boolean,
    val tamperingFree: Boolean,
    val uniquenessVerified: Boolean,
    val authorityScore: Double,
    val freshnessScore: Double
) {
    fun upstreamGatesPass(): Boolean =
        evidenceIntakeValid &&
            sourceQualityVerified &&
            authorityVerified &&
            freshnessVerified &&
            sourceIndependenceVerified &&
            duplicateFree &&
            fingerprintIntegrityVerified &&
            tamperingFree &&
            uniquenessVerified

    fun finiteAuditFields(): Boolean =
        authorityScore.isFinite() &&
            authorityScore in 0.0..1.0 &&
            freshnessScore.isFinite() &&
            freshnessScore in 0.0..1.0
}

/**
 * Small bridge used by Point 10. It intentionally contains no thresholds,
 * weights, averaging, normalization, or repair logic.
 */
class AmarEvidenceQualityCertification {
    fun certify(input: AmarEvidenceQualityCertificationInput): Double =
        if (input.upstreamGatesPass() && input.finiteAuditFields()) 1.0 else 0.0

    fun certifyAll(inputs: List<AmarEvidenceQualityCertificationInput>): Double =
        if (inputs.isNotEmpty() && inputs.all { it.upstreamGatesPass() && it.finiteAuditFields() }) 1.0 else 0.0
}
