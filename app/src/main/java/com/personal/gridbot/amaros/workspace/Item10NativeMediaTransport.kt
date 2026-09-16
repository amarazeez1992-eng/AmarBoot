package com.personal.gridbot.amaros.workspace

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Provider-neutral Android screen capture transport.
 * Permission -> VirtualDisplay -> ImageReader -> immutable PNG evidence.
 * It never grants execution authority and never persists frames by itself.
 */
class Item10NativeMediaTransport(
    private val activity: Activity,
    private val width: Int,
    private val height: Int,
    private val densityDpi: Int,
    private val onFrame: (CapturedScreenFrame) -> Unit
) {
    companion object { const val REQUEST_CODE = 4107 }

    data class CapturedScreenFrame(
        val timestampMs: Long,
        val width: Int,
        val height: Int,
        val pngBytes: ByteArray
    )

    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private val stopped = AtomicBoolean(true)

    fun permissionIntent(): Intent? = runCatching {
        activity.getSystemService(MediaProjectionManager::class.java)?.createScreenCaptureIntent()
    }.getOrNull()

    fun start(resultCode: Int, data: Intent?): Boolean {
        if (resultCode != Activity.RESULT_OK || data == null) return false
        stop()
        if (width <= 0 || height <= 0 || densityDpi <= 0) return false
        val manager = activity.getSystemService(MediaProjectionManager::class.java) ?: return false
        val mediaProjection = runCatching { manager.getMediaProjection(resultCode, data) }.getOrNull() ?: return false
        projection = mediaProjection
        stopped.set(false)
        reader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2).also {
            it.setOnImageAvailableListener({ source -> captureLatest(source) }, null)
        }
        display = runCatching {
            mediaProjection.createVirtualDisplay(
                "AMAR-Screen-Evidence",
                width,
                height,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader?.surface,
                null,
                null
            )
        }.getOrNull()
        if (display == null) stop()
        return display != null
    }

    private fun captureLatest(source: ImageReader) {
        if (stopped.get()) return
        val image = runCatching { source.acquireLatestImage() }.getOrNull() ?: return
        image.use {
            val plane = it.planes.firstOrNull() ?: return
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            if (pixelStride <= 0 || rowStride <= 0) return
            val paddedWidth = width + (rowStride - pixelStride * width).coerceAtLeast(0) / pixelStride
            val bitmap = runCatching {
                Bitmap.createBitmap(paddedWidth, height, Bitmap.Config.ARGB_8888).also { target ->
                    target.copyPixelsFromBuffer(plane.buffer)
                }
            }.getOrNull() ?: return
            val cropped = runCatching {
                if (paddedWidth == width) bitmap else Bitmap.createBitmap(bitmap, 0, 0, width, height)
            }.getOrNull()
            if (cropped == null) { bitmap.recycle(); return }
            val output = ByteArrayOutputStream()
            if (cropped.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                onFrame(CapturedScreenFrame(System.currentTimeMillis(), width, height, output.toByteArray()))
            }
            if (cropped !== bitmap) cropped.recycle()
            bitmap.recycle()
        }
    }

    fun stop() {
        stopped.set(true)
        display?.release(); display = null
        reader?.close(); reader = null
        projection?.stop(); projection = null
    }

    fun isActive(): Boolean = !stopped.get() && display != null && projection != null
}
