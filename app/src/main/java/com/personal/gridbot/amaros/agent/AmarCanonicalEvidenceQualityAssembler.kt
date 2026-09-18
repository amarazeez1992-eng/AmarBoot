package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessAnalyzer
import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessReport

/**
 * Stage 11 / Item 3 composition boundary.
 *
 * This adapter connects the closed Point 2-9 contracts to the canonical Point 10
 * certification engine without reimplementing their methodologies.
 *
 * Point 10 still certifies only its defined inputs. Point 7 integrity and Point 9
 * aggregate reports remain audit facts for later boundaries; they are not silently
 * converted into extra Point 10 weights.
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

        val items = findings.mapIndexed { index, finding ->
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
            itemScores = items.map { scoreEngine.score(it) },
            items = items,
            point2UsableEvidence = findings.count { sourceQuality.assess(it).usable },
            point5Verification = sourceVerification,
            point6Duplicates = duplicates,
            point7FingerprintIntegrity = fingerprints,
            point9Uniqueness = uniqueness,
            aggregateScore = scoreEngine.aggregate(items)
        )
    }
}

data class AmarCanonicalEvidenceQualityReport(
    val itemScores: List<Double>,
    val items: List<AmarEvidenceQualityItem>,
    val point2UsableEvidence: Int,
    val point5Verification: AmarSourceVerification,
    val point6Duplicates: AmarDuplicateEvidenceReport,
    val point7FingerprintIntegrity: AmarFingerprintIntegrityReport,
    val point9Uniqueness: AmarEvidenceUniquenessReport,
    val aggregateScore: Double
)
