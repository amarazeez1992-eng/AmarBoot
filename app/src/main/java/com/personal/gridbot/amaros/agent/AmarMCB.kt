package com.personal.gridbot.amaros.agent

/**
 * MCB = Market/Capability Brain.
 *
 * It discovers and ranks capabilities, but admission is fail-closed. A capability
 * cannot become trusted until every mandatory institutional gate is satisfied.
 *
 * NOTE: this type intentionally has a unique name because AmarCapabilityCandidate
 * is already the canonical discovery-fabric candidate contract.
 */
data class AmarMCBCapabilityCandidate(
    val capabilityId: String,
    val title: String,
    val category: String,
    val sourceId: String,
    val evidenceScore: Double,
    val provenanceVerified: Boolean,
    val licenseVerified: Boolean,
    val securityReviewed: Boolean,
    val deterministicTestsPassed: Boolean,
    val domainValidated: Boolean,
    val benchmarked: Boolean,
    val regressionPassed: Boolean,
    val adversarialReviewed: Boolean
) {
    init {
        require(capabilityId.isNotBlank())
        require(title.isNotBlank())
        require(category.isNotBlank())
        require(sourceId.isNotBlank())
        require(evidenceScore.isFinite() && evidenceScore in 0.0..1.0)
    }

    val eligibleForAdmission: Boolean
        get() = provenanceVerified &&
            licenseVerified &&
            securityReviewed &&
            deterministicTestsPassed &&
            domainValidated &&
            benchmarked &&
            regressionPassed &&
            adversarialReviewed &&
            evidenceScore >= 0.80
}

class AmarMCB(
    private val vault: AmarKnowledgeVault = AmarKnowledgeVault()
) {
    @Synchronized
    fun discover(candidate: AmarMCBCapabilityCandidate): AmarAdmissionStatus {
        val status = if (candidate.eligibleForAdmission) {
            AmarAdmissionStatus.ADMITTED
        } else {
            AmarAdmissionStatus.DISCOVERED
        }
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
                confidence = candidate.evidenceScore,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
                tags = setOf(
                    candidate.category,
                    if (status == AmarAdmissionStatus.ADMITTED) "trusted" else "pending"
                )
            )
        )
        return status
    }

    fun knowledge(): AmarKnowledgeVault = vault
}
