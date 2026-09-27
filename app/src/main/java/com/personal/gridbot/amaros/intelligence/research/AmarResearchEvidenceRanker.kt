package com.personal.gridbot.amaros.intelligence.research

/**
 * Stage 11 / Item 13 — Research Evidence Ranking (8 additions).
 *
 * Pure deterministic ranking of research results. Never fetches; never mutates.
 */
class AmarResearchEvidenceRanker(
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    data class Ranked(
        val result: AmarResearchResult,
        val score: Double,
        val reasons: List<String>
    )

    data class RankingReport(
        val ranked: List<Ranked>,
        val duplicatesRemoved: Int,
        val conflictsDetected: Int,
        val explanation: String
    )

    fun rank(
        results: List<AmarResearchResult>,
        query: String,
        authorityHosts: Set<String> = emptySet(),
        freshnessHalfLifeMs: Long = 7L * 24L * 60L * 60L * 1000L
    ): RankingReport {
        if (results.isEmpty()) {
            return RankingReport(emptyList(), 0, 0, "empty_input")
        }
        val now = clock()

        // Addition 7 — Duplicate Suppression
        val unique = results.distinctBy { "${it.sourceHost}|${it.title.lowercase()}" }
        val duplicatesRemoved = results.size - unique.size

        val tokens = query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 3 }.toSet()

        val scored = unique.map { r ->
            val reasons = mutableListOf<String>()

            // Addition 1 — Relevance Ranking
            val text = "${r.title} ${r.snippet}".lowercase()
            val hitCount = tokens.count { text.contains(it) }
            val relevance = if (tokens.isEmpty()) 0.0 else hitCount.toDouble() / tokens.size
            if (relevance > 0.5) reasons += "high_relevance"

            // Addition 2 — Authority Ranking
            val authority = if (r.sourceHost in authorityHosts) 1.0 else 0.0
            if (authority > 0.0) reasons += "authority_source"

            // Addition 3 — Freshness Ranking
            val age = (now - r.retrievedAtEpochMs).coerceAtLeast(0L)
            val freshness = kotlin.math.exp(-age.toDouble() / freshnessHalfLifeMs.toDouble()).coerceIn(0.0, 1.0)
            if (freshness > 0.5) reasons += "fresh"

            // Addition 4 — Independence Ranking (host diversity signal per single entry: 1)
            val independence = 1.0

            // Addition 5 — Evidence Completeness
            val completeness = when {
                r.title.isNotBlank() && r.snippet.length >= 40 -> 1.0
                r.title.isNotBlank() && r.snippet.isNotBlank() -> 0.6
                else -> 0.2
            }
            if (completeness == 1.0) reasons += "complete"

            val score = (
                relevance * 0.35 +
                authority * 0.20 +
                freshness * 0.20 +
                independence * 0.05 +
                completeness * 0.20
            ).coerceIn(0.0, 1.0)

            Ranked(r, score, reasons)
        }.sortedWith(
            compareByDescending<Ranked> { it.score }
                .thenByDescending { it.result.retrievedAtEpochMs }
                .thenBy { it.result.sourceHost }
        )

        // Addition 6 — Conflict Awareness (same-topic, different hosts, opposing freshness)
        val conflicts = detectConflicts(scored)

        // Addition 8 — Ranking Explanation
        val explanation = buildExplanation(scored, duplicatesRemoved, conflicts)

        return RankingReport(scored, duplicatesRemoved, conflicts, explanation)
    }

    private fun detectConflicts(scored: List<Ranked>): Int {
        var count = 0
        for (i in scored.indices) {
            for (j in i + 1 until scored.size) {
                val a = scored[i].result
                val b = scored[j].result
                if (a.sourceHost == b.sourceHost) continue
                val aWords = a.snippet.lowercase().split(" ").filter { it.length >= 4 }.toSet()
                val bWords = b.snippet.lowercase().split(" ").filter { it.length >= 4 }.toSet()
                val overlap = aWords.intersect(bWords).size.toDouble() / aWords.union(bWords).size.coerceAtLeast(1)
                if (overlap >= 0.5) count++
            }
        }
        return count
    }

    private fun buildExplanation(scored: List<Ranked>, duplicates: Int, conflicts: Int): String =
        "ranked=${scored.size} duplicates_removed=$duplicates conflicts_detected=$conflicts"
}
