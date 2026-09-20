package com.personal.gridbot.amaros.agent

/** Deterministic planning contract: planning produces intent, never broker commands. */
class AmarAgentPlanner {
    fun plan(request: AmarAgentRequest, availableTools: List<AmarAgentTool>): AmarAgentPlan {
        val intent = classify(request.text)
        val tools = availableTools
            .filter { tool ->
                when (intent) {
                    AgentIntent.RESEARCH -> tool.scope == AmarToolScope.RESEARCH || tool.scope == AmarToolScope.READ_ONLY
                    AgentIntent.TRADE_ANALYSIS -> tool.scope == AmarToolScope.RESEARCH ||
                        tool.scope == AmarToolScope.READ_ONLY || tool.scope == AmarToolScope.SIMULATION
                    AgentIntent.STRATEGY_DESIGN -> tool.scope == AmarToolScope.READ_ONLY ||
                        tool.scope == AmarToolScope.STRATEGY_WRITE || tool.scope == AmarToolScope.SIMULATION
                    AgentIntent.GENERAL -> tool.scope == AmarToolScope.READ_ONLY
                }
            }
            .map { it.id }
            .distinct()
        return AmarAgentPlan(
            intent = intent,
            steps = listOf(
                "normalize_request",
                if (intent == AgentIntent.RESEARCH || intent == AgentIntent.TRADE_ANALYSIS) "retrieve_and_verify_evidence" else "inspect_local_context",
                "reason_with_constraints",
                "challenge_assumptions",
                if (intent == AgentIntent.TRADE_ANALYSIS && request.requireBacktestWhenApplicable) "require_simulation_and_risk_check" else "produce_answer",
                "audit_output"
            ),
            requiredTools = tools,
            stopConditions = listOf("missing_evidence", "policy_denied", "unsafe_execution_request", "budget_exhausted")
        )
    }

    private fun classify(text: String): AgentIntent {
        val q = text.lowercase().trim()
        val greeting = listOf("هلو", "مرحبا", "مرحباً", "السلام عليكم", "hello", "hi", "hey")
            .any { q == it || q.startsWith("$it ") }
        if (greeting) return AgentIntent.GENERAL

        return when {
            listOf("استراتيجية", "strategy", "روبوت", "bot").any(q::contains) &&
                !listOf("صفقة", "تداول", "mt5", "trade").any(q::contains) -> AgentIntent.STRATEGY_DESIGN
            listOf("تداول", "صفقة", "backtest", "mt5", "trade", "ذهب", "gold", "forex", "سوق", "market")
                .any(q::contains) -> AgentIntent.TRADE_ANALYSIS
            else -> AgentIntent.RESEARCH
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
