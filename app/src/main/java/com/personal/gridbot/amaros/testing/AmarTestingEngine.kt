package com.personal.gridbot.amaros.testing

/** B21: deterministic contract-testing engine. It observes modules and never executes trades. */
class AmarTestingEngine {
    data class TestCase<T>(val name: String, val action: () -> T, val verify: (T) -> Boolean)
    data class TestResult(val name: String, val passed: Boolean, val durationMs: Long, val error: String? = null)
    data class TestReport(val startedAtEpochMs: Long, val finishedAtEpochMs: Long, val results: List<TestResult>) {
        val passed: Boolean get() = results.all { it.passed }
        val passedCount: Int get() = results.count { it.passed }
        val failedCount: Int get() = results.count { !it.passed }
    }

    fun <T> run(cases: List<TestCase<T>>, now: () -> Long = { System.currentTimeMillis() }): TestReport {
        require(cases.none { it.name.isBlank() })
        val started = now()
        val results = cases.map { test ->
            val begin = now()
            try {
                val value = test.action()
                TestResult(test.name, test.verify(value), (now() - begin).coerceAtLeast(0L))
            } catch (error: Throwable) {
                TestResult(test.name, false, (now() - begin).coerceAtLeast(0L), error.message ?: error::class.simpleName)
            }
        }
        return TestReport(started, now(), results)
    }
}
