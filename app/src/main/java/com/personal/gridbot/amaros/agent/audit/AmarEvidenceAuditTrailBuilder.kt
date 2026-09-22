package com.personal.gridbot.amaros.agent.audit

import com.personal.gridbot.amaros.agent.chain.ChainIntegrity
import com.personal.gridbot.amaros.agent.chain.ChainLinkType
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Stateless, deterministic Point 24 evidence audit boundary. */
class AmarEvidenceAuditTrailBuilder : AmarEvidenceAuditTrailContract {

    override fun build(input: EvidenceAuditTrailInput): EvidenceAuditTrailResult {
        if (input.currentTimeMs < 0L) {
            return failed(AuditReason.INVALID_INPUT)
        }

        if (isEmptyInput(input)) {
            return EvidenceAuditTrailResult.immutable(
                entries = emptyList(),
                isDownstreamReady = true,
                reason = AuditReason.VALID_AUDIT
            )
        }

        if (!upstreamReady(input)) {
            return failed(AuditReason.MISSING_UPSTREAM)
        }

        if (input.provenanceNodes.any {
                it.evidenceFingerprint.isBlank() ||
                    it.sourceUri.isBlank() ||
                    it.retrievedAtEpochMs < 0L ||
                    it.retrievedAtEpochMs > input.currentTimeMs
            }
        ) {
            return failed(AuditReason.INVALID_INPUT)
        }

        if (input.lifecycleResult.transitions.any {
                it.timestamp < 0L || it.timestamp > input.currentTimeMs
            }
        ) {
            return failed(AuditReason.INVALID_INPUT)
        }

        if (input.provenanceNodes.map { it.evidenceFingerprint }.distinct().size !=
            input.provenanceNodes.size
        ) {
            return failed(AuditReason.AUDIT_FAILED)
        }

        return runCatching {
            val entries = mutableListOf<EvidenceAuditEntry>()

            input.provenanceNodes.forEach { node ->
                entries += entry(
                    evidenceFingerprint = node.evidenceFingerprint,
                    eventType = AuditEventType.EVIDENCE_INTAKE,
                    timestamp = node.retrievedAtEpochMs,
                    actor = "AmarProvenanceChain",
                    details = mapOf(
                        "sequence" to node.sequence.toString(),
                        "sourceUri" to node.sourceUri,
                        "previousHash" to node.previousHash,
                        "chainHash" to node.chainHash
                    )
                )
            }

            val knownFingerprints = (
                input.provenanceNodes.map { it.evidenceFingerprint } +
                    input.currentEvidence.chainLinks.flatMap { listOf(it.fromFingerprint, it.toFingerprint) }
            ).filter { it.isNotBlank() && it != "GENESIS" }.toSet()

            input.currentEvidence.chainLinks.forEach { link ->
                val fingerprint = link.toFingerprint.ifBlank { link.fromFingerprint }
                if (fingerprint.isBlank() || fingerprint == "GENESIS") {
                    throw IllegalArgumentException("Missing chain-link evidence fingerprint")
                }
                entries += entry(
                    evidenceFingerprint = fingerprint,
                    eventType = AuditEventType.CHAIN_LINK_CREATED,
                    timestamp = input.currentTimeMs,
                    actor = "AmarEvidenceChainBuilder",
                    details = mapOf(
                        "fromFingerprint" to link.fromFingerprint,
                        "toFingerprint" to link.toFingerprint,
                        "linkType" to link.linkType.name,
                        "reason" to link.reason
                    )
                )
            }

            input.historicalValidation.comparableCases.forEach { comparableCase ->
                val fingerprint = comparableCase.historicalCase.id
                if (fingerprint.isBlank() || fingerprint !in knownFingerprints) {
                    throw IllegalArgumentException("Historical validation fingerprint is not represented upstream")
                }
                entries += entry(
                    evidenceFingerprint = fingerprint,
                    eventType = AuditEventType.HISTORICAL_VALIDATED,
                    timestamp = input.currentTimeMs,
                    actor = "HistoricalValidation",
                    details = mapOf(
                        "historicalCaseId" to comparableCase.historicalCase.id,
                        "decisionTimeMs" to comparableCase.historicalCase.decisionTimeMs.toString(),
                        "differences" to comparableCase.differences.sorted().joinToString(";")
                    )
                )
            }

            input.crossSourceCorrelation.correlatedGroups.forEach { group ->
                val fingerprints = group.evidenceFingerprints.filter { it.isNotBlank() }.sorted()
                if (fingerprints.size != group.evidenceFingerprints.size || !fingerprints.all { it in knownFingerprints }) {
                    throw IllegalArgumentException("Cross-source correlation fingerprint is not represented upstream")
                }
                fingerprints.forEach { fingerprint ->
                    entries += entry(
                        evidenceFingerprint = fingerprint,
                        eventType = AuditEventType.CROSS_SOURCE_CORRELATED,
                        timestamp = input.currentTimeMs,
                        actor = "CrossSourceCorrelation",
                        details = mapOf(
                            "correlationType" to group.correlationType.name,
                            "groupFingerprints" to fingerprints.joinToString(","),
                            "sharedClaims" to group.sharedClaims.sorted().joinToString(";")
                        )
                    )
                }
            }

            input.lifecycleResult.currentStates.forEach { snapshot ->
                snapshot.transitions.forEach { transition ->
                    entries += entry(
                        evidenceFingerprint = snapshot.evidenceFingerprint,
                        eventType = AuditEventType.LIFECYCLE_TRANSITION,
                        timestamp = transition.timestamp,
                        actor = "AmarEvidenceLifecycleManager",
                        details = mapOf(
                            "fromState" to transition.fromState.name,
                            "toState" to transition.toState.name,
                            "reason" to transition.reason
                        )
                    )
                }
            }

            val ordered = entries.sortedWith(
                compareBy<EvidenceAuditEntry> { it.timestamp }
                    .thenBy { it.eventType.ordinal }
                    .thenBy { it.evidenceFingerprint }
                    .thenBy { it.actor }
                    .thenBy { it.auditId }
            )

            EvidenceAuditTrailResult.immutable(
                entries = ordered,
                isDownstreamReady = true,
                reason = AuditReason.VALID_AUDIT
            )
        }.getOrElse {
            failed(AuditReason.AUDIT_FAILED)
        }
    }

