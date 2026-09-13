package com.personal.gridbot.amaros.agent

/** Stage 3: bounded memory + unified evidence retrieval with provenance and freshness. */
class AmarStageThreeEngine(
    private val memory: AmarAgentMemoryStore = AmarAgentMemoryStore(),
    private val maxRetrievedMemories: Int = 8,
    private val maxRetrievedEvidence: Int = 20
) {
    init {
        require(maxRetrievedMemories > 0)
        require(maxRetrievedEvidence > 0)
    }

    fun synchronize(question: String, findings: List<ResearchFinding>, nowEpochMs: Long = System.currentTimeMillis()): AmarStageThreeResult {
        require(question.isNotBlank())
        val safeQuestion = question.trim()
        val valid = findings
            .filter { it.sourceUri.isNotBlank() && it.evidence.isNotBlank() }
            .distinctBy { evidenceKey(it) }
            .take(100)

        // Retrieve only evidence that was already in memory before this synchronization.
        // Newly supplied findings must not be counted as retrieved memory in the same pass.
        val retrieved = memory.search(safeQuestion, nowEpochMs).take(maxRetrievedMemories)

        valid.forEach { finding ->
            memory.remember(
                AmarMemoryRecord(
                    key = evidenceKey(finding),
                    query = safeQuestion,
                    content = finding.evidence.trim(),
                    sourceUri = finding.sourceUri.trim(),
                    sourceTitle = finding.sourceTitle.trim(),
                    fingerprint = finding.fingerprint.ifBlank { AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}") },
                    createdAtEpochMs = nowEpochMs,
                    expiresAtEpochMs = nowEpochMs + MEMORY_TTL_MS
                )
            )
        }

        val evidence = (valid.map { it } + retrieved.map { it.toFinding() })
            .distinctBy { evidenceKey(it) }
            .take(maxRetrievedEvidence)
        val independentSources = evidence.mapNotNull { hostOf(it.sourceUri) }.distinct().size
        val staleCount = evidence.count { nowEpochMs - it.retrievedAtEpochMs >= MEMORY_TTL_MS }

        return AmarStageThreeResult(
            query = safeQuestion,
            newEvidenceCount = valid.size,
            retrievedMemoryCount = retrieved.size,
            unifiedEvidence = evidence,
            independentSourceCount = independentSources,
            staleEvidenceCount = staleCount,
            memorySize = memory.size(nowEpochMs)
        )
    }

    private fun evidenceKey(finding: ResearchFinding): String =
        finding.fingerprint.ifBlank { "${finding.sourceUri}|${finding.sourceTitle}|${finding.evidence}" }

    private fun hostOf(uri: String): String? = runCatching {
        java.net.URI(uri).host?.lowercase()?.removePrefix("www.")
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private companion object {
        const val MEMORY_TTL_MS = 7L * 24L * 60L * 60L * 1000L
    }
}

class AmarAgentMemoryStore(
    private val capacity: Int = 500
) {
    private val records = LinkedHashMap<String, AmarMemoryRecord>()

    init { require(capacity > 0) }

    @Synchronized
    fun remember(record: AmarMemoryRecord) {
        if (record.key.isBlank() || record.content.isBlank() || record.sourceUri.isBlank()) return
        records.remove(record.key)
        records[record.key] = record
        while (records.size > capacity) records.remove(records.keys.first())
    }

    @Synchronized
    fun search(query: String, nowEpochMs: Long = System.currentTimeMillis()): List<AmarMemoryRecord> {
        purgeExpired(nowEpochMs)
        val tokens = tokens(query)
        return records.values
            .map { it to overlap(tokens, tokens("${it.query} ${it.content} ${it.sourceTitle}")) }
            .filter { it.second > 0.0 }
            .sortedWith(compareByDescending<Pair<AmarMemoryRecord, Double>> { it.second }.thenByDescending { it.first.createdAtEpochMs })
            .map { it.first }
    }

    @Synchronized
    fun size(nowEpochMs: Long = System.currentTimeMillis()): Int {
        purgeExpired(nowEpochMs)
        return records.size
    }

    @Synchronized
    fun snapshot(nowEpochMs: Long = System.currentTimeMillis()): List<AmarMemoryRecord> {
        purgeExpired(nowEpochMs)
        return records.values.toList()
    }

    private fun purgeExpired(nowEpochMs: Long) {
        records.entries.removeIf { it.value.expiresAtEpochMs <= nowEpochMs }
    }

    private fun tokens(text: String): Set<String> = text.lowercase()
        .split(Regex("[^\\p{L}\\p{N}]+"))
        .filter { it.length >= 3 }
        .toSet()

    private fun overlap(a: Set<String>, b: Set<String>): Double =
        if (a.isEmpty()) 0.0 else a.intersect(b).size.toDouble() / a.size
}

data class AmarMemoryRecord(
    val key: String,
    val query: String,
    val content: String,
    val sourceUri: String,
    val sourceTitle: String,
    val fingerprint: String,
    val createdAtEpochMs: Long,
    val expiresAtEpochMs: Long
) {
    fun toFinding(): ResearchFinding = ResearchFinding(
        sourceTitle = sourceTitle,
        sourceUri = sourceUri,
        evidence = content,
        retrievedAtEpochMs = createdAtEpochMs,
        fingerprint = fingerprint
    )
}

data class AmarStageThreeResult(
    val query: String,
    val newEvidenceCount: Int,
    val retrievedMemoryCount: Int,
    val unifiedEvidence: List<ResearchFinding>,
    val independentSourceCount: Int,
    val staleEvidenceCount: Int,
    val memorySize: Int
)
