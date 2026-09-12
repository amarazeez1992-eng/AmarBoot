package com.personal.gridbot.amaros.agent

/** Initial read-only trading tool catalog. Execution tools are intentionally absent. */
class AmarTradingTools : AmarAgentToolRegistry {
    override fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> = buildList {
        if (policy.allowResearch) {
            add(AmarAgentTool("market_research", "Search and compare trading evidence", AmarToolScope.RESEARCH))
        }
        add(AmarAgentTool("indicator_knowledge", "Explain indicators and their limitations", AmarToolScope.READ_ONLY))
        add(AmarAgentTool("strategy_analysis", "Analyze a strategy without executing it", AmarToolScope.READ_ONLY))
        if (policy.allowStrategyDrafting) {
            add(AmarAgentTool("strategy_draft", "Create a draft strategy specification", AmarToolScope.STRATEGY_WRITE))
        }
        if (policy.allowSimulation) {
            add(AmarAgentTool("simulation", "Evaluate a strategy through simulation inputs", AmarToolScope.SIMULATION))
        }
        add(AmarAgentTool("risk_audit", "Check risk, drawdown and failure modes", AmarToolScope.READ_ONLY))
        add(AmarAgentTool("source_provenance", "Track source, license and evidence provenance", AmarToolScope.READ_ONLY))
        add(AmarAgentTool("agent_memory", "Read/write approved AMAR agent memory", AmarToolScope.READ_ONLY))
    }
}
