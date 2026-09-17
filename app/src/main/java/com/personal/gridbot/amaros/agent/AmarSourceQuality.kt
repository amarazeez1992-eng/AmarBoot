package com.personal.gridbot.amaros.agent

/**
 * Point 2 boundary: evaluates the quality of already-collected evidence.
 * It does not collect, rank, route, explain, verify claims, or make decisions.
 */
class AmarSourceQualityAnalyzer {
    fun assess(finding: ResearchFinding): AmarSourceQuality = AmarSourceQuality(
        hasSource = finding.sourceUri.isNotBlank(),
        hasEvidence = finding.evidence.isNotBlank(),
        authority = finding.authority
    )
}

data class AmarSourceQuality(
    val hasSource: Boolean,
    val hasEvidence: Boolean,
    val authority: Authority
) {
    val usable: Boolean get() = hasSource && hasEvidence && authority != Authority.UNKNOWN
}
