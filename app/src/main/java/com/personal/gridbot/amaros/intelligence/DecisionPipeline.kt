package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.data.AmarDataProvider

/** B7 orchestration: data -> context -> proposal -> safety gate. No execution path exists here. */
class DecisionPipeline(
    private val provider: AmarDataProvider,
    private val analyzer: MarketAnalyzer = MarketAnalyzer(),
    private val decisionEngine: DecisionEngine = DecisionEngine()
) {
    data class Result(
        val intelligence: IntelligenceSnapshot,
        val proposal: DecisionEngine.DecisionProposal
    )

    fun evaluate(): Result {
        val data = provider.snapshot()
        val context = analyzer.analyze(data)
        val validation = DecisionValidation.forDemo()
        val intelligence = IntelligenceSnapshot(data, context, validation, data.generatedAtEpochMs)
        return Result(intelligence, decisionEngine.evaluate(data, context))
    }
}
