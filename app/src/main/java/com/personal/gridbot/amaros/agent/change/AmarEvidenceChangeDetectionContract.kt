package com.personal.gridbot.amaros.agent.change

data class EvidenceChangeDetectionInput(
    val currentSnapshot: EvidenceSnapshot,
    val previousSnapshot: EvidenceSnapshot?,
    val currentTimeEpochMs: Long
)

interface AmarEvidenceChangeDetectionContract {
    fun detect(input: EvidenceChangeDetectionInput): EvidenceChangeDetectionResult
}
