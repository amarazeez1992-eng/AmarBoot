package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentOrchestrator
import com.personal.gridbot.amaros.agent.AmarAgentPolicy
import com.personal.gridbot.amaros.agent.AmarAgentRequest
import com.personal.gridbot.amaros.agent.AmarAgentResponse
import com.personal.gridbot.amaros.agent.AmarAgentTool
import com.personal.gridbot.amaros.agent.AmarAgentToolRegistry
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceVerifier
import com.personal.gridbot.amaros.agent.AmarAgentEvidenceConsensus
import com.personal.gridbot.amaros.agent.AmarAgentCritic
import com.personal.gridbot.amaros.agent.AmarAgentVerifier
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.AmarToolScope
import com.personal.gridbot.amaros.agent.AmarAgentPlanner
import com.personal.gridbot.amaros.agent.AmarAgentBudget

/**
 * Canonical AMAR AI Agent application boundary.
 *
 * The Agent owns planning, evidence, verification and policy. UI/native layers are
 * transports only. No external model, API key or provider is required by this class.
 * Execution authority remains fail-closed; future MT5 work is deliberately outside
 * this milestone.
 */
class AmarAiAgentEngine(private val context: Context? = null) {
    data class Result(
        val answer: String,
        val proposedActions: List<String>,
        val toolEvidence: List<String>
    )

    private val policy = AmarAgentPolicy(
        agentEnabled = true,
        allowResearch = true,
        allowStrategyDrafting = true,
        allowSimulation = true,
        allowBrokerExecution = false,
        allowBroadResearch = true
    )

    private val tools = object : AmarAgentToolRegistry {
        override fun availableTools(policy: AmarAgentPolicy): List<AmarAgentTool> =
            AmarAiToolRegistry.all().mapNotNull { spec ->
                val scope = when (spec.name) {
                    "research_external", "multi_source_research", "trading_library_search", "bot_discovery" -> AmarToolScope.RESEARCH
                    "test_strategy", "validate_results", "precision_audit", "uncertainty_audit", "evolution_gate", "champion_challenger", "counterfactual" -> AmarToolScope.SIMULATION
                    "strategy_save", "approval_proposal" -> AmarToolScope.STRATEGY_WRITE
                    else -> AmarToolScope.READ_ONLY
                }
                if (policy.allows(scope)) AmarAgentTool(spec.name, "AMAR capability: ${spec.name}", scope) else null
            }
    }

    private val research = object : AmarResearchEngine {
        private val web = AmarAiExternalResearch()
        override suspend fun research(request: ResearchRequest): ResearchReport {
            val results = runCatching { web.search(request.question, request.maxSources) }.getOrElse {
                return ResearchReport(emptyList(), listOf("research_unavailable:${it.message ?: "unknown"}"), 0.0)
            }
            val findings = results.map {
                ResearchFinding(
                    sourceTitle = it.title,
                    sourceUri = it.url,
                    evidence = it.excerpt,
                    authority = when (it.source) {
                        "Wikipedia" -> Authority.REPUTABLE
                        "GitHub" -> Authority.REPUTABLE
                        else -> Authority.UNKNOWN
                    },
                    publisher = it.source
                )
            }
            return ResearchReport(findings, confidence = if (findings.isNotEmpty()) 0.70 else 0.0)
        }
    }

    private val orchestrator = AmarAgentOrchestrator(
        planner = AmarAgentPlanner(),
        researchEngine = research,
        sourceVerifier = AmarSourceVerifier(),
        consensusEngine = AmarAgentEvidenceConsensus(),
        critic = AmarAgentCritic(),
        verifier = AmarAgentVerifier(policy.minimumEvidenceConfidence),
        reasoningProvider = AmarLocalReasoning(),
        toolRegistry = tools,
        policy = policy
    )

    /** Compatibility signature: credentials are intentionally ignored. */
    suspend fun ask(@Suppress("UNUSED_PARAMETER") apiKey: String, @Suppress("UNUSED_PARAMETER") model: String, request: String): Result {
        return ask(request)
    }

    suspend fun ask(request: String): Result {
        val safe = request.trim()
        if (safe.isEmpty()) return Result("اكتب طلبك للوكيل أولاً.", emptyList(), emptyList())

        val result = runCatching {
            orchestrator.run(
                AmarAgentRequest(
                    text = safe,
                    requestedSourceCount = 8,
                    maximumSourceCount = 20,
                    requireCrossValidation = true,
                    requireBacktestWhenApplicable = true
                ),
                availableTools = tools.availableTools(policy),
                budget = AmarAgentBudget()
            )
        }.getOrElse {
            return Result(
                answer = "تعذر إكمال دورة الوكيل بأمان: ${it.message ?: "خطأ غير معروف"}",
                proposedActions = emptyList(),
                toolEvidence = listOf("FAIL_CLOSED|${it.javaClass.simpleName}")
            )
        }

        val evidence = result.sessionEvents.map { "${it.stage.name}|${it.message}" }
        val actions = result.plan.requiredTools.map { "AGENT_TOOL|$it" }
        val answer = result.response.answer
        return Result(answer, actions, evidence)
    }
}
