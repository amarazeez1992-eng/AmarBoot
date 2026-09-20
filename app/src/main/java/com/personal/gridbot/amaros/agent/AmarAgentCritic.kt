package com.personal.gridbot.amaros.agent

import java.net.URI

/**
 * Challenges a draft before it can become the agent's final answer.
 * Stage 11 Item 6 extends the existing critic instead of introducing a second critique engine.
 */
class AmarAgentCritic {
    fun review(
        draft: String,
        evidence: List<ResearchFinding>,
        requireEvidence: Boolean = false
    ): AmarCritique {
        val validEvidence = evidence.filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
        val issues = mutableListOf<String>()

        if (draft.isBlank()) issues += "empty_answer"
        if (requireEvidence && validEvidence.isEmpty()) issues += "no_evidence"
        if (requireEvidence && evidence.isNotEmpty() && validEvidence.size != evidence.size) issues += "invalid_evidence"

        // Independence is publisher/host based, not raw-URL based. Multiple pages on one
        // domain are one source family and must not satisfy cross-validation by themselves.
        val independentSources = validEvidence.mapNotNull { hostOf(it.sourceUri) }.distinct().size
        if (requireEvidence && independentSources < 2) issues += "insufficient_independent_sources"

        val hasSupport = validEvidence.any { it.stance == EvidenceStance.SUPPORTS }
        val hasOpposition = validEvidence.any { it.stance == EvidenceStance.OPPOSES }
        if (hasSupport && hasOpposition) issues += "evidence_conflict"

        val certaintyLanguage = listOf(
            "مؤكد", "بالتأكيد", "قطعاً", "حتماً", "مضمون", "guaranteed", "certainly", "definitely", "always", "never"
        )
        if (requireEvidence && certaintyLanguage.any { draft.contains(it, ignoreCase = true) }) {
            if (validEvidence.size < 2 || hasSupport && hasOpposition) issues += "unsupported_certainty"
        }

        if (draft.contains("مضمون", ignoreCase = true) || draft.contains("guaranteed", ignoreCase = true)) {
            issues += "guarantee_language"
        }

        val numericClaim = Regex("(?<!\w)\d+(?:[.,]\d+)?%?(?!\w)").containsMatchIn(draft)
        if (numericClaim && requireEvidence && validEvidence.isEmpty()) issues += "unsupported_numeric_claim"

        val distinctIssues = issues.distinct()
        val score = score(validEvidence, requireEvidence, distinctIssues)
        return AmarCritique(
            accepted = distinctIssues.isEmpty(),
            issues = distinctIssues,
            recommendation = if (distinctIssues.isEmpty()) "PASS" else "REVISE",
            score = score,
            evidenceCount = validEvidence.size,
            independentSourceCount = independentSources
        )
    }

    private fun score(
        validEvidence: List<ResearchFinding>,
        requireEvidence: Boolean,
        issues: List<String>
    ): Double {
        if (issues.contains("empty_answer")) return 0.0
        var value = 1.0
        if (requireEvidence && validEvidence.isEmpty()) value -= 0.55
        if (validEvidence.isNotEmpty()) value += (validEvidence.size.coerceAtMost(4) * 0.05)
        if (issues.contains("invalid_evidence")) value -= 0.25
        if (issues.contains("insufficient_independent_sources")) value -= 0.20
        if (issues.contains("evidence_conflict")) value -= 0.25
        if (issues.contains("unsupported_certainty")) value -= 0.20
        if (issues.contains("guarantee_language")) value -= 0.20
        if (issues.contains("unsupported_numeric_claim")) value -= 0.20
        return value.coerceIn(0.0, 1.0)
    }

    private fun hostOf(uri: String): String? = runCatching {
        URI(uri).host?.lowercase()?.removePrefix("www.")
    }.getOrNull()
}

data class AmarCritique(
    val accepted: Boolean,
    val issues: List<String>,
    val recommendation: String,
    val score: Double = if (accepted) 1.0 else 0.0,
    val evidenceCount: Int = 0,
    val independentSourceCount: Int = 0
)
