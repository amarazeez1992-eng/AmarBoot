package com.personal.gridbot.amaros.workspace

/** Deterministic Item 10 coordinator: conversation -> memory -> knowledge -> external evidence. */
class AmarWorkspaceCoordinator(
    private val store: AmarConversationMemoryStore,
    private val perception: AmarMultimodalPerceptionEngine,
    private val fusion: AmarEvidenceFusionEngine,
    private val qualityGate: AmarNinePointNineQualityGate,
    private val audit: AmarWorkspaceAuditLog,
    private val screenWorkspace: AmarScreenWorkspace = AmarScreenWorkspace()
) {
    fun analyzeVideo(segments: List<AmarVideoSegment>): ReasoningResult {
        val analysis = perception.analyzeVideo(segments)
        if (!analysis.evidenceBacked) return fusion.fuse("", emptyList(), 0.0)
        val evidence = analysis.segments.flatMap { segment ->
            val frameEvidence = segment.frames
                .filter { it.confidence >= 0.8 && (it.visibleText.isNotBlank() || it.description.isNotBlank()) }
                .map { frame ->
                    EvidenceRecord(
                        id = "video:frame:${frame.timestampMs}",
                        source = "multimodal:${frame.timestampMs}",
                        statement = frame.visibleText.ifBlank { frame.description },
                        independent = true,
                        valid = true
                    )
                }
            val transcriptEvidence = segment.transcript.trim().takeIf { it.isNotBlank() }?.let { transcript ->
                EvidenceRecord(
                    id = "video:transcript:${segment.startMs}-${segment.endMs}",
                    source = "multimodal:transcript:${segment.startMs}-${segment.endMs}",
                    statement = transcript,
                    independent = true,
                    valid = true
                )
            }
            frameEvidence + listOfNotNull(transcriptEvidence)
        }.distinctBy { it.id }
        return fusion.fuse(analysis.summary, evidence, if (evidence.isEmpty()) 0.0 else 1.0)
    }

    fun analyzeScreen(frames: List<AmarMediaFrame>): ReasoningResult {
        val analysis = perception.analyzeScreen(frames)
        if (!analysis.evidenceBacked) return fusion.fuse("", emptyList(), 0.0)
        val evidence = analysis.segments.flatMap { segment ->
            segment.frames
                .filter { it.confidence >= 0.8 && (it.visibleText.isNotBlank() || it.description.isNotBlank()) }
                .map { frame ->
                    EvidenceRecord(
                        id = "screen:frame:${frame.timestampMs}",
                        source = "screen:${frame.timestampMs}",
                        statement = frame.visibleText.ifBlank { frame.description },
                        independent = true,
                        valid = true
                    )
                }
        }.distinctBy { it.id }
        return fusion.fuse(analysis.summary, evidence, if (evidence.isEmpty()) 0.0 else 1.0)
    }

    fun certifyQuality(weightedScore: Double, requiredGatesPassed: Boolean, evidenceCurrent: Boolean): AmarQualityMeasurement =
        qualityGate.evaluate(weightedScore, requiredGatesPassed, evidenceCurrent)

    fun recordStop(actor: String, nowEpochMs: Long): Boolean {
        val stopped = screenWorkspace.stop()
        audit.record(
            AmarWorkspaceAuditEvent(
                id = "screen-stop:$nowEpochMs",
                action = "screen_stop",
                actor = actor,
                timestampEpochMs = nowEpochMs,
                allowed = stopped,
                reason = if (stopped) "user_or_system_stop" else "no_active_screen_session"
            )
        )
        return stopped
    }

    fun recall(query: String): List<ConversationRecord> = store.searchConversations(query)
}
