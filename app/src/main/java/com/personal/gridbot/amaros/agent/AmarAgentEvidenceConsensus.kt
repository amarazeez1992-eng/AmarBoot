package com.personal.gridbot.amaros.agent

/**
 * Converts many findings into an auditable consensus summary.
 * Consensus measures evidence agreement; it is not mathematical proof or guaranteed truth.
 */
class AmarAgentEvidenceConsensus {
    fun summarize(findings: List<ResearchFinding>): AmarConsensusReport {
        if (findings.isEmpty()) return AmarConsensusReport(0, 0, 0, 0, 0, 0.0, 0.0, emptyList(), "No sources available")

        val valid = findings.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        val independent = valid.map { independentKey(it.sourceUri) }.distinct().size
        val authority = valid.map { it.authority }.groupingBy { it }.eachCount()
        val supporting = valid.count { it.stance == EvidenceStance.SUPPORTS }
        val opposing = valid.count { it.stance == EvidenceStance.OPPOSES }
        val unknown = valid.count { it.stance == EvidenceStance.UNKNOWN || it.stance == EvidenceStance.MIXED }
        val weightedAuthority = if (valid.isEmpty()) 0.0 else valid.map { it.authority.weight() }.average().coerceIn(0.0, 1.0)
        val independenceScore = if (valid.isEmpty()) 0.0 else (independent.toDouble() / valid.size).coerceIn(0.0, 1.0)
        val knownStance = supporting + opposing
        val agreementScore = if (knownStance == 0) 0.0 else supporting.toDouble() / knownStance.toDouble()
        val consensusScore = if (knownStance == 0) {
            (weightedAuthority * 0.65 + independenceScore * 0.35).coerceIn(0.0, 1.0)
        } else {
            (agreementScore * 0.50 + weightedAuthority * 0.30 + independenceScore * 0.20).coerceIn(0.0, 1.0)
        }

        val details = authority.entries
            .sortedByDescending { it.value }
            .map { "${it.key}: ${it.value}" }

        return AmarConsensusReport(
            totalSources = valid.size,
            independentSources = independent,
            supportingSources = supporting,
            opposingSources = opposing,
            unknownSources = unknown,
            consensusScore = consensusScore,
            authorityScore = weightedAuthority,
            authorityBreakdown = details,
            conclusion = when {
                valid.isEmpty() -> "No usable evidence"
                knownStance == 0 -> "Sources collected, but their stance is not classified"
                consensusScore >= 0.90 -> "Strong multi-source agreement"
                consensusScore >= 0.80 -> "High agreement; continue validation for high-stakes decisions"
                consensusScore >= 0.70 -> "Moderate agreement; conflicting or weaker evidence remains"
                else -> "Insufficient consensus; do not present as confirmed"
            }
        )
    }

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

data class AmarConsensusReport(
    val totalSources: Int,
    val independentSources: Int,
    val supportingSources: Int,
    val opposingSources: Int,
    val unknownSources: Int,
    val consensusScore: Double,
    val authorityScore: Double,
    val authorityBreakdown: List<String>,
    val conclusion: String
)
