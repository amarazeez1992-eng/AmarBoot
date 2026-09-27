package com.personal.gridbot.amaros.agent

/**
 * Stage 11 / Item 3 — Evidence Gate (unified entry point).
 * Delegates to existing Evidence components. Never duplicates logic.
 */
class AmarEvidenceGate(
    private val sourceVerifier: AmarSourceVerifier = AmarSourceVerifier(),
    private val duplicateDetector: AmarDuplicateEvidenceDetector = AmarDuplicateEvidenceDetector(),
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    data class Report(
        val verification: AmarSourceVerification,
        val duplicates: AmarDuplicateEvidenceReport,
        val findingsCount: Int,
        val accepted: Boolean,
        val rationale: String,
        val evaluatedAtEpochMs: Long
    )

    fun evaluate(findings: List<ResearchFinding>): Report {
        val verification = sourceVerifier.verify(findings)
        val duplicates = duplicateDetector.detect(findings)
        val accepted = verification.accepted && !duplicates.hasDuplicates
        return Report(
            verification = verification,
            duplicates = duplicates,
            findingsCount = findings.size,
            accepted = accepted,
            rationale = "verification=${verification.accepted}; duplicates=${duplicates.duplicateGroupCount}; sources=${verification.totalSources}",
            evaluatedAtEpochMs = clock()
        )
    }

    fun isIndependent(finding: ResearchFinding, findings: List<ResearchFinding>): Boolean =
        sourceVerifier.isIndependent(finding, findings)

    fun authorityVerified(findings: List<ResearchFinding>): Boolean =
        sourceVerifier.authorityVerified(findings)

    fun authorityScore(authority: Authority): Double =
        sourceVerifier.authorityScore(authority)
}
