package com.personal.gridbot.amaros.agent

enum class AttributionReason {
    ALL_ATTRIBUTED,
    MISSING_SOURCE_URI,
    NO_FINDINGS,
    INVALID_INPUT
}

data class AttributionResult(
    val attributed: Boolean,
    val reason: AttributionReason,
    val unattributedFindings: List<String>
)

object SourceAttributionEnforcer {
    fun evaluate(
        report: AmarClaimVerificationReport,
        findings: List<ResearchFinding>
    ): AttributionResult {
        if (!report.accepted) {
            return AttributionResult(
                attributed = false,
                reason = AttributionReason.INVALID_INPUT,
                unattributedFindings = emptyList()
            )
        }

        if (findings.isEmpty()) {
            return AttributionResult(
                attributed = false,
                reason = AttributionReason.NO_FINDINGS,
                unattributedFindings = emptyList()
            )
        }

        val unattributed = findings.filter { it.sourceUri.isBlank() }

        if (unattributed.isNotEmpty()) {
            return AttributionResult(
                attributed = false,
                reason = AttributionReason.MISSING_SOURCE_URI,
                unattributedFindings = unattributed.map { it.fingerprint }
            )
        }

        return AttributionResult(
            attributed = true,
            reason = AttributionReason.ALL_ATTRIBUTED,
            unattributedFindings = emptyList()
        )
    }
}
