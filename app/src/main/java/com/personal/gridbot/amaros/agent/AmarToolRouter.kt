package com.personal.gridbot.amaros.agent

/**
 * Central capability router. Every agent tool must declare the minimum scope it needs.
 * Execution scopes are intentionally unavailable until a future verified MT5 milestone.
 */
class AmarToolRouter(
    private val tools: List<AmarAgentTool>
) {
    fun available(policy: AmarAgentPolicy): List<AmarAgentTool> =
        tools.filter { policy.allows(it.scope) }

    fun resolve(id: String, policy: AmarAgentPolicy): AmarAgentTool? =
        available(policy).firstOrNull { it.id == id }

    fun canInvoke(id: String, policy: AmarAgentPolicy): Boolean = resolve(id, policy) != null
}

enum class AmarToolScope {
    READ_ONLY,
    RESEARCH,
    SIMULATION,
    STRATEGY_WRITE,
    EXECUTION_FUTURE
}

fun AmarAgentPolicy.allows(scope: AmarToolScope): Boolean = when (scope) {
    AmarToolScope.READ_ONLY -> agentEnabled
    AmarToolScope.RESEARCH -> agentEnabled && allowResearch
    AmarToolScope.SIMULATION -> agentEnabled && allowSimulation
    AmarToolScope.STRATEGY_WRITE -> agentEnabled && allowStrategyDrafting
    AmarToolScope.EXECUTION_FUTURE -> agentEnabled && allowBrokerExecution
}
