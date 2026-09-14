package com.personal.gridbot.amaros.workforce

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarParallelWorkforceTest {
    @Test
    fun aggregatesIndependentWorkersInDeterministicInputOrder() = runBlocking {
        val workforce = AmarParallelWorkforce<Int, String>(
            AmarParallelWorkforce.Config(maxWorkers = 2, timeoutMs = 2_000L, retries = 0)
        )
        val items = listOf(1, 2, 3, 4).map {
            AmarParallelWorkforce.WorkItem(key = it, cacheKey = "k-$it")
        }
        val report = workforce.execute(items) {
            delay((5 - it.key).toLong())
            "value-${it.key}"
        }

        assertEquals(listOf(1, 2, 3, 4), report.outcomes.map { it.key })
        assertEquals(4, report.successCount)
        assertEquals(0, report.failureCount)
        assertTrue(report.outcomes.all { it is AmarParallelWorkforce.Outcome.Success })
    }

    @Test
    fun cacheIsBoundedAndSecondRunUsesCachedResults() = runBlocking {
        val workforce = AmarParallelWorkforce<Int, String>(
            AmarParallelWorkforce.Config(maxWorkers = 1, cacheCapacity = 2, retries = 0)
        )
        val items = (1..3).map { AmarParallelWorkforce.WorkItem(it, "k-$it") }
        var executions = 0

        workforce.execute(items) { executions++; "v-$it" }
        val second = workforce.execute(listOf(items[1], items[2])) { executions++; "unexpected-$it" }

        assertEquals(3, executions)
        assertEquals(2, second.cacheHits)
    }

    @Test
    fun offlineNetworkWorkIsSkippedWithoutCallingWorker() = runBlocking {
        val workforce = AmarParallelWorkforce<Int, String>()
        val item = AmarParallelWorkforce.WorkItem(1, "network", requiresNetwork = true)
        var called = false

        val report = workforce.execute(listOf(item), AmarParallelWorkforce.Connectivity.OFFLINE) {
            called = true
            "not-allowed"
        }

        assertTrue(!called)
        assertEquals(1, report.skippedCount)
        assertTrue(report.partial)
    }

    @Test
    fun timeoutRecoversOnSecondAttempt() = runBlocking {
        val workforce = AmarParallelWorkforce<Int, String>(
            AmarParallelWorkforce.Config(maxWorkers = 1, timeoutMs = 20L, retries = 1)
        )
        var attempts = 0
        val item = AmarParallelWorkforce.WorkItem(1, "recover", timeoutMs = 20L)
        val report = workforce.execute(listOf(item)) {
            attempts++
            if (attempts == 1) delay(100L)
            "recovered"
        }

        val success = report.outcomes.single() as AmarParallelWorkforce.Outcome.Success
        assertEquals("recovered", success.value)
        assertEquals(2, success.attempts)
        assertEquals(1, report.recoveredFailures)
    }

    @Test
    fun cancellationPropagatesToCaller() = runBlocking {
        val workforce = AmarParallelWorkforce<Int, String>(
            AmarParallelWorkforce.Config(maxWorkers = 2, timeoutMs = 10_000L)
        )
        val job = launch {
            workforce.execute((1..4).map { AmarParallelWorkforce.WorkItem(it, "cancel-$it") }) {
                delay(10_000L)
                "never"
            }
        }
        delay(20L)
        job.cancel()
        job.join()
        assertTrue(job.isCancelled)
    }
}
