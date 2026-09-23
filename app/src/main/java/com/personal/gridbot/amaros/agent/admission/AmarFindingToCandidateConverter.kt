package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.ResearchFinding

class AmarFindingToCandidateConverter {
    fun toCandidate(finding: ResearchFinding): EvidenceCandidate =
        EvidenceCandidate(
            provider = finding.publisher,
            title = finding.sourceTitle,
            url = finding.sourceUri,
            excerpt = finding.evidence,
            retrievedAtEpochMs = finding.retrievedAtEpochMs
        )
}
