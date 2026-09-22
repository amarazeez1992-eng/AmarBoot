package com.personal.gridbot.amaros.agent.correlation

import com.personal.gridbot.amaros.agent.conflict.EvidenceConflictAwarenessResult
import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.agent.status.ClassifiedEvidence

data class CrossSourceCorrelationInput(
    val classifiedEvidence: List<ClassifiedEvidence>,
    val independenceStates: Map<String, Boolean>,
    val conflictAwareness: EvidenceConflictAwarenessResult,
    val deterministicEvidence: DeterministicEvidenceResult
)

interface AmarCrossSourceCorrelationContract {
    fun correlate(input: CrossSourceCorrelationInput): CrossSourceCorrelationResult
}
