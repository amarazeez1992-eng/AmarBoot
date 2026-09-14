package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerificationEngine
import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityEngine
import com.personal.gridbot.amaros.agent.AmarEvidenceQualityReport
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding
import java.net.URI

/** Stage 11 / 2 — Verification Layer. */
class AmarVerificationLayer(
    private val evidenceQuality: AmarEvidenceQualityEngine = AmarEvidenceQualityEngine(),
    private val claimVerifier: AmarClaimVerificationEngine = AmarClaimVerificationEngine(),
    private val sourceRegistry: AmarSourceRegistry = AmarSourceRegistry()
) {
    fun verify(answer: String, findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): AmarVerificationReport {
        val quality = evidenceQuality.assess(findings, nowEpochMs)
        val sourceSnapshot = sourceRegistry.index(findings)
        return buildReport(findings, quality, sourceSnapshot, claimVerifier.verify(answer, findings))
    }

    /** Evidence-only path: research evidence is verified without inventing a final answer claim. */
    fun verifyEvidenceOnly(findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): AmarVerificationReport {
        val quality = evidenceQuality.assess(findings, nowEpochMs)
        val sourceSnapshot = sourceRegistry.index(findings)
        return buildReport(findings, quality, sourceSnapshot, AmarClaimVerificationReport(emptyList(), accepted = true))
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
        val score = (quality.score * 0.45 + sourceSnapshot.integrityScore * 0.20 + claimScore + if (conflicts.isEmpty()) 0.10 else 0.0)
            .coerceIn(0.0, 1.0)
        val status = when {
            findings.isEmpty() || usable == 0 -> AmarVerificationStatus.UNVERIFIABLE
            claimVerification.accepted && conflicts.isEmpty() && score >= 0.70 -> AmarVerificationStatus.VERIFIED
            score >= 0.40 -> AmarVerificationStatus.PARTIAL
            else -> AmarVerificationStatus.REJECTED
        }
        return AmarVerificationReport(status, score, quality, sourceSnapshot, claimVerification, conflicts, provenance, usable, invalid)
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

data class AmarSourceRegistryEntry(val host: String, val sourceUri: String, val authority: String, val fingerprint: String)
data class AmarSourceRegistrySnapshot(val entries: List<AmarSourceRegistryEntry>, val independentHosts: List<String>, val integrityScore: Double)

/** Conflict is claim-scoped by semantic evidence overlap; unrelated support/opposition is not a conflict. */
object AmarConflictDetector {
    fun detect(findings: List<ResearchFinding>): List<AmarConflict> {
        val support = findings.filter { it.stance == EvidenceStance.SUPPORTS || it.stance == EvidenceStance.MIXED }
        val oppose = findings.filter { it.stance == EvidenceStance.OPPOSES }
        if (support.isEmpty() || oppose.isEmpty()) return emptyList()
        val pairs = support.flatMap { s -> oppose.mapNotNull { o ->
            if (overlap(tokens(s.evidence), tokens(o.evidence)) >= 0.25) s to o else null
        }}
        if (pairs.isEmpty()) return emptyList()
        return listOf(
            AmarConflict(
                supportingFingerprints = pairs.map { it.first.fingerprint }.distinct(),
                opposingFingerprints = pairs.map { it.second.fingerprint }.distinct(),
                reason = "Evidence contains materially overlapping supporting and opposing stances"
            )
        )
    }

    private fun tokens(text: String): Set<String> = text.lowercase().split(Regex("[^\\p{L}\\p{N}]+" )).filter { it.length >= 4 }.toSet()
    private fun overlap(a: Set<String>, b: Set<String>): Double = if (a.isEmpty()) 0.0 else a.intersect(b).size.toDouble() / a.size
}

data class AmarConflict(val supportingFingerprints: List<String>, val opposingFingerprints: List<String>, val reason: String)

object AmarProvenanceChain {
    fun build(findings: List<ResearchFinding>): List<AmarProvenanceNode> {
        var previous = "GENESIS"
        return findings.mapIndexed { index, finding ->
            val fingerprint = finding.fingerprint.ifBlank { AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}") }
            val chainHash = AmarEvidence.fingerprintOf("$previous|$index|${finding.sourceUri}|$fingerprint|${finding.retrievedAtEpochMs}")
            AmarProvenanceNode(index, finding.sourceUri, fingerprint, finding.retrievedAtEpochMs, previous, chainHash).also { previous = chainHash }
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
