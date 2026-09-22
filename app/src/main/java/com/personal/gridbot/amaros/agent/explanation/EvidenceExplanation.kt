package com.personal.gridbot.amaros.agent.explanation

data class EvidenceExplanation(
    val summary: String,
    val details: List<String>,
    val sourceInfo: String,
    val statusInfo: String,
    val language: String
) {
    init {
        require(summary.isNotBlank()) { "summary must not be blank" }
        require(details.all { it.isNotBlank() }) { "details must not contain blank entries" }
        require(sourceInfo.isNotBlank()) { "sourceInfo must not be blank" }
        require(statusInfo.isNotBlank()) { "statusInfo must not be blank" }
        require(language == "EN") { "language must be canonical EN" }
    }
}

data class EvidenceExplanationResult(
    val explained: List<ExplainedEvidence>,
    val rejected: List<ExplainedEvidence>,
    val isDownstreamReady: Boolean
) {
    companion object {
        fun empty() = EvidenceExplanationResult(emptyList(), emptyList(), false)
    }
}
