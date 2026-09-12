package com.personal.gridbot.amaros.agent

/**
 * Converts many independent findings into an auditable consensus summary.
 * Consensus is about evidence agreement, not mathematical proof or guaranteed truth.
 */
class AmarAgentEvidenceConsensus {
    fun summarize(findings: List<ResearchFinding>): AmarConsensusReport {
        if (findings.isEmpty()) return AmarConsensusReport(0, 0, 0, 0.0, 0.0, emptyList(), "No sources available")

        val normalized = findings.map { it.evidence.trim().lowercase() }.filter { it.isNotEmpty() }
        val unique = findings.map { it.sourceUri.trim() }.filter { it.isNotEmpty() }.distinct().size
        val sourceCount = findings.size
        val authority = findings.map { it.authority }.groupingBy { it }.eachCount()
        val weightedAgreement = findings.map { it.authority.weight() }.average().coerceIn(0.0, 1.0)
        val evidenceCoverage = (unique.toDouble() / sourceCount.toDouble()).coerceIn(0.0, 1.0)
        val consensusScore = (weightedAgreement * 0.65 + evidenceCoverage * 0.35).coerceIn(0.0, 1.0)

        val details = authority.entries
            .sortedByDescending { it.value }
            .map { "${it.key}: ${it.value}" }

        return AmarConsensusReport(
            totalSources = sourceCount,
            independentSources = unique,
            supportingSources = normalized.size,
            consensusScore = consensusScore,
            authorityScore = weightedAgreement,
            authorityBreakdown = details,
            conclusion = when {
                consensusScore >= 0.90 -> "Strong multi-source agreement"
                consensusScore >= 0.80 -> "High agreement; continue validation for high-stakes decisions"
                consensusScore >= 0.70 -> "Moderate agreement; conflicting or weaker evidence remains"
                else -> "Insufficient consensus; do not present as confirmed"
            }
        )
    }

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
    val consensusScore: Double,
    val authorityScore: Double,
    val authorityBreakdown: List<String>,
    val conclusion: String
)
