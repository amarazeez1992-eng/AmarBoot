package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentOrchestrator
import com.personal.gridbot.amaros.agent.AmarAgentPlanner
import com.personal.gridbot.amaros.agent.AmarAgentRequest
import com.personal.gridbot.amaros.agent.AmarAgentTool
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
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest

/**
 * AMAR AI Agent boundary.
 * The UI enters the canonical Agent Orchestrator; no UI-level shortcut bypasses
 * planning, research/verification gates, critique, hierarchy and final validation.
 * Broker execution remains fail-closed.
 */
class AmarAiAgentEngine(
    private val context: Context? = null
) {
    data class Result(
        val answer: String,
        val proposedActions: List<String>,
        val toolEvidence: List<String>
    )

    private val reasoningProvider: AmarReasoningProvider = AmarLocalReasoning()
    private val toolRegistry: AmarAgentToolRegistry = AmarTradingTools()
    private val orchestrator = AmarAgentOrchestrator(
        planner = AmarAgentPlanner(),
        researchEngine = AmarExternalResearchAdapter(),
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
                requestedSourceCount = 40,
                maximumSourceCount = 100,
                requireCrossValidation = true,
                requireBacktestWhenApplicable = true
            ),
            availableTools = toolRegistry.availableTools(AmarAgentPolicy())
        ).response
        return Result(
            answer = response.answer,
            proposedActions = response.actions,
            toolEvidence = emptyList()
        )
    }

}
