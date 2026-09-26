package com.personal.gridbot.amaros.agent

/**
 * Item 10 — Advanced Memory Intelligence.
 *
 * Item 10 sits above Stage 3 Memory Authority.
 * It does not own persistent memory storage.
 * It classifies, scores, retrieves, and manages the lifecycle of memory context
 * exposed by Stage 3 through a bounded contract.
 */
enum class AmarMemoryKind {
    CONTEXT,
    LONG_TERM,
    TASK,
    PREFERENCE,
    FACT
}

data class AmarMemoryEntry(
    val id: String,
    val kind: AmarMemoryKind,
    val content: String,
    val provenance: List<String>,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long? = null,
    val score: Double = 0.0
) {
    init {
        require(id.isNotBlank()) { "memory id must not be blank" }
        require(content.isNotBlank()) { "memory content must not be blank" }
        require(createdAtEpochMs > 0) { "createdAtEpochMs must be positive" }
        require(score in 0.0..1.0) { "score must be in [0,1]" }
    }
}

data class AmarMemoryQuery(
    val text: String,
    val kinds: Set<AmarMemoryKind> = AmarMemoryKind.values().toSet(),
    val maxResults: Int = 10,
    val minScore: Double = 0.0,
    val nowEpochMs: Long = System.currentTimeMillis()
) {
    init {
        require(text.isNotBlank()) { "query text must not be blank" }
        require(maxResults in 1..100) { "maxResults must be in [1,100]" }
        require(minScore in 0.0..1.0) { "minScore must be in [0,1]" }
    }
}

data class AmarMemoryMatch(
    val entry: AmarMemoryEntry,
    val relevance: Double,
    val reason: String
) {
    init {
        require(relevance in 0.0..1.0) { "relevance must be in [0,1]" }
    }
}

data class AmarMemoryRetrievalResult(
    val matches: List<AmarMemoryMatch>,
    val totalConsidered: Int,
    val filteredOut: Int,
    val decisionState: String
)

class AmarAdvancedMemory(
    private val relevanceScorer: AmarMemoryRelevanceScorer = AmarMemoryRelevanceScorer(),
    private val lifecycle: AmarMemoryLifecycle = AmarMemoryLifecycle()
) {
    fun retrieve(
        query: AmarMemoryQuery,
        entries: List<AmarMemoryEntry>
    ): AmarMemoryRetrievalResult {
        val eligible = entries.filter { entry ->
            entry.kind in query.kinds &&
                lifecycle.isActive(entry, query.nowEpochMs)
        }
        val scored = eligible.map { entry ->
            val relevance = relevanceScorer.score(query.text, entry)
            AmarMemoryMatch(
                entry = entry,
                relevance = relevance,
                reason = relevanceScorer.explain(query.text, entry)
            )
        }
        val filtered = scored
            .filter { it.relevance >= query.minScore }
            .sortedWith(
                compareByDescending<AmarMemoryMatch> { it.relevance }
                    .thenByDescending { it.entry.createdAtEpochMs }
                    .thenBy { it.entry.id }
            )
            .take(query.maxResults)

        return AmarMemoryRetrievalResult(
            matches = filtered,
            totalConsidered = entries.size,
            filteredOut = entries.size - filtered.size,
            decisionState = if (filtered.isEmpty()) "EMPTY" else "READY"
        )
    }

    fun scoreRelevance(query: String, entry: AmarMemoryEntry): Double =
        relevanceScorer.score(query, entry)

    fun isActive(entry: AmarMemoryEntry, nowEpochMs: Long = System.currentTimeMillis()): Boolean =
        lifecycle.isActive(entry, nowEpochMs)

    fun classifyKind(content: String): AmarMemoryKind {
        val normalized = content.lowercase()
        return when {
            normalized.startsWith("prefer ") || normalized.startsWith("أفضل ") ||
                normalized.startsWith("أحب ") -> AmarMemoryKind.PREFERENCE
            normalized.startsWith("task:") || normalized.startsWith("مهمة:") -> AmarMemoryKind.TASK
            normalized.startsWith("fact:") || normalized.startsWith("حقيقة:") -> AmarMemoryKind.FACT
            normalized.startsWith("context:") || normalized.startsWith("سياق:") -> AmarMemoryKind.CONTEXT
            else -> AmarMemoryKind.LONG_TERM
        }
    }
}

class AmarMemoryRelevanceScorer {
    fun score(query: String, entry: AmarMemoryEntry): Double {
        val q = tokens(query)
        if (q.isEmpty()) return 0.0
        val e = tokens(entry.content)
        val overlap = q.intersect(e).size.toDouble() / q.size.toDouble()
        val exact = if (normalize(entry.content).contains(normalize(query))) 0.25 else 0.0
        return (overlap + exact).coerceIn(0.0, 1.0)
    }

    fun explain(query: String, entry: AmarMemoryEntry): String {
        val q = tokens(query)
        val e = tokens(entry.content)
        val overlap = q.intersect(e)
        return if (overlap.isEmpty()) "no-token-overlap"
        else "overlap=" + overlap.joinToString(",")
    }

    private fun tokens(text: String): Set<String> =
        normalize(text).split(" ").filter { it.length >= 3 }.toSet()

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("""[\u064B-\u065F\u0670]"""), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace(Regex("""[^\p{L}\p{Nd}]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
}

class AmarMemoryLifecycle {
    fun isActive(entry: AmarMemoryEntry, nowEpochMs: Long): Boolean {
        val expiry = entry.expiresAtEpochMs ?: return true
        return nowEpochMs < expiry
    }

    fun isExpired(entry: AmarMemoryEntry, nowEpochMs: Long = System.currentTimeMillis()): Boolean =
        !isActive(entry, nowEpochMs)

    fun filterActive(
        entries: List<AmarMemoryEntry>,
        nowEpochMs: Long = System.currentTimeMillis()
    ): List<AmarMemoryEntry> = entries.filter { isActive(it, nowEpochMs) }
}
