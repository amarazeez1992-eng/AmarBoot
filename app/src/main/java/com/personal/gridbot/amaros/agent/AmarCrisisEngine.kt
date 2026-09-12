package com.personal.gridbot.amaros.agent

/** Detects operational/trading crises and recommends safe non-execution responses. */
class AmarCrisisEngine {
    fun assess(input: AmarCrisisInput): AmarCrisisAssessment {
        val events = mutableListOf<String>()
        if (input.drawdownPercent >= input.maxDrawdownPercent) events += "drawdown_limit_reached"
        if (input.dataAgeSeconds > input.maxDataAgeSeconds) events += "stale_market_data"
        if (input.sourceConflicts >= input.maxSourceConflicts) events += "evidence_conflict"
        if (input.brokerConnectionHealthy == false) events += "broker_connection_unhealthy"

        val severity = when {
            events.any { it == "drawdown_limit_reached" || it == "broker_connection_unhealthy" } -> AmarCrisisSeverity.CRITICAL
            events.isNotEmpty() -> AmarCrisisSeverity.WARNING
            else -> AmarCrisisSeverity.NORMAL
        }
        val actions = when (severity) {
            AmarCrisisSeverity.CRITICAL -> listOf("PAUSE", "REQUEST_REVIEW", "PRESERVE_AUDIT_TRAIL")
            AmarCrisisSeverity.WARNING -> listOf("REDUCE_CONFIDENCE", "RESEARCH_AGAIN", "REQUEST_REVIEW")
            AmarCrisisSeverity.NORMAL -> listOf("CONTINUE_MONITORING")
        }
        return AmarCrisisAssessment(severity, events, actions)
    }
}

data class AmarCrisisInput(
    val drawdownPercent: Double = 0.0,
    val maxDrawdownPercent: Double = 10.0,
    val dataAgeSeconds: Long = 0,
    val maxDataAgeSeconds: Long = 30,
    val sourceConflicts: Int = 0,
    val maxSourceConflicts: Int = 3,
    val brokerConnectionHealthy: Boolean? = true
)

data class AmarCrisisAssessment(
    val severity: AmarCrisisSeverity,
    val events: List<String>,
    val recommendedActions: List<String>
)

enum class AmarCrisisSeverity { NORMAL, WARNING, CRITICAL }
