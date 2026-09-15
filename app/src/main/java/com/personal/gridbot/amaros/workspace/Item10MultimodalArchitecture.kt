package com.personal.gridbot.amaros.workspace

/** Item 10 multimodal contracts. Implementations are provider-neutral and fail closed. */
enum class AmarInputKind { TEXT, IMAGE, AUDIO, VIDEO, SCREEN }

data class AmarMediaFrame(
    val timestampMs: Long,
    val visibleText: String = "",
    val description: String = "",
    val confidence: Double = 0.0
)

data class AmarVideoSegment(
    val startMs: Long,
    val endMs: Long,
    val frames: List<AmarMediaFrame>,
    val transcript: String = ""
)

data class AmarVideoAnalysis(
    val segments: List<AmarVideoSegment>,
    val summary: String,
    val importantTimestampsMs: List<Long>,
    val evidenceBacked: Boolean,
    val blockers: List<String> = emptyList()
)

interface AmarMultimodalPerceptionEngine {
    fun analyzeVideo(segments: List<AmarVideoSegment>): AmarVideoAnalysis
    fun analyzeScreen(frames: List<AmarMediaFrame>): AmarVideoAnalysis
}

class AmarFailClosedMultimodalPerception : AmarMultimodalPerceptionEngine {
    override fun analyzeVideo(segments: List<AmarVideoSegment>): AmarVideoAnalysis {
        if (segments.isEmpty() || segments.any { it.endMs < it.startMs }) {
            return AmarVideoAnalysis(emptyList(), "", emptyList(), false, listOf("insufficient_visual_evidence"))
        }
        val usable = segments.filter { segment ->
            segment.frames.any { it.confidence in 0.0..1.0 && (it.visibleText.isNotBlank() || it.description.isNotBlank()) } || segment.transcript.isNotBlank()
        }
        if (usable.isEmpty()) {
            return AmarVideoAnalysis(emptyList(), "", emptyList(), false, listOf("unreadable_media"))
        }
        val timestamps = usable.flatMap { it.frames.filter { frame -> frame.confidence >= 0.8 }.map { it.timestampMs } }.distinct().sorted()
        return AmarVideoAnalysis(usable, usable.joinToString(" ") { it.transcript.ifBlank { it.frames.firstOrNull()?.description.orEmpty() } }.trim(), timestamps, true)
    }

    override fun analyzeScreen(frames: List<AmarMediaFrame>): AmarVideoAnalysis =
        analyzeVideo(listOf(AmarVideoSegment(0L, frames.maxOfOrNull { it.timestampMs } ?: 0L, frames)))
}

data class AmarScreenShareSession(
    val id: String,
    val startedAtEpochMs: Long,
    val appLabel: String,
    val permissionGranted: Boolean,
    val stopped: Boolean = false
)

class AmarScreenWorkspace {
    private var session: AmarScreenShareSession? = null

    fun start(request: AmarScreenShareSession): Boolean {
        if (!request.permissionGranted || request.stopped) return false
        session = request
        return true
    }

    fun stop(): Boolean {
        val current = session ?: return false
        session = current.copy(stopped = true)
        return true
    }

    fun active(): Boolean = session?.permissionGranted == true && session?.stopped == false
}

enum class AmarSearchDepth { FAST, DEEP, PARALLEL }

data class AmarWebRequest(
    val query: String,
    val mode: AmarWebMode,
    val depth: AmarSearchDepth,
    val userConfirmedSensitiveAction: Boolean = false
)

data class AmarWebSource(
    val id: String,
    val uri: String,
    val title: String,
    val trusted: Boolean,
    val independent: Boolean,
    val evidence: String
)

interface AmarWebSearchEngine {
    fun search(request: AmarWebRequest): List<AmarWebSource>
}

class AmarWebPolicyGuard {
    fun allow(request: AmarWebRequest): Boolean {
        if (request.query.isBlank()) return false
        return when (request.mode) {
            AmarWebMode.RESTRICTED_SEARCH -> request.depth != AmarSearchDepth.PARALLEL
            AmarWebMode.OPEN_SEARCH -> true
        }
    }
}

data class AmarOpenSourceAdapter(
    val id: String,
    val capabilities: Set<String>,
    val enabled: Boolean = true
)

class AmarOpenSourceIntegrationRegistry {
    private val adapters = linkedMapOf<String, AmarOpenSourceAdapter>()

    fun register(adapter: AmarOpenSourceAdapter): Boolean {
        if (adapter.id.isBlank() || adapters.containsKey(adapter.id)) return false
        adapters[adapter.id] = adapter
        return true
    }

    fun enabled(): List<AmarOpenSourceAdapter> = adapters.values.filter { it.enabled }
}

data class AmarEngineFinding(
    val engineId: String,
    val statement: String,
    val evidence: List<EvidenceRecord>,
    val confidence: Double
)

class AmarMultiEngineOrchestrator(private val fusion: AmarEvidenceFusionEngine) {
    fun fuse(answer: String, findings: List<AmarEngineFinding>): ReasoningResult {
        val evidence = findings.flatMap { it.evidence }.distinctBy { it.id }
        val confidence = findings.map { it.confidence }.filter { it in 0.0..1.0 }.average().takeIf { !it.isNaN() } ?: 0.0
        return fusion.fuse(answer, evidence, confidence)
    }
}

class AmarUncertaintyEngine {
    fun normalize(confidence: Double, blockers: List<String>): Double =
        if (blockers.isNotEmpty() || confidence !in 0.0..1.0) 0.0 else confidence
}

data class AmarQualityMeasurement(
    val weightedScore: Double,
    val requiredGatesPassed: Boolean,
    val evidenceCurrent: Boolean,
    val certified: Boolean
)

class AmarNinePointNineQualityGate {
    fun evaluate(
        weightedScore: Double,
        requiredGatesPassed: Boolean,
        evidenceCurrent: Boolean
    ): AmarQualityMeasurement {
        val scoreOk = weightedScore >= 0.99
        val certified = scoreOk && requiredGatesPassed && evidenceCurrent
        return AmarQualityMeasurement(weightedScore.coerceIn(0.0, 1.0), requiredGatesPassed, evidenceCurrent, certified)
    }
}
