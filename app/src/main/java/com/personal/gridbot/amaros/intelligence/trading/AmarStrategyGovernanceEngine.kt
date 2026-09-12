package com.personal.gridbot.amaros.intelligence.trading

/**
 * Governance layer for strategy mutation/evolution.
 * It can generate challengers, compare measured reports and gate adoption.
 * It never mutates an approved strategy automatically.
 */
object AmarStrategyGovernanceEngine {
    enum class Gate { REJECT, CHALLENGER_ONLY, HUMAN_REVIEW, APPROVAL_READY }

    data class Mutation(val id: String, val description: String, val parentVersion: Int)
    data class Candidate(
        val name: String,
        val version: Int,
        val oosExpectancyR: Double,
        val maxDrawdownR: Double,
        val profitFactor: Double,
        val sampleSize: Int,
        val complexityDelta: Double = 0.0,
        val leakageFree: Boolean = true,
        val stressPass: Boolean = true
    )
    data class Comparison(val champion: Candidate, val challenger: Candidate, val gate: Gate, val reasons: List<String>)

    fun mutations(parentVersion: Int, requestedChanges: List<String>): List<Mutation> =
        requestedChanges.filter { it.isNotBlank() }.mapIndexed { index, change ->
            Mutation("MUT-${parentVersion + 1}-$index", change.trim(), parentVersion)
        }

    fun compare(champion: Candidate, challenger: Candidate): Comparison {
        val reasons = mutableListOf<String>()
        if (!challenger.leakageFree) reasons += "Challenger has leakage risk"
        if (!challenger.stressPass) reasons += "Challenger failed stress gate"
        if (challenger.sampleSize < 30) reasons += "OOS sample is below governance minimum of 30"
        if (challenger.maxDrawdownR > champion.maxDrawdownR * 1.15) reasons += "Drawdown worsened materially"
        if (challenger.complexityDelta > 0.25) reasons += "Complexity increased too much"
        val improves = challenger.oosExpectancyR > champion.oosExpectancyR &&
            challenger.profitFactor >= champion.profitFactor &&
            challenger.maxDrawdownR <= champion.maxDrawdownR * 1.05
        val gate = when {
            reasons.any { it.contains("leakage") } -> Gate.REJECT
            reasons.any { it.contains("failed stress") } -> Gate.REJECT
            !improves -> Gate.CHALLENGER_ONLY
            reasons.isNotEmpty() -> Gate.HUMAN_REVIEW
            else -> Gate.APPROVAL_READY
        }
        if (!improves && gate == Gate.CHALLENGER_ONLY) reasons += "No sufficient measured OOS improvement"
        if (gate != Gate.REJECT) reasons += "Human approval is mandatory before adoption"
        return Comparison(champion, challenger, gate, reasons)
    }
}
