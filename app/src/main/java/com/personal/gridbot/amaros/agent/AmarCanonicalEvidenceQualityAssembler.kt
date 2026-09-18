package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessAnalyzer

/**
 * Stage 11 / Item 3 composition boundary.
 *
 * This adapter connects the existing Point 2-9 analyzers to auditable Point 10
 * inputs. It never invents Point 1 or Point 8 state: those states must be
 * supplied by their owning upstream contracts before constitutional certification.
 */
class AmarCanonicalEvidenceQualityAssembler(
    private val sourceQuality: AmarSourceQualityAnalyzer = AmarSourceQualityAnalyzer(),
    private val sourceVerifier: AmarSourceVerifier = AmarSourceVerifier(),
    private val freshness: AmarEvidenceFreshnessAnalyzer = AmarEvidenceFreshnessAnalyzer(),
    private val duplicateDetector: AmarDuplicateEvidenceDetector = AmarDuplicateEvidenceDetector(),
    private val fingerprintVerifier: AmarFingerprintIntegrityVerifier = AmarFingerprintIntegrityVerifier(),
    private val uniquenessAnalyzer: AmarEvidenceUniquenessAnalyzer = AmarEvidenceUniquenessAnalyzer(),
    private val scoreEngine: AmarEvidenceQualityScoreEngine = AmarEvidenceQualityScoreEngine()
) {
    fun assemble(
        findings: List<ResearchFinding>,
        nowEpochMs: Long
    ): AmarCanonicalEvidenceQualityReport {
        require(nowEpochMs >= 0L)

        val duplicates = duplicateDetector.detect(findings)
        val fingerprints = fingerprintVerifier.verify(findings)
        val uniqueness = uniquenessAnalyzer.analyze(findings)
        val sourceVerification = sourceVerifier.verify(findings)

        val items = findings.map { finding ->
            val source = sourceQuality.assess(finding)
            val fresh = freshness.assess(finding.retrievedAtEpochMs, nowEpochMs)
            val independent = sourceVerifier.isIndependent(finding, findings)
            val unique = uniquenessAnalyzer.isUnique(finding, findings)

            AmarEvidenceQualityItem(
                fingerprint = finding.fingerprint,
                authorityScore = sourceVerifier.authorityScore(source.authority),
                freshnessScore = fresh.score,
                independentSource = independent,
                uniqueEvidence = unique,
                authorityVerified = source.usable,
                freshnessVerified = fresh.status != FreshnessStatus.FUTURE
            )
        }

        return AmarCanonicalEvidenceQualityReport(
            compatibilityItemScores = items.map { scoreEngine.score(it) },
            items = items,
            point2UsableEvidence = findings.count { sourceQuality.assess(it).usable },
            point5Verification = sourceVerification,
            point6Duplicates = duplicates,
            point7FingerprintIntegrity = fingerprints,
            point9Uniqueness = uniqueness
        )
    }

    /**
     * Constitutional Point 10 certification.
     *
     * The caller must provide one canonical upstream state for every finding.
     * Missing/mismatched state fails closed; no default-success state is created here.
     */
    fun certify(
        findings: List<ResearchFinding>,
        nowEpochMs: Long,
        upstreamStates: List<AmarEvidenceQualityUpstreamState>
    ): AmarCanonicalEvidenceQualityCertificationReport {
        val quality = assemble(findings, nowEpochMs)
        val certificationScore = scoreEngine.aggregate(quality.items, upstreamStates)
        return AmarCanonicalEvidenceQualityCertificationReport(
            quality = quality,
            upstreamStates = upstreamStates,
            certificationScore = certificationScore
        )
    }
}

data class AmarCanonicalEvidenceQualityReport(
    val compatibilityItemScores: List<Double>,
    val items: List<AmarEvidenceQualityItem>,
    val point2UsableEvidence: Int,
    val point5Verification: AmarSourceVerification,
    val point6Duplicates: AmarDuplicateEvidenceReport,
    val point7FingerprintIntegrity: AmarFingerprintIntegrityReport,
    val point9Uniqueness: AmarEvidenceUniquenessReport
)

data class AmarCanonicalEvidenceQualityCertificationReport(
    val quality: AmarCanonicalEvidenceQualityReport,
    val upstreamStates: List<AmarEvidenceQualityUpstreamState>,
    val certificationScore: Double
)