    private fun isEmptyInput(input: EvidenceAuditTrailInput): Boolean =
        input.currentEvidence.chainLinks.isEmpty() &&
            input.lifecycleResult.currentStates.isEmpty() &&
            input.lifecycleResult.transitions.isEmpty() &&
            input.provenanceNodes.isEmpty() &&
            input.historicalValidation.comparableCases.isEmpty() &&
            input.crossSourceCorrelation.correlatedGroups.isEmpty() &&
            input.currentEvidence.isDownstreamReady &&
            input.lifecycleResult.isDownstreamReady &&
            input.historicalValidation.isDownstreamReady &&
            input.crossSourceCorrelation.isDownstreamReady

    private fun upstreamReady(input: EvidenceAuditTrailInput): Boolean =
        input.currentEvidence.isDownstreamReady &&
            input.currentEvidence.chainIntegrity == ChainIntegrity.INTACT &&
            input.lifecycleResult.isDownstreamReady &&
            input.historicalValidation.isDownstreamReady &&
            input.crossSourceCorrelation.isDownstreamReady &&
            input.provenanceNodes.isNotEmpty()

    private fun entry(
        evidenceFingerprint: String,
        eventType: AuditEventType,
        timestamp: Long,
        actor: String,
        details: Map<String, String>
    ): EvidenceAuditEntry {
        val canonicalDetails = details.toSortedMap()
        val payload = buildString {
            append(evidenceFingerprint)
            append('|')
            append(eventType.name)
            append('|')
            append(timestamp)
            append('|')
            append(actor)
            append('|')
            canonicalDetails.entries.joinTo(this, separator = "&") {
                "${it.key}=${escape(it.value)}"
            }
        }
        return EvidenceAuditEntry.immutable(
            auditId = sha256(payload),
            evidenceFingerprint = evidenceFingerprint,
            eventType = eventType,
            timestamp = timestamp,
            actor = actor,
            details = canonicalDetails
        )
    }

    private fun escape(value: String): String =
        value.replace("%", "%25")
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("|", "%7C")

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(StandardCharsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun failed(reason: AuditReason): EvidenceAuditTrailResult =
        EvidenceAuditTrailResult.immutable(
            entries = emptyList(),
            isDownstreamReady = false,
            reason = reason
        )
}
