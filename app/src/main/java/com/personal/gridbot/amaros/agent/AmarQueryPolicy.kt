package com.personal.gridbot.amaros.agent

/**
 * Query Policy is the canonical traffic policy between intent understanding and
 * the existing Agent pipeline. It selects the evidence strictness required by
 * the request without creating a second Agent or duplicating the orchestration.
 *
 * Classification is intent-driven, not a raw financial-keyword gate.
 */
class AmarQueryPolicy {
    fun classify(plan: AmarAgentPlan, request: AmarAgentRequest): AmarQueryPolicyDecision {
        return when (plan.intent) {
            AgentIntent.SYSTEM_IDENTITY,
            AgentIntent.SYSTEM_TIME,
            AgentIntent.SYSTEM_DATE,
            AgentIntent.GENERAL,
            AgentIntent.SMALL_TALK -> AmarQueryPolicyDecision(
                mode = AmarQueryPolicyMode.LOCAL_CONVERSATIONAL,
                requiresResearch = false,
                requiresStrictEvidence = false
            )

            AgentIntent.RESEARCH -> AmarQueryPolicyDecision(
                mode = AmarQueryPolicyMode.GENERAL_FACTUAL,
                requiresResearch = true,
                requiresStrictEvidence = false
            )

            AgentIntent.TRADE_ANALYSIS -> AmarQueryPolicyDecision(
                mode = AmarQueryPolicyMode.FINANCIAL_TRADING,
                requiresResearch = true,
                requiresStrictEvidence = true
            )

            AgentIntent.STRATEGY_DESIGN -> AmarQueryPolicyDecision(
                mode = AmarQueryPolicyMode.STRATEGY_ENGINEERING,
                requiresResearch = true,
                requiresStrictEvidence = false
            )
        }
    }
}

enum class AmarQueryPolicyMode {
    LOCAL_CONVERSATIONAL,
    GENERAL_FACTUAL,
    FINANCIAL_TRADING,
    STRATEGY_ENGINEERING
}

data class AmarQueryPolicyDecision(
    val mode: AmarQueryPolicyMode,
    val requiresResearch: Boolean,
    val requiresStrictEvidence: Boolean
)
