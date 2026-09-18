package com.personal.gridbot.amaros.agent

/**
 * Canonical Stage 11 / Item 3 / Point 10 adapter.
 *
 * Points 1-9 own the verification booleans. This class assembles those established
 * states and delegates the actual binary certification decision to the canonical
 * AmarEvidenceQualityScoreEngine. It contains no independent scoring methodology.
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
 * Wiring adapter only: all Point 1-9 states must pass before the canonical
 * Point 10 score engine is allowed to certify the evidence item.
 */
class AmarEvidenceQualityCertification(
    private val scoreEngine: AmarEvidenceQualityScoreEngine = AmarEvidenceQualityScoreEngine()
) {
    fun certify(input: AmarEvidenceQualityCertificationInput): Double {
        if (!input.upstreamGatesPass() || !input.finiteAuditFields()) return 0.0

        val item = AmarEvidenceQualityItem(
            fingerprint = "canonical-upstream-state",
            authorityScore = input.authorityScore,
            freshnessScore = input.freshnessScore,
            independentSource = input.sourceIndependenceVerified,
            uniqueEvidence = input.uniquenessVerified,
            authorityVerified = input.authorityVerified,
            freshnessVerified = input.freshnessVerified
        )
        return scoreEngine.score(item)
    }

    fun certifyAll(inputs: List<AmarEvidenceQualityCertificationInput>): Double =
        if (inputs.isNotEmpty() && inputs.all { certify(it) == 1.0 }) 1.0 else 0.0
}
