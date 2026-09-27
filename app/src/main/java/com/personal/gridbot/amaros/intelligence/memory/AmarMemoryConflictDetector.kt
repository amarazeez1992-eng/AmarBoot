package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource

/**
 * Stage 11 / Item 11 — Memory Conflict Detection.
 *
 * Detects, classifies, and audits conflicts between memory entries.
 * Never deletes or mutates underlying memory; only reports.
 */
class AmarMemoryConflictDetector(
    private val repository: AmarMemoryRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {

    enum class ResolutionState {
        NO_CONFLICT, UNRESOLVED, RESOLVED_BY_AUTHORITY, RESOLVED_BY_TIME, RESOLVED_BY_CONFIDENCE
    }

    data class Conflict(
        val leftId: String,
        val rightId: String,
        val reason: String,
        val resolution: ResolutionState,
        val winnerId: String?,
        val detectedAtEpochMs: Long
    )

    private val auditLog = mutableListOf<Conflict>()

    fun scan(limit: Int = 200): List<Conflict> {
        require(limit > 0) { "limit must be positive" }
        val entries = repository.recent(limit)
        val found = mutableListOf<Conflict>()
        for (i in entries.indices) {
            for (j in i + 1 until entries.size) {
                val conflict = detectBetween(entries[i], entries[j]) ?: continue
                found += conflict
                auditLog += conflict
            }
        }
        return found
    }

    fun detectBetween(left: AmarMemoryEntry, right: AmarMemoryEntry): Conflict? {
        if (left.id == right.id) return null
        if (left.type != right.type) return null
        if (!overlaps(left, right)) return null

        val sourceWinner = compareSource(left, right)
        val authorityWinner = compareAuthority(left, right)
        val confidenceWinner = compareConfidence(left, right)
        val timeWinner = if (left.updatedAtEpochMs >= right.updatedAtEpochMs) left else right

        val (winner, resolution) = decideWinner(sourceWinner, authorityWinner, confidenceWinner, timeWinner)

        return Conflict(
            leftId = left.id,
            rightId = right.id,
            reason = "conflict(${left.id},${right.id}) winner=${winner?.id ?: "none"}",
            resolution = resolution,
            winnerId = winner?.id,
            detectedAtEpochMs = clock()
        )
    }

    fun auditTrail(): List<Conflict> = auditLog.toList()
    fun clearAudit() { auditLog.clear() }

    private fun overlaps(a: AmarMemoryEntry, b: AmarMemoryEntry): Boolean {
        val ta = tokens(a.text)
        val tb = tokens(b.text)
        if (ta.isEmpty() || tb.isEmpty()) return false
        val inter = ta.intersect(tb).size.toDouble()
        val union = ta.union(tb).size.toDouble()
        return (inter / union) >= 0.5
    }

    private fun tokens(text: String): Set<String> =
        text.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.length >= 4 }.toSet()

    private fun compareSource(a: AmarMemoryEntry, b: AmarMemoryEntry): AmarMemoryEntry? {
        val ra = rank(a.source); val rb = rank(b.source)
        return when { ra > rb -> a; rb > ra -> b; else -> null }
    }

    private fun rank(source: AmarMemorySource): Int = when (source) {
        AmarMemorySource.USER -> 3
        AmarMemorySource.AUDIT -> 3
        AmarMemorySource.RESEARCH -> 2
        AmarMemorySource.SYSTEM -> 1
    }

    private fun compareAuthority(a: AmarMemoryEntry, b: AmarMemoryEntry): AmarMemoryEntry? = when {
        a.permanent && !b.permanent -> a
        b.permanent && !a.permanent -> b
        else -> null
    }

    private fun compareConfidence(a: AmarMemoryEntry, b: AmarMemoryEntry): AmarMemoryEntry? = when {
        a.tags.size > b.tags.size -> a
        b.tags.size > a.tags.size -> b
        else -> null
    }

    private fun decideWinner(
        source: AmarMemoryEntry?,
        authority: AmarMemoryEntry?,
        confidence: AmarMemoryEntry?,
        time: AmarMemoryEntry
    ): Pair<AmarMemoryEntry?, ResolutionState> {
        source?.let { return it to ResolutionState.RESOLVED_BY_AUTHORITY }
        authority?.let { return it to ResolutionState.RESOLVED_BY_AUTHORITY }
        confidence?.let { return it to ResolutionState.RESOLVED_BY_CONFIDENCE }
        return time to ResolutionState.RESOLVED_BY_TIME
    }
}
