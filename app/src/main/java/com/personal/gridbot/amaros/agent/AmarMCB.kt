package com.personal.gridbot.amaros.agent

/** MCB = Market/Capability Brain: discovers and ranks new capabilities without silently trusting them. */
data class AmarCapabilityCandidate(
    val capabilityId: String,
    val title: String,
    val category: String,
    val sourceId: String,
    val evidenceScore: Double,
    val licenseVerified: Boolean,
    val securityReviewed: Boolean,
    val benchmarked: Boolean,
    val regressionPassed: Boolean
) {
    val eligibleForAdmission: Boolean
        get() = licenseVerified && securityReviewed && benchmarked && regressionPassed && evidenceScore >= 0.80
}

class AmarMCB(
    private val vault: AmarKnowledgeVault = AmarKnowledgeVault()
) {
    @Synchronized fun discover(candidate: AmarCapabilityCandidate): AmarAdmissionStatus {
        val status = if (candidate.eligibleForAdmission) AmarAdmissionStatus.ADMITTED else AmarAdmissionStatus.DISCOVERED
        val now = System.currentTimeMillis()
        vault.upsert(
            AmarKnowledgeRecord(
                id = candidate.capabilityId,
                type = when (candidate.category.lowercase()) {
                    "indicator" -> AmarKnowledgeRecordType.INDICATOR
                    "strategy" -> AmarKnowledgeRecordType.STRATEGY
                    "engine" -> AmarKnowledgeRecordType.ENGINE
                    else -> AmarKnowledgeRecordType.RESEARCH
                },
                title = candidate.title,
                version = "discovered",
                status = status,
                sourceId = candidate.sourceId,
                provenanceFingerprint = AmarEvidence.fingerprintOf(candidate.toString()),
                content = candidate.toString(),
                confidence = candidate.evidenceScore.coerceIn(0.0, 1.0),
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
                tags = setOf(candidate.category, if (status == AmarAdmissionStatus.ADMITTED) "trusted" else "pending")
            )
        )
        return status
    }

    fun knowledge(): AmarKnowledgeVault = vault
}
