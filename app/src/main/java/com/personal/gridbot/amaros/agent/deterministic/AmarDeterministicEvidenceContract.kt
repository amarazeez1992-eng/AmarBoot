package com.personal.gridbot.amaros.agent.deterministic

import com.personal.gridbot.amaros.agent.protection.InvalidFutureEvidenceProtectionResult

interface AmarDeterministicEvidenceContract {
    fun handle(input: InvalidFutureEvidenceProtectionResult): DeterministicEvidenceResult
}
