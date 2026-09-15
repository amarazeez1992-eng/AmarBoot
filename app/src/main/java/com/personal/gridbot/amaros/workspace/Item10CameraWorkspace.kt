package com.personal.gridbot.amaros.workspace

/** Camera session is user-initiated, permission-bound, auditable, and stops immediately on request. */
data class AmarCameraFrame(
    val timestampMs: Long,
    val visibleText: String = "",
    val visibleDescription: String = "",
    val confidence: Double = 0.0
)

data class AmarCameraSession(
    val id: String,
    val startedAtEpochMs: Long,
    val permissionGranted: Boolean,
    val userInitiated: Boolean,
    val stopped: Boolean = false
)

data class AmarCameraObservation(
    val sessionId: String,
    val summary: String,
    val observedFrames: List<AmarCameraFrame>,
    val evidenceBacked: Boolean,
    val blockers: List<String> = emptyList()
)

class AmarCameraWorkspace(private val audit: AmarWorkspaceAuditLog) {
    private var session: AmarCameraSession? = null

    fun start(request: AmarCameraSession): Boolean {
        if (!request.permissionGranted || !request.userInitiated || request.stopped) return false
        session = request
        audit.record(AmarWorkspaceAuditEvent(request.id, "camera_start", "user", request.startedAtEpochMs, true, "authorized"))
        return true
    }

    fun stop(nowEpochMs: Long, actor: String = "user"): Boolean {
        val current = session ?: return false
        session = current.copy(stopped = true)
        audit.record(AmarWorkspaceAuditEvent(current.id, "camera_stop", actor, nowEpochMs, true, "stopped"))
        return true
    }

    fun active(): Boolean = session?.permissionGranted == true && session?.userInitiated == true && session?.stopped == false

    fun analyze(frames: List<AmarCameraFrame>): AmarCameraObservation {
        val current = session
        if (current == null || !active()) return AmarCameraObservation("", "", emptyList(), false, listOf("camera_not_active"))
        if (frames.isEmpty()) return AmarCameraObservation(current.id, "", emptyList(), false, listOf("insufficient_visual_evidence"))
        val usable = frames.filter { it.confidence in 0.0..1.0 && (it.visibleText.isNotBlank() || it.visibleDescription.isNotBlank()) }
        if (usable.isEmpty()) return AmarCameraObservation(current.id, "", emptyList(), false, listOf("unreadable_camera_view"))
        val summary = usable.joinToString(" ") { it.visibleDescription.ifBlank { it.visibleText } }.trim()
        return AmarCameraObservation(current.id, summary, usable, summary.isNotBlank(), if (summary.isBlank()) listOf("no_interpretable_observation") else emptyList())
    }
}
