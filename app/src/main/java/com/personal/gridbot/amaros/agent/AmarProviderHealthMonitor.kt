package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.core.AmarCircuitBreaker
import com.personal.gridbot.amaros.core.AmarRuntimeConfig

enum class AmarProviderHealthState { UNKNOWN, HEALTHY, DEGRADED, FAILED }

data class AmarProviderHealthSnapshot(
    val providerId: String,
    val state: AmarProviderHealthState,
    val consecutiveFailures: Int,
    val lastFailure: AmarProviderFailureClass?,
    val lastSuccessAtMs: Long?,
    val lastFailureAtMs: Long?
)

class AmarProviderHealthMonitor(
    private val runtimeConfig: AmarRuntimeConfig,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    init {
        require(runtimeConfig.circuitFailureThreshold > 0)
        require(runtimeConfig.circuitOpenMs > 0)
    }

    private data class Entry(
        val breaker: AmarCircuitBreaker,
        var state: AmarProviderHealthState = AmarProviderHealthState.UNKNOWN,
        var consecutiveFailures: Int = 0,
        var lastFailure: AmarProviderFailureClass? = null,
        var lastSuccessAtMs: Long? = null,
        var lastFailureAtMs: Long? = null
    )

    private val entries = mutableMapOf<String, Entry>()

    @Synchronized
    private fun entry(providerId: String): Entry {
        require(providerId.isNotBlank()) { "provider id must not be blank" }
        return entries.getOrPut(providerId) {
            Entry(
                AmarCircuitBreaker(
                    runtimeConfig.circuitFailureThreshold,
                    runtimeConfig.circuitOpenMs,
                    clock
                )
            )
        }
    }

    @Synchronized
    fun state(providerId: String): AmarProviderHealthState {
        val e = entry(providerId)
        return when {
            e.breaker.isOpen() -> AmarProviderHealthState.FAILED
            e.state == AmarProviderHealthState.FAILED -> AmarProviderHealthState.DEGRADED
            else -> e.state
        }
    }

    @Synchronized
    fun isAvailable(providerId: String): Boolean =
        state(providerId) != AmarProviderHealthState.FAILED

    @Synchronized
    fun recordSuccess(providerId: String) {
        val e = entry(providerId)
        e.breaker.recordSuccess()
        e.state = AmarProviderHealthState.HEALTHY
        e.consecutiveFailures = 0
        e.lastSuccessAtMs = clock()
    }

    @Synchronized
    fun recordFailure(providerId: String, failure: AmarProviderFailureClass) {
        val e = entry(providerId)
        e.breaker.recordFailure()
        e.consecutiveFailures++
        e.lastFailure = failure
        e.lastFailureAtMs = clock()
        e.state = when (failure) {
            AmarProviderFailureClass.PERMANENT,
            AmarProviderFailureClass.MALFORMED_RESULT,
            AmarProviderFailureClass.UNKNOWN -> AmarProviderHealthState.FAILED
            AmarProviderFailureClass.TIMEOUT,
            AmarProviderFailureClass.TRANSIENT ->
                if (e.breaker.isOpen()) AmarProviderHealthState.FAILED
                else AmarProviderHealthState.DEGRADED
        }
    }

    @Synchronized
    fun snapshot(providerId: String): AmarProviderHealthSnapshot {
        val e = entry(providerId)
        return AmarProviderHealthSnapshot(
            providerId, state(providerId), e.consecutiveFailures, e.lastFailure,
            e.lastSuccessAtMs, e.lastFailureAtMs
        )
    }
}
