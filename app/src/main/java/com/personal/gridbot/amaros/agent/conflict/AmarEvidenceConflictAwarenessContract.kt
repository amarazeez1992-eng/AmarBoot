package com.personal.gridbot.amaros.agent.conflict

import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.intelligence.verification.AmarConflict

data class AmarEvidenceConflictAwarenessInput(
    val findings: List<ResearchFinding>,
    val conflicts: List<AmarConflict>?
)

interface AmarEvidenceConflictAwarenessContract {
    fun evaluate(input: AmarEvidenceConflictAwarenessInput): EvidenceConflictAwarenessResult
}
