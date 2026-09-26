package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.core.AmarRuntimeConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.pow
import kotlin.random.Random

/**
 * Model/AI provider recovery policy only. Search Providers are outside this boundary.
 * Circuit breaking is delegated to the existing central runtime primitive; retry remains here
 * so every individual failure is classified, circuit-updated, and audited before another call.
 */
class AmarProviderFallbackPolicy(
    private val runtimeConfig: AmarRuntimeConfig,
    private val health: AmarProviderHealthMonitor,
    private val classifier: AmarProviderFailureClassifier,
    private val integrity: AmarProviderResultIntegrity,
    private val audit: AmarProviderRecoveryAudit,
    private val random: Random = Random.Default
) {
    suspend fun generate(
        operationId: String,
        providers: List<AmarModelProviderProfile>,
        request: AmarGenerationRequest
    ): AmarProviderRecoveryResult {
        require(operationId.isNotBlank())
        val attempted = linkedSetOf<String>()
        var providerAttempt = 0

        while (providerAttempt < providers.size) {
            val candidate = providers.firstOrNull {
                it.provider.id.isNotBlank() && it.available &&
                    it.provider.id !in attempted && health.isAvailable(it.provider.id)
            } ?: break

            attempted += candidate.provider.id
            providerAttempt++
            var retryAttempt = 0

            audit.record(
                operationId, candidate.provider.id, "PROVIDER_ATTEMPT", providerAttempt,
                null, health.state(candidate.provider.id), null, false, "ATTEMPT_STARTED"
            )

            while (true) {
                try {
                    val result = withTimeout(runtimeConfig.operationTimeoutMs) {
                        candidate.provider.generate(request)
                    }
                    val accepted = acceptResult(
                        operationId, providerAttempt, candidate, providers, attempted, request, result
                    )
                    if (accepted.decisionState == "SUCCESS") return accepted
                    break
                } catch (error: TimeoutCancellationException) {
                    val failure = AmarProviderFailureClass.TIMEOUT
                    recordFailure(
                        operationId, candidate.provider.id, providerAttempt, retryAttempt,
                        error, providers, attempted, failure
                    )
                    if (!canRetry(candidate.provider.id, failure, retryAttempt)) break
                    retryAttempt++
                    backoff(retryAttempt)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    val failure = classifier.classify(error)
                    recordFailure(
                        operationId, candidate.provider.id, providerAttempt, retryAttempt,
                        error, providers, attempted, failure
                    )
                    if (!canRetry(candidate.provider.id, failure, retryAttempt)) break
                    retryAttempt++
                    backoff(retryAttempt)
                }
            }
        }

        audit.record(
            operationId, null, "FAIL_CLOSED", providerAttempt.coerceAtLeast(1),
            AmarProviderFailureClass.UNKNOWN, AmarProviderHealthState.FAILED,
            null, false, "NO_VALID_PROVIDER_RESULT"
        )
        return AmarProviderRecoveryResult(null, null, AmarProviderFailureClass.UNKNOWN, "FAIL_CLOSED")
    }

    private suspend fun backoff(retryAttempt: Int) {
        val exponential = (
            runtimeConfig.initialBackoffMs * 2.0.pow((retryAttempt - 1).toDouble())
        ).toLong().coerceAtMost(runtimeConfig.maxBackoffMs)
        val jitter = (
            exponential * runtimeConfig.jitterRatio * random.nextDouble()
        ).toLong()
        delay(exponential + jitter)
    }

    private fun canRetry(
        providerId: String,
        failure: AmarProviderFailureClass,
        retryAttempt: Int
    ): Boolean =
        health.isAvailable(providerId) &&
            retryAttempt < runtimeConfig.maxRetries &&
            (failure == AmarProviderFailureClass.TRANSIENT ||
                failure == AmarProviderFailureClass.TIMEOUT)

    private fun acceptResult(
        operationId: String,
        attempt: Int,
        candidate: AmarModelProviderProfile,
        providers: List<AmarModelProviderProfile>,
        attempted: Set<String>,
        request: AmarGenerationRequest,
        result: AmarGenerationResult
    ): AmarProviderRecoveryResult {
        val check = integrity.validate(candidate.provider.id, request, result)
        if (!check.accepted) {
            val failure = AmarProviderFailureClass.MALFORMED_RESULT
            health.recordFailure(candidate.provider.id, failure)
            audit.record(
                operationId, candidate.provider.id, "RESULT_REJECTED", attempt,
                failure, health.state(candidate.provider.id),
                nextId(providers, attempted), false, check.reason
            )
            return AmarProviderRecoveryResult(null, null, failure, "FALLBACK")
        }

        health.recordSuccess(candidate.provider.id)
        audit.record(
            operationId, candidate.provider.id, "RECOVERY_SUCCESS", attempt,
            null, health.state(candidate.provider.id), null, true, "VALID_RESULT"
        )
        return AmarProviderRecoveryResult(candidate.provider.id, result, null, "SUCCESS")
    }

    private fun recordFailure(
        operationId: String,
        providerId: String,
        attempt: Int,
        error: Throwable,
        providers: List<AmarModelProviderProfile>,
        attempted: Set<String>,
        classifiedFailure: AmarProviderFailureClass? = null
    ) {
        val failure = classifiedFailure ?: classifier.classify(error)
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
