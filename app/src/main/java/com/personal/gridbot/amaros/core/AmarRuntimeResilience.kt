package com.personal.gridbot.amaros.core

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlin.math.pow
import kotlin.random.Random

/** Central runtime policy. Defaults are conservative and Demo-safe. */
data class AmarRuntimeConfig(
    val operationTimeoutMs: Long = 5_000L,
    val maxRetries: Int = 2,
    val initialBackoffMs: Long = 250L,
    val maxBackoffMs: Long = 5_000L,
    val jitterRatio: Double = 0.20,
    val circuitFailureThreshold: Int = 3,
    val circuitOpenMs: Long = 30_000L
) {
    init {
        require(operationTimeoutMs > 0)
        require(maxRetries >= 0)
        require(initialBackoffMs >= 0)
        require(maxBackoffMs >= initialBackoffMs)
        require(jitterRatio in 0.0..1.0)
        require(circuitFailureThreshold > 0)
        require(circuitOpenMs > 0)
    }
}

class AmarIdempotencyStore(private val capacity: Int = 2_000) {
    private val results = LinkedHashMap<String, Any?>()
    init { require(capacity > 0) }
    @Synchronized fun contains(key: String): Boolean = results.containsKey(key)
    @Synchronized fun get(key: String): Any? = results[key]
    @Synchronized fun put(key: String, result: Any?) {
        if (key.isBlank()) return
        results[key] = result
        while (results.size > capacity) results.remove(results.keys.first())
    }
    @Synchronized fun clear() { results.clear() }
}

class AmarCircuitBreaker(
    private val failureThreshold: Int = 3,
    private val openDurationMs: Long = 30_000L,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    init {
        require(failureThreshold > 0)
        require(openDurationMs > 0)
    }
    private var failures = 0
    private var openedAt = 0L
    @Synchronized fun isOpen(): Boolean = openedAt != 0L && clock() - openedAt < openDurationMs
    @Synchronized fun recordSuccess() { failures = 0; openedAt = 0L }
    @Synchronized fun recordFailure() { failures++; if (failures >= failureThreshold) openedAt = clock() }
    @Synchronized fun reset() { failures = 0; openedAt = 0L }
}

suspend fun <T> amarWithRetry(
    config: AmarRuntimeConfig = AmarRuntimeConfig(),
    random: Random = Random.Default,
    operation: suspend () -> T
): T {
    var attempt = 0
    var lastError: Throwable? = null
    while (attempt <= config.maxRetries) {
        try {
            return withTimeout(config.operationTimeoutMs) { operation() }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            lastError = error
            if (attempt == config.maxRetries) break
            val exponential = (config.initialBackoffMs * 2.0.pow(attempt.toDouble())).toLong().coerceAtMost(config.maxBackoffMs)
            val jitter = (exponential * config.jitterRatio * random.nextDouble()).toLong()
            delay(exponential + jitter)
            attempt++
        }
    }
    throw lastError ?: IllegalStateException("Operation failed without an error")
}
