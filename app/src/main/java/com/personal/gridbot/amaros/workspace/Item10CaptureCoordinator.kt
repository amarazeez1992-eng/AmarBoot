package com.personal.gridbot.amaros.workspace

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Base64
import java.security.MessageDigest

/**
 * Single lifecycle coordinator for the three live input transports:
 * voice, camera, and screen. It only produces evidence metadata; interpretation
 * remains an Agent capability and is never fabricated here.
 */
class Item10CaptureCoordinator(
    private val activity: Activity,
    private val onEvidence: (Evidence) -> Unit
) {
    data class Evidence(
        val channel: Channel,
        val timestampMs: Long,
        val byteCount: Int,
        val sha256: String,
        val width: Int = 0,
        val height: Int = 0,
        val uriOrPath: String? = null
    )

    enum class Channel { VOICE, CAMERA, SCREEN }

    private val voice = Item10VoiceRecorder(activity)
    private val camera = Item10NativeCameraTransport(activity, onFrame = { frame ->
        onEvidence(Evidence(Channel.CAMERA, frame.timestampMs, frame.jpegBytes.size, sha256(frame.jpegBytes), frame.width, frame.height))
    })
    private val screen = Item10NativeMediaTransport(
        activity = activity,
        width = activity.resources.displayMetrics.widthPixels,
        height = activity.resources.displayMetrics.heightPixels,
        densityDpi = activity.resources.displayMetrics.densityDpi,
        onFrame = { frame ->
            onEvidence(Evidence(Channel.SCREEN, frame.timestampMs, frame.pngBytes.size, sha256(frame.pngBytes), frame.width, frame.height))
        }
    )

    fun startVoice(): Boolean = voice.start()

    fun stopVoice(): Evidence? = voice.stop()?.let {
        Evidence(Channel.VOICE, it.startedAtMs, it.file.length().toInt(), sha256(it.file.readBytes()), uriOrPath = it.file.absolutePath)
    }

    fun cancelVoice() = voice.cancel()

    fun startCamera(): Boolean = camera.start()
    fun stopCamera() = camera.stop()
    fun isCameraActive(): Boolean = camera.isActive()

    fun screenPermissionIntent(): Intent? = screen.permissionIntent()
    fun startScreen(resultCode: Int, data: Intent?): Boolean = screen.start(resultCode, data)
    fun stopScreen() = screen.stop()
    fun isScreenActive(): Boolean = screen.isActive()

    fun stopAll() {
        voice.cancel()
        camera.stop()
        screen.stop()
    }

    companion object {
        private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
