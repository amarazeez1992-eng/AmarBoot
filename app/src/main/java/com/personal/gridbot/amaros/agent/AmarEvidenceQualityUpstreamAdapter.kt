package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10 canonical bridge.
 *
 * This adapter has no evidence-analysis logic. It only transports verification
 * states already established by Points 1–9 into the canonical Point 10 score engine.
 * Callers must obtain every state from its owning upstream boundary.
 */
class AmarEvidenceQualityUpstreamAdapter(
    private val scoreEngine: AmarEvidenceQualityScoreEngine = AmarEvidenceQualityScoreEngine()
) {
    fun certify(
        item: AmarEvidenceQualityItem,
        upstream: AmarEvidenceQualityUpstreamState
    ): Double = scoreEngine.score(item, upstream)

    fun certifyAll(
        items: List<AmarEvidenceQualityItem>,
        upstreamStates: List<AmarEvidenceQualityUpstreamState>
    ): Double = scoreEngine.aggregate(items, upstreamStates)
}
