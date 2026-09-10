package com.personal.gridbot.amaros.supervisor

import com.personal.gridbot.amaros.analytics.AmarAnalyticsSnapshot
import com.personal.gridbot.amaros.core.AmarRuntimeController
import com.personal.gridbot.amaros.explainability.AmarDecisionExplanation
import com.personal.gridbot.amaros.guardian.AmarGuardianDecision
import com.personal.gridbot.amaros.security.AmarSecurityState

/** B20 Supervisor contract. It observes and recommends; it cannot execute trades. */
data class AmarSupervisorSnapshot(
    val health: String,
    val guardianAllowed: Boolean,
    val securityMode: String,
    val recommendation: String,
    val explanation: AmarDecisionExplanation?,
    val analytics: AmarAnalyticsSnapshot
)

class AmarSupervisor {
    fun inspect(
        runtime: AmarRuntimeController.RuntimeState,
        analytics: AmarAnalyticsSnapshot,
        guardian: AmarGuardianDecision,
        security: AmarSecurityState,
        explanation: AmarDecisionExplanation?
    ): AmarSupervisorSnapshot {
        val recommendation = when {
            security.emergencyLock -> "STOP_AND_REVIEW_SECURITY"
            !guardian.allowed -> "HOLD_AND_REVIEW_GUARDIAN"
            runtime.health.status.name == "FAULTED" -> "STOP_AND_REVIEW_RUNTIME"
            else -> "OBSERVE"
        }
        return AmarSupervisorSnapshot(runtime.health.status.name, guardian.allowed, security.mode.name, recommendation, explanation, analytics)
    }
}
