package com.personal.gridbot.amaros.agent.lifecycle

import com.personal.gridbot.amaros.agent.chain.EvidenceChainResult
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

data class EvidenceLifecycleInput(
    val currentEvidence: EvidenceChainResult,
    val previousSnapshots: Map<String, EvidenceLifecycleSnapshot>,
    val provenanceNodes: List<AmarProvenanceNode>,
    val currentTimeMs: Long,
    val policy: LifecyclePolicy
)

interface AmarEvidenceLifecycleContract {
    fun evaluate(input: EvidenceLifecycleInput): EvidenceLifecycleResult
}
