package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10.
 *
 * Strict certification gate. There are no heuristic weights and no partial
 * confidence. A record is certified at 100% only when every required
 * evidence-quality condition is fully satisfied; otherwise it is 0%.
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
