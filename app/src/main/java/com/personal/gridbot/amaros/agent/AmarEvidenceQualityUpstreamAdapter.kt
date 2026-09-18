package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10 canonical bridge.
 *
 * This adapter has no evidence-analysis logic. It only transports verification
 * states already established by Points 1–9 into the Point 10 certification gate.
 * Callers must obtain every state from its owning upstream boundary.
 */
class AmarEvidenceQualityUpstreamAdapter(
    private val scoreEngine: AmarEvidenceQualityScoreEngine = AmarEvidenceQualityScoreEngine()
) {
    fun certify(states: List<AmarEvidenceQualityUpstreamState>): Double =
        scoreEngine.aggregate(states.map { it.toQualityItem() })

    fun certifyOne(state: AmarEvidenceQualityUpstreamState): Double =
        scoreEngine.score(state.toQualityItem())
}

data class AmarEvidenceQualityUpstreamState(
    val fingerprint: String,
    val authorityScore: Double,
    val freshnessScore: Double,
    val authorityVerified: Boolean,
    val freshnessVerified: Boolean,
    val independentSource: Boolean,
    val uniqueEvidence: Boolean
) {
    fun toQualityItem(): AmarEvidenceQualityItem = AmarEvidenceQualityItem(
        fingerprint = fingerprint,
        authorityScore = authorityScore,
        freshnessScore = freshnessScore,
        independentSource = independentSource,
        uniqueEvidence = uniqueEvidence,
        authorityVerified = authorityVerified,
        freshnessVerified = freshnessVerified
    )
}
