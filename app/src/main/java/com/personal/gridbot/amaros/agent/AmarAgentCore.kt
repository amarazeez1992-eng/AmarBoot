package com.personal.gridbot.amaros.agent

/**
 * AMAR AI Agent Core.
 *
 * Independent orchestration layer for the proprietary AMAR trading agent.
 * No Gemini, ChatGPT, broker or MT5 dependency is required by this core.
 * External providers can be plugged in later through [AmarReasoningProvider].
 */
class AmarAgentCore(
    private val reasoningProvider: AmarReasoningProvider,
    private val toolRegistry: AmarAgentToolRegistry,
    private val policy: AmarAgentPolicy = AmarAgentPolicy()
) {
    suspend fun ask(request: AmarAgentRequest): AmarAgentResponse {
        if (!policy.agentEnabled) {
            return AmarAgentResponse.blocked("AMAR AI Agent is disabled by policy.")
        }
        val tools = toolRegistry.availableTools(policy)
        val context = AmarAgentContext(
            userText = request.text,
            tools = tools,
            executionAllowed = false,
            brokerAccessAllowed = false
        )
        return reasoningProvider.respond(context)
    }
}

data class AmarAgentRequest(val text: String)

data class AmarAgentResponse(
    val answer: String,
    val status: Status = Status.READY,
    val actions: List<String> = emptyList()
) {
    enum class Status { READY, BLOCKED, ERROR }

    companion object {
        fun blocked(message: String) = AmarAgentResponse(message, Status.BLOCKED)
    }
}

data class AmarAgentContext(
    val userText: String,
    val tools: List<AmarAgentTool>,
    val executionAllowed: Boolean,
    val brokerAccessAllowed: Boolean
)

data class AmarAgentPolicy(
    val agentEnabled: Boolean = true,
    val allowResearch: Boolean = true,
    val allowStrategyDrafting: Boolean = true,
    val allowSimulation: Boolean = true,
    val allowBrokerExecution: Boolean = false
)

interface AmarReasoningProvider {
    suspend fun respond(context: AmarAgentContext): AmarAgentResponse
}

data class AmarAgentTool(
    val id: String,
    val description: String,
    val readOnly: Boolean = true
)

interface AmarAgentToolRegistry {
    fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool>
}
