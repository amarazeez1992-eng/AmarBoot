package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
import org.junit.Test

class AmarModelComplexityEstimatorTest {
    private val estimator = AmarModelComplexityEstimator()

    @Test fun low_task_is_low() {
        assertEquals(
            AmarModelComplexity.LOW,
            estimator.estimate(
                AmarModelTask.SIMPLE_EXPLANATION,
                AmarModelComplexityContext("hello")
            )
        )
    }

    @Test fun medium_context_is_medium() {
        assertEquals(
            AmarModelComplexity.MEDIUM,
            estimator.estimate(
                AmarModelTask.SIMPLE_EXPLANATION,
                AmarModelComplexityContext("x".repeat(8000))
            )
        )
    }

    @Test fun multi_factor_is_high() {
        assertEquals(
            AmarModelComplexity.HIGH,
            estimator.estimate(
                AmarModelTask.MULTI_FACTOR_ANALYSIS,
                AmarModelComplexityContext("compare several factors")
            )
        )
    }

    @Test fun unknown_is_high_and_fail_closed() {
        assertEquals(
            AmarModelComplexity.HIGH,
            estimator.estimate(
                AmarModelTask.UNKNOWN,
                AmarModelComplexityContext("")
            )
        )
    }
}
