package com.personal.gridbot.amaros.agent.chain

enum class EvidenceChainReason {
    VALID_CHAIN,
    MISSING_PROVENANCE,
    INCONSISTENT_UPSTREAM,
    INVALID_INPUT,
    BROKEN_CHAIN
}
