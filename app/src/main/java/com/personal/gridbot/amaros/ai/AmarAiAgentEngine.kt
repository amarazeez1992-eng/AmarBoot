package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.agent.AmarAgentOrchestrator
import com.personal.gridbot.amaros.agent.AmarAgentPlanner
import com.personal.gridbot.amaros.agent.AmarAgentRequest
import com.personal.gridbot.amaros.agent.AmarAgentToolRegistry
import com.personal.gridbot.amaros.agent.AmarAgentPolicy
import com.personal.gridbot.amaros.agent.AmarAgentResponse
import com.personal.gridbot.amaros.agent.AmarReasoningProvider
import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceVerifier
import com.personal.gridbot.amaros.agent.AmarAgentEvidenceConsensus
import com.personal.gridbot.amaros.agent.AmarAgentCritic
import com.personal.gridbot.amaros.agent.AmarAgentVerifier
import com.personal.gridbot.amaros.agent.AmarTradingTools
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import com.personal.gridbot.amaros.agent.AmarSourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Canonical AMAR AI Agent boundary.
 * The UI enters the existing orchestrator; research is connected here without bypassing
 * planning, verification, consensus, critique, or final validation.
 */
class AmarAiAgentEngine {
    data class Result(
        val answer: String,
        val proposedActions: List<String>,
        val toolEvidence: List<String>
    )

    private val reasoningProvider: AmarReasoningProvider = AmarLocalReasoning()
    private val toolRegistry: AmarAgentToolRegistry = AmarTradingTools()
    private val externalResearch = AmarAiExternalResearch()
    private val orchestrator = AmarAgentOrchestrator(
        planner = AmarAgentPlanner(),
        researchEngine = ExternalResearchAdapter(externalResearch),
        sourceVerifier = AmarSourceVerifier(),
        consensusEngine = AmarAgentEvidenceConsensus(),
        critic = AmarAgentCritic(),
        verifier = AmarAgentVerifier(),
        reasoningProvider = reasoningProvider
    )

    suspend fun ask(_apiKey: String, _model: String, request: String): Result {
        val response = orchestrator.run(
            request = AmarAgentRequest(
                text = request,
                requestedSourceCount = 6,
                maximumSourceCount = 12,
                requireCrossValidation = true,
                requireBacktestWhenApplicable = true
            ),
            availableTools = toolRegistry.availableTools(AmarAgentPolicy())
        ).response
        return Result(response.answer, response.actions, emptyList())
    }

    private class ExternalResearchAdapter(
        private val research: AmarAiExternalResearch
    ) : AmarResearchEngine {
        override suspend fun research(request: ResearchRequest): ResearchReport = withContext(Dispatchers.IO) {
            val results = research.search(request.question, request.maxSources.coerceAtMost(12))
            if (results.isEmpty()) {
                return@withContext ResearchReport(
                    findings = emptyList(),
                    conflicts = listOf("external_research_no_results"),
                    confidence = 0.0
                )
            }
            val findings = results.map {
                ResearchFinding(
                    sourceTitle = it.title,
                    sourceUri = it.url,
                    evidence = it.excerpt.ifBlank { it.title },
                    publisher = it.source,
                    sourceType = AmarSourceType.KNOWLEDGE
                )
            }
            ResearchReport(
                findings = findings,
                conflicts = emptyList(),
                confidence = (findings.size / request.maxSources.toDouble()).coerceIn(0.0, 1.0)
            )
        }
    }
}
