package com.personal.gridbot.amaros.agent.protection

data class InvalidFutureEvidenceProtectionResult(
    val protected: List<ProtectedEvidence>,
    val invalid: List<InvalidEvidence>,
    val future: List<FutureEvidence>,
    val isDownstreamReady: Boolean
) {
    companion object {
        fun empty() = InvalidFutureEvidenceProtectionResult(
            emptyList(), emptyList(), emptyList(), false
        )
    }
}
