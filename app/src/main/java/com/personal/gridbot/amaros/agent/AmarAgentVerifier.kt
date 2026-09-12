package com.personal.gridbot.amaros.agent

/** Final gate: the agent must explain what is known, inferred, tested and still uncertain. */
class AmarAgentVerifier(
    private val minimumConsensusConfidence: Double = 0.80
) {
    fun verify(
        answer: String,
        consensus: AmarConsensusReport?,
        critique: AmarCritique,
        sourceVerification: AmarSourceVerification? = null
    ): AmarDecisionVerification {
        val threshold = minimumConsensusConfidence.coerceIn(0.0, 1.0)
        val issues = mutableListOf<String>()
        if (answer.isBlank()) issues += "empty_answer"
        if (!critique.accepted) issues += critique.issues
        if (sourceVerification != null && !sourceVerification.accepted) {
            issues += "source_verification_failed"
        }
        if (consensus != null && consensus.totalSources > 0 && consensus.consensusScore < threshold) {
            issues += "weak_source_consensus"
        }
        val evidenceConfidence = when {
            consensus != null && sourceVerification != null ->
                minOf(consensus.consensusScore, sourceVerification.confidence)
            consensus != null -> consensus.consensusScore
            sourceVerification != null -> sourceVerification.confidence
            else -> 0.0
        }
        return AmarDecisionVerification(
            approved = issues.isEmpty(),
            issues = issues.distinct(),
            evidenceConfidence = evidenceConfidence
        )
    }
}

data class AmarDecisionVerification(
    val approved: Boolean,
    val issues: List<String>,
    val evidenceConfidence: Double
)
