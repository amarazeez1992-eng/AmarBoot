package com.personal.gridbot.amaros.agent.research

import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import com.personal.gridbot.amaros.agent.AmarResearchEngine

/** Daily learning coordinator. Providers perform retrieval; this layer schedules bounded, verified learning. */
class AmarDailyKnowledgeEngine(
    private val researchEngine: AmarResearchEngine,
    private val catalog: AmarSourceCatalog = AmarSourceCatalog()
) {
    suspend fun learnDaily(topics: List<String>, maxSourcesPerTopic: Int = 20): List<ResearchReport> {
        val bounded = maxSourcesPerTopic.coerceIn(1, 100)
        return topics.filter { it.isNotBlank() }.map { topic ->
            researchEngine.research(
                ResearchRequest(
                    question = topic,
                    maxSources = bounded,
                    requireIndependentSources = true,
                    targetIndependentSources = minOf(10, bounded)
                )
            )
        }
    }

    fun sourceDirectory() = catalog.defaultLinks()
}
