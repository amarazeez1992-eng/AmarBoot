package com.personal.gridbot.amaros.intelligence.research

import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceType
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer
import com.personal.gridbot.amaros.intelligence.verification.AmarVerificationReport
import com.personal.gridbot.amaros.workforce.AmarParallelWorkforce
import kotlinx.coroutines.CancellationException

/**
 * Stage 11 / 4 — Deep Research Orchestrator.
 *
 * Coordinates bounded parallel research, deterministic source ranking, deduplication and
 * verification. Retrieval remains provider-owned; this layer never browses directly and never
 * grants execution authority.
 */
class AmarDeepResearchOrchestrator(
    private val researchEngine: AmarResearchEngine,
    private val verificationLayer: AmarVerificationLayer = AmarVerificationLayer(),
    private val policy: AmarDeepResearchPolicy = AmarDeepResearchPolicy(),
    private val workforce: AmarParallelWorkforce<String, ResearchReport> = AmarParallelWorkforce(
        AmarParallelWorkforce.Config(
            maxWorkers = policy.maxParallel,
            timeoutMs = policy.timeoutMs,
            maxItems = policy.maxQuestions,
            cacheCapacity = 0,
            retries = policy.retries
        )
    )
) {
    suspend fun research(
        questions: List<String>,
        nowEpochMs: Long
    ): AmarDeepResearchReport {
        require(nowEpochMs >= 0L)
        val normalized = questions.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        require(normalized.isNotEmpty()) { "at least one research question is required" }
        require(normalized.size <= policy.maxQuestions) { "research question limit exceeded" }

        val items = normalized.mapIndexed { index, question ->
            AmarParallelWorkforce.WorkItem(
                key = question,
                cacheKey = "research-$index-${question.lowercase()}",
                requiresNetwork = false
            )
        }
        val execution = workforce.execute(items) {
            researchEngine.research(
                ResearchRequest(
                    question = it.key,
                    maxSources = policy.maxSourcesPerQuestion,
                    requireIndependentSources = policy.requireIndependentSources,
                    targetIndependentSources = minOf(policy.targetIndependentSources, policy.maxSourcesPerQuestion)
                )
            )
        }

        val taskReports = execution.outcomes.mapNotNull { outcome ->
            when (outcome) {
                is AmarParallelWorkforce.Outcome.Success -> buildTaskReport(outcome.key, outcome.value, nowEpochMs)
                is AmarParallelWorkforce.Outcome.Failed -> AmarResearchTaskReport(
                    question = outcome.key,
                    findings = emptyList(),
                    verification = verificationLayer.verify("", emptyList(), nowEpochMs),
                    independentSourceCount = 0,
                    error = outcome.reason
                )
                is AmarParallelWorkforce.Outcome.Skipped -> AmarResearchTaskReport(
                    question = outcome.key,
                    findings = emptyList(),
                    verification = verificationLayer.verify("", emptyList(), nowEpochMs),
                    independentSourceCount = 0,
                    error = outcome.reason
                )
            }
        }

        val ranked = rankAndDeduplicate(taskReports.flatMap { it.findings })
        val globalVerification = verificationLayer.verify("", ranked, nowEpochMs)
        val conflicts = taskReports.flatMap { it.verification.conflicts }.distinctBy { it.supportingFingerprints to it.opposingFingerprints } + globalVerification.conflicts
        val independentSources = ranked.mapNotNull { hostOf(it.sourceUri) }.distinct()
        val taskConfidence = if (taskReports.isEmpty()) 0.0 else taskReports.map { it.verification.score }.average()
        val diversityScore = (independentSources.size.toDouble() / policy.targetIndependentSources.coerceAtLeast(1)).coerceIn(0.0, 1.0)
        val confidence = (taskConfidence * 0.55 + globalVerification.score * 0.25 + diversityScore * 0.20)
            .coerceIn(0.0, 1.0)

        return AmarDeepResearchReport(
            tasks = taskReports,
            rankedFindings = ranked,
            independentSources = independentSources,
            conflicts = conflicts,
            verification = globalVerification,
            confidence = confidence,
            partial = execution.partial || taskReports.any { it.error != null }
        )
    }

    private fun buildTaskReport(question: String, report: ResearchReport, nowEpochMs: Long): AmarResearchTaskReport {
        val ranked = rankAndDeduplicate(report.findings).take(policy.maxSourcesPerQuestion)
        val verification = verificationLayer.verify("", ranked, nowEpochMs)
        return AmarResearchTaskReport(
            question = question,
            findings = ranked,
            verification = verification,
            independentSourceCount = ranked.mapNotNull { hostOf(it.sourceUri) }.distinct().size,
            error = null
        )
    }

    private fun rankAndDeduplicate(findings: List<ResearchFinding>): List<ResearchFinding> =
        findings
            .filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
            .distinctBy { it.fingerprint.ifBlank { "${it.sourceUri.trim()}|${it.evidence.trim()}" } }
            .sortedWith(
                compareByDescending<ResearchFinding> { authorityWeight(it.authority) }
                    .thenByDescending { if (it.sourceType == AmarSourceType.RESEARCH || it.sourceType == AmarSourceType.DOCUMENT) 1 else 0 }
                    .thenByDescending { it.evidence.length }
                    .thenBy { it.sourceUri }
                    .thenBy { it.fingerprint }
            )
            .take(policy.maxTotalFindings)

    private fun authorityWeight(authority: Authority): Int = when (authority) {
        Authority.PRIMARY -> 6
        Authority.OFFICIAL -> 5
        Authority.PEER_REVIEWED -> 4
        Authority.REPUTABLE -> 3
        Authority.COMMUNITY -> 2
        Authority.UNKNOWN -> 1
    }

    private fun hostOf(uri: String): String? = runCatching {
        java.net.URI(uri).host?.lowercase()?.removePrefix("www.")
    }.getOrNull()?.takeIf { it.isNotBlank() }
}

data class AmarDeepResearchPolicy(
    val maxQuestions: Int = 16,
    val maxParallel: Int = 4,
    val timeoutMs: Long = 15_000L,
    val retries: Int = 1,
    val maxSourcesPerQuestion: Int = 40,
    val targetIndependentSources: Int = 6,
    val maxTotalFindings: Int = 200,
    val requireIndependentSources: Boolean = true
) {
    init {
        require(maxQuestions in 1..64)
        require(maxParallel in 1..16)
        require(timeoutMs in 1L..120_000L)
        require(retries in 0..3)
        require(maxSourcesPerQuestion in 1..1_000)
        require(targetIndependentSources in 1..maxSourcesPerQuestion)
        require(maxTotalFindings in 1..2_000)
    }
}

data class AmarResearchTaskReport(
    val question: String,
    val findings: List<ResearchFinding>,
    val verification: AmarVerificationReport,
    val independentSourceCount: Int,
    val error: String?
)

data class AmarDeepResearchReport(
    val tasks: List<AmarResearchTaskReport>,
    val rankedFindings: List<ResearchFinding>,
    val independentSources: List<String>,
    val conflicts: List<com.personal.gridbot.amaros.intelligence.verification.AmarConflict>,
    val verification: AmarVerificationReport,
    val confidence: Double,
    val partial: Boolean
) {
    init {
        require(confidence in 0.0..1.0)
    }
}
