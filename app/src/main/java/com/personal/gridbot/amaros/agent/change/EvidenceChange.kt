package com.personal.gridbot.amaros.agent.change

data class EvidenceChange(
    val symbol: String,
    val changeType: ChangeType,
    val evidenceFingerprint: String,
    val previousSourceUri: String?,
    val currentSourceUri: String?,
    val previousContent: String?,
    val currentContent: String?
)
