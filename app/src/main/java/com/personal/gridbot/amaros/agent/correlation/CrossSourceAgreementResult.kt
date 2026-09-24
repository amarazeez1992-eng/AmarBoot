package com.personal.gridbot.amaros.agent.correlation

data class CrossSourceAgreementResult(
    val status: CrossSourceAgreementStatus,
    val agreementGroups: List<CorrelatedGroup>,
    val dependencyGroups: List<CorrelatedGroup>,
    val disagreementGroups: List<CorrelatedGroup>,
    val reason: CorrelationReason
) {
    data class Input(
        val correlation: CrossSourceCorrelationResult
    )
}

enum class CrossSourceAgreementStatus {
    READY,
    INSUFFICIENT_AGREEMENT_DATA
}
