package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.ResearchFinding

/**
 * Stage 11 / Item 3 / Point 9.
 *
 * Verifies uniqueness of evidence records by their canonical record identity:
 * source URI + evidence payload + retrieval timestamp.
 *
 * This is intentionally different from Point 6 (semantic duplicate content)
 * and Point 7 (fingerprint integrity). It does not validate or trust the
 * supplied fingerprint field.
 */
class AmarEvidenceUniquenessAnalyzer {
    fun analyze(findings: List<ResearchFinding>): AmarEvidenceUniquenessReport {
        val eligible = findings.filter {
            it.sourceUri.isNotBlank() &&
                it.evidence.isNotBlank()
        }

        val identities = eligible.map { canonicalIdentity(it) }
        val counts = identities.groupingBy { it }.eachCount()
        val collisions = counts.filterValues { it > 1 }

        return AmarEvidenceUniquenessReport(
            totalFindings = findings.size,
            eligibleFindings = eligible.size,
            uniqueRecordCount = counts.size,
            collisionGroupCount = collisions.size,
            collidingFindingCount = collisions.values.sum(),
            uniquenessRatio = if (eligible.isEmpty()) 0.0
            else counts.size.toDouble() / eligible.size
        )
    }

    /** Point 9 composition helper: returns whether this finding's canonical record identity is unique. */
    fun isUnique(finding: ResearchFinding, findings: List<ResearchFinding>): Boolean {
        if (finding.sourceUri.isBlank() || finding.evidence.isBlank()) return false
        val identity = canonicalIdentity(finding)
        val occurrences = findings.count { candidate ->
            candidate.sourceUri.isNotBlank() &&
                candidate.evidence.isNotBlank() &&
                canonicalIdentity(candidate) == identity
        }
        return occurrences == 1
    }

    private fun canonicalIdentity(finding: ResearchFinding): String =
        AmarEvidence.fingerprintOf(
            finding.sourceUri.trim() +
                "|" +
                finding.evidence.trim() +
                "|" +
                finding.retrievedAtEpochMs
        )
}

data class AmarEvidenceUniquenessReport(
    val totalFindings: Int,
    val eligibleFindings: Int,
    val uniqueRecordCount: Int,
    val collisionGroupCount: Int,
    val collidingFindingCount: Int,
    val uniquenessRatio: Double
) {
    val unique: Boolean
        get() = eligibleFindings == totalFindings &&
            collisionGroupCount == 0
}
