package com.personal.gridbot.amaros.agent

/**
 * Point 1 of Stage 11 Item 3: the single structural admission boundary for evidence.
 *
 * Intake performs only structural checks. Authority, freshness, independence,
 * uniqueness, fingerprint integrity, ranking, and claim verification remain
 * downstream responsibilities of their canonical components.
 */
class AmarEvidenceIntake {
    fun admit(findings: List<ResearchFinding>): AmarEvidenceIntakeResult {
        val admitted = findings.filter(::isAdmissible)
        val rejected = findings.filterNot(::isAdmissible)
        return AmarEvidenceIntakeResult(
            admitted = admitted,
            rejected = rejected,
            receivedCount = findings.size
        )
    }

    private fun isAdmissible(finding: ResearchFinding): Boolean =
        finding.sourceUri.isNotBlank() && finding.evidence.isNotBlank()
}

data class AmarEvidenceIntakeResult(
    val admitted: List<ResearchFinding>,
    val rejected: List<ResearchFinding>,
    val receivedCount: Int
) {
    init {
        require(receivedCount >= 0)
        require(admitted.size + rejected.size == receivedCount)
    }

    val admittedCount: Int get() = admitted.size
    val rejectedCount: Int get() = rejected.size
}
