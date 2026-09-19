package com.personal.gridbot.amaros.agent

/** Deterministic intent planning: intent controls capability; it is not a question/answer table. */
class AmarAgentPlanner {
    fun plan(request: AmarAgentRequest, availableTools: List<AmarAgentTool>): AmarAgentPlan {
        val intent = classify(request.text)
        val tools = availableTools.filter { tool ->
            when (intent) {
                AgentIntent.RESEARCH -> tool.scope == AmarToolScope.RESEARCH || tool.scope == AmarToolScope.READ_ONLY
                AgentIntent.TRADE_ANALYSIS -> tool.scope == AmarToolScope.RESEARCH ||
                    tool.scope == AmarToolScope.READ_ONLY || tool.scope == AmarToolScope.SIMULATION
                AgentIntent.STRATEGY_DESIGN -> tool.scope == AmarToolScope.READ_ONLY ||
                    tool.scope == AmarToolScope.STRATEGY_WRITE || tool.scope == AmarToolScope.SIMULATION
                AgentIntent.GENERAL -> tool.scope == AmarToolScope.READ_ONLY
            }
        }.map { it.id }.distinct()

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
        val researchSignals = listOf(
            "ابحث", "بحث", "مصدر", "مصادر", "خبر", "أخبار", "حدث", "الأخبار", "دراسة",
            "سعر", "قيمة", "الآن", "حاليا", "حاليًا", "اليوم", "أمس", "الشهر السابق",
            "last month", "today", "now", "current", "price", "value", "news", "research"
        )
        val tradingSignals = listOf(
            "تداول", "صفقة", "ذهب", "فوركس", "سوق", "mt5", "trade", "trading", "gold", "forex",
            "backtest", "شراء", "بيع", "دخول", "خروج", "وقف", "هدف"
        )
        val strategySignals = listOf("استراتيجية", "strategy", "روبوت", "bot")

        return when {
            strategySignals.any(q::contains) && !tradingSignals.any(q::contains) -> AgentIntent.STRATEGY_DESIGN
            tradingSignals.any(q::contains) -> AgentIntent.TRADE_ANALYSIS
            researchSignals.any(q::contains) -> AgentIntent.RESEARCH
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
