package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 — Evidence Authority (Facade).
 *
 * Single entry point for evidence evaluation. Delegates to existing
 * Evidence Engine components without duplicating their logic.
 * Performs no retrieval, no ranking decisions, no trade execution.
 */
class AmarEvidenceAuthority(
    private val sourceVerifier: AmarSourceVerifier = AmarSourceVerifier(),
    private val duplicateDetector: AmarDuplicateEvidenceDetector = AmarDuplicateEvidenceDetector(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    data class EvidenceAuthorityReport(
        val verification: AmarSourceVerification,
        val duplicates: AmarDuplicateEvidenceReport,
        val findingsCount: Int,
        val accepted: Boolean,
        val rationale: String,
        val evaluatedAtEpochMs: Long
    )

    fun evaluate(findings: List<ResearchFinding>): EvidenceAuthorityReport {
        val now = clock()
        val verification = sourceVerifier.verify(findings)
        val duplicates = duplicateDetector.detect(findings)
        val accepted = verification.accepted && !duplicates.hasDuplicates
        val rationale = "verification=${verification.accepted}; duplicates=${duplicates.duplicateGroupCount}; sources=${verification.totalSources}"
        return EvidenceAuthorityReport(
            verification = verification,
            duplicates = duplicates,
            findingsCount = findings.size,
            accepted = accepted,
            rationale = rationale,
            evaluatedAtEpochMs = now
        )
    }

    fun isIndependent(finding: ResearchFinding, findings: List<ResearchFinding>): Boolean =
        sourceVerifier.isIndependent(finding, findings)

    fun authorityVerified(findings: List<ResearchFinding>): Boolean =
        sourceVerifier.authorityVerified(findings)

    fun authorityScore(authority: Authority): Double =
        sourceVerifier.authorityScore(authority)
}
