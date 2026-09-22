package com.personal.gridbot.amaros.agent.audit

import com.personal.gridbot.amaros.agent.chain.EvidenceChainResult
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult
import com.personal.gridbot.amaros.agent.historical.HistoricalValidationResult
import com.personal.gridbot.amaros.agent.lifecycle.EvidenceLifecycleResult
import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

data class EvidenceAuditTrailInput(
    val currentEvidence: EvidenceChainResult,
    val lifecycleResult: EvidenceLifecycleResult,
    val provenanceNodes: List<AmarProvenanceNode>,
    val historicalValidation: HistoricalValidationResult,
    val crossSourceCorrelation: CrossSourceCorrelationResult,
    val currentTimeMs: Long
)
