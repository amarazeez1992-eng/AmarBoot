package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10.
 *
 * Strict certification gate. Numeric quality values are retained for auditability,
 * but Point 10 never converts them into an invented percentage. The authoritative
 * decision is based on upstream verification states plus independence and uniqueness.
 */
class AmarEvidenceQualityScoreEngine {
    fun score(item: AmarEvidenceQualityItem): Double =
        if (isFullyVerified(item)) 1.0 else 0.0

    fun isFullyVerified(item: AmarEvidenceQualityItem): Boolean =
        item.authorityVerified &&
            item.freshnessVerified &&
            item.independentSource &&
            item.uniqueEvidence &&
            item.authorityScore.isFinite() &&
            item.authorityScore in 0.0..1.0 &&
            item.freshnessScore.isFinite() &&
            item.freshnessScore in 0.0..1.0

    fun aggregate(items: List<AmarEvidenceQualityItem>): Double =
        if (items.isNotEmpty() && items.all(::isFullyVerified)) 1.0 else 0.0
}
