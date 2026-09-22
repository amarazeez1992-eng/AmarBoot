package com.personal.gridbot.amaros.agent.chain

import com.personal.gridbot.amaros.agent.deterministic.DeterministicEvidenceResult
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationResult
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

data class EvidenceChainInput(
    val deterministicEvidence: DeterministicEvidenceResult,
    val historicalValidation: HistoricalValidationResult,
    val crossSourceCorrelation: CrossSourceCorrelationResult,
    val provenanceNodes: List<AmarProvenanceNode>
)

interface AmarEvidenceChainContract {
    fun build(input: EvidenceChainInput): EvidenceChainResult
}
