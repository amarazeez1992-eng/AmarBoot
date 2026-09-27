package com.personal.gridbot.amaros.intelligence.verification

import com.personal.gridbot.amaros.agent.AmarClaimVerificationReport
import com.personal.gridbot.amaros.agent.AttributionResult
import com.personal.gridbot.amaros.agent.BlockingResult
import com.personal.gridbot.amaros.agent.HallucinationDetectionGuard
import com.personal.gridbot.amaros.agent.HallucinationDetectionResult
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.SelfContradictionDetector
import com.personal.gridbot.amaros.agent.SelfContradictionResult
import com.personal.gridbot.amaros.agent.SelfContradictionStatus
import com.personal.gridbot.amaros.agent.SourceAttributionEnforcer
import com.personal.gridbot.amaros.agent.TemporalConsistencyCheck
import com.personal.gridbot.amaros.agent.TemporalConsistencyResult
import com.personal.gridbot.amaros.agent.UnsupportedClaimBlocking
import com.personal.gridbot.amaros.agent.correlation.AmarCrossSourceAgreementConsumer
import com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementResult
import com.personal.gridbot.amaros.agent.correlation.CrossSourceCorrelationResult

/**
 * Stage 11 / Item 4 — Hallucination Firewall unified gate.
 *
 * Addition 8 coordination: consumes additions 1-7 in deterministic order
 * and produces the final decision. Delegates all logic to existing components.
 */
class AmarHallucinationFirewallGate(
    private val blocking: UnsupportedClaimBlocking = UnsupportedClaimBlocking,
    private val attribution: SourceAttributionEnforcer = SourceAttributionEnforcer,
    private val detection: HallucinationDetectionGuard = HallucinationDetectionGuard,
    private val temporal: TemporalConsistencyCheck = TemporalConsistencyCheck,
    private val agreementConsumer: AmarCrossSourceAgreementConsumer = AmarCrossSourceAgreementConsumer(),
    private val selfContradiction: SelfContradictionDetector = SelfContradictionDetector(),
    private val coverageGate: FinalAnswerClaimCoverageGate = FinalAnswerClaimCoverageGate(),
    private val decisionGate: FinalHallucinationDecisionGate = FinalHallucinationDecisionGate()
) {
    data class FirewallReport(
        val blocking: BlockingResult,
        val attribution: AttributionResult,
        val hallucination: HallucinationDetectionResult,
        val temporal: TemporalConsistencyResult,
        val agreement: CrossSourceAgreementResult,
        val selfContradiction: SelfContradictionResult,
        val coverage: FinalAnswerClaimCoverageResult,
        val decision: FinalDecisionResult
    )

    fun evaluate(
        answer: String,
        claims: List<String>,
        report: AmarClaimVerificationReport,
        findings: List<ResearchFinding>,
        correlation: CrossSourceCorrelationResult?,
        temporalEvidence: List<com.personal.gridbot.amaros.agent.TemporalEvidenceRecord> = emptyList(),
        coverage: FinalAnswerClaimCoverageResult? = null,
        nowEpochMs: Long = System.currentTimeMillis()
    ): FirewallReport {
        val blockingResult = blocking.evaluate(report)
        val attributionResult = attribution.evaluate(report, findings)
        val hallucinationResult = detection.evaluate(report, blockingResult, attributionResult, answer)
        val temporalResult = temporal.evaluate(report, temporalEvidence, hallucinationResult, nowEpochMs)
        val agreementResult = correlation?.let { agreementConsumer.consume(it) }
            ?: CrossSourceAgreementResult(
                status = com.personal.gridbot.amaros.agent.correlation.CrossSourceAgreementStatus.INSUFFICIENT_AGREEMENT_DATA,
                agreementGroups = emptyList(),
                dependencyGroups = emptyList(),
                disagreementGroups = emptyList(),
                reason = com.personal.gridbot.amaros.agent.correlation.CorrelationReason.INSUFFICIENT_CORRELATION_DATA
            )
        val scResult = selfContradiction.detect(claims)
        val coverageResult = coverage ?: FinalAnswerClaimCoverageResult(
            status = FinalAnswerClaimCoverageStatus.INSUFFICIENT_COVERAGE_DATA,
            uncoveredClaimIds = emptyList()
        )
        val decision = decisionGate.evaluate(
            FinalHallucinationDecisionInput(
                addition1 = blockingResult,
                addition2 = attributionResult,
                addition3 = hallucinationResult,
                addition4 = temporalResult,
                addition5 = agreementResult,
                addition6 = scResult,
                addition7 = coverageResult
            )
        )
        return FirewallReport(
            blockingResult, attributionResult, hallucinationResult,
            temporalResult, agreementResult, scResult, coverageResult, decision
        )
    }
}
