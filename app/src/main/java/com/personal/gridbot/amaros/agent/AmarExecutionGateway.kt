package com.personal.gridbot.amaros.agent

/** Separate execution boundary; disabled by default until explicit future authorization. */
interface AmarExecutionGateway {
    suspend fun execute(request: AmarExecutionRequest): AmarExecutionResult
}

data class AmarExecutionRequest(
    val intentId: String,
    val direction: AmarDecisionDirection,
    val symbol: String,
    val reason: String,
    val approvedByPolicy: Boolean = false,
    val approvedByRisk: Boolean = false,
    val userConfirmed: Boolean = false
)

data class AmarExecutionResult(
    val executed: Boolean,
    val message: String,
    val externalOrderId: String? = null
)

class DisabledAmarExecutionGateway : AmarExecutionGateway {
    override suspend fun execute(request: AmarExecutionRequest): AmarExecutionResult =
        AmarExecutionResult(false, "التنفيذ الحقيقي غير مفعّل في AMAR AI حالياً.")
}
