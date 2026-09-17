package com.personal.gridbot.amaros.agent

import java.net.URI
import kotlin.math.exp

/**
 * Canonical Stage 11 Item 3 evidence boundary.
 * It evaluates supplied evidence quality only; it does not retrieve, research, decide, or execute.
 */
class AmarEvidenceQualityEngine(
    private val policy: AmarEvidencePolicy = AmarEvidencePolicy.DEFAULT,
    freshnessWindowMs: Long? = null
) {
    private val effectivePolicy = freshnessWindowMs?.let {
        require(it > 0) { "freshnessWindowMs must be positive" }
        policy.copy(freshnessHalfLifeMs = it)
    } ?: policy

    fun assess(findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): AmarEvidenceQualityReport {
        val items = findings.map { finding -> assessItem(finding, findings, nowEpochMs) }
        val usableIndices = items.indices.filter { items[it].integrityValid && items[it].contentValid }
        val usable = usableIndices.map { items[it] }
        val score = if (usable.isEmpty()) 0.0 else usable.map { it.score }.average()
        val independentHosts = usable.mapNotNull { it.host }.distinct()
        // Duplication is a property of supplied evidence content, not of fingerprint integrity.
        // Integrity remains fail-closed independently through item.score and report.status.
        val allEvidenceKeys = findings.map { normalizedEvidenceKey(it.evidence.trim()) }.filter { it.isNotBlank() }
        val duplicateCount = allEvidenceKeys.size - allEvidenceKeys.distinct().size
        val hasIntegrityFailure = items.any { !it.integrityValid }
        val hasContentFailure = items.any { !it.contentValid }
        val status = when {
            findings.isEmpty() || usable.isEmpty() -> AmarEvidenceQualityStatus.UNVERIFIABLE
            hasIntegrityFailure -> AmarEvidenceQualityStatus.UNVERIFIABLE
            hasContentFailure -> AmarEvidenceQualityStatus.WEAK
            score >= effectivePolicy.verifiedThreshold -> AmarEvidenceQualityStatus.VERIFIED
            score >= effectivePolicy.weakThreshold -> AmarEvidenceQualityStatus.WEAK
            else -> AmarEvidenceQualityStatus.UNVERIFIABLE
        }
        return AmarEvidenceQualityReport(
            score = score.coerceIn(0.0, 1.0),
            independentSourceCount = independentHosts.size,
            duplicateEvidenceCount = duplicateCount,
            items = items,
            policyVersion = effectivePolicy.version,
            status = status
        )
    }

    fun rank(findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): List<AmarEvidenceQualityItem> =
        findings
            .map { assessItem(it, findings, nowEpochMs) }
            .sortedWith(compareByDescending<AmarEvidenceQualityItem> { it.score }.thenBy { it.fingerprint })

    private fun assessItem(
        finding: ResearchFinding,
        allFindings: List<ResearchFinding>,
        nowEpochMs: Long
    ): AmarEvidenceQualityItem {
        val sourceUri = finding.sourceUri.trim()
        val evidence = finding.evidence.trim()
        val expectedFingerprint = AmarEvidence.fingerprintOf("$sourceUri|$evidence")
        val suppliedFingerprint = finding.fingerprint.ifBlank { expectedFingerprint }
        val integrityValid = suppliedFingerprint == expectedFingerprint
        val contentValid = sourceUri.isNotBlank() && evidence.isNotBlank()
        require(nowEpochMs >= finding.retrievedAtEpochMs) { "evidence cannot be from the future" }

        val authority = authorityScore(finding.authority)
        val age = nowEpochMs - finding.retrievedAtEpochMs
        val freshness = exp(-age.toDouble() / effectivePolicy.freshnessHalfLifeMs.toDouble()).coerceIn(0.0, 1.0)
        val host = hostOf(sourceUri)
        val hostCount = if (host == null) 0 else allFindings.count { hostOf(it.sourceUri) == host }
        val independent = host != null && hostCount == 1
        val contentKey = normalizedEvidenceKey(evidence)
        val contentCount = allFindings.count { normalizedEvidenceKey(it.evidence.trim()) == contentKey }
        val unique = contentKey.isNotBlank() && contentCount == 1
        val score = (
            authority * effectivePolicy.authorityWeight +
                freshness * effectivePolicy.freshnessWeight +
                (if (independent) 1.0 else 0.0) * effectivePolicy.independenceWeight +
                (if (unique) 1.0 else 0.0) * effectivePolicy.uniquenessWeight
            ).coerceIn(0.0, 1.0)

        return AmarEvidenceQualityItem(
            fingerprint = suppliedFingerprint,
            authorityScore = authority,
            freshnessScore = freshness,
            independentSource = independent,
            uniqueEvidence = unique,
            score = if (integrityValid && contentValid) score else 0.0,
            integrityValid = integrityValid,
            contentValid = contentValid,
            host = host
        )
    }

    private fun normalizedEvidenceKey(evidence: String): String = evidence
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun authorityScore(authority: Authority): Double = when (authority) {
        Authority.PRIMARY -> 1.0
        Authority.OFFICIAL -> .95
        Authority.PEER_REVIEWED -> .90
        Authority.REPUTABLE -> .75
        Authority.COMMUNITY -> .40
        Authority.UNKNOWN -> .15
    }

    private fun hostOf(uri: String): String? = runCatching {
        URI(uri).host?.lowercase()?.removePrefix("www.")
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

data class AmarEvidenceQualityItem(
    val fingerprint: String,
    val authorityScore: Double,
    val freshnessScore: Double,
    val independentSource: Boolean,
    val uniqueEvidence: Boolean,
    val score: Double = 0.0,
    val integrityValid: Boolean = true,
    val contentValid: Boolean = true,
    val host: String? = null
)

enum class AmarEvidenceQualityStatus { VERIFIED, WEAK, UNVERIFIABLE }

data class AmarEvidenceQualityReport(
    val score: Double,
    val independentSourceCount: Int,
    val duplicateEvidenceCount: Int,
    val items: List<AmarEvidenceQualityItem>,
    val policyVersion: Int = 1,
    val status: AmarEvidenceQualityStatus = AmarEvidenceQualityStatus.UNVERIFIABLE
)

class AmarClaimVerificationEngine {
    fun verify(answer: String, findings: List<ResearchFinding>): AmarClaimVerificationReport {
        val claims = answer.split(Regex("(?<=[.!?؟])\\s+|\\n+")).map { it.trim() }.filter { it.length >= 20 }
        val evidence = findings.filter { it.evidence.isNotBlank() }
        val results = claims.map { claim ->
            val tokens = tokens(claim)
            val matches = evidence.filter { overlap(tokens, tokens(it.evidence)) >= .25 }
            val support = matches.filter { it.stance == EvidenceStance.SUPPORTS || it.stance == EvidenceStance.MIXED }
            val opposition = matches.filter { it.stance == EvidenceStance.OPPOSES }
            AmarClaimVerification(claim, support.size, opposition.size, support.isNotEmpty() && opposition.isEmpty())
        }
        val accepted = results.isNotEmpty() && results.all { it.accepted }
        return AmarClaimVerificationReport(results, accepted)
    }

    private fun tokens(text: String): Set<String> = text.lowercase().split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.length >= 4 }.toSet()
    private fun overlap(a: Set<String>, b: Set<String>): Double = if (a.isEmpty()) 0.0 else a.intersect(b).size.toDouble() / a.size
}

data class AmarClaimVerification(val claim: String, val supportingEvidence: Int, val opposingEvidence: Int, val accepted: Boolean)
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