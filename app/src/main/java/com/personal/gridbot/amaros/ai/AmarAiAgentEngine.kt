package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.agent.AmarAgentOrchestrator
import com.personal.gridbot.amaros.agent.AmarAgentPlanner
import com.personal.gridbot.amaros.agent.AmarAgentRequest
import com.personal.gridbot.amaros.agent.AmarAgentToolRegistry
import com.personal.gridbot.amaros.agent.AmarAgentPolicy
import com.personal.gridbot.amaros.agent.AmarReasoningProvider
import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceVerifier
import com.personal.gridbot.amaros.agent.AmarAgentEvidenceConsensus
import com.personal.gridbot.amaros.agent.AmarAgentCritic
import com.personal.gridbot.amaros.agent.AmarAgentVerifier
import com.personal.gridbot.amaros.agent.AmarTradingTools
import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.AmarEvidenceAdmissionLayer

/**
 * AMAR AI Agent boundary.
 * The UI enters the canonical Agent Orchestrator; no UI-level shortcut bypasses
 * planning, research/verification gates, critique, hierarchy and final validation.
 */
class AmarAiAgentEngine(
    private val context: Context? = null
) {
    data class Result(
        val answer: String,
        val proposedActions: List<String>,
        val toolEvidence: List<String>,
        val sourcesSearched: Int = 0,
        val sourcesAccepted: Int = 0,
        val elapsedMs: Long = 0L
    )

    private val reasoningProvider: AmarReasoningProvider = AmarLocalReasoning()
    private val toolRegistry: AmarAgentToolRegistry = AmarTradingTools()
    private val externalResearch = AmarAiExternalResearch()

    private val orchestrator = AmarAgentOrchestrator(
        planner = AmarAgentPlanner(),
        researchEngine = ExternalResearchAdapter(externalResearch),
        sourceVerifier = AmarSourceVerifier(),
        consensusEngine = AmarAgentEvidenceConsensus(),
        critic = AmarAgentCritic(),
        verifier = AmarAgentVerifier(),
        reasoningProvider = reasoningProvider
    )

    suspend fun ask(_apiKey: String, _model: String, request: String, progress: ((com.personal.gridbot.amaros.agent.AmarAgentProgress) -> Unit)? = null): Result {
        val startedAt = System.currentTimeMillis()
        val runResult = orchestrator.run(
            request = AmarAgentRequest(
                text = request,
                requestedSourceCount = 80,
                maximumSourceCount = 100,
                requireCrossValidation = true,
                requireBacktestWhenApplicable = true
            ),
            availableTools = toolRegistry.availableTools(AmarAgentPolicy()),
            progress = progress
        )
        val response = runResult.response
        val elapsedMs = System.currentTimeMillis() - startedAt
        val discoveredSources = runResult.research?.findings
            ?.asSequence()
            ?.map { it.sourceUri.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.count()
            ?: 0
        val acceptedSources = runResult.sourceVerification?.totalSources ?: 0

        // Final telemetry is emitted from the canonical run result, not from
        // UI defaults. Counts are factual records returned/accepted by AMAR.
        progress?.invoke(
            com.personal.gridbot.amaros.agent.AmarAgentProgress(
                state = com.personal.gridbot.amaros.agent.AgentTaskState.RESPONDING,
                message = "اكتمل التحقق وإعداد النتيجة",
                sourcesSearched = discoveredSources,
                sourcesAccepted = acceptedSources,
                elapsedMs = elapsedMs
            )
        )

        return Result(
            answer = response.answer,
            proposedActions = response.actions,
            toolEvidence = emptyList(),
            sourcesSearched = discoveredSources,
            sourcesAccepted = acceptedSources,
            elapsedMs = elapsedMs
        )
    }

    /**
     * Adapter keeps the canonical AmarResearchEngine contract intact while allowing
     * the approved keyless public-web retrieval implementation to feed the orchestrator.
     */
    private class ExternalResearchAdapter(
        private val external: AmarAiExternalResearch,
        private val admission: AmarEvidenceAdmissionLayer = AmarEvidenceAdmissionLayer()
    ) : AmarResearchEngine {
        override suspend fun research(request: ResearchRequest): ResearchReport {
            val results = external.search(request.question, request.maxSources)
            val findings = results.map { source ->
                ResearchFinding(
                    sourceTitle = source.title.ifBlank { source.source },
                    sourceUri = source.url,
                    evidence = source.excerpt,
                    authority = authorityFor(source),
                    publisher = source.source,
                    relevanceScore = source.relevanceScore
                )
            }
            val admissionResult = admission.admit(request.question, findings)
            val admitted = admissionResult.admitted.map { it.finding.copy(relevanceScore = it.relevanceScore) }
            val rejectedCount = admissionResult.rejected.size
            val conflicts = buildList {
                if (admitted.isEmpty()) add("no_question_relevant_evidence_admitted")
                if (rejectedCount > 0) add("retrieval_candidates_rejected_by_admission=$rejectedCount")
            }
            val distinctPublishers = admitted.map { it.publisher }.filter { it.isNotBlank() }.distinct().size
            val evidenceCoverage = admitted.count { it.evidence.isNotBlank() }.toDouble() / admitted.size.coerceAtLeast(1)
            val authorityCoverage = admitted.count { it.authority != Authority.UNKNOWN }.toDouble() / admitted.size.coerceAtLeast(1)
            val independenceCoverage = (distinctPublishers.toDouble() / admitted.size.coerceAtLeast(1)).coerceIn(0.0, 1.0)
            val confidence = if (admitted.isEmpty()) 0.0 else
                (0.40 * evidenceCoverage + 0.35 * authorityCoverage + 0.25 * independenceCoverage).coerceIn(0.0, 1.0)
            return ResearchReport(
                findings = admitted,
                conflicts = conflicts,
                confidence = confidence
            )
        }

        private fun authorityFor(source: AmarAiExternalResearch.SourceResult): Authority {
            val host = runCatching { java.net.URI(source.url).host.orEmpty().lowercase() }.getOrDefault("")
            return when {
                host.endsWith(".gov") || host.contains(".gov.") -> Authority.OFFICIAL
                host.endsWith(".edu") || host.contains(".edu.") -> Authority.PEER_REVIEWED
                source.source == "Wikipedia" -> Authority.REPUTABLE
                source.source == "GitHub" -> Authority.COMMUNITY
                host.contains("reuters.com") || host.contains("apnews.com") ||
                    host.contains("bbc.com") || host.contains("nature.com") ||
                    host.contains("arxiv.org") -> Authority.REPUTABLE
                else -> Authority.UNKNOWN
            }
        }
    }
}
