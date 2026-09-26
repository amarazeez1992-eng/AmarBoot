package com.personal.gridbot.amaros.agent

import com.personal.gridbot.amaros.ai.AmarAiAgentEngine
import com.personal.gridbot.amaros.core.AmarRuntimeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AmarProviderResilienceTest {
    private class TestProvider(
        override val id: String,
        private val behavior: suspend (AmarGenerationRequest) -> AmarGenerationResult
    ) : AmarModelProvider {
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 4096, 128, true)
        override suspend fun generate(request: AmarGenerationRequest) = behavior(request)
        override suspend fun unload() = Unit
    }

    private fun profile(provider: AmarModelProvider, available: Boolean = true) =
        AmarModelProviderProfile(
            provider = provider,
            capabilities = setOf(AmarModelCapability.SIMPLE_EXPLANATION),
            maxContextTokens = 4096,
            estimatedRamMb = 128,
            quality = AmarModelQuality.STANDARD,
            cost = AmarModelCost.LOW,
            available = available
        )

    private fun request() = AmarGenerationRequest("system", "hello", maxTokens = 16)

    @Test fun healthStateTransitions() {
        var now = 0L
        val runtimeConfig = AmarRuntimeConfig(circuitFailureThreshold = 2, circuitOpenMs = 1000L)
        val monitor = AmarProviderHealthMonitor(runtimeConfig) { now }
        assertEquals(AmarProviderHealthState.UNKNOWN, monitor.state("p"))
        monitor.recordSuccess("p")
        assertEquals(AmarProviderHealthState.HEALTHY, monitor.state("p"))
        monitor.recordFailure("p", AmarProviderFailureClass.TRANSIENT)
        assertEquals(AmarProviderHealthState.DEGRADED, monitor.state("p"))
        monitor.recordFailure("p", AmarProviderFailureClass.TRANSIENT)
        assertEquals(AmarProviderHealthState.FAILED, monitor.state("p"))
        now = 1001L
        assertTrue(monitor.isAvailable("p"))
    }

    @Test fun timeoutIsFailedAndFallsBack() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(
            operationTimeoutMs = 5L,
            maxRetries = 0,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0
        )
        val health = AmarProviderHealthMonitor(runtimeConfig)
        val audit = AmarProviderRecoveryAudit { 7L }
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig, health, AmarProviderFailureClassifier(), AmarProviderResultIntegrity(), audit
        )
        val slow = profile(TestProvider("slow") { delay(50); AmarGenerationResult("slow") })
        val good = profile(TestProvider("good") { AmarGenerationResult("ok") })
        val result = policy.generate("op-timeout", listOf(slow, good), request())
        assertEquals("good", result.providerId)
        assertEquals(AmarProviderFailureClass.TIMEOUT, health.snapshot("slow").lastFailure)
    }


    @Test fun timeoutIsRetriedWithinBound() = runBlocking {
        var calls = 0
        val runtimeConfig = AmarRuntimeConfig(
            operationTimeoutMs = 5L,
            maxRetries = 1,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0
        )
        val provider = profile(TestProvider("timeout-retry") {
            calls++
            if (calls == 1) {
                delay(50)
                AmarGenerationResult("late")
            } else {
                AmarGenerationResult("recovered")
            }
        })
        val audit = AmarProviderRecoveryAudit { 8L }
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            AmarProviderHealthMonitor(runtimeConfig),
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            audit
        )
        val result = policy.generate("op-timeout-retry", listOf(provider), request())
        assertEquals("timeout-retry", result.providerId)
        assertEquals("recovered", result.result?.text)
        assertEquals(2, calls)
        assertTrue(audit.records().any {
            it.providerId == "timeout-retry" &&
                it.event == "PROVIDER_FAILED" &&
                it.failureClass == AmarProviderFailureClass.TIMEOUT
        })
    }

    @Test fun permanentFailureIsNotRetriedAndFallsBack() = runBlocking {
        var calls = 0
        val runtimeConfig = AmarRuntimeConfig(
            maxRetries = 2,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0
        )
        val permanent = profile(TestProvider("permanent") {
            calls++
            throw IllegalArgumentException("invalid request")
        })
        val good = profile(TestProvider("good-permanent") { AmarGenerationResult("ok") })
        val audit = AmarProviderRecoveryAudit()
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            AmarProviderHealthMonitor(runtimeConfig),
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            audit
        )
        val result = policy.generate("op-permanent", listOf(permanent, good), request())
        assertEquals("good-permanent", result.providerId)
        assertEquals(1, calls)
        assertTrue(audit.records().any {
            it.providerId == "permanent" &&
                it.failureClass == AmarProviderFailureClass.PERMANENT
        })
    }

    @Test fun retryReclassifiesEveryAttemptBeforeContinuing() = runBlocking {
        var calls = 0
        val runtimeConfig = AmarRuntimeConfig(
            maxRetries = 2,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0
        )
        val mixed = profile(TestProvider("mixed") {
            calls++
            if (calls == 1) throw IOException("temporary")
            throw IllegalArgumentException("permanent")
        })
        val good = profile(TestProvider("good-mixed") { AmarGenerationResult("fallback") })
        val audit = AmarProviderRecoveryAudit()
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            AmarProviderHealthMonitor(runtimeConfig),
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            audit
        )
        val result = policy.generate("op-reclassify", listOf(mixed, good), request())
        assertEquals("good-mixed", result.providerId)
        assertEquals(2, calls)
        assertTrue(audit.records().any {
            it.providerId == "mixed" && it.failureClass == AmarProviderFailureClass.TRANSIENT
        })
        assertTrue(audit.records().any {
            it.providerId == "mixed" && it.failureClass == AmarProviderFailureClass.PERMANENT
        })
    }

    @Test fun failureClassification() {
        val c = AmarProviderFailureClassifier()
        assertEquals(AmarProviderFailureClass.TRANSIENT, c.classify(IOException("io")))
        assertEquals(AmarProviderFailureClass.PERMANENT, c.classify(IllegalArgumentException("bad")))
        assertEquals(AmarProviderFailureClass.UNKNOWN, c.classify(IllegalStateException("unknown")))
    }

    @Test fun fallbackSelectionValidAndInvalid() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(maxRetries = 0, initialBackoffMs = 0, maxBackoffMs = 0, jitterRatio = 0.0)
        val health = AmarProviderHealthMonitor(runtimeConfig)
        val audit = AmarProviderRecoveryAudit()
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig, health, AmarProviderFailureClassifier(), AmarProviderResultIntegrity(), audit
        )
        val invalid = profile(TestProvider("invalid") { error("boom") }, available = false)
        val good = profile(TestProvider("good") { AmarGenerationResult("ok") })
        val result = policy.generate("op-fallback", listOf(invalid, good), request())
        assertEquals("good", result.providerId)
        assertTrue(audit.records().any { it.providerId == "good" && it.success })
    }


    @Test fun circuitOpenStopsFurtherRetriesAndMovesToFallback() = runBlocking {
        var calls = 0
        val runtimeConfig = AmarRuntimeConfig(
            maxRetries = 3,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0,
            circuitFailureThreshold = 1,
            circuitOpenMs = 1000L
        )
        val health = AmarProviderHealthMonitor(runtimeConfig)
        val failing = profile(TestProvider("circuit-fail") {
            calls++
            throw IOException("transient")
        })
        val good = profile(TestProvider("circuit-good") { AmarGenerationResult("recovered") })
        val audit = AmarProviderRecoveryAudit()
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            health,
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            audit
        )
        val result = policy.generate("op-circuit-fallback", listOf(failing, good), request())
        assertEquals("circuit-good", result.providerId)
        assertEquals(1, calls)
        assertFalse(health.isAvailable("circuit-fail"))
        assertTrue(audit.records().any {
            it.providerId == "circuit-fail" &&
                it.failureClass == AmarProviderFailureClass.TRANSIENT
        })
    }

    @Test fun retryPolicyIsBounded() = runBlocking {
        var attempts = 0
        val runtimeConfig = AmarRuntimeConfig(
            maxRetries = 2,
            initialBackoffMs = 0,
            maxBackoffMs = 0,
            jitterRatio = 0.0
        )
        val provider = TestProvider("retry") {
            attempts++
            throw IOException("transient")
        }
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            AmarProviderHealthMonitor(runtimeConfig),
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            AmarProviderRecoveryAudit()
        )
        val result = policy.generate("op-retry", listOf(profile(provider)), request())
        assertEquals("FAIL_CLOSED", result.decisionState)
        assertEquals(3, attempts)
    }

    @Test fun circuitBreakerThresholdAndRecoveryAreReused() {
        var now = 0L
        val runtimeConfig = AmarRuntimeConfig(circuitFailureThreshold = 2, circuitOpenMs = 100L)
        val monitor = AmarProviderHealthMonitor(runtimeConfig) { now }
        monitor.recordFailure("p", AmarProviderFailureClass.TRANSIENT)
        assertTrue(monitor.isAvailable("p"))
        monitor.recordFailure("p", AmarProviderFailureClass.TRANSIENT)
        assertFalse(monitor.isAvailable("p"))
        now = 101L
        assertTrue(monitor.isAvailable("p"))
        monitor.recordSuccess("p")
        assertEquals(AmarProviderHealthState.HEALTHY, monitor.state("p"))
    }

    @Test fun malformedResultIsRejectedWithoutFabrication() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(maxRetries = 0, initialBackoffMs = 0, maxBackoffMs = 0, jitterRatio = 0.0)
        val bad = profile(TestProvider("bad") { AmarGenerationResult("") })
        val good = profile(TestProvider("good") { AmarGenerationResult("valid") })
        val health = AmarProviderHealthMonitor(runtimeConfig)
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig, health, AmarProviderFailureClassifier(), AmarProviderResultIntegrity(),
            AmarProviderRecoveryAudit()
        )
        val result = policy.generate("op-integrity", listOf(bad, good), request())
        assertEquals("good", result.providerId)
        assertEquals(AmarProviderFailureClass.MALFORMED_RESULT, health.snapshot("bad").lastFailure)
        assertNotNull(result.result)
        assertEquals("valid", result.result?.text)
    }

    @Test fun adaptiveProviderUsesResiliencePolicyAndRecordsFallbackDecisions() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(maxRetries = 0, initialBackoffMs = 0, maxBackoffMs = 0, jitterRatio = 0.0)
        val recoveryAudit = AmarProviderRecoveryAudit { 99L }
        val resilience = AmarProviderFallbackPolicy(
            runtimeConfig = runtimeConfig,
            health = AmarProviderHealthMonitor(runtimeConfig),
            classifier = AmarProviderFailureClassifier(),
            integrity = AmarProviderResultIntegrity(),
            audit = recoveryAudit
        )
        val first = profile(TestProvider("first") { throw IOException("transient") })
        val second = profile(TestProvider("second") { AmarGenerationResult("fallback") })
        val routingAudit = AmarModelRoutingAudit()
        val adaptive = AmarAdaptiveReasoningProvider(
            catalog = AmarModelProviderCatalog(listOf(first, second)),
            audit = routingAudit,
            resilience = resilience
        )
        val outcome = adaptive.generate(
            context = AmarAgentContext("compare multiple factors in detail", emptyList(), false, false),
            task = AmarModelTask.SIMPLE_EXPLANATION,
            complexity = AmarModelComplexity.HIGH,
            maxContextTokens = 4096,
            maxRamMb = 4096,
            minimumQuality = AmarModelQuality.STANDARD
        )
        assertEquals("fallback", outcome.answer)
        val recovery = recoveryAudit.records()
        assertTrue(recovery.any { it.providerId == "first" && it.event == "PROVIDER_FAILED" })
        assertTrue(recovery.any { it.providerId == "second" && it.event == "RECOVERY_SUCCESS" })
        assertTrue(routingAudit.records().any {
            it.selectedProvider == "second" && it.decisionState == "SELECTED" && it.reason == "PROVIDER_GENERATION_SUCCESS"
        })
    }

    @Test fun fullEngineUsesInjectedResilienceAuthority() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(maxRetries = 0, initialBackoffMs = 0, maxBackoffMs = 0, jitterRatio = 0.0)
        val recoveryAudit = AmarProviderRecoveryAudit { 100L }
        val resilience = AmarProviderFallbackPolicy(
            runtimeConfig = runtimeConfig,
            health = AmarProviderHealthMonitor(runtimeConfig),
            classifier = AmarProviderFailureClassifier(),
            integrity = AmarProviderResultIntegrity(),
            audit = recoveryAudit
        )
        val first = profile(TestProvider("first-engine") { throw IOException("transient") })
        val second = profile(TestProvider("second-engine") { AmarGenerationResult("engine-fallback") })
        val engine = AmarAiAgentEngine(
            modelProviderCatalog = AmarModelProviderCatalog(listOf(first, second)),
            runtimeConfig = runtimeConfig,
            providerResilience = resilience
        )
        val reasoningField = AmarAiAgentEngine::class.java.getDeclaredField("reasoningProvider")
        reasoningField.isAccessible = true
        val router = reasoningField.get(engine) as AmarReasoningRouter
        val response = router.respond(
            AmarAgentContext("compare multiple factors in detail", emptyList(), false, false)
        )
        assertEquals("engine-fallback", response.answer)
        assertTrue(recoveryAudit.records().any {
            it.providerId == "first-engine" &&
                it.event == "PROVIDER_FAILED" &&
                it.failureClass == AmarProviderFailureClass.TRANSIENT
        })
        assertTrue(recoveryAudit.records().any {
            it.providerId == "second-engine" &&
                it.event == "RECOVERY_SUCCESS" &&
                it.success
        })
    }

    @Test fun auditRecordIsComplete() = runBlocking {
        val runtimeConfig = AmarRuntimeConfig(maxRetries = 0, initialBackoffMs = 0, maxBackoffMs = 0, jitterRatio = 0.0)
        val provider = profile(TestProvider("audit") { throw IllegalStateException("unknown") })
        val audit = AmarProviderRecoveryAudit { 42L }
        val policy = AmarProviderFallbackPolicy(
            runtimeConfig,
            AmarProviderHealthMonitor(runtimeConfig),
            AmarProviderFailureClassifier(),
            AmarProviderResultIntegrity(),
            audit
        )
        val result = policy.generate("op-audit", listOf(provider), request())
        assertEquals("FAIL_CLOSED", result.decisionState)
        val failed = audit.records().first { it.event == "PROVIDER_FAILED" }
        assertEquals("op-audit", failed.operationId)
        assertEquals("audit", failed.providerId)
        assertEquals(1, failed.attempt)
        assertEquals(AmarProviderFailureClass.UNKNOWN, failed.failureClass)
        assertEquals(42L, failed.timestampMs)
        assertFalse(failed.success)
        assertTrue(failed.reason.isNotBlank())
        val closed = audit.records().last()
        assertEquals("FAIL_CLOSED", closed.event)
        assertNull(closed.providerId)
    }
}
