package com.personal.gridbot.amaros.workspace

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/** Explicit, user-started voice-message recorder. No background capture. */
class Item10VoiceRecorder(private val context: Context) {
    data class VoiceClip(val file: File, val startedAtMs: Long, val durationMs: Long)

    private var recorder: MediaRecorder? = null
    private var startedAt = 0L
    private val active = AtomicBoolean(false)
    private var output: File? = null

    fun start(): Boolean {
        if (active.get()) return true
        val file = File(context.cacheDir, "amar_voice_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.m4a")
        return runCatching {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(128000)
            r.setAudioSamplingRate(44100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            output = file
            startedAt = System.currentTimeMillis()
            active.set(true)
            true
        }.getOrElse {
            recorder?.release()
            recorder = null
            output = null
            active.set(false)
            false
        }
    }

    fun stop(): VoiceClip? {
        if (!active.get()) return null
        val started = startedAt
        val file = output
        val duration = (System.currentTimeMillis() - started).coerceAtLeast(0L)
        runCatching { recorder?.stop() }
        runCatching { recorder?.reset() }
        runCatching { recorder?.release() }
        recorder = null
        output = null
        active.set(false)
        return file?.takeIf { it.exists() && it.length() > 0L }?.let { VoiceClip(it, started, duration) }
    }

    fun cancel() {
        if (!active.get()) return
        runCatching { recorder?.stop() }
        runCatching { recorder?.reset() }
        runCatching { recorder?.release() }
        recorder = null
        output?.delete()
        output = null
        active.set(false)
    }

    fun isActive(): Boolean = active.get()
}
