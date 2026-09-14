package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerificationEngine
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityEngine
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityReport
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import java.net.URI

/**
 * Stage 11 / 2 — Verification Layer.
 *
 * Composes existing evidence contracts rather than redefining them. This layer verifies
 * source/evidence integrity, detects opposing evidence, and emits a deterministic
 * provenance chain. It never grants execution authority and never mutates governance.
 */
class AmarVerificationLayer(
    private val evidenceQuality: AmarEvidenceQualityEngine = AmarEvidenceQualityEngine(),
    private val claimVerifier: AmarClaimVerificationEngine = AmarClaimVerificationEngine(),
    private val sourceRegistry: AmarSourceRegistry = AmarSourceRegistry()
) {
    fun verify(
        answer: String,
        findings: List<ResearchFinding>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): AmarVerificationReport {
        val quality = evidenceQuality.assess(findings, nowEpochMs)
        val sourceSnapshot = sourceRegistry.index(findings)
        val claimVerification = claimVerifier.verify(answer, findings)
        return buildReport(findings, quality, sourceSnapshot, claimVerification)
    }

    /**
     * Verifies a research evidence set without fabricating an answer claim. Claim verification
     * is intentionally neutral because research findings are evidence inputs, not a final answer.
     */
    fun verifyEvidenceOnly(
        findings: List<ResearchFinding>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): AmarVerificationReport {
        val quality = evidenceQuality.assess(findings, nowEpochMs)
        val sourceSnapshot = sourceRegistry.index(findings)
        val neutralClaimVerification = AmarClaimVerificationReport(emptyList(), accepted = true)
        return buildReport(findings, quality, sourceSnapshot, neutralClaimVerification)
    }

    private fun buildReport(
        findings: List<ResearchFinding>,
        quality: AmarEvidenceQualityReport,
        sourceSnapshot: AmarSourceRegistrySnapshot,
        claimVerification: AmarClaimVerificationReport
    ): AmarVerificationReport {
        val conflicts = AmarConflictDetector.detect(findings)
        val provenance = AmarProvenanceChain.build(findings)
        val usable = findings.count { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        val invalid = findings.size - usable
        val claimScore = if (claimVerification.claims.isEmpty()) {
            if (claimVerification.accepted) 0.25 else 0.0
        } else {
            claimVerification.claims.count { it.accepted }.toDouble() / claimVerification.claims.size * 0.25
        }
        val score = (
            quality.score * 0.45 +
                sourceSnapshot.integrityScore * 0.20 +
                claimScore +
                if (conflicts.isEmpty()) 0.10 else 0.0
            ).coerceIn(0.0, 1.0)
        val status = when {
            findings.isEmpty() || usable == 0 -> AmarVerificationStatus.UNVERIFIABLE
            claimVerification.accepted && conflicts.isEmpty() && score >= 0.70 -> AmarVerificationStatus.VERIFIED
            score >= 0.40 -> AmarVerificationStatus.PARTIAL
            else -> AmarVerificationStatus.REJECTED
        }
        return AmarVerificationReport(
            status = status,
            score = score,
            evidenceQuality = quality,
            sourceRegistry = sourceSnapshot,
            claimVerification = claimVerification,
            conflicts = conflicts,
            provenance = provenance,
            usableEvidenceCount = usable,
            invalidEvidenceCount = invalid
        )
    }
}

enum class AmarVerificationStatus { VERIFIED, PARTIAL, REJECTED, UNVERIFIABLE }

data class AmarVerificationReport(
    val status: AmarVerificationStatus,
    val score: Double,
    val evidenceQuality: AmarEvidenceQualityReport,
    val sourceRegistry: AmarSourceRegistrySnapshot,
    val claimVerification: AmarClaimVerificationReport,
    val conflicts: List<AmarConflict>,
    val provenance: List<AmarProvenanceNode>,
    val usableEvidenceCount: Int,
    val invalidEvidenceCount: Int
)

/** Verification-time source registry. It is deterministic and does not persist or browse. */
class AmarSourceRegistry {
    fun index(findings: List<ResearchFinding>): AmarSourceRegistrySnapshot {
        val entries = findings.mapNotNull { finding ->
            val host = runCatching { URI(finding.sourceUri).host?.lowercase()?.removePrefix("www.") }.getOrNull()
            if (host.isNullOrBlank() || finding.evidence.isBlank()) null
            else AmarSourceRegistryEntry(host, finding.sourceUri.trim(), finding.authority.name, finding.fingerprint)
        }.distinctBy { "${it.host}|${it.fingerprint}" }
        val hosts = entries.map { it.host }.distinct()
        val integrity = if (findings.isEmpty()) 0.0 else entries.size.toDouble() / findings.size
        return AmarSourceRegistrySnapshot(entries, hosts, integrity.coerceIn(0.0, 1.0))
    }
}

data class AmarSourceRegistryEntry(
    val host: String,
    val sourceUri: String,
    val authority: String,
    val fingerprint: String
)

data class AmarSourceRegistrySnapshot(
    val entries: List<AmarSourceRegistryEntry>,
    val independentHosts: List<String>,
    val integrityScore: Double
)

object AmarConflictDetector {
    fun detect(findings: List<ResearchFinding>): List<AmarConflict> {
        val support = findings.filter { it.stance == EvidenceStance.SUPPORTS }
        val oppose = findings.filter { it.stance == EvidenceStance.OPPOSES }
        if (support.isEmpty() || oppose.isEmpty()) return emptyList()
        return listOf(
            AmarConflict(
                supportingFingerprints = support.map { it.fingerprint }.distinct(),
                opposingFingerprints = oppose.map { it.fingerprint }.distinct(),
                reason = "Evidence contains both supporting and opposing stances"
            )
        )
    }
}

data class AmarConflict(
    val supportingFingerprints: List<String>,
    val opposingFingerprints: List<String>,
    val reason: String
)

object AmarProvenanceChain {
    fun build(findings: List<ResearchFinding>): List<AmarProvenanceNode> {
        var previous = "GENESIS"
        return findings.mapIndexed { index, finding ->
            val fingerprint = finding.fingerprint.ifBlank {
                AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}")
            }
            val chainHash = AmarEvidence.fingerprintOf(
                "$previous|$index|${finding.sourceUri}|$fingerprint|${finding.retrievedAtEpochMs}"
            )
            AmarProvenanceNode(index, finding.sourceUri, fingerprint, finding.retrievedAtEpochMs, previous, chainHash)
                .also { previous = chainHash }
        }
    }
}

data class AmarProvenanceNode(
    val sequence: Int,
    val sourceUri: String,
    val evidenceFingerprint: String,
    val retrievedAtEpochMs: Long,
    val previousHash: String,
    val chainHash: String
)
