package com.personal.gridbot.amaros.agent

/** Initial read-only trading tool catalog. Execution tools are intentionally absent. */
class AmarTradingTools : AmarAgentToolRegistry {
    override fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> = buildList {
        if (policy.allowResearch) add(AmarAgentTool("market_research", "Search and compare trading evidence"))
        add(AmarAgentTool("indicator_knowledge", "Explain indicators and their limitations"))
        add(AmarAgentTool("strategy_analysis", "Analyze a strategy without executing it"))
        if (policy.allowStrategyDrafting) add(AmarAgentTool("strategy_draft", "Create a draft strategy specification"))
        if (policy.allowSimulation) add(AmarAgentTool("simulation", "Evaluate a strategy through simulation inputs"))
        add(AmarAgentTool("risk_audit", "Check risk, drawdown and failure modes"))
        add(AmarAgentTool("source_provenance", "Track source, license and evidence provenance"))
        add(AmarAgentTool("agent_memory", "Read/write approved AMAR agent memory"))
    }
}
