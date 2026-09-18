package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.intelligence.verification.AmarProvenanceNode

/** Point 8 boundary: detects mutation of an already-recorded evidence provenance chain. */
class AmarEvidenceTamperingDetector {
    fun detect(
        findings: List<ResearchFinding>,
        recordedChain: List<AmarProvenanceNode>
    ): AmarEvidenceTamperingReport {
        if (findings.size != recordedChain.size) {
            return AmarEvidenceTamperingReport(false, "Chain size does not match evidence set")
        }

        var previous = "GENESIS"
        for (index in findings.indices) {
            val finding = findings[index]
            val node = recordedChain[index]
            val expectedFingerprint = if (finding.sourceUri.isNotBlank() && finding.evidence.isNotBlank()) {
                AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}")
            } else ""
            val expectedChainHash = AmarEvidence.fingerprintOf(
                "${previous}|${index}|${finding.sourceUri}|${expectedFingerprint}|${finding.retrievedAtEpochMs}"
            )

            if (node.sequence != index ||
                node.previousHash != previous ||
                node.sourceUri != finding.sourceUri ||
                node.evidenceFingerprint != expectedFingerprint ||
                node.retrievedAtEpochMs != finding.retrievedAtEpochMs ||
                node.chainHash != expectedChainHash
            ) {
                return AmarEvidenceTamperingReport(false, "Provenance chain mismatch at index $index")
            }
            previous = node.chainHash
        }

        return AmarEvidenceTamperingReport(true, "Provenance chain intact")
    }
}

data class AmarEvidenceTamperingReport(
    val intact: Boolean,
    val rationale: String
)
