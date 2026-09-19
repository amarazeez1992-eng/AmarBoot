package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataProvider

/** B7 orchestration: data -> context -> proposal -> safety gate. No execution path exists here. */
class DecisionPipeline(
    private val provider: AmarDataProvider,
    private val analyzer: MarketAnalyzer = MarketAnalyzer(),
    private val decisionEngine: DecisionEngine = DecisionEngine(),
    private val confidenceEngine: com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine = com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine
) {
    data class Result(
        val intelligence: IntelligenceSnapshot,
        val proposal: DecisionEngine.DecisionProposal,
        val confidence: com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine.Result
    )

    fun evaluate(): Result {
        val data = provider.snapshot()
        val context = analyzer.analyze(data)
        val validation = DecisionValidation.forDemo()
        val intelligence = IntelligenceSnapshot(data, context, validation, data.generatedAtEpochMs)
        val proposal = decisionEngine.evaluate(data, context)
        val evidence = context.evidence
        val confidence = confidenceEngine.evaluate(com.personal.gridbot.amaros.intelligence.confidence.AmarConfidenceEngine.Evidence(
            quality = evidence.map { it.confidence }.average().coerceIn(0.0, 1.0),
            completeness = (evidence.size / 4.0).coerceIn(0.0, 1.0),
            freshness = 1.0,
            agreement = if (evidence.isEmpty()) 0.0 else (1.0 - evidence.map { kotlin.math.abs(it.score - context.directionalScore) }.average()).coerceIn(0.0, 1.0),
            sourceReliability = evidence.map { it.confidence }.average().coerceIn(0.0, 1.0)
        ))
        return Result(intelligence, proposal, confidence)
    }
}
