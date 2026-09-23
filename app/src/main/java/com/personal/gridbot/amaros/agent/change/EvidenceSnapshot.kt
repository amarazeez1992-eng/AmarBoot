package com.personal.gridbot.amaros.agent.change

data class EvidenceSnapshot(
    val symbol: String,
    val capturedAtEpochMs: Long,
    val entries: List<EvidenceSnapshotEntry>
)
