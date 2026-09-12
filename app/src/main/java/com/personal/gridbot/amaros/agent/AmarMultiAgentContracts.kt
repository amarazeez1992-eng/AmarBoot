package com.personal.gridbot.amaros.agent

/**
 * Stage 2 multi-role contract. Roles produce structured decisions; prose is evidence,
 * not the voting key. No role has execution authority.
 */
interface AmarAnalystRole {
    val id: String
    suspend fun analyze(context: AmarAnalysisContext): AmarRoleReport
}

data class AmarAnalysisContext(
    val question: String,
    val marketSnapshot: AmarMarketSnapshot? = null,
    val evidence: List<ResearchFinding> = emptyList()
)

data class AmarMarketSnapshot(
    val symbol: String,
    val timeframe: String,
    val candles: List<AmarMarketCandle> = emptyList(),
    val quote: AmarMarketQuote? = null
)

data class AmarRoleReport(
    val roleId: String,
    val conclusion: String,
    val confidence: Double,
    val direction: AmarDecisionDirection = AmarDecisionDirection.UNKNOWN,
    val supportingEvidence: List<String> = emptyList(),
    val opposingEvidence: List<String> = emptyList(),
    val risks: List<String> = emptyList()
) {
    init {
        require(roleId.isNotBlank())
        require(confidence in 0.0..1.0)
    }
}

data class AmarDeliberationResult(
    val reports: List<AmarRoleReport>,
    val consensus: String,
    val confidence: Double,
    val conflicts: List<String> = emptyList(),
    val approvedForSimulation: Boolean,
    val consensusDirection: AmarDecisionDirection = AmarDecisionDirection.UNKNOWN
)

/**
 * Safety-first coordinator. Duplicate role IDs, unknown decisions, ties and directional
 * disagreement are explicit blockers; they are never silently normalized away.
 */
class AmarDeliberationCoordinator(
    private val minimumConfidence: Double = 0.80,
    private val minimumRoles: Int = 2
) {
    suspend fun deliberate(
        context: AmarAnalysisContext,
        roles: List<AmarAnalystRole>
    ): AmarDeliberationResult {
        val conflicts = mutableListOf<String>()
        val normalizedRoles = roles.filter { it.id.isNotBlank() }
        val duplicateIds = normalizedRoles.groupingBy { it.id }.eachCount().filterValues { it > 1 }.keys
        if (duplicateIds.isNotEmpty()) conflicts += "duplicate_role_ids:${duplicateIds.joinToString(",")}"

        val reports = normalizedRoles.map { it.analyze(context) }
        if (reports.size < minimumRoles) conflicts += "insufficient_roles"
        if (reports.any { it.conclusion.isBlank() }) conflicts += "blank_role_conclusion"

        val confidence = reports.map { it.confidence }.averageOrNull() ?: 0.0
        val actionable = reports.filter { it.direction != AmarDecisionDirection.UNKNOWN }
        if (actionable.size != reports.size) conflicts += "unknown_role_direction"

        val directionCounts = actionable.groupingBy { it.direction }.eachCount()
        val strongest = directionCounts.values.maxOrNull() ?: 0
        val tiedStrongest = directionCounts.values.count { it == strongest } > 1
        if (tiedStrongest && strongest > 0) conflicts += "direction_tie"
        if (directionCounts.size > 1) conflicts += "direction_conflict"

        val consensusDirection = if (directionCounts.size == 1 && actionable.size == reports.size) {
            directionCounts.keys.single()
        } else AmarDecisionDirection.UNKNOWN

        val highConfidenceCount = reports.count { it.confidence >= minimumConfidence }
        if (highConfidenceCount != reports.size) conflicts += "insufficient_role_confidence"

        val uniqueConclusions = reports.map { it.conclusion.trim() }.filter { it.isNotEmpty() }.distinct()
        val consensus = when {
            consensusDirection != AmarDecisionDirection.UNKNOWN -> consensusDirection.name
            uniqueConclusions.isEmpty() -> "NO_CONSENSUS"
            else -> "MULTIPLE_CONCLUSIONS"
        }

        val finalConflicts = conflicts.distinct()
        val approved = reports.size >= minimumRoles &&
            highConfidenceCount == reports.size &&
            consensusDirection != AmarDecisionDirection.UNKNOWN &&
            finalConflicts.isEmpty()

        return AmarDeliberationResult(
            reports = reports,
            consensus = consensus,
            confidence = confidence.coerceIn(0.0, 1.0),
            conflicts = finalConflicts,
            approvedForSimulation = approved,
            consensusDirection = consensusDirection
        )
    }

    private fun Iterable<Double>.averageOrNull(): Double? {
        val values = toList()
        return if (values.isEmpty()) null else values.average()
    }
}
