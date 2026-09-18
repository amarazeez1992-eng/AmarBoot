package com.personal.gridbot.amaros.agent

/** Evidence quality gate. It does not treat model confidence as truth. */
class AmarSourceVerifier {
    fun verify(findings: List<ResearchFinding>): AmarSourceVerification {
        val valid = findings.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        if (valid.isEmpty()) return AmarSourceVerification(false, 0.0, 0, 0, 0.0, "No usable evidence")

        val independent = valid.map { independentKey(it.sourceUri) }.distinct().size
        val authorityScore = valid.map { it.authority.weight() }.average().coerceIn(0.0, 1.0)
        val independenceScore = (independent / valid.size.toDouble()).coerceIn(0.0, 1.0)
        val confidence = (authorityScore * 0.7 + independenceScore * 0.3).coerceIn(0.0, 1.0)
        return AmarSourceVerification(
            accepted = confidence >= 0.70 && independent >= 2,
            confidence = confidence,
            totalSources = valid.size,
            independentSources = independent,
            authorityScore = authorityScore,
            rationale = "authority=$authorityScore; independence=$independenceScore"
        )
    }

    /** Point 5 composition helper: exposes the owning independence rule without duplicating it downstream. */
    fun isIndependent(finding: ResearchFinding, findings: List<ResearchFinding>): Boolean {
        if (finding.sourceUri.isBlank() || finding.evidence.isBlank()) return false
        val key = independentKey(finding.sourceUri)
        return findings.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
            .count { independentKey(it.sourceUri) == key } == 1
    }

    /** Point 3 composition helper: exposes the canonical authority value already used by this verifier. */
    fun authorityScore(authority: Authority): Double = authority.weight()

    private fun independentKey(uri: String): String = runCatching {
        java.net.URI(uri).host?.lowercase()?.removePrefix("www.") ?: uri.trim().lowercase()
    }.getOrElse { uri.trim().lowercase() }

    private fun Authority.weight(): Double = when (this) {
        Authority.PRIMARY -> 1.0
        Authority.OFFICIAL -> 0.95
        Authority.PEER_REVIEWED -> 0.90
        Authority.REPUTABLE -> 0.75
        Authority.COMMUNITY -> 0.40
        Authority.UNKNOWN -> 0.15
    }
}

data class AmarSourceVerification(
    val accepted: Boolean,
    val confidence: Double,
    val totalSources: Int,
    val independentSources: Int,
    val authorityScore: Double,
    val rationale: String
)
