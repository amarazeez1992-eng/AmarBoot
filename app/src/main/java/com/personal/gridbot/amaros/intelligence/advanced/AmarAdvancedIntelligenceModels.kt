package com.personal.gridbot.amaros.intelligence.advanced

import java.time.Instant

enum class AmarMarketRegime { TREND, RANGE, BREAKOUT, HIGH_VOLATILITY, LOW_VOLATILITY, NEWS_ABNORMAL, TRANSITION, UNKNOWN }

data class AmarOhlc(val timestamp: Long, val open: Double, val high: Double, val low: Double, val close: Double, val spread: Double = 0.0)
data class AmarRegimeObservation(val regime: AmarMarketRegime, val confidence: Double, val volatilityScore: Double, val trendScore: Double, val breakoutScore: Double, val abnormalityScore: Double, val timestamp: Long)
enum class AmarStrategyDirection { LONG, SHORT, BOTH, NONE }
data class AmarStrategyProfile(val id: String, val version: Int, val name: String, val direction: AmarStrategyDirection, val entryRules: List<String>, val invalidationRules: List<String>, val stopLossRule: String?, val takeProfitRule: String?, val riskPerTrade: Double?, val allowedRegimes: Set<AmarMarketRegime> = emptySet(), val maxSpread: Double? = null, val sessionRules: List<String> = emptyList(), val executionConstraints: List<String> = emptyList())
data class AmarCompiledStrategyPlan(val strategyId: String, val sourceVersion: Int, val executableRules: List<String>, val riskConstraints: List<String>, val regimeConstraints: Set<AmarMarketRegime>, val brokerConstraints: List<String>, val valid: Boolean, val errors: List<String>, val warnings: List<String>)
data class AmarTimeSplit(val trainStart: Long, val trainEnd: Long, val validationStart: Long, val validationEnd: Long, val oosStart: Long, val oosEnd: Long)
data class AmarValidationWindowResult(val window: AmarTimeSplit, val trainScore: Double, val validationScore: Double, val oosScore: Double, val leakageDetected: Boolean, val sampleCount: Int)
data class AmarWalkForwardReport(val windows: List<AmarValidationWindowResult>, val aggregateOosScore: Double, val robustnessScore: Double, val leakageFree: Boolean, val verified: Boolean)
enum class AmarStressType { SPREAD, SLIPPAGE, NEWS, REGIME_SHIFT }
data class AmarStressScenario(val type: AmarStressType, val magnitude: Double, val description: String)
data class AmarStressResult(val scenario: AmarStressScenario, val baseScore: Double, val stressedScore: Double, val degradation: Double)
enum class AmarEvidenceTier { PRIMARY, SECONDARY, IMPLEMENTATION, OPINION, UNKNOWN }
data class AmarEvidence(val id: String, val source: String, val claim: String, val tier: AmarEvidenceTier, val quality: Double, val timestamp: Long = Instant.now().toEpochMilli())
data class AmarEvidenceScore(val evidenceId: String, val score: Double, val reasons: List<String>)
data class AmarEvidenceConflict(val claimKey: String, val evidenceIds: List<String>, val severity: Double, val explanation: String)
enum class AmarLineageEventType { CREATED, COMPILED, TESTED, MUTATED, EVOLVED, APPROVED, REJECTED, RETIRED }
data class AmarStrategyLineageEvent(val strategyId: String, val parentStrategyId: String?, val event: AmarLineageEventType, val version: Int, val resultReference: String?, val timestamp: Long = Instant.now().toEpochMilli())
data class AmarDecisionMemory(val id: String, val decision: String, val context: String, val outcome: String?, val userApproved: Boolean, val timestamp: Long = Instant.now().toEpochMilli())
data class AmarExposure(val strategyId: String, val symbol: String, val direction: AmarStrategyDirection, val risk: Double, val regime: AmarMarketRegime)
data class AmarPortfolioExposureReport(val grossRisk: Double, val netDirectionalRisk: Double, val concentrationRisk: Double, val correlatedStrategyRisk: Double, val warnings: List<String>)
data class AmarGraphNode(val id: String, val type: String, val label: String)
data class AmarGraphEdge(val from: String, val to: String, val relation: String, val weight: Double)
data class AmarKnowledgeGraphSnapshot(val nodes: List<AmarGraphNode>, val edges: List<AmarGraphEdge>)
