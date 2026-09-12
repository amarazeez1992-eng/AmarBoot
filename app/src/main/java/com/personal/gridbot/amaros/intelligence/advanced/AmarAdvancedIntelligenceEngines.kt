package com.personal.gridbot.amaros.intelligence.advanced

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

class AmarStrategyCompiler {
    fun compile(profile: AmarStrategyProfile, brokerConstraints: List<String> = emptyList()): AmarCompiledStrategyPlan {
        val errors = mutableListOf<String>(); val warnings = mutableListOf<String>()
        if (profile.id.isBlank()) errors += "strategy id is empty"
        if (profile.name.isBlank()) errors += "strategy name is empty"
        if (profile.entryRules.isEmpty()) errors += "entry rules are missing"
        if (profile.invalidationRules.isEmpty()) errors += "invalidation rules are missing"
        if (profile.stopLossRule.isNullOrBlank()) errors += "stop-loss rule is missing"
        if (profile.takeProfitRule.isNullOrBlank()) warnings += "take-profit rule is not explicit"
        if (profile.riskPerTrade == null || profile.riskPerTrade <= 0.0) errors += "risk per trade must be positive"
        if (profile.allowedRegimes.isEmpty()) warnings += "no regime restriction"
        if (profile.maxSpread != null && profile.maxSpread < 0.0) errors += "max spread cannot be negative"
        val executable = profile.entryRules.map { "ENTRY: $it" } + profile.invalidationRules.map { "INVALIDATION: $it" } + listOfNotNull(profile.stopLossRule?.let { "SL: $it" }, profile.takeProfitRule?.let { "TP: $it" })
        return AmarCompiledStrategyPlan(profile.id, profile.version, executable, listOfNotNull(profile.riskPerTrade?.let { "risk_per_trade=$it" }, profile.maxSpread?.let { "max_spread=$it" }), profile.allowedRegimes, brokerConstraints, errors.isEmpty(), errors, warnings)
    }
}

class AmarWalkForwardEngine {
    fun split(candles: List<AmarOhlc>, trainSize: Int, validationSize: Int, oosSize: Int, step: Int = oosSize): List<AmarTimeSplit> {
        require(trainSize > 0 && validationSize > 0 && oosSize > 0 && step > 0)
        if (candles.size < trainSize + validationSize + oosSize) return emptyList()
        val out = mutableListOf<AmarTimeSplit>(); var start = 0
        while (start + trainSize + validationSize + oosSize <= candles.size) {
            val train = candles.subList(start, start + trainSize)
            val validation = candles.subList(start + trainSize, start + trainSize + validationSize)
            val oos = candles.subList(start + trainSize + validationSize, start + trainSize + validationSize + oosSize)
            out += AmarTimeSplit(train.first().timestamp, train.last().timestamp, validation.first().timestamp, validation.last().timestamp, oos.first().timestamp, oos.last().timestamp)
            start += step
        }
        return out
    }
    fun aggregate(windows: List<AmarValidationWindowResult>): AmarWalkForwardReport {
        if (windows.isEmpty()) return AmarWalkForwardReport(emptyList(), 0.0, 0.0, true, false)
        val oos = windows.map { it.oosScore }; val mean = oos.average(); val variance = oos.map { (it - mean) * (it - mean) }.average()
        return AmarWalkForwardReport(windows, mean, (mean / (1.0 + sqrt(variance))).coerceIn(-1.0, 1.0), windows.none { it.leakageDetected }, windows.all { !it.leakageDetected && it.sampleCount > 0 })
    }
}

class AmarStressTestingEngine {
    fun evaluate(baseScore: Double, scenarios: List<AmarStressScenario>): List<AmarStressResult> = scenarios.map { s ->
        val penalty = when (s.type) { AmarStressType.SPREAD -> s.magnitude * .20; AmarStressType.SLIPPAGE -> s.magnitude * .25; AmarStressType.NEWS -> s.magnitude * .35; AmarStressType.REGIME_SHIFT -> s.magnitude * .30 }
        AmarStressResult(s, baseScore, baseScore - penalty, max(0.0, penalty))
    }
}

