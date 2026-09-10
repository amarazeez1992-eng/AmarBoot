package com.personal.gridbot.amaros.guardian

import com.personal.gridbot.amaros.core.AmarRuntimeController
import com.personal.gridbot.amaros.core.AmarRuntimeHealthStatus

/** B17 Guardian: fail-closed runtime policy, read-only and execution-independent. */
data class AmarGuardianDecision(
    val allowed: Boolean,
    val reasons: List<String>,
    val evaluatedAtEpochMs: Long
)

class AmarGuardian(private val clock: () -> Long = { System.currentTimeMillis() }) {
    fun evaluate(state: AmarRuntimeController.RuntimeState): AmarGuardianDecision {
        val reasons = mutableListOf<String>()
        if (state.health.status == AmarRuntimeHealthStatus.FAULTED) reasons += "runtime_faulted"
        if (state.health.consecutiveFailures > 0) reasons += "runtime_failures_present"
        if (state.risk?.allowed != true) reasons += "risk_gate_blocked"
        if (state.decision?.executable == true) reasons += "execution_proposal_requires_boundary"
        reasons += "demo_execution_guarded"
        return AmarGuardianDecision(allowed = false, reasons = reasons, evaluatedAtEpochMs = clock())
    }
}
