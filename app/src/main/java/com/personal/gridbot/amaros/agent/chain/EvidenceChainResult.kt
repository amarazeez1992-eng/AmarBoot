package com.personal.gridbot.amaros.agent.chain

data class EvidenceChainResult(
    val chainLinks: List<EvidenceChainLink>,
    val chainIntegrity: ChainIntegrity,
    val isDownstreamReady: Boolean,
    val reason: EvidenceChainReason
)
