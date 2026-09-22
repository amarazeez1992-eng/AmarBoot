package com.personal.gridbot.amaros.agent.explanation

import com.personal.gridbot.amaros.agent.ranking.RankedEvidence

interface AmarEvidenceExplanationContract {
    fun explain(evidence: List<RankedEvidence>): EvidenceExplanationResult
}
