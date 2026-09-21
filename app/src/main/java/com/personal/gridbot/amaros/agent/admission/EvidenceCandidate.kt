package com.personal.gridbot.amaros.agent.admission

data class EvidenceCandidate(
    val provider: String,
    val title: String,
    val url: String,
    val excerpt: String,
    val retrievedAtEpochMs: Long
)
