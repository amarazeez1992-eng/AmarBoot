package com.personal.gridbot.amaros.agent
import org.junit.Assert.assertEquals
import org.junit.Test
class AmarModelComplexityEstimatorTest {
 private val e=AmarModelComplexityEstimator()
 @Test fun low()=assertEquals(AmarModelComplexity.LOW,e.estimate(AmarModelTask.SIMPLE_EXPLANATION,AmarModelComplexityContext("hello")))
 @Test fun medium()=assertEquals(AmarModelComplexity.MEDIUM,e.estimate(AmarModelTask.SIMPLE_EXPLANATION,AmarModelComplexityContext("x".repeat(8000))))
 @Test fun high()=assertEquals(AmarModelComplexity.HIGH,e.estimate(AmarModelTask.MULTI_FACTOR_ANALYSIS,AmarModelComplexityContext("compare")))
 @Test fun unknown()=assertEquals(AmarModelComplexity.UNKNOWN,e.estimate(AmarModelTask.UNKNOWN,AmarModelComplexityContext("")))
}
