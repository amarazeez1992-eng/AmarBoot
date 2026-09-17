package com.personal.gridbot.amaros.agent

/** Point 6 boundary: detects duplicate evidence by normalized content only. */
class AmarDuplicateEvidenceDetector {
    fun detect(findings: List<ResearchFinding>): AmarDuplicateEvidenceReport {
        val groups = findings
            .filter { it.evidence.isNotBlank() }
            .groupBy { normalize(it.evidence) }
            .filterKeys { it.isNotEmpty() && findings.count { finding -> finding.evidence.isNotBlank() && normalize(finding.evidence) == it } > 1 }

        val duplicateIndexes = groups.values.flatten().mapNotNull { finding -> findings.indexOf(finding).takeIf { it >= 0 } }.toSet()
        return AmarDuplicateEvidenceReport(
            duplicateGroupCount = groups.size,
            duplicateFindingCount = duplicateIndexes.size,
            duplicateGroups = groups.values.map { it.toList() }
        )
    }

    private fun normalize(evidence: String): String = evidence
        .trim()
        .lowercase()
        .replace(Regex("\\s+"), " ")
}

data class AmarDuplicateEvidenceReport(
    val duplicateGroupCount: Int,
    val duplicateFindingCount: Int,
    val duplicateGroups: List<List<ResearchFinding>>
) {
    val hasDuplicates: Boolean get() = duplicateGroupCount > 0
}
