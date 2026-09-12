package com.personal.gridbot.amaros.agent

/** Multi-source research contract. Implementations may use public/local sources. */
interface AmarResearchEngine {
    suspend fun research(request: ResearchRequest): ResearchReport
}

data class ResearchRequest(
    val question: String,
    val maxSources: Int = 8,
    val requireIndependentSources: Boolean = true
)

data class ResearchReport(
    val findings: List<ResearchFinding>,
    val conflicts: List<String> = emptyList(),
    val confidence: Double = 0.0
)

data class ResearchFinding(
    val sourceTitle: String,
    val sourceUri: String,
    val evidence: String,
    val authority: Authority = Authority.UNKNOWN
)

enum class Authority { PRIMARY, OFFICIAL, PEER_REVIEWED, REPUTABLE, COMMUNITY, UNKNOWN }
