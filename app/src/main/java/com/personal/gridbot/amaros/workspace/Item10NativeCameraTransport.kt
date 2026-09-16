package com.personal.gridbot.amaros.workspace

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Provider-neutral Camera2 evidence transport.
 * It captures frames only after CAMERA permission and exposes them as JPEG evidence.
 * No hidden capture, persistence, or execution authority is included.
 */
class Item10NativeCameraTransport(
    private val context: Context,
    private val width: Int = 1280,
    private val height: Int = 720,
    private val onFrame: (CapturedCameraFrame) -> Unit
) {
    data class CapturedCameraFrame(
        val timestampMs: Long,
        val width: Int,
        val height: Int,
        val jpegBytes: ByteArray
    )

    private val active = AtomicBoolean(false)
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private var camera: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var reader: ImageReader? = null
    private var thread: HandlerThread? = null
    private var handler: Handler? = null

    fun start(): Boolean {
        if (active.get()) return true
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return false
        val manager = cameraManager ?: return false
        val id = chooseCamera(manager) ?: return false
        stop()
        val worker = HandlerThread("AMAR-Camera-Evidence").also { it.start() }
        thread = worker
        handler = Handler(worker.looper)
        reader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2).also {
            it.setOnImageAvailableListener({ source -> capture(source) }, handler)
        }
        return try {
            manager.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) {
                    if (!active.get()) { device.close(); return }
                    camera = device
                    val output = reader?.surface ?: run { stop(); return }
                    device.createCaptureSession(listOf(output), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(value: CameraCaptureSession) {
                            if (!active.get()) { value.close(); return }
                            session = value
                            val request = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                                addTarget(output)
                                set(android.hardware.camera2.CaptureRequest.CONTROL_MODE, android.hardware.camera2.CameraMetadata.CONTROL_MODE_AUTO)
                            }.build()
                            runCatching { value.setRepeatingRequest(request, null, handler) }.onFailure { stop() }
                        }
                        override fun onConfigureFailed(value: CameraCaptureSession) { stop() }
                    }, handler)
                }
                override fun onDisconnected(device: CameraDevice) { device.close(); stop() }
                override fun onError(device: CameraDevice, error: Int) { device.close(); stop() }
            }, handler)
            active.set(true)
            true
        } catch (_: SecurityException) {
            stop(); false
        } catch (_: Exception) {
            stop(); false
        }
    }

    private fun chooseCamera(manager: CameraManager): String? = runCatching {
        manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: manager.cameraIdList.firstOrNull()
    }.getOrNull()

    private fun capture(source: ImageReader) {
        if (!active.get()) return
        val image = runCatching { source.acquireLatestImage() }.getOrNull() ?: return
        image.use {
            val jpeg = yuvToJpeg(it) ?: return
            onFrame(CapturedCameraFrame(System.currentTimeMillis(), it.width, it.height, jpeg))
        }
    }

    private fun yuvToJpeg(image: Image): ByteArray? = runCatching {
        val y = image.planes[0].buffer
        val u = image.planes[1].buffer
        val v = image.planes[2].buffer
        val yBytes = ByteArray(y.remaining()).also { y.get(it) }
        val uBytes = ByteArray(u.remaining()).also { u.get(it) }
        val vBytes = ByteArray(v.remaining()).also { v.get(it) }
        val nv21 = ByteArray(yBytes.size + uBytes.size + vBytes.size)
        yBytes.copyInto(nv21, 0)
        var offset = yBytes.size
        val chroma = minOf(uBytes.size, vBytes.size)
        for (i in 0 until chroma) {
            nv21[offset++] = vBytes[i]
            if (offset < nv21.size) nv21[offset++] = uBytes[i]
        }
        val output = ByteArrayOutputStream()
        YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null).compressToJpeg(Rect(0, 0, image.width, image.height), 85, output)
        output.toByteArray()
    }.getOrNull()

    fun stop() {
        active.set(false)
        runCatching { session?.stopRepeating() }
        session?.close(); session = null
        camera?.close(); camera = null
        reader?.close(); reader = null
        thread?.quitSafely(); thread = null; handler = null
    }

    fun isActive(): Boolean = active.get() && camera != null && session != null
}
