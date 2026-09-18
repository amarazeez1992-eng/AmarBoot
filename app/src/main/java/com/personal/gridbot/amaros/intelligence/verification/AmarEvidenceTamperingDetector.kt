package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarEvidence
import com.personal.gridbot.amaros.agent.ResearchFinding

/** Stage 11 / Item 3 / Point 8: detects mutation of an already-recorded evidence provenance chain. */
class AmarEvidenceTamperingDetector {
    fun detect(
        findings: List<ResearchFinding>,
        recordedChain: List<AmarProvenanceNode>
    ): AmarEvidenceTamperingReport {
        if (findings.size != recordedChain.size) {
            return AmarEvidenceTamperingReport(false, "Chain size does not match evidence set")
        }

        var previous = "GENESIS"
        findings.forEachIndexed { index, finding ->
            val node = recordedChain[index]
            val expectedFingerprint = if (finding.sourceUri.isNotBlank() && finding.evidence.isNotBlank()) {
                AmarEvidence.fingerprintOf("${finding.sourceUri}|${finding.evidence}")
            } else {
                ""
            }
            val expectedChainHash = AmarEvidence.fingerprintOf(
                "${previous}|${index}|${finding.sourceUri}|${expectedFingerprint}|${finding.retrievedAtEpochMs}"
            )

            val intact = node.sequence == index &&
                node.previousHash == previous &&
                node.sourceUri == finding.sourceUri &&
                node.evidenceFingerprint == expectedFingerprint &&
                node.retrievedAtEpochMs == finding.retrievedAtEpochMs &&
                node.chainHash == expectedChainHash

            if (!intact) {
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
