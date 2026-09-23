package com.personal.gridbot.amaros.agent.change

data class EvidenceSnapshotEntry(
    val evidenceFingerprint: String,
    val sourceUri: String,
    val content: String,
    val retrievedAtEpochMs: Long
)
