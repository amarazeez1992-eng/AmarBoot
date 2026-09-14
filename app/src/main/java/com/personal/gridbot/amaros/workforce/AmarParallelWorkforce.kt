package com.personal.gridbot.amaros.workforce

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.LinkedHashMap
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/**
 * Stage 9 bounded parallel workforce for independent research/analysis work.
 *
 * The engine is intentionally transport- and execution-neutral. It provides bounded
 * concurrency, per-item latency budgets, cooperative cancellation, deterministic
 * aggregation, bounded in-memory caching, retry/recovery, and explicit offline gating.
 */
class AmarParallelWorkforce<K : Any, V : Any>(
    private val config: Config = Config(),
    private val cache: AmarBoundedWorkCache<K, V> = AmarBoundedWorkCache(config.cacheCapacity)
) {
    data class Config(
        val maxWorkers: Int = 4,
        val timeoutMs: Long = 5_000L,
        val maxItems: Int = 64,
        val cacheCapacity: Int = 128,
        val retries: Int = 1,
    ) {
        init {
            require(maxWorkers in 1..16)
            require(timeoutMs in 1L..120_000L)
            require(maxItems in 1..512)
            require(cacheCapacity in 0..2_048)
            require(retries in 0..3)
        }
    }

    enum class Connectivity { ONLINE, PARTIAL, OFFLINE }

    data class WorkItem<K : Any>(
        val key: K,
        val cacheKey: String,
        val requiresNetwork: Boolean = false,
        val timeoutMs: Long? = null,
        val retries: Int? = null,
    ) {
        init {
            require(cacheKey.isNotBlank())
            require(timeoutMs == null || timeoutMs > 0L)
            require(retries == null || retries >= 0)
        }
    }

    sealed interface Outcome<out K : Any, out V : Any> {
        val key: K
        val index: Int

        data class Success<K : Any, V : Any>(
            override val key: K,
            override val index: Int,
            val value: V,
            val cached: Boolean,
            val attempts: Int,
        ) : Outcome<K, V>

        data class Failed<K : Any>(
            override val key: K,
            override val index: Int,
            val reason: String,
            val attempts: Int,
        ) : Outcome<K, Nothing>

        data class Skipped<K : Any>(
            override val key: K,
            override val index: Int,
            val reason: String,
        ) : Outcome<K, Nothing>
    }

    data class Report<K : Any, V : Any>(
        val outcomes: List<Outcome<K, V>>,
        val cacheHits: Int,
        val recoveredFailures: Int,
        val partial: Boolean,
    ) {
        val successCount: Int get() = outcomes.count { it is Outcome.Success }
        val failureCount: Int get() = outcomes.count { it is Outcome.Failed }
        val skippedCount: Int get() = outcomes.count { it is Outcome.Skipped }
    }

    suspend fun execute(
        items: List<WorkItem<K>>,
        connectivity: Connectivity = Connectivity.ONLINE,
        worker: suspend (WorkItem<K>) -> V,
    ): Report<K, V> {
        require(items.size <= config.maxItems) { "workforce item limit exceeded" }
        require(items.map { it.cacheKey }.distinct().size == items.size) { "duplicate cache keys are not allowed" }
        if (!currentCoroutineContext().isActive) throw CancellationException("workforce cancelled before start")

        val semaphore = Semaphore(config.maxWorkers)
        val dispatcher = Dispatchers.IO
        return coroutineScope {
            val jobs = items.mapIndexed { index, item ->
                async(dispatcher) {
                    if (item.requiresNetwork && connectivity == Connectivity.OFFLINE) {
                        return@async Outcome.Skipped(item.key, index, "network unavailable") as Outcome<K, V>
                    }

                    cache.get(item.cacheKey)?.let { cached ->
                        return@async Outcome.Success(item.key, index, cached, cached = true, attempts = 0) as Outcome<K, V>
                    }

                    semaphore.withPermit {
                        runWithRecovery(item, index, worker)
                    }
                }
            }
            jobs.awaitAll().sortedBy { it.index }.let { outcomes ->
                val hits = outcomes.count { it is Outcome.Success && it.cached }
                val recovered = outcomes.count { it is Outcome.Success && it.attempts > 1 }
                Report(
                    outcomes = outcomes,
                    cacheHits = hits,
                    recoveredFailures = recovered,
                    partial = outcomes.any { it !is Outcome.Success },
                )
            }
        }
    }

    private suspend fun runWithRecovery(
        item: WorkItem<K>,
        index: Int,
        worker: suspend (WorkItem<K>) -> V,
    ): Outcome<K, V> {
        val maxAttempts = (item.retries ?: config.retries) + 1
        var attempt = 0
        var lastFailure = "worker failed"
        while (attempt < maxAttempts) {
            attempt++
            try {
                val value = withTimeout(item.timeoutMs ?: config.timeoutMs) {
                    worker(item)
                }
                cache.put(item.cacheKey, value)
                return Outcome.Success(item.key, index, value, cached = false, attempts = attempt)
            } catch (cancel: CancellationException) {
                if (!currentCoroutineContext().isActive) throw cancel
                lastFailure = "cancelled"
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                lastFailure = "timeout"
            } catch (failure: Throwable) {
                lastFailure = failure.message ?: failure::class.simpleName.orEmpty().ifBlank { "worker failed" }
            }
            if (attempt < maxAttempts) delay(1L)
        }
        return Outcome.Failed(item.key, index, lastFailure, attempt)
    }
}

/** Thread-safe bounded LRU cache; capacity zero disables caching without changing execution. */
class AmarBoundedWorkCache<K : Any, V : Any>(private val capacity: Int) {
    private val lock = Any()
    private val entries = object : LinkedHashMap<K, V>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = size > capacity
    }

    init { require(capacity >= 0) }

    fun get(key: K): V? = synchronized(lock) { entries[key] }

    fun put(key: K, value: V) = synchronized(lock) {
        if (capacity == 0) return@synchronized
        entries[key] = value
    }

    fun size(): Int = synchronized(lock) { entries.size }

    fun clear() = synchronized(lock) { entries.clear() }
}
