package com.personal.gridbot.amaros.agent.ranking

object RankingSignal {
    const val RELEVANCE = "relevanceScore"
    const val STATUS_WEIGHT = "statusWeight"
    const val AUTHORITY = "authorityScore"
    const val FRESHNESS = "freshnessScore"
    const val FINAL_SCORE = "finalScore"

    fun requiredKeys(): Set<String> = setOf(
        RELEVANCE, STATUS_WEIGHT, AUTHORITY, FRESHNESS, FINAL_SCORE
    )
}
