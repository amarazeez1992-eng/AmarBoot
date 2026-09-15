package com.personal.gridbot.amaros.intelligence

import com.personal.gridbot.amaros.agent.AmarAgentCritic
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.EvidenceStance
import com.personal.gridbot.amaros.agent.ResearchFinding

/**
 * Stage 11 / Item 7 deterministic quality benchmark.
 *
 * This is a quality harness, not an execution engine. It evaluates stable
 * contracts already owned by DecisionEngine and AmarAgentCritic and fails
 * closed when benchmark cases are malformed or results are non-finite.
 */
class AmarIntelligenceQualityBenchmark(
    private val decisionEngine: DecisionEngine = DecisionEngine(),
    private val critic: AmarAgentCritic = AmarAgentCritic()
) {
    data class BenchmarkCase(
        val id: String,
        val expected: Boolean,
        val evaluate: () -> Boolean
    )

    data class BenchmarkReport(
        val totalCases: Int,
        val passedCases: Int,
        val accuracy: Double,
        val calibrationError: Double,
        val passed: Boolean,
        val failedCaseIds: List<String>
    )

    fun run(): BenchmarkReport {
        val cases = cases()
        require(cases.isNotEmpty())
        val results = cases.map { case ->
            val actual = runSafely(case)
            case to actual
        }
        val passedCases = results.count { (case, actual) -> actual == case.expected }
        val accuracy = passedCases.toDouble() / cases.size
        val calibrationError = results.map { (case, actual) ->
            kotlin.math.abs((if (actual) 1.0 else 0.0) - (if (case.expected) 1.0 else 0.0))
        }.average()
        val failed = results.filter { (case, actual) -> actual != case.expected }.map { it.first.id }
        return BenchmarkReport(
            totalCases = cases.size,
            passedCases = passedCases,
            accuracy = accuracy,
            calibrationError = calibrationError,
            passed = accuracy >= MIN_ACCURACY && calibrationError <= MAX_CALIBRATION_ERROR,
            failedCaseIds = failed
        )
    }

    private fun runSafely(case: BenchmarkCase): Boolean = try {
        case.evaluate()
    } catch (_: RuntimeException) {
        false
    }

    private fun cases(): List<BenchmarkCase> = listOf(
        BenchmarkCase("risk.no_evidence", expected = false) {
            decisionEngine.assessQuantitativeRisk(DecisionEngine.QuantitativeInput()) == null
        },
        BenchmarkCase("risk.low_vs_high_monotonic", expected = true) {
            val low = decisionEngine.assessQuantitativeRisk(lowRiskInput())
            val high = decisionEngine.assessQuantitativeRisk(highRiskInput())
            low != null && high != null && high.score > low.score
        },
        BenchmarkCase("risk.invalid_is_not_zero", expected = true) {
            decisionEngine.assessQuantitativeRisk(
                DecisionEngine.QuantitativeInput(
                    closes = listOf(100.0, Double.NaN, 101.0, 102.0),
                    equityCurve = listOf(1000.0, Double.POSITIVE_INFINITY),
                    losses = listOf(10.0, -1.0)
                )
            ) == null
        },
        BenchmarkCase("risk.partial_invalid_preserves_valid_drawdown", expected = true) {
            val risk = decisionEngine.assessQuantitativeRisk(
                DecisionEngine.QuantitativeInput(
                    closes = listOf(100.0, Double.NaN, 101.0, 102.0),
                    equityCurve = listOf(1000.0, 1200.0, 900.0, 950.0),
                    losses = listOf(10.0, 20.0, 30.0, 40.0, 50.0),
                    varConfidence = 0.95,
                    varScale = 100.0
                )
            )
            risk != null && risk.realizedVolatility == null && risk.maxDrawdown != null &&
                risk.normalizedHistoricalVar != null && risk.score.isFinite()
        },
        BenchmarkCase("risk.score_bounded", expected = true) {
            val risk = decisionEngine.assessQuantitativeRisk(highRiskInput())
            risk != null && risk.score.isFinite() && risk.score in 0.0..1.0
        },
        BenchmarkCase("critic.accepts_supported_answer", expected = true) {
            val evidence = listOf(
                finding("https://source-a.test", EvidenceStance.SUPPORTS),
                finding("https://source-b.test", EvidenceStance.SUPPORTS)
            )
            critic.review("The evidence supports the conclusion.", evidence, requireEvidence = true).accepted
        },
        BenchmarkCase("critic_rejects_conflict", expected = true) {
            val evidence = listOf(
                finding("https://source-a.test", EvidenceStance.SUPPORTS),
                finding("https://source-b.test", EvidenceStance.OPPOSES)
            )
            !critic.review("The evidence is mixed.", evidence, requireEvidence = true).accepted
        },
        BenchmarkCase("critic_rejects_guarantee", expected = true) {
            val evidence = listOf(
                finding("https://source-a.test", EvidenceStance.SUPPORTS),
                finding("https://source-b.test", EvidenceStance.SUPPORTS)
            )
            val result = critic.review("This result is guaranteed.", evidence, requireEvidence = true)
            !result.accepted && "guarantee_language" in result.issues
        }
    )

    private fun lowRiskInput() = DecisionEngine.QuantitativeInput(
        closes = listOf(100.0, 100.1, 100.0, 100.1),
        equityCurve = listOf(1000.0, 1005.0, 1003.0, 1007.0),
        losses = listOf(1.0, 1.0, 2.0, 1.0, 2.0),
        varConfidence = 0.95,
        varScale = 100.0
    )

    private fun highRiskInput() = DecisionEngine.QuantitativeInput(
        closes = listOf(100.0, 120.0, 80.0, 130.0),
        equityCurve = listOf(1000.0, 1200.0, 400.0, 450.0),
        losses = listOf(50.0, 70.0, 90.0, 100.0, 80.0),
        varConfidence = 0.95,
        varScale = 100.0
    )

    private fun finding(uri: String, stance: EvidenceStance) = ResearchFinding(
        sourceTitle = uri,
        sourceUri = uri,
        evidence = "deterministic benchmark evidence",
        authority = Authority.REPUTABLE,
        stance = stance
    )

    private companion object {
        private const val MIN_ACCURACY = 0.90
        private const val MAX_CALIBRATION_ERROR = 0.10
    }
}
