package com.personal.gridbot.amaros.agent

/** Deterministic planning contract: planning produces intent, never broker commands. */
class AmarAgentPlanner {
    fun plan(request: AmarAgentRequest, availableTools: List<AmarAgentTool>): AmarAgentPlan {
        val intent = classify(request.text)
        val researchRequired = intent == AgentIntent.RESEARCH || intent == AgentIntent.TRADE_ANALYSIS
        val tools = availableTools
            .filter { !researchRequired || it.scope == AmarToolScope.RESEARCH || it.scope == AmarToolScope.READ_ONLY }
            .map { it.id }
        return AmarAgentPlan(
            intent = intent,
            steps = listOf(
                "normalize_request",
                if (researchRequired) "retrieve_and_verify_evidence" else "inspect_local_context",
                "reason_with_constraints",
                "challenge_assumptions",
                if (intent == AgentIntent.TRADE_ANALYSIS && request.requireBacktestWhenApplicable) "simulate_and_risk_check" else "produce_answer",
                "audit_output"
            ),
            requiredTools = tools,
            stopConditions = listOf("missing_evidence", "policy_denied", "unsafe_execution_request", "budget_exhausted")
        )
    }

    private fun classify(text: String): AgentIntent {
        val q = text.lowercase()
        return when {
            listOf("بحث", "مصدر", "دراسة", "research").any(q::contains) -> AgentIntent.RESEARCH
            listOf("تداول", "صفقة", "استراتيجية", "backtest", "mt5", "trade").any(q::contains) -> AgentIntent.TRADE_ANALYSIS
            listOf("استراتيجية", "strategy", "روبوت", "bot").any(q::contains) -> AgentIntent.STRATEGY_DESIGN
            else -> AgentIntent.GENERAL
        }
    }
}

data class AmarAgentPlan(
    val intent: AgentIntent,
    val steps: List<String>,
    val requiredTools: List<String>,
    val stopConditions: List<String>
)

enum class AgentIntent { GENERAL, RESEARCH, TRADE_ANALYSIS, STRATEGY_DESIGN }
