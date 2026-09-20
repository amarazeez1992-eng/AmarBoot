package com.personal.gridbot.amaros.agent.admission

import com.personal.gridbot.amaros.agent.ResearchFinding

/**
 * Step 1 contract only. Runtime admission implementation is introduced separately.
 *
 * Relevance threshold ownership remains with the canonical relevance authority until
 * the migration plan explicitly transfers that responsibility.
 */
interface AmarEvidenceAdmissionContract {
    fun admit(
        question: String,
        findings: List<ResearchFinding>
    ): AdmissionResult
}
