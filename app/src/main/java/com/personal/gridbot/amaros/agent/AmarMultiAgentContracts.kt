package com.personal.gridbot.amaros.agent

/**
 * Multi-role deliberation contract inspired by multi-agent frameworks.
 * Roles are isolated analyzers; none may execute trades directly.
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
    val supportingEvidence: List<String> = emptyList(),
    val opposingEvidence: List<String> = emptyList(),
    val risks: List<String> = emptyList()
) {
    init { require(confidence in 0.0..1.0) }
}

data class AmarDeliberationResult(
    val reports: List<AmarRoleReport>,
    val consensus: String,
    val confidence: Double,
    val conflicts: List<String> = emptyList(),
    val approvedForSimulation: Boolean
)

/** Deterministic coordinator: disagreement is surfaced, not hidden. */
class AmarDeliberationCoordinator {
    suspend fun deliberate(
        context: AmarAnalysisContext,
        roles: List<AmarAnalystRole>
    ): AmarDeliberationResult {
        val reports = roles.distinctBy { it.id }.map { it.analyze(context) }
        if (reports.isEmpty()) {
            return AmarDeliberationResult(emptyList(), "NO_ANALYSTS", 0.0, approvedForSimulation = false)
        }
        val high = reports.filter { it.confidence >= 0.80 }
        val conclusions = reports.map { it.conclusion.trim() }.filter { it.isNotEmpty() }.distinct()
        val conflicts = if (conclusions.size > 1) listOf("analyst_disagreement") else emptyList()
        val confidence = reports.map { it.confidence }.average()
        return AmarDeliberationResult(
            reports = reports,
            consensus = if (conclusions.size == 1) conclusions.single() else "MULTIPLE_CONCLUSIONS",
            confidence = confidence,
            conflicts = conflicts,
            approvedForSimulation = conflicts.isEmpty() && high.isNotEmpty()
        )
    }
}
