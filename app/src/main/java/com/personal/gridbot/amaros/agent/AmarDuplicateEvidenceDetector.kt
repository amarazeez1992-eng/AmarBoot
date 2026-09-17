package com.personal.gridbot.amaros.agent

/** Point 6 boundary: detects duplicate evidence by normalized content only. */
class AmarDuplicateEvidenceDetector {
    fun detect(findings: List<ResearchFinding>): AmarDuplicateEvidenceReport {
        val indexed = findings.withIndex()
            .filter { it.value.evidence.isNotBlank() }
            .groupBy { normalize(it.value.evidence) }
            .filterKeys { it.isNotEmpty() }
            .filterValues { it.size > 1 }

        return AmarDuplicateEvidenceReport(
            duplicateGroupCount = indexed.size,
            duplicateFindingCount = indexed.values.sumOf { it.size },
            duplicateGroups = indexed.values.map { entries -> entries.map { it.value } }
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
