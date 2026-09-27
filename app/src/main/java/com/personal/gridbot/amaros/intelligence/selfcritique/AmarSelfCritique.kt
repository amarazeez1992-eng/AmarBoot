package com.personal.gridbot.amaros.intelligence.selfcritique

class AmarSelfCritique {
    data class Claim(val text: String, val supported: Boolean, val evidenceIds: List<String>)
    data class Draft(val answer: String, val claims: List<Claim>, val evidenceIds: List<String>, val assumptions: List<String>)
    data class Review(val qualityScore: Double, val coverageScore: Double, val contradictions: List<String>, val unsupported: List<Claim>, val overclaiming: List<String>, val decision: RevisionDecision, val summary: String)
    enum class RevisionDecision { ACCEPT, REVISE, REJECT }

    fun review(draft: Draft): Review {
        val quality = answerQuality(draft)
        val coverage = evidenceCoverage(draft)
        val contradictions = detectContradictions(draft)
        val unsupported = draft.claims.filter { !it.supported }
        val overclaiming = detectOverclaiming(draft)
        val decision = when {
            draft.answer.isBlank() -> RevisionDecision.REJECT
            unsupported.isNotEmpty() && unsupported.size * 2 >= draft.claims.size -> RevisionDecision.REJECT
            contradictions.isNotEmpty() || overclaiming.isNotEmpty() -> RevisionDecision.REVISE
            quality < 0.5 || coverage < 0.5 -> RevisionDecision.REVISE
            else -> RevisionDecision.ACCEPT
        }
        val summary = "quality=${"%.2f".format(quality)} coverage=${"%.2f".format(coverage)} unsupported=${unsupported.size} contradictions=${contradictions.size} overclaiming=${overclaiming.size}"
        return Review(quality, coverage, contradictions, unsupported, overclaiming, decision, summary)
    }

    private fun answerQuality(draft: Draft): Double {
        if (draft.answer.isBlank()) return 0.0
        val len = when { draft.answer.length < 20 -> 0.3; draft.answer.length < 80 -> 0.7; else -> 1.0 }
        val has = if (draft.claims.isNotEmpty()) 1.0 else 0.4
        return (len * 0.6 + has * 0.4).coerceIn(0.0, 1.0)
    }

    private fun evidenceCoverage(draft: Draft): Double {
        if (draft.claims.isEmpty()) return 0.0
        return draft.claims.count { it.evidenceIds.isNotEmpty() }.toDouble() / draft.claims.size
    }

    private fun detectContradictions(draft: Draft): List<String> {
        val out = mutableListOf<String>()
        val negations = listOf("not ", "never ", "no ", "cannot ")
        for (claim in draft.claims) {
            val cl = claim.text.lowercase()
            val opp = draft.claims.firstOrNull {
                it !== claim && negations.any { n -> cl.contains(n) != it.text.lowercase().contains(n) } && shareKeyToken(claim.text, it.text)
            }
            if (opp != null) out += "${claim.text} ↔ ${opp.text}"
        }
        if (draft.answer.lowercase().contains("always") && draft.answer.lowercase().contains("never")) out += "always_vs_never"
        return out.distinct()
    }

    private fun shareKeyToken(a: String, b: String): Boolean {
        val ta = a.lowercase().split(" ").filter { it.length >= 4 }.toSet()
        val tb = b.lowercase().split(" ").filter { it.length >= 4 }.toSet()
        return ta.intersect(tb).size >= 2
    }

    private fun detectOverclaiming(draft: Draft): List<String> {
        val forbidden = listOf("guarantee", "guaranteed", "100%", "certainly", "definitely will", "risk-free")
        val lower = draft.answer.lowercase()
        return forbidden.filter { lower.contains(it) }
    }
}
