package com.personal.gridbot.amaros.agent

/** Deterministic planning contract: planning produces intent, never broker commands. */
class AmarAgentPlanner(
    private val understanding: AmarIntentUnderstanding = AmarIntentUnderstanding()
) {
    fun plan(request: AmarAgentRequest, availableTools: List<AmarAgentTool>): AmarAgentPlan {
        val analysis = understanding.understand(request.text)
        val intent = analysis.intent
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

        val ambiguityStep = if (analysis.ambiguous) {
            "resolve_intent_ambiguity_without_guessing"
        } else {
            "intent_unambiguous"
        }

        return AmarAgentPlan(
            intent = intent,
            steps = listOf(
                "normalize_request",
                "understand_intent:" + analysis.questionForm +
                    ":confidence=" + String.format(java.util.Locale.US, "%.2f", analysis.confidence),
                "entities:" + analysis.entities.joinToString(",").ifBlank { "none" },
                ambiguityStep,
                if (intent == AgentIntent.RESEARCH || intent == AgentIntent.TRADE_ANALYSIS)
                    "retrieve_and_verify_evidence"
                else "inspect_local_context",
                "reason_with_constraints",
                "challenge_assumptions",
                if (intent == AgentIntent.TRADE_ANALYSIS && request.requireBacktestWhenApplicable)
                    "require_simulation_and_risk_check"
                else "produce_answer",
                "audit_output"
            ),
            requiredTools = tools,
            stopConditions = listOf(
                "missing_evidence",
                "policy_denied",
                "unsafe_execution_request",
                "budget_exhausted"
            )
        )
    }
}

data class AmarAgentPlan(
    val intent: AgentIntent,
    val steps: List<String>,
    val requiredTools: List<String>,
    val stopConditions: List<String>
)

enum class AgentIntent { GENERAL, RESEARCH, TRADE_ANALYSIS, STRATEGY_DESIGN }
