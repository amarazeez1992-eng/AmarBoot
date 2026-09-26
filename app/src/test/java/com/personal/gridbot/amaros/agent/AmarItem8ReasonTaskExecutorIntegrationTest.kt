package com.personal.gridbot.amaros.agent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarItem8ReasonTaskExecutorIntegrationTest {
    private class Provider : AmarModelProvider {
        override val id = "integration-provider"
        var calls = 0
        override suspend fun load(modelPath: String) = AmarModelInfo(id, "test", 8192, 256, true)
        override suspend fun generate(request: AmarGenerationRequest): AmarGenerationResult {
            calls++
            return AmarGenerationResult("provider-response")
        }
        override suspend fun unload() = Unit
    }

    @Test fun reason_executor_uses_registry_router_adaptive_and_real_provider() = runBlocking {
        val provider = Provider()
        val catalog = AmarModelProviderCatalog(listOf(
            AmarModelProviderProfile(
                provider, setOf(AmarModelCapability.MULTI_FACTOR_ANALYSIS),
                8192, 256, AmarModelQuality.HIGH, AmarModelCost.LOW
            )
        ))
        val audit = AmarModelRoutingAudit()
        val router = AmarReasoningRouter(
            local = object : AmarReasoningProvider {
                override suspend fun respond(context: AmarAgentContext) = AmarAgentResponse("local")
            },
            adaptive = AmarAdaptiveReasoningProvider(catalog, audit),
            classifier = AmarModelTaskClassifier(),
            complexity = AmarModelComplexityEstimator(),
            audit = audit
        )
        val plan = AmarAgentPlanner().plan(
            AmarAgentRequest("compare multiple factors in detail", 1, 1, false, false),
            emptyList()
        )
        val runtime = TaskExecutionRuntime(
            request = AmarAgentRequest("compare multiple factors in detail", 1, 1, false, false),
            budget = AmarAgentBudget().normalized(),
            plan = plan,
            safeRequestedSources = 1,
            safeMaximumSources = 1,
            safeTools = emptyList(),
            plannedTools = emptyList(),
            understanding = AmarIntentUnderstanding(),
            queryPolicy = AmarQueryPolicy(),
            researchEngine = object : AmarResearchEngine {
                override suspend fun research(request: ResearchRequest) = ResearchReport(emptyList(), emptyList(), 0.0)
            },
            sourceVerifier = AmarSourceVerifier(),
            consensusEngine = AmarAgentEvidenceConsensus(),
            critic = AmarAgentCritic(),
            verifier = AmarAgentVerifier(),
            reasoningProvider = router,
            stageTwoEngine = AmarStageTwoEngine(router),
            stageThreeEngine = AmarStageThreeEngine(),
            evidenceQualityEngine = AmarEvidenceQualityEngine(),
            claimVerificationEngine = AmarClaimVerificationEngine(),
            confidenceCalibrationEngine = AmarConfidenceCalibrationEngine(),
            canonicalEvidenceQuality = AmarCanonicalEvidenceQualityAssembler(),
            evidenceIntake = AmarEvidenceIntake(),
            findingToCandidateConverter = AmarFindingToCandidateConverter(),
            verificationLayer = com.personal.gridbot.amaros.intelligence.verification.AmarVerificationLayer(),
            hierarchy = AmarAgentHierarchy(),
            decisionCouncil = AmarDecisionCouncil(),
            directionEngine = AmarDecisionDirectionEngine(),
            roleOpinionEngine = AmarRoleOpinionEngine()
        )
        val registry = defaultStage11EngineRegistry()
        assertTrue(registry.get(AmarTaskKind.REASON) is ReasonTaskExecutor)
        val task = AmarTaskUnit("reason", AmarTaskKind.REASON, "output", emptyList())
        val result = registry.get(AmarTaskKind.REASON).execute(
            task,
            ContextEnvelope("integration-session", "reason", emptyMap(), artifacts = mapOf("runtime" to runtime))
        )
        val artifact = result.value as ReasonTaskArtifact
        assertEquals("provider-response", artifact.answer.answer)
        assertEquals(1, provider.calls)
        assertTrue(audit.records().any { it.level == 2 && it.selectedProvider == "integration-provider" })
        assertTrue(audit.records().any { it.level == 1 && it.selectedProvider == "integration-provider" })
    }
}
