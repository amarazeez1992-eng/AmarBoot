package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarEvidenceUniquenessAnalyzer
import java.net.URI

/**
 * Compatibility hardening boundary retained for previously closed Stage 2 consumers.
 *
 * Constitutional Point 10 certification is performed by AmarEvidenceQualityScoreEngine.
 * This class remains an upstream/advisory adapter and must not invent a second
 * Point 10 methodology.
 */
class AmarEvidenceQualityEngine(
    private val freshnessWindowMs: Long = 30L * 24L * 60L * 60L * 1000L
) {
    private val freshnessAnalyzer = AmarEvidenceFreshnessAnalyzer(freshnessWindowMs)

    init { require(freshnessWindowMs > 0) }

    fun assess(findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): AmarEvidenceQualityReport {
        val valid = findings.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        val hostCounts = valid.mapNotNull { hostOf(it.sourceUri) }.groupingBy { it }.eachCount()

        val duplicateReport = AmarDuplicateEvidenceDetector().detect(valid)
        val uniquenessReport = AmarEvidenceUniquenessAnalyzer().analyze(findings)

        val scores = valid.map { finding ->
            val authority = authorityScore(finding.authority)
            val freshness = freshnessAnalyzer.assess(finding.retrievedAtEpochMs, nowEpochMs)
            val independent = hostOf(finding.sourceUri)?.let { hostCounts[it] == 1 } ?: false

            AmarEvidenceQualityItem(
                fingerprint = finding.fingerprint,
                authorityScore = authority,
                freshnessScore = freshness.score,
                independentSource = independent,
                uniqueEvidence = uniquenessReport.unique,
                authorityVerified = finding.authority != Authority.UNKNOWN,
                freshnessVerified = freshness.status != FreshnessStatus.FUTURE
            )
        }
        val independentHosts = valid.mapNotNull { hostOf(it.sourceUri) }.distinct().size
        val score = AmarEvidenceQualityScoreEngine().aggregate(scores)
        return AmarEvidenceQualityReport(score, independentHosts, duplicateReport.duplicateGroupCount, scores)
    }

    private fun authorityScore(authority: Authority): Double = when (authority) {
        Authority.PRIMARY -> 1.0
        Authority.OFFICIAL -> .95
        Authority.PEER_REVIEWED -> .90
        Authority.REPUTABLE -> .75
        Authority.COMMUNITY -> .40
        Authority.UNKNOWN -> .15
    }

    private fun hostOf(uri: String): String? =
        runCatching { URI(uri).host?.lowercase()?.removePrefix("www.") }.getOrNull()
}

data class AmarEvidenceQualityItem(
    val fingerprint: String,
    val authorityScore: Double,
    val freshnessScore: Double,
    val independentSource: Boolean,
    val uniqueEvidence: Boolean,
    val authorityVerified: Boolean,
    val freshnessVerified: Boolean
) {
    fun score(): Double = AmarEvidenceQualityScoreEngine().score(this)
}

data class AmarEvidenceQualityReport(
    val score: Double,
    val independentSourceCount: Int,
    val duplicateEvidenceCount: Int,
    val items: List<AmarEvidenceQualityItem>
)

class AmarClaimVerificationEngine {
    fun verify(answer: String, findings: List<ResearchFinding>): AmarClaimVerificationReport {
        val claims = answer.split(Regex("""(?<=[.!?؟])\s+|\n+""")).map { it.trim() }.filter { it.length >= 20 }
        val evidence = findings.filter { it.evidence.isNotBlank() }
        val results = claims.map { claim ->
            val claimTokens = tokens(claim)
            val matches = evidence.filter { overlap(claimTokens, tokens(it.evidence)) >= .25 }
            val opposition = matches.count { it.stance == EvidenceStance.OPPOSES }
            val support = matches.count { it.stance == EvidenceStance.SUPPORTS || it.stance == EvidenceStance.MIXED }
            val neutralMatches = matches.count { it.stance == EvidenceStance.UNKNOWN }
            // Unknown stance is not treated as positive proof, but it is valid matched
            // evidence. Only an explicit opposing match blocks a factual claim.
            val accepted = matches.isNotEmpty() && opposition == 0
            AmarClaimVerification(
                claim = claim,
                supportingEvidence = support,
                opposingEvidence = opposition,
                accepted = accepted,
                matchedEvidence = matches.size,
                neutralEvidence = neutralMatches
            )
        }
        val accepted = results.isNotEmpty() && results.all { it.accepted }
        return AmarClaimVerificationReport(results, accepted)
    }

    private fun tokens(text: String): Set<String> =
        text.lowercase().split(Regex("[^\p{L}\p{N}]+")).filter { it.length >= 4 }.toSet()

    private fun overlap(a: Set<String>, b: Set<String>): Double =
        if (a.isEmpty()) 0.0 else a.intersect(b).size.toDouble() / a.size
}

data class AmarClaimVerification(
    val claim: String,
    val supportingEvidence: Int,
    val opposingEvidence: Int,
    val accepted: Boolean,
    val matchedEvidence: Int = 0,
    val neutralEvidence: Int = 0
)
data class AmarClaimVerificationReport(val claims: List<AmarClaimVerification>, val accepted: Boolean)

class AmarConfidenceCalibrationEngine {
    fun calibrate(rawConfidence: Double, evidenceQuality: Double, claimVerification: AmarClaimVerificationReport, conflictCount: Int): Double {
        val claimFactor = when {
            claimVerification.claims.isEmpty() -> 0.0
            claimVerification.accepted -> 1.0
            else -> claimVerification.claims.count { it.accepted }.toDouble() / claimVerification.claims.size
        }
        val conflictPenalty = (1.0 - (conflictCount.coerceAtMost(5) * .10)).coerceIn(.5, 1.0)
        return (rawConfidence.coerceIn(0.0, 1.0) * .40 + evidenceQuality.coerceIn(0.0, 1.0) * .35 + claimFactor * .25) * conflictPenalty
    }
}

data class AmarStageTwoHardeningReport(
    val evidenceQuality: AmarEvidenceQualityReport,
    val claimVerification: AmarClaimVerificationReport,
    val calibratedConfidence: Double,
    val approved: Boolean,
    val issues: List<String>
)

private fun URI.hostSafe(): String? = runCatching { host?.lowercase()?.removePrefix("www.") }.getOrNull()
