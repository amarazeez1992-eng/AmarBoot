package com.personal.gridbot.amaros.agent

/** Challenges a draft before it can become the agent's final answer. */
class AmarAgentCritic {
    fun review(
        draft: String,
        evidence: List<ResearchFinding>,
        requireEvidence: Boolean = false
    ): AmarCritique {
        val issues = mutableListOf<String>()
        if (draft.isBlank()) issues += "empty_answer"
        if (requireEvidence && evidence.isEmpty()) issues += "no_evidence"
        if (draft.contains("مؤكد", ignoreCase = true) && evidence.size < 2) issues += "unsupported_certainty"
        if (draft.contains("مضمون", ignoreCase = true) || draft.contains("guaranteed", ignoreCase = true)) issues += "guarantee_language"
        return AmarCritique(
            accepted = issues.isEmpty(),
            issues = issues.distinct(),
            recommendation = if (issues.isEmpty()) "PASS" else "REVISE"
        )
    }
}

data class AmarCritique(
    val accepted: Boolean,
    val issues: List<String>,
    val recommendation: String
)
