package com.personal.gridbot.amaros.agent.chain

data class EvidenceChainLink(
    val fromFingerprint: String,
    val toFingerprint: String,
    val linkType: ChainLinkType,
    val reason: String
)
