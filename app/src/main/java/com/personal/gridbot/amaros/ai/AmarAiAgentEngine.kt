package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentCore
import com.personal.gridbot.amaros.agent.AmarAgentRequest
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.AmarTradingTools

/**
 * AMAR AI Agent boundary.
 * External hosted vendors are not dependencies of the Agent core.
 * The current phase uses the verified local reasoning fallback and remains fail-closed.
 */
class AmarAiAgentEngine(
    private val context: Context? = null
) {
    data class Result(
        val answer: String,
        val proposedActions: List<String>,
        val toolEvidence: List<String>
    )

    private val core = AmarAgentCore(
        reasoningProvider = AmarLocalReasoning(),
        toolRegistry = AmarTradingTools()
    )

    suspend fun ask(_apiKey: String, _model: String, request: String): Result {
        val response = core.ask(
            AmarAgentRequest(
                text = request,
                requestedSourceCount = 40,
                maximumSourceCount = 100,
                requireCrossValidation = true,
                requireBacktestWhenApplicable = true
            )
        )
        return Result(
            answer = response.answer,
            proposedActions = response.actions,
            toolEvidence = emptyList()
        )
    }
}
