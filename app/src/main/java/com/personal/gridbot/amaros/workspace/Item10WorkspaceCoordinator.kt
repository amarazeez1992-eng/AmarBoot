package com.personal.gridbot.amaros.workspace

/** Deterministic Item 10 coordinator: conversation -> memory -> knowledge -> external evidence. */
class AmarWorkspaceCoordinator(
    private val store: AmarConversationMemoryStore,
    private val perception: AmarMultimodalPerceptionEngine,
    private val fusion: AmarEvidenceFusionEngine,
    private val qualityGate: AmarNinePointNineQualityGate,
    private val audit: AmarWorkspaceAuditLog,
    private val screenWorkspace: AmarScreenWorkspace = AmarScreenWorkspace(),
    private val cameraWorkspace: AmarCameraWorkspace = AmarCameraWorkspace(audit)
) {
    fun analyzeVideo(segments: List<AmarVideoSegment>): ReasoningResult {
        val analysis = perception.analyzeVideo(segments)
        if (!analysis.evidenceBacked) return fusion.fuse("", emptyList(), 0.0)
        val evidence = analysis.segments.flatMap { segment ->
            val frameEvidence = segment.frames
                .filter { it.confidence >= 0.8 && (it.visibleText.isNotBlank() || it.description.isNotBlank()) }
                .map { frame -> EvidenceRecord("video:frame:${frame.timestampMs}", "multimodal:${frame.timestampMs}", frame.visibleText.ifBlank { frame.description }, true, true) }
            val transcriptEvidence = segment.transcript.trim().takeIf { it.isNotBlank() }?.let { transcript ->
                EvidenceRecord("video:transcript:${segment.startMs}-${segment.endMs}", "multimodal:transcript:${segment.startMs}-${segment.endMs}", transcript, true, true)
            }
            frameEvidence + listOfNotNull(transcriptEvidence)
        }.distinctBy { it.id }
        return fusion.fuse(analysis.summary, evidence, if (evidence.isEmpty()) 0.0 else 1.0)
    }

    fun analyzeScreen(frames: List<AmarMediaFrame>): ReasoningResult {
        val analysis = perception.analyzeScreen(frames)
        if (!analysis.evidenceBacked) return fusion.fuse("", emptyList(), 0.0)
        val evidence = analysis.segments.flatMap { segment ->
            segment.frames.filter { it.confidence >= 0.8 && (it.visibleText.isNotBlank() || it.description.isNotBlank()) }
                .map { frame -> EvidenceRecord("screen:frame:${frame.timestampMs}", "screen:${frame.timestampMs}", frame.visibleText.ifBlank { frame.description }, true, true) }
        }.distinctBy { it.id }
        return fusion.fuse(analysis.summary, evidence, if (evidence.isEmpty()) 0.0 else 1.0)
    }

    fun analyzeCamera(frames: List<AmarCameraFrame>): AmarCameraObservation = cameraWorkspace.analyze(frames)

    fun startCamera(session: AmarCameraSession): Boolean = cameraWorkspace.start(session)

    fun stopCamera(actor: String, nowEpochMs: Long): Boolean = cameraWorkspace.stop(nowEpochMs, actor)

    fun certifyQuality(weightedScore: Double, requiredGatesPassed: Boolean, evidenceCurrent: Boolean): AmarQualityMeasurement =
        qualityGate.evaluate(weightedScore, requiredGatesPassed, evidenceCurrent)

    fun recordStop(actor: String, nowEpochMs: Long): Boolean {
        val stopped = screenWorkspace.stop()
        audit.record(AmarWorkspaceAuditEvent("screen-stop:$nowEpochMs", "screen_stop", actor, nowEpochMs, stopped, if (stopped) "user_or_system_stop" else "no_active_screen_session"))
        return stopped
    }

    fun recall(query: String): List<ConversationRecord> = store.searchConversations(query)
}
