package com.personal.gridbot.amaros.agent

/** Evidence quality gate. It does not treat model confidence as truth. */
class AmarSourceVerifier {
    fun verify(findings: List<AmarResearchFinding>): AmarSourceVerification {
        if (findings.isEmpty()) return AmarSourceVerification(false, 0.0, "No evidence")
        val independent = findings.map { it.sourceUri }.distinct().size
        val authorityScore = findings.map { it.authority.weight() }.average()
        val independenceScore = (independent / findings.size.toDouble()).coerceIn(0.0, 1.0)
        val confidence = (authorityScore * 0.7 + independenceScore * 0.3).coerceIn(0.0, 1.0)
        return AmarSourceVerification(confidence >= 0.70, confidence, "authority=$authorityScore; independence=$independenceScore")
    }

    private fun AmarResearchEngine.Authority.weight(): Double = when (this) {
        AmarResearchEngine.Authority.PRIMARY -> 1.0
        AmarResearchEngine.Authority.OFFICIAL -> 0.95
        AmarResearchEngine.Authority.PEER_REVIEWED -> 0.90
        AmarResearchEngine.Authority.REPUTABLE -> 0.75
        AmarResearchEngine.Authority.COMMUNITY -> 0.40
        AmarResearchEngine.Authority.UNKNOWN -> 0.15
    }
}

data class AmarSourceVerification(val accepted: Boolean, val confidence: Double, val rationale: String)
