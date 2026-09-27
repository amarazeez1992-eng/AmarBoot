package com.personal.gridbot.amaros.intelligence.research

/**
 * Stage 11 / Item 12 — Research Engine (8 additions).
 *
 * Orchestrates research query lifecycle. Does NOT perform retrieval itself;
 * it consumes a ResearchProvider contract and applies strategy/stopping rules.
 */
class AmarResearchEngineAuthority(
    private val provider: AmarResearchProvider,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    data class Query(val raw: String, val keywords: List<String>, val intent: Intent)
    enum class Intent { FACTUAL, COMPARATIVE, EXPLORATORY, VERIFICATION }

    data class Strategy(val maxRounds: Int, val expansionEnabled: Boolean, val diversityTarget: Int)

    data class StoppingCondition(val maxRounds: Int, val minResults: Int, val minUniqueSources: Int)

    data class ResearchRound(
        val round: Int,
        val queries: List<Query>,
        val results: List<AmarResearchResult>,
        val uniqueSources: Int,
        val stopped: Boolean,
        val stopReason: String?
    )

    data class ResearchAudit(
        val startedAtEpochMs: Long,
        val endedAtEpochMs: Long,
        val rounds: Int,
        val totalResults: Int,
        val uniqueSources: Int,
        val stopReason: String
    )

    data class ResearchReport(
        val originalQuery: Query,
        val strategy: Strategy,
        val rounds: List<ResearchRound>,
        val results: List<AmarResearchResult>,
        val audit: ResearchAudit
    )

    // Addition 1 — Query Understanding
    fun understand(raw: String): Query {
        require(raw.isNotBlank()) { "query must not be blank" }
        val lower = raw.lowercase()
        val keywords = lower.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 3 }.distinct()
        val intent = when {
            lower.contains("compare") || lower.contains("versus") || lower.contains(" vs ") -> Intent.COMPARATIVE
            lower.contains("verify") || lower.contains("confirm") || lower.contains("check") -> Intent.VERIFICATION
            lower.contains("why") || lower.contains("how") || lower.contains("explore") -> Intent.EXPLORATORY
            else -> Intent.FACTUAL
        }
        return Query(raw, keywords, intent)
    }

    // Addition 2 — Query Decomposition
    fun decompose(query: Query, maxSubQueries: Int = 3): List<Query> {
        require(maxSubQueries > 0)
        if (query.keywords.size <= 2) return listOf(query)
        val chunks = query.keywords.chunked((query.keywords.size + maxSubQueries - 1) / maxSubQueries)
        return chunks.mapIndexed { i, kw ->
            Query(
                raw = "${query.raw} [focus ${i + 1}]",
                keywords = kw,
                intent = query.intent
            )
        }.take(maxSubQueries)
    }

    // Addition 3 — Search Strategy
    fun strategyFor(query: Query): Strategy = when (query.intent) {
        Intent.FACTUAL -> Strategy(maxRounds = 1, expansionEnabled = false, diversityTarget = 2)
        Intent.VERIFICATION -> Strategy(maxRounds = 2, expansionEnabled = false, diversityTarget = 3)
        Intent.COMPARATIVE -> Strategy(maxRounds = 2, expansionEnabled = true, diversityTarget = 3)
        Intent.EXPLORATORY -> Strategy(maxRounds = 3, expansionEnabled = true, diversityTarget = 4)
    }

    // Main orchestrator
    fun run(
        rawQuery: String,
        stopping: StoppingCondition = StoppingCondition(2, 3, 2)
    ): ResearchReport {
        val started = clock()
        val query = understand(rawQuery)
        val strategy = strategyFor(query)
        val rounds = mutableListOf<ResearchRound>()
        val collected = mutableListOf<AmarResearchResult>()

        var round = 0
        var stopReason: String? = null
        while (round < strategy.maxRounds) {
            round++
            val queries = if (round == 1) decompose(query) else emptyList()

            // Addition 4 — Source Discovery + Addition 5 — Multi-Source Retrieval
            val results = queries.flatMap { provider.search(it.raw) }
            collected += results
            val uniqueSources = collected.map { it.sourceHost }.distinct().size

            // Addition 7 — Research Stopping Condition
            val stopNow = when {
                collected.size >= stopping.minResults && uniqueSources >= stopping.minUniqueSources && round >= 1 ->
                    "conditions_met".also { stopReason = it }.let { true }
                round >= strategy.maxRounds -> "max_rounds".also { stopReason = it }.let { true }
                else -> false
            }

            rounds += ResearchRound(
                round = round,
                queries = queries,
                results = results,
                uniqueSources = uniqueSources,
                stopped = stopNow,
                stopReason = if (stopNow) stopReason else null
            )
            if (stopNow) break
        }

        // Addition 6 — Research Expansion
        if (strategy.expansionEnabled && collected.size < stopping.minResults) {
            val extra = provider.search("$rawQuery expanded")
            collected += extra
        }

        // Addition 8 — Research Audit
        val ended = clock()
        val audit = ResearchAudit(
            startedAtEpochMs = started,
            endedAtEpochMs = ended,
            rounds = rounds.size,
            totalResults = collected.size,
            uniqueSources = collected.map { it.sourceHost }.distinct().size,
            stopReason = stopReason ?: "completed"
        )

        return ResearchReport(query, strategy, rounds, collected, audit)
    }
}

data class AmarResearchResult(
    val query: String,
    val sourceHost: String,
    val title: String,
    val snippet: String,
    val retrievedAtEpochMs: Long
)

interface AmarResearchProvider {
    suspend fun search(query: String): List<AmarResearchResult>
}