class AmarEvidenceEngine {
    fun score(e: AmarEvidence): AmarEvidenceScore {
        val tier = when (e.tier) { AmarEvidenceTier.PRIMARY -> 1.0; AmarEvidenceTier.SECONDARY -> .8; AmarEvidenceTier.IMPLEMENTATION -> .6; AmarEvidenceTier.OPINION -> .3; AmarEvidenceTier.UNKNOWN -> .1 }
        return AmarEvidenceScore(e.id, (tier * e.quality.coerceIn(0.0, 1.0)).coerceIn(0.0, 1.0), listOf("tier=${e.tier}", "quality=${e.quality}"))
    }
    fun conflicts(evidence: List<AmarEvidence>): List<AmarEvidenceConflict> = evidence.groupBy { normalize(it.claim) }.values.filter { it.size > 1 }.map { group ->
        val claims = group.map { it.claim.lowercase().trim() }.distinct()
        AmarEvidenceConflict(normalize(group.first().claim), group.map { it.id }, if (claims.size > 1) 1.0 else 0.0, if (claims.size > 1) "sources disagree" else "duplicate supporting evidence")
    }
    private fun normalize(s: String) = s.lowercase().replace(Regex("[^a-z0-9\\s]"), " ").replace(Regex("\\s+"), " ").trim()
}

class AmarStrategyLineageStore {
    private val events = mutableListOf<AmarStrategyLineageEvent>()
    fun append(event: AmarStrategyLineageEvent) { events += event }
    fun snapshot(): List<AmarStrategyLineageEvent> = events.toList()
}

class AmarDecisionMemoryStore {
    private val records = mutableListOf<AmarDecisionMemory>()
    fun remember(record: AmarDecisionMemory) { records += record }
    fun search(query: String): List<AmarDecisionMemory> = records.filter { query.isBlank() || it.decision.contains(query, true) || it.context.contains(query, true) || it.outcome.orEmpty().contains(query, true) }
    fun snapshot(): List<AmarDecisionMemory> = records.toList()
}

class AmarPortfolioExposureEngine {
    fun assess(exposures: List<AmarExposure>): AmarPortfolioExposureReport {
        if (exposures.isEmpty()) return AmarPortfolioExposureReport(0.0, 0.0, 0.0, 0.0, emptyList())
        val gross = exposures.sumOf { abs(it.risk) }
        val net = exposures.sumOf { when (it.direction) { AmarStrategyDirection.LONG -> it.risk; AmarStrategyDirection.SHORT -> -it.risk; else -> 0.0 } }
        val concentration = (exposures.groupBy { it.symbol }.values.maxOfOrNull { group -> group.sumOf { abs(it.risk) } } ?: 0.0) / max(gross, 1e-12)
        val regimeConcentration = (exposures.groupBy { it.regime }.values.maxOfOrNull { group -> group.sumOf { abs(it.risk) } } ?: 0.0) / max(gross, 1e-12)
        val warnings = buildList { if (concentration > .5) add("single-symbol concentration > 50%"); if (regimeConcentration > .7) add("regime concentration > 70%") }
        return AmarPortfolioExposureReport(gross, net, concentration, regimeConcentration, warnings)
    }
}

class AmarTradingKnowledgeGraph {
    private val nodes = linkedMapOf<String, AmarGraphNode>(); private val edges = mutableListOf<AmarGraphEdge>()
    fun upsertNode(node: AmarGraphNode) { nodes[node.id] = node }
    fun connect(from: String, to: String, relation: String, weight: Double = 1.0) { if (nodes.containsKey(from) && nodes.containsKey(to)) edges += AmarGraphEdge(from, to, relation, weight.coerceIn(-1.0, 1.0)) }
    fun snapshot() = AmarKnowledgeGraphSnapshot(nodes.values.toList(), edges.toList())
}

class AmarAdvancedIntelligenceFacade(
    val regimeEngine: AmarMarketRegimeEngine = AmarMarketRegimeEngine(),
    val compiler: AmarStrategyCompiler = AmarStrategyCompiler(),
    val walkForward: AmarWalkForwardEngine = AmarWalkForwardEngine(),
    val stress: AmarStressTestingEngine = AmarStressTestingEngine(),
    val evidence: AmarEvidenceEngine = AmarEvidenceEngine(),
    val lineage: AmarStrategyLineageStore = AmarStrategyLineageStore(),
    val decisions: AmarDecisionMemoryStore = AmarDecisionMemoryStore(),
    val portfolio: AmarPortfolioExposureEngine = AmarPortfolioExposureEngine(),
    val knowledgeGraph: AmarTradingKnowledgeGraph = AmarTradingKnowledgeGraph()
)
