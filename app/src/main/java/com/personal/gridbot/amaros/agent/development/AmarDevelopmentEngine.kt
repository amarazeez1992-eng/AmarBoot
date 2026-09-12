package com.personal.gridbot.amaros.agent.development

/** Self-development is proposal + verification only; it never silently rewrites protected code. */
interface AmarDevelopmentEngine {
    suspend fun inspect(request: AmarDevelopmentRequest): AmarDevelopmentReport
}

data class AmarDevelopmentRequest(
    val goal: String,
    val codeContext: String = "",
    val runTests: Boolean = true
)

data class AmarDevelopmentReport(
    val findings: List<AmarDevelopmentFinding>,
    val proposedChanges: List<AmarProposedChange> = emptyList(),
    val safeToApply: Boolean = false
)

data class AmarDevelopmentFinding(val id: String, val severity: AmarFindingSeverity, val message: String)
enum class AmarFindingSeverity { INFO, LOW, MEDIUM, HIGH, CRITICAL }
data class AmarProposedChange(val file: String, val reason: String, val patchDescription: String)
