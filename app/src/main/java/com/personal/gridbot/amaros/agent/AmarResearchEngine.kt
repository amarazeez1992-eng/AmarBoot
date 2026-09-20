package com.personal.gridbot.amaros.agent

/** Multi-source research contract. Implementations may use public/local sources. */
interface AmarResearchEngine {
    suspend fun research(request: ResearchRequest): ResearchReport
}

data class ResearchRequest(
    val question: String,
    val maxSources: Int = 80,
    val requireIndependentSources: Boolean = true,
    val targetIndependentSources: Int = 80
) {
    init {
        require(question.isNotBlank())
        require(maxSources in 1..1_000)
        require(targetIndependentSources in 1..maxSources)
    }
}

data class ResearchReport(
    val findings: List<ResearchFinding>,
    val conflicts: List<String> = emptyList(),
    val confidence: Double = 0.0
)

data class ResearchFinding(
    val sourceTitle: String,
    val sourceUri: String,
    val evidence: String,
    val authority: Authority = Authority.UNKNOWN,
    val stance: EvidenceStance = EvidenceStance.UNKNOWN,
    val publisher: String = "",
    val sourceType: AmarSourceType = AmarSourceType.KNOWLEDGE,
    /** Canonical question-to-evidence relevance score; 0.0 means not admitted. */
    val relevanceScore: Double = 0.0,
    val retrievedAtEpochMs: Long = System.currentTimeMillis(),
    val fingerprint: String = AmarEvidence.fingerprintOf("$sourceUri|$evidence")
)

enum class EvidenceStance { SUPPORTS, OPPOSES, MIXED, UNKNOWN }
enum class Authority { PRIMARY, OFFICIAL, PEER_REVIEWED, REPUTABLE, COMMUNITY, UNKNOWN }
