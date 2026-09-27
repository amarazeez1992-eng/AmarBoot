package com.personal.gridbot.amaros.intelligence.memory

import com.personal.gridbot.amaros.agent.memory.AmarMemoryEntry
import com.personal.gridbot.amaros.agent.memory.AmarMemoryRepository
import com.personal.gridbot.amaros.agent.memory.AmarMemorySource

/**
 * Stage 11 / Item 11 — Memory Conflict Detection (11 additions).
 *
 * Detects, classifies, groups, audits, and escalates conflicts between
 * memory entries. Never deletes or mutates underlying memory.
 */
class AmarMemoryConflictDetector(
    private val repository: AmarMemoryRepository,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val auditSink: AmarConflictAuditSink? = null
) {

    enum class ResolutionState {
        NO_CONFLICT, UNRESOLVED, RESOLVED_BY_AUTHORITY, RESOLVED_BY_TIME,
        RESOLVED_BY_CONFIDENCE, ESCALATED
    }

    enum class ConflictSeverity { LOW, MEDIUM, HIGH, CRITICAL }

    data class Conflict(
        val leftId: String,
        val rightId: String,
        val reason: String,
        val severity: ConflictSeverity,
        val resolution: ResolutionState,
        val winnerId: String?,
        val detectedAtEpochMs: Long
    )

    data class ConflictGroup(
        val memberIds: List<String>,
        val severity: ConflictSeverity,
        val resolution: ResolutionState,
        val winnerId: String?,
        val reasons: List<String>
    )

    data class Escalation(
        val conflict: Conflict,
        val reason: String,
        val escalatedAtEpochMs: Long
    )

    private val auditLog = mutableListOf<Conflict>()
    private val escalationLog = mutableListOf<Escalation>()

    fun scan(limit: Int = 200): List<Conflict> {
        val found = computeConflicts(limit)
        for (c in found) {
            auditLog += c
            auditSink?.onConflictDetected(c)
            if (c.resolution == ResolutionState.ESCALATED) {
                val esc = Escalation(c, "equal_authority_no_winner", clock())
                escalationLog += esc
                auditSink?.onEscalation(esc)
            }
        }
        return found
    }

    fun scanGrouped(limit: Int = 200): List<ConflictGroup> {
        val pairs = computeConflicts(limit)
        if (pairs.isEmpty()) return emptyList()

        val groups = mutableListOf<MutableSet<String>>()
        for (p in pairs) {
            val matching = groups.filter { it.contains(p.leftId) || it.contains(p.rightId) }
            if (matching.isEmpty()) {
                groups += mutableSetOf(p.leftId, p.rightId)
            } else {
                val merged = mutableSetOf<String>()
                matching.forEach { merged += it; groups.remove(it) }
                merged += p.leftId
                merged += p.rightId
                groups += merged
            }
        }

        return groups.map { memberIds ->
            val related = pairs.filter { it.leftId in memberIds && it.rightId in memberIds }
            val highest = related.maxByOrNull { severityRank(it.severity) }?.severity ?: ConflictSeverity.LOW
            val winnerIds = related.mapNotNull { it.winnerId }.distinct()
            val winner = if (winnerIds.size == 1) winnerIds.first() else null
            val resolution = if (winner == null) ResolutionState.ESCALATED
                             else related.first().resolution
            ConflictGroup(
                memberIds = memberIds.sorted(),
                severity = highest,
                resolution = resolution,
                winnerId = winner,
                reasons = related.map { it.reason }.distinct()
            )
        }
    }

    fun detectBetween(left: AmarMemoryEntry, right: AmarMemoryEntry): Conflict? {
        if (left.id == right.id) return null
        if (left.type != right.type) return null
        if (!overlaps(left, right)) return null

        val sourceWinner = compareSource(left, right)
        val authorityWinner = compareAuthority(left, right)
        val confidenceWinner = compareConfidence(left, right)
        val timeWinner = if (left.updatedAtEpochMs >= right.updatedAtEpochMs) left else right

        val severity = computeSeverity(sourceWinner, authorityWinner, confidenceWinner)
        val (winner, resolution) = decideWinner(sourceWinner, authorityWinner, confidenceWinner, timeWinner, left, right)

        return Conflict(
            leftId = left.id,
            rightId = right.id,
            reason = "conflict(${left.id},${right.id}) winner=${winner?.id ?: "none"}",
            severity = severity,
            resolution = resolution,
            winnerId = winner?.id,
            detectedAtEpochMs = clock()
        )
    }

    fun auditTrail(): List<Conflict> = auditLog.toList()
    fun escalations(): List<Escalation> = escalationLog.toList()
    fun clearAudit() { auditLog.clear(); escalationLog.clear() }

    private fun computeConflicts(limit: Int): List<Conflict> {
        require(limit > 0) { "limit must be positive" }
        val entries = repository.recent(limit)
        val found = mutableListOf<Conflict>()
        for (i in entries.indices) {
            for (j in i + 1 until entries.size) {
                val c = detectBetween(entries[i], entries[j]) ?: continue
                found += c
            }
        }
        return found
    }

    private fun overlaps(a: AmarMemoryEntry, b: AmarMemoryEntry): Boolean {
        val ta = tokens(a.text); val tb = tokens(b.text)
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

    private fun computeSeverity(
        source: AmarMemoryEntry?,
        authority: AmarMemoryEntry?,
        confidence: AmarMemoryEntry?
    ): ConflictSeverity = when {
        source != null -> ConflictSeverity.CRITICAL
        authority != null -> ConflictSeverity.HIGH
        confidence != null -> ConflictSeverity.MEDIUM
        else -> ConflictSeverity.LOW
    }

    private fun severityRank(s: ConflictSeverity): Int = when (s) {
        ConflictSeverity.LOW -> 0
        ConflictSeverity.MEDIUM -> 1
        ConflictSeverity.HIGH -> 2
        ConflictSeverity.CRITICAL -> 3
    }

    private fun decideWinner(
        source: AmarMemoryEntry?,
        authority: AmarMemoryEntry?,
        confidence: AmarMemoryEntry?,
        time: AmarMemoryEntry,
        left: AmarMemoryEntry,
        right: AmarMemoryEntry
    ): Pair<AmarMemoryEntry?, ResolutionState> {
        source?.let { return it to ResolutionState.RESOLVED_BY_AUTHORITY }
        authority?.let { return it to ResolutionState.RESOLVED_BY_AUTHORITY }
        confidence?.let { return it to ResolutionState.RESOLVED_BY_CONFIDENCE }
        if (left.permanent && right.permanent) {
            return null to ResolutionState.ESCALATED
        }
        return time to ResolutionState.RESOLVED_BY_TIME
    }
}

interface AmarConflictAuditSink {
    fun onConflictDetected(conflict: AmarMemoryConflictDetector.Conflict) {}
    fun onEscalation(escalation: AmarMemoryConflictDetector.Escalation) {}
}
