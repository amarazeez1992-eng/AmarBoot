package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10.
 *
 * Strict verification gate. No heuristic weights, estimates, or partial
 * confidence are introduced here.
 *
 * Score = 1.0 only when every upstream evidence-quality condition is itself
 * fully verified. Otherwise the result is 0.0 (not verified).
 */
class AmarEvidenceQualityScoreEngine {
    fun score(item: AmarEvidenceQualityItem): Double =
        if (isFullyVerified(item)) 1.0 else 0.0

    fun isFullyVerified(item: AmarEvidenceQualityItem): Boolean =
        item.authorityScore == 1.0 &&
            item.freshnessScore == 1.0 &&
            item.independentSource &&
            item.uniqueEvidence

    fun aggregate(items: List<AmarEvidenceQualityItem>): Double =
        if (items.isNotEmpty() && items.all(::isFullyVerified)) 1.0 else 0.0
}
