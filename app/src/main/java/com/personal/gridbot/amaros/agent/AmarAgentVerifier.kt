package com.personal.gridbot.amaros.agent

/** Final gate: the agent must explain what is known, inferred, tested and still uncertain. */
class AmarAgentVerifier {
    fun verify(
        answer: String,
        consensus: AmarConsensusReport?,
        critique: AmarCritique
    ): AmarDecisionVerification {
        val issues = mutableListOf<String>()
        if (answer.isBlank()) issues += "empty_answer"
        if (!critique.accepted) issues += critique.issues
        if (consensus != null && consensus.totalSources > 0 && consensus.consensusScore < 0.80) {
            issues += "weak_source_consensus"
        }
        return AmarDecisionVerification(
            approved = issues.isEmpty(),
            issues = issues.distinct(),
            evidenceConfidence = consensus?.consensusScore ?: 0.0
        )
    }
}

data class AmarDecisionVerification(
    val approved: Boolean,
    val issues: List<String>,
    val evidenceConfidence: Double
)
