package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource
import com.personal.gridbot.amaros.agent.memory.AmarMemoryType
import java.security.MessageDigest
import kotlin.math.exp

/**
 * Stage 11 item 3: deterministic intelligence over the existing memory contract.
 * It adds relevance ranking, recency decay, explicit consolidation and reproducible snapshots
 * without replacing the established repository or granting execution authority.
 */
class AmarAdvancedMemory(
    private val repository: AmarMemoryRepository,
    private val policy: AmarAdvancedMemoryPolicy = AmarAdvancedMemoryPolicy()
) {
    fun remember(entry: AmarMemoryEntry): AmarMemoryEntry {
        require(entry.text.isNotBlank())
        repository.save(entry.copy(text = entry.text.trim(), tags = entry.tags.distinct()))
        return repository.get(entry.id) ?: entry
    }

    fun recall(query: String, nowEpochMs: Long, type: AmarMemoryType? = null): List<AmarMemoryMatch> {
        require(nowEpochMs >= 0)
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return emptyList()

        return repository.search(query, policy.candidateLimit)
            .asSequence()
            .filter { type == null || it.type == type }
            .map { entry -> score(entry, normalizedQuery, nowEpochMs) }
            .filter { it.score >= policy.minimumRelevance }
            .sortedWith(compareByDescending<AmarMemoryMatch> { it.score }.thenByDescending { it.entry.updatedAtEpochMs }.thenBy { it.entry.id })
            .take(policy.resultLimit)
            .toList()
    }

    /** Explicit consolidation only: exact normalized duplicates collapse to the newest record. */
    fun consolidate(limit: Int = policy.consolidationLimit): AmarMemoryConsolidationReport {
        require(limit > 0)
        val records = repository.recent(limit.coerceAtLeast(1))
        val groups = records.groupBy { normalize(it.text) }
        var removed = 0
        val keptIds = mutableListOf<String>()

        groups.values.forEach { group ->
            val keep = group.maxWithOrNull(compareBy<AmarMemoryEntry> { it.updatedAtEpochMs }.thenBy { it.id }) ?: return@forEach
            keptIds += keep.id
            group.filter { it.id != keep.id }.forEach {
                if (repository.delete(it.id)) removed++
            }
        }
        return AmarMemoryConsolidationReport(
            inspected = records.size,
            groups = groups.size,
            removedDuplicates = removed,
            keptIds = keptIds.sorted()
        )
    }

    /** Records an explicit supersession relation without destroying historical memory. */
    fun supersede(oldId: String, replacement: AmarMemoryEntry): AmarMemoryEntry {
        require(repository.get(oldId) != null) { "Unknown memory id: $oldId" }
        val tagged = replacement.copy(tags = (replacement.tags + "supersedes:$oldId").distinct())
        repository.save(tagged)
        return tagged
    }

    fun snapshot(limit: Int = policy.snapshotLimit): AmarMemorySnapshot {
        val entries = repository.recent(limit.coerceAtLeast(1)).sortedBy { it.id }
        val canonical = entries.joinToString("\n") {
            listOf(it.id, it.type.name, it.text.trim(), it.tags.sorted().joinToString(","), it.source.name,
                it.createdAtEpochMs, it.updatedAtEpochMs, it.permanent).joinToString("|")
        }
        return AmarMemorySnapshot(entries.size, sha256(canonical), entries.map { it.id })
    }

    private fun score(entry: AmarMemoryEntry, query: String, nowEpochMs: Long): AmarMemoryMatch {
        val queryTokens = tokenize(query)
        val textTokens = tokenize(entry.text)
        val tagTokens = entry.tags.flatMap(::tokenize).toSet()
        val textOverlap = overlap(queryTokens, textTokens)
        val tagOverlap = overlap(queryTokens, tagTokens)
        val age = (nowEpochMs - entry.updatedAtEpochMs).coerceAtLeast(0L)
        val recency = exp(-age.toDouble() / policy.recencyHalfLifeMs.toDouble()).coerceIn(0.0, 1.0)
        val permanentBoost = if (entry.permanent) 0.10 else 0.0
        val sourceBoost = when (entry.source) {
            AmarMemorySource.USER, AmarMemorySource.AUDIT -> 0.10
            AmarMemorySource.RESEARCH -> 0.05
            AmarMemorySource.SYSTEM -> 0.03
        }
        val score = (textOverlap * 0.55 + tagOverlap * 0.15 + recency * 0.20 + permanentBoost + sourceBoost).coerceIn(0.0, 1.0)
        val reasons = buildList {
            if (textOverlap > 0.0) add("text-overlap")
            if (tagOverlap > 0.0) add("tag-overlap")
            if (recency >= 0.5) add("recent")
            if (entry.permanent) add("permanent")
        }
        return AmarMemoryMatch(entry, score, reasons)
    }

    private fun overlap(a: Set<String>, b: Set<String>): Double {
        if (a.isEmpty() || b.isEmpty()) return 0.0
        return a.intersect(b).size.toDouble() / a.size.toDouble()
    }

    private fun tokenize(value: String): Set<String> = normalize(value).split(' ').filter { it.length >= 2 }.toSet()

    private fun normalize(value: String): String = value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}

data class AmarAdvancedMemoryPolicy(
    val candidateLimit: Int = 100,
    val resultLimit: Int = 20,
    val minimumRelevance: Double = 0.20,
    val recencyHalfLifeMs: Long = 7L * 24L * 60L * 60L * 1000L,
    val consolidationLimit: Int = 500,
    val snapshotLimit: Int = 500
) {
    init {
        require(candidateLimit > 0)
        require(resultLimit in 1..candidateLimit)
        require(minimumRelevance in 0.0..1.0)
        require(recencyHalfLifeMs > 0)
        require(consolidationLimit > 0)
        require(snapshotLimit > 0)
    }
}

data class AmarMemoryMatch(
    val entry: AmarMemoryEntry,
    val score: Double,
    val reasons: List<String>
)

data class AmarMemoryConsolidationReport(
    val inspected: Int,
    val groups: Int,
    val removedDuplicates: Int,
    val keptIds: List<String>
)

data class AmarMemorySnapshot(
    val entryCount: Int,
    val digest: String,
    val entryIds: List<String>
)