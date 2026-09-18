package com.personal.gridbot.amaros.agent

/** Stage 11 / Item 3 / Point 1: deterministic validation of already-intaken evidence records. */
class AmarEvidenceIntakeValidator {
    fun validate(findings: List<ResearchFinding>): AmarEvidenceIntakeReport {
        val invalid = findings.mapIndexedNotNull { index, finding ->
            val reasons = buildList {
                if (finding.sourceTitle.isBlank()) add("missing_source_title")
                if (finding.sourceUri.isBlank()) add("missing_source_uri")
                if (finding.evidence.isBlank()) add("missing_evidence")
                if (finding.retrievedAtEpochMs < 0L) add("invalid_retrieval_timestamp")
            }
            if (reasons.isEmpty()) null else AmarEvidenceIntakeIssue(index, reasons)
        }
        return AmarEvidenceIntakeReport(
            totalFindings = findings.size,
            validFindings = findings.size - invalid.size,
            invalidFindings = invalid.size,
            issues = invalid
        )
    }
}

data class AmarEvidenceIntakeIssue(val index: Int, val reasons: List<String>)

data class AmarEvidenceIntakeReport(
    val totalFindings: Int,
    val validFindings: Int,
    val invalidFindings: Int,
    val issues: List<AmarEvidenceIntakeIssue>
) {
    val accepted: Boolean get() = invalidFindings == 0
}
