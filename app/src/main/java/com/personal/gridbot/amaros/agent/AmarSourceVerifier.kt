package com.personal.gridbot.amaros.agent

/** Evidence quality gate. It does not treat model confidence as truth. */
class AmarSourceVerifier {
    fun verify(findings: List<ResearchFinding>): AmarSourceVerification {
        if (findings.isEmpty()) return AmarSourceVerification(false, 0.0, 0, 0, 0, "No evidence")
        val independent = findings.map { it.sourceUri.trim() }.filter { it.isNotEmpty() }.distinct().size
        val authorityScore = findings.map { it.authority.weight() }.average().coerceIn(0.0, 1.0)
        val independenceScore = (independent / findings.size.toDouble()).coerceIn(0.0, 1.0)
        val confidence = (authorityScore * 0.7 + independenceScore * 0.3).coerceIn(0.0, 1.0)
        return AmarSourceVerification(
            accepted = confidence >= 0.70 && independent >= 2,
            confidence = confidence,
            totalSources = findings.size,
            independentSources = independent,
            authorityScore = authorityScore,
            rationale = "authority=$authorityScore; independence=$independenceScore"
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

data class AmarSourceVerification(
    val accepted: Boolean,
    val confidence: Double,
    val totalSources: Int,
    val independentSources: Int,
    val authorityScore: Double,
    val rationale: String
)
