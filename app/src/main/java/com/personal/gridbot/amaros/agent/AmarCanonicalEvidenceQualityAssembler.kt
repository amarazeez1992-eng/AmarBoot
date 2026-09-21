package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceTamperingDetector
import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessAnalyzer
import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessReport
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationReport
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationStatus
import com.personal.gridbot.amaros.agent.admission.EvidenceIntakeResult

/**
 * Stage 11 / Item 3 composition boundary.
 *
 * This adapter connects the existing Point 1-9 owner outputs to auditable Point 10
 * inputs. It never invents upstream state and never creates a second evidence methodology.
 */
class AmarCanonicalEvidenceQualityAssembler(
    private val sourceQuality: AmarSourceQualityAnalyzer = AmarSourceQualityAnalyzer(),
    private val sourceVerifier: AmarSourceVerifier = AmarSourceVerifier(),
    private val freshness: AmarEvidenceFreshnessAnalyzer = AmarEvidenceFreshnessAnalyzer(),
    private val duplicateDetector: AmarDuplicateEvidenceDetector = AmarDuplicateEvidenceDetector(),
    private val fingerprintVerifier: AmarFingerprintIntegrityVerifier = AmarFingerprintIntegrityVerifier(),
    private val uniquenessAnalyzer: AmarEvidenceUniquenessAnalyzer = AmarEvidenceUniquenessAnalyzer(),
    private val tamperingDetector: AmarEvidenceTamperingDetector = AmarEvidenceTamperingDetector(),
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
     * Constitutional Point 10 certification from the real upstream verification boundary.
     *
     * Point 1 and Point 8 are sourced from AmarVerificationReport:
     * - Point 1: the owning verification layer's usable/invalid evidence counts.
     * - Point 8: the owning verification layer's recorded provenance chain, rechecked
     *   by AmarEvidenceTamperingDetector.
     *
     * Points 2-7 and 9 remain owned by their existing analyzers. No heuristic score is
     * introduced here; this method only composes their boolean verification states.
     */
    fun certify(
        findings: List<ResearchFinding>,
        nowEpochMs: Long,
        verification: AmarVerificationReport,
        intakeResult: EvidenceIntakeResult
    ): AmarCanonicalEvidenceQualityCertificationReport {
        require(verification.provenance.size == findings.size)

        val quality = assemble(findings, nowEpochMs)
        val point1 = intakeResult.intakeVerified
        val point2 = quality.point2UsableEvidence == findings.size && findings.isNotEmpty()
        val point3 = sourceVerifier.authorityVerified(findings)
        val point4 = quality.items.isNotEmpty() && quality.items.all { it.freshnessVerified }
        val point5 = verification.status == AmarVerificationStatus.VERIFIED && verification.sourceRegistry.independentHosts.size >= 2
        val point6 = !quality.point6Duplicates.hasDuplicates
        val point7 = quality.point7FingerprintIntegrity.intact
        val point8 = tamperingDetector.detect(findings, verification.provenance).intact
        val point9 = quality.point9Uniqueness.unique

        val upstreamState = AmarEvidenceQualityUpstreamState(
            point1EvidenceIntakeVerified = point1,
            point2SourceQualityVerified = point2,
            point3AuthorityVerified = point3,
            point4FreshnessVerified = point4,
            point5SourceIndependenceVerified = point5,
            point6DuplicateFreeVerified = point6,
            point7FingerprintIntegrityVerified = point7,
            point8TamperingIntegrityVerified = point8,
            point9EvidenceUniquenessVerified = point9
        )

        val certificationScore = scoreEngine.aggregate(
            quality.items,
            List(findings.size) { upstreamState }
        )
        return AmarCanonicalEvidenceQualityCertificationReport(
            quality = quality,
            upstreamStates = List(findings.size) { upstreamState },
            certificationScore = certificationScore
        )
    }

    /**
     * Compatibility overload for isolated contract tests and fail-closed callers.
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
