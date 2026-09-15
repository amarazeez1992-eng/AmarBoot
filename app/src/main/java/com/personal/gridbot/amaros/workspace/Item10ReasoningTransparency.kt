package com.personal.gridbot.amaros.workspace

/** User-visible progress is a concise audit trace, never hidden chain-of-thought. */
enum class AmarReasoningEventType { STARTED, PLANNING, ENGINE_QUERY, ENGINE_RESPONSE, EVIDENCE_CHECK, CONFLICT_CHECK, SYNTHESIS, COMPLETED, BLOCKED }

data class AmarReasoningProgressEvent(val sequence: Long, val type: AmarReasoningEventType, val label: String, val detail: String = "", val elapsedMs: Long, val engineId: String? = null)

enum class AmarAnswerDetailLevel { SHORT, DETAILED }

data class AmarReasoningSession(val requestId: String, val startedAtEpochMs: Long, val finishedAtEpochMs: Long? = null, val events: List<AmarReasoningProgressEvent> = emptyList(), val answerDetail: AmarAnswerDetailLevel = AmarAnswerDetailLevel.SHORT) {
    val elapsedMs: Long get() = ((finishedAtEpochMs ?: startedAtEpochMs) - startedAtEpochMs).coerceAtLeast(0L)
}

data class AmarEngineDialogue(val engineId: String, val requestSummary: String, val responseSummary: String, val elapsedMs: Long, val evidenceIds: List<String> = emptyList())

data class AmarFinalAnswerView(val answer: String, val detailLevel: AmarAnswerDetailLevel, val elapsedMs: Long, val confidence: Double, val evidence: List<EvidenceRecord>, val confirmed: Boolean, val blockers: List<String> = emptyList())

class AmarReasoningTransparency {
    private var sequence = 0L
    private val events = mutableListOf<AmarReasoningProgressEvent>()
    private val dialogues = mutableListOf<AmarEngineDialogue>()
    fun event(type: AmarReasoningEventType, label: String, detail: String, elapsedMs: Long, engineId: String? = null) { events += AmarReasoningProgressEvent(++sequence, type, label, detail, elapsedMs.coerceAtLeast(0L), engineId) }
    fun engineDialogue(engineId: String, requestSummary: String, responseSummary: String, elapsedMs: Long, evidenceIds: List<String>) { dialogues += AmarEngineDialogue(engineId, requestSummary, responseSummary, elapsedMs.coerceAtLeast(0L), evidenceIds.distinct()); event(AmarReasoningEventType.ENGINE_RESPONSE, "Engine response", responseSummary, elapsedMs, engineId) }
    fun snapshot(): List<AmarReasoningProgressEvent> = events.toList()
    fun engineSnapshot(): List<AmarEngineDialogue> = dialogues.toList()
    fun finalAnswer(result: ReasoningResult, detailLevel: AmarAnswerDetailLevel, elapsedMs: Long): AmarFinalAnswerView = AmarFinalAnswerView(result.answer, detailLevel, elapsedMs.coerceAtLeast(0L), result.confidence, result.evidence, !result.blocked && result.evidence.isNotEmpty(), result.blockers)
}

/** Exposes safe summaries of the process, not private chain-of-thought. */
class AmarReasoningProgressController(private val trace: AmarReasoningTransparency) {
    fun start(nowElapsedMs: Long = 0L) = trace.event(AmarReasoningEventType.STARTED, "Started", "Request accepted", nowElapsedMs)
    fun plan(detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.PLANNING, "Planning", detail, elapsedMs)
    fun queryEngine(engineId: String, detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.ENGINE_QUERY, "Consulting engine", detail, elapsedMs, engineId)
    fun checkEvidence(detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.EVIDENCE_CHECK, "Checking evidence", detail, elapsedMs)
    fun checkConflicts(detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.CONFLICT_CHECK, "Checking conflicts", detail, elapsedMs)
    fun synthesize(detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.SYNTHESIS, "Building final answer", detail, elapsedMs)
    fun complete(elapsedMs: Long) = trace.event(AmarReasoningEventType.COMPLETED, "Completed", "Final answer ready", elapsedMs)
    fun blocked(detail: String, elapsedMs: Long) = trace.event(AmarReasoningEventType.BLOCKED, "Blocked", detail, elapsedMs)
}
