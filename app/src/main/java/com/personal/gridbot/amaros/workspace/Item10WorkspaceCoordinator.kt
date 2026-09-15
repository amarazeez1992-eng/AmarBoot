package com.personal.gridbot.amaros.workspace

/** Deterministic Item 10 coordinator: conversation -> memory -> knowledge -> external evidence. */
class AmarWorkspaceCoordinator(
    private val store: AmarConversationMemoryStore,
    private val perception: AmarMultimodalPerceptionEngine,
    private val fusion: AmarEvidenceFusionEngine,
    private val qualityGate: AmarNinePointNineQualityGate,
    private val audit: AmarWorkspaceAuditLog
) {
    fun analyzeVideo(segments: List<AmarVideoSegment>): ReasoningResult {
        val analysis = perception.analyzeVideo(segments)
        if (!analysis.evidenceBacked) {
            return fusion.fuse("", emptyList(), 0.0)
        }
        val evidence = analysis.segments.flatMap { segment ->
            segment.frames.filter { it.confidence >= 0.8 }.map { frame ->
                EvidenceRecord(
                    id = "video:${frame.timestampMs}",
                    source = "multimodal:${frame.timestampMs}",
                    statement = frame.visibleText.ifBlank { frame.description },
                    independent = true,
                    valid = true
                )
            }
        }.filter { it.statement.isNotBlank() }
        return fusion.fuse(analysis.summary, evidence, if (evidence.isEmpty()) 0.0 else 1.0)
    }

    fun analyzeScreen(frames: List<AmarMediaFrame>): ReasoningResult = analyzeVideo(
        listOf(AmarVideoSegment(0L, frames.maxOfOrNull { it.timestampMs } ?: 0L, frames))
    )

    fun certifyQuality(weightedScore: Double, requiredGatesPassed: Boolean, evidenceCurrent: Boolean): AmarQualityMeasurement =
        qualityGate.evaluate(weightedScore, requiredGatesPassed, evidenceCurrent)

    fun recordStop(actor: String, nowEpochMs: Long): Boolean {
        audit.record(
            AmarWorkspaceAuditEvent(
                id = "screen-stop:$nowEpochMs",
                action = "screen_stop",
                actor = actor,
                timestampEpochMs = nowEpochMs,
                allowed = true,
                reason = "user_or_system_stop"
            )
        )
        return true
    }

    fun recall(query: String): List<ConversationRecord> = store.searchConversations(query)
}
