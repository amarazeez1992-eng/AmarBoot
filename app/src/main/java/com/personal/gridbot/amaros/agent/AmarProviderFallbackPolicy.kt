package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.core.AmarRuntimeConfig
import com.personal.gridbot.amaros.core.amarWithRetry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Model/AI provider recovery policy only. Search Providers are outside this boundary.
 * Retry and circuit breaking are delegated to the existing central runtime primitives.
 */
class AmarProviderFallbackPolicy(
    private val health: AmarProviderHealthMonitor,
    private val classifier: AmarProviderFailureClassifier,
    private val integrity: AmarProviderResultIntegrity,
    private val audit: AmarProviderRecoveryAudit,
    private val runtimeConfig: AmarRuntimeConfig = AmarRuntimeConfig()
) {
    suspend fun generate(
        operationId: String,
        providers: List<AmarModelProviderProfile>,
        request: AmarGenerationRequest
    ): AmarProviderRecoveryResult {
        require(operationId.isNotBlank())
        val attempted = linkedSetOf<String>()
        var attempt = 0

        while (attempt < providers.size) {
            val candidate = providers.firstOrNull {
                it.provider.id.isNotBlank() && it.available &&
                    it.provider.id !in attempted && health.isAvailable(it.provider.id)
            } ?: break
            attempted += candidate.provider.id
            attempt++
            audit.record(
                operationId, candidate.provider.id, "PROVIDER_ATTEMPT", attempt,
                null, health.state(candidate.provider.id), null, false, "ATTEMPT_STARTED"
            )

            try {
                val result = amarWithRetry(runtimeConfig) {
                    candidate.provider.generate(request)
                }
                val check = integrity.validate(candidate.provider.id, request, result)
                if (!check.accepted) {
                    val failure = AmarProviderFailureClass.MALFORMED_RESULT
                    health.recordFailure(candidate.provider.id, failure)
                    audit.record(
                        operationId, candidate.provider.id, "RESULT_REJECTED", attempt,
                        failure, health.state(candidate.provider.id),
                        nextId(providers, attempted), false, check.reason
                    )
                    continue
                }

                health.recordSuccess(candidate.provider.id)
                audit.record(
                    operationId, candidate.provider.id, "RECOVERY_SUCCESS", attempt,
                    null, health.state(candidate.provider.id), null, true, "VALID_RESULT"
                )
                return AmarProviderRecoveryResult(candidate.provider.id, result, null, "SUCCESS")
            } catch (error: TimeoutCancellationException) {
                recordFailure(operationId, candidate.provider.id, attempt, error, providers, attempted)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                recordFailure(operationId, candidate.provider.id, attempt, error, providers, attempted)
            }
        }

        audit.record(
            operationId, null, "FAIL_CLOSED", attempt.coerceAtLeast(1),
            AmarProviderFailureClass.UNKNOWN, AmarProviderHealthState.FAILED,
            null, false, "NO_VALID_PROVIDER_RESULT"
        )
        return AmarProviderRecoveryResult(null, null, AmarProviderFailureClass.UNKNOWN, "FAIL_CLOSED")
    }

    private fun recordFailure(
        operationId: String,
        providerId: String,
        attempt: Int,
        error: Throwable,
        providers: List<AmarModelProviderProfile>,
        attempted: Set<String>
    ) {
        val failure = classifier.classify(error)
        health.recordFailure(providerId, failure)
        audit.record(
            operationId, providerId, "PROVIDER_FAILED", attempt, failure,
            health.state(providerId), nextId(providers, attempted), false, failure.name
        )
    }

    private fun nextId(
        providers: List<AmarModelProviderProfile>,
        attempted: Set<String>
    ): String? = providers.firstOrNull {
        it.provider.id.isNotBlank() && it.available &&
            it.provider.id !in attempted && health.isAvailable(it.provider.id)
    }?.provider?.id
}

data class AmarProviderRecoveryResult(
    val providerId: String?,
    val result: AmarGenerationResult?,
    val failureClass: AmarProviderFailureClass?,
    val decisionState: String
)
