package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.agent.AmarLocalReasoning
import com.personal.gridbot.amaros.agent.AmarReasoningProvider
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import com.personal.gridbot.amaros.intelligence.trading.AmarTradingIntelligenceRegistry

/**
 * Provider-neutral AMAR engine mesh.
 * Gemini/ChatGPT/other providers are adapters only; the mesh owns no external AI dependency.
 * It exposes the real local intelligence boundaries already present in the application.
 */
class AmarAiEngineMesh(
    val reasoning: AmarReasoningProvider = AmarLocalReasoning(),
    val research: AmarAiExternalResearch = AmarAiExternalResearch()
) {
    data class EngineSnapshot(
        val providerNeutral: Boolean,
        val reasoningEngine: String,
        val marketEngine: String,
        val intelligenceEngineCount: Int,
        val executionAuthority: Boolean
    )

    fun snapshot(): EngineSnapshot {
        val market = AmarMarketStateStore.snapshot
        return EngineSnapshot(
            providerNeutral = true,
            reasoningEngine = "AMAR_LOCAL_REASONING",
            marketEngine = "${market.symbol}:${market.timeframe}",
            intelligenceEngineCount = AmarTradingIntelligenceRegistry.intelligenceEngines.size,
            executionAuthority = false
        )
    }

    /** Stable contract used by callers that need the connected engine catalog without a model provider. */
    fun connectedEngineIds(): List<String> = listOf(
        "reasoning",
        "market",
        "research",
        "knowledge",
        "source_mesh",
        "backtest",
        "validation",
        "precision",
        "uncertainty",
        "evolution",
        "champion_challenger",
        "counterfactual",
        "self_audit",
        "camera_multimodal",
        "screen_multimodal",
        "conversation_memory",
        "provider_gateway",
        "update_engine"
    )
}
