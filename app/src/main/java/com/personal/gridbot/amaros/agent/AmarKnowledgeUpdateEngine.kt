package com.personal.gridbot.amaros.agent

/** Provider-neutral daily knowledge update contract; implementations may use open/public sources. */
interface AmarKnowledgeUpdateEngine {
    suspend fun update(request: AmarKnowledgeUpdateRequest): AmarKnowledgeUpdateReport
}

data class AmarKnowledgeUpdateRequest(
    val topics: List<String>,
    val maxSources: Int = 100,
    val requireIndependentSources: Boolean = true
)

data class AmarKnowledgeUpdateReport(
    val addedItems: Int,
    val updatedItems: Int,
    val rejectedItems: Int,
    val confidence: Double,
    val conflicts: List<String> = emptyList()
)
