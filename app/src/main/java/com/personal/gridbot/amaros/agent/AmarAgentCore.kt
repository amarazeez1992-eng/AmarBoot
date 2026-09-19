package com.personal.gridbot.amaros.agent

/**
 * AMAR AI Agent Core.
 * Provider-neutral and capability-gated. The model never receives direct broker authority.
 */
class AmarAgentCore(
    private val reasoningProvider: AmarReasoningProvider,
    private val toolRegistry: AmarAgentToolRegistry,
    private val policy: AmarAgentPolicy = AmarAgentPolicy()
) {
    suspend fun ask(request: AmarAgentRequest): AmarAgentResponse {
        if (!policy.agentEnabled) return AmarAgentResponse.blocked("AMAR AI Agent is disabled by policy.")
        val tools = toolRegistry.availableTools(policy)
        val safeMaximumSources = request.maximumSourceCount.coerceIn(1, policy.maxResearchSources.coerceAtLeast(1))
        val safeRequestedSources = request.requestedSourceCount.coerceIn(1, safeMaximumSources)
        val context = AmarAgentContext(
            userText = request.text,
            tools = tools,
            executionAllowed = false,
            brokerAccessAllowed = false,
            requestedSourceCount = safeRequestedSources,
            maximumSourceCount = safeMaximumSources,
            requireCrossValidation = request.requireCrossValidation,
            requireBacktestWhenApplicable = request.requireBacktestWhenApplicable
        )
        return reasoningProvider.respond(context)
    }
}

data class AmarAgentRequest(
    val text: String,
    val requestedSourceCount: Int = 40,
    val maximumSourceCount: Int = 100,
    val requireCrossValidation: Boolean = true,
    val requireBacktestWhenApplicable: Boolean = true
)

data class AmarAgentResponse(
    val answer: String,
    val status: Status = Status.READY,
    val actions: List<String> = emptyList()
) {
    enum class Status { READY, BLOCKED, ERROR }
    companion object { fun blocked(message: String) = AmarAgentResponse(message, Status.BLOCKED) }
}

data class AmarAgentContext(
    val userText: String,
    val tools: List<AmarAgentTool>,
    val executionAllowed: Boolean,
    val brokerAccessAllowed: Boolean,
    val requestedSourceCount: Int = 40,
    val maximumSourceCount: Int = 100,
    val requireCrossValidation: Boolean = true,
    val requireBacktestWhenApplicable: Boolean = true
)

data class AmarAgentPolicy(
    val agentEnabled: Boolean = true,
    val allowResearch: Boolean = true,
    val allowStrategyDrafting: Boolean = true,
    val allowSimulation: Boolean = true,
    val allowBrokerExecution: Boolean = false,
    val allowBroadResearch: Boolean = true,
    val maxResearchSources: Int = 100,
    val minimumEvidenceConfidence: Double = 0.80
)

interface AmarReasoningProvider { suspend fun respond(context: AmarAgentContext): AmarAgentResponse }

data class AmarAgentTool(
    val id: String,
    val description: String,
    val scope: AmarToolScope = AmarToolScope.READ_ONLY,
    val readOnly: Boolean = scope == AmarToolScope.READ_ONLY ||
        scope == AmarToolScope.RESEARCH ||
        scope == AmarToolScope.SIMULATION
)

interface AmarAgentToolRegistry { fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> }
