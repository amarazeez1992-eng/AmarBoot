package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 / Point 10.
 *
 * Deterministically converts already-established evidence dimensions into
 * one bounded quality score. It does not collect evidence or perform any
 * upstream evidence-integrity operation.
 */
class AmarEvidenceQualityScoreEngine {
    fun score(item: AmarEvidenceQualityItem): Double {
        val authority = item.authorityScore.coerceIn(0.0, 1.0)
        val freshness = item.freshnessScore.coerceIn(0.0, 1.0)
        val independenceContribution = if (item.independentSource) 0.20 else 0.05
        val uniqueness = if (item.uniqueEvidence) 1.0 else 0.0

        return (
            authority * 0.45 +
                freshness * 0.20 +
                independenceContribution +
                uniqueness * 0.15
            ).coerceIn(0.0, 1.0)
    }

    fun aggregate(items: List<AmarEvidenceQualityItem>): Double =
        if (items.isEmpty()) 0.0 else items.map(::score).average().coerceIn(0.0, 1.0)
}
