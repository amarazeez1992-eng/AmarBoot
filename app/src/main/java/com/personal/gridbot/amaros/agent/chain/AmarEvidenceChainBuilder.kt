package com.personal.gridbot.amaros.agent.chain

import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

/**
 * Stateless Point 21 adapter.
 *
 * Consumes Point 8 provenance and the canonical outputs of Points 18-20.
 * It never rebuilds AmarProvenanceChain and never recalculates upstream state.
 */
class AmarEvidenceChainBuilder : AmarEvidenceChainContract {

    override fun build(input: EvidenceChainInput): EvidenceChainResult {
        if (input.deterministicEvidence.canonicalEvidence.any {
                it.evidence.evidence.candidate.candidate.fingerprint.isBlank()
            }
        ) {
            return failed(EvidenceChainReason.INVALID_INPUT)
        }

        if (!input.deterministicEvidence.isDownstreamReady ||
            !input.historicalValidation.isDownstreamReady ||
            !input.crossSourceCorrelation.isDownstreamReady
        ) {
            return failed(EvidenceChainReason.INCONSISTENT_UPSTREAM)
        }

        val canonicalFingerprints = input.deterministicEvidence.canonicalEvidence
            .map { it.evidence.evidence.candidate.candidate.fingerprint }

        if (canonicalFingerprints.isEmpty()) {
            return failed(EvidenceChainReason.INVALID_INPUT)
        }

        if (input.provenanceNodes.isEmpty()) {
            return failed(EvidenceChainReason.MISSING_PROVENANCE)
        }

        if (!provenanceIsConsistent(input.provenanceNodes)) {
            return failed(EvidenceChainReason.BROKEN_CHAIN)
        }

        val provenanceFingerprints = input.provenanceNodes
            .map { it.evidenceFingerprint }
            .toSet()

        if (!canonicalFingerprints.all { it in provenanceFingerprints }) {
            return failed(EvidenceChainReason.MISSING_PROVENANCE)
        }

        val links = mutableListOf<EvidenceChainLink>()

        input.provenanceNodes.forEach { node ->
            links += EvidenceChainLink(
                fromFingerprint = node.previousHash,
                toFingerprint = node.evidenceFingerprint,
                linkType = ChainLinkType.PROVENANCE,
                reason = "Consumed existing Point 8 provenance node"
            )
        }

        canonicalFingerprints.zipWithNext().forEach { (from, to) ->
            links += EvidenceChainLink(
                fromFingerprint = from,
                toFingerprint = to,
                linkType = ChainLinkType.DETERMINISTIC,
                reason = "Canonical Point 18 evidence order"
            )
        }

        input.crossSourceCorrelation.correlatedGroups
            .filter { it.evidenceFingerprints.size >= 2 }
            .sortedBy { it.evidenceFingerprints.minOrNull().orEmpty() }
            .forEach { correlatedGroup ->
                val ordered = correlatedGroup.evidenceFingerprints.sorted()
                ordered.zipWithNext().forEach { (from, to) ->
                    links += EvidenceChainLink(
                        fromFingerprint = from,
                        toFingerprint = to,
                        linkType = ChainLinkType.CROSS_SOURCE,
                        reason = "Consumed Point 20 cross-source relationship: " +
                            correlatedGroup.correlationType.name
                    )
                }
            }

        val canonicalSet = canonicalFingerprints.toSet()
        input.historicalValidation.comparableCases
            .map { it.historicalCase.id }
            .filter { it in canonicalSet }
            .sorted()
            .forEach { fingerprint ->
                links += EvidenceChainLink(
                    fromFingerprint = fingerprint,
                    toFingerprint = fingerprint,
                    linkType = ChainLinkType.HISTORICAL,
                    reason = "Explicit historical-case identifier matches canonical evidence fingerprint"
                )
            }

        val orderedLinks = links.sortedWith(
            compareBy<EvidenceChainLink> { it.linkType.ordinal }
                .thenBy { it.fromFingerprint }
                .thenBy { it.toFingerprint }
        )

        return EvidenceChainResult(
            chainLinks = orderedLinks,
            chainIntegrity = ChainIntegrity.INTACT,
            isDownstreamReady = true,
            reason = EvidenceChainReason.VALID_CHAIN
        )
    }

    private fun provenanceIsConsistent(nodes: List<AmarProvenanceNode>): Boolean {
        if (nodes.first().previousHash != "GENESIS") return false
        if (nodes.zipWithNext().any { (current, next) -> next.previousHash != current.chainHash }) return false
        if (nodes.map { it.sequence } != nodes.indices.toList()) return false
        return nodes.all {
            it.evidenceFingerprint.isNotBlank() &&
                it.chainHash.isNotBlank()
        }
    }

    private fun failed(reason: EvidenceChainReason): EvidenceChainResult =
        EvidenceChainResult(
            chainLinks = emptyList(),
            chainIntegrity = when (reason) {
                EvidenceChainReason.MISSING_PROVENANCE -> ChainIntegrity.INCOMPLETE
                EvidenceChainReason.BROKEN_CHAIN -> ChainIntegrity.BROKEN
                else -> ChainIntegrity.INCOMPLETE
            },
            isDownstreamReady = false,
            reason = reason
        )
}
