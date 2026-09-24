package com.personal.gridbot.amaros.logging

import android.util.Log

interface AmarLogSink {
    fun write(event: AmarLogEvent)
}

class AndroidAmarLogSink : AmarLogSink {
    override fun write(event: AmarLogEvent) {
        when (event.level) {
            AmarLogLevel.DEBUG -> Log.d(event.tag, event.message)
            AmarLogLevel.INFO -> Log.i(event.tag, event.message)
            AmarLogLevel.WARN -> Log.w(event.tag, event.message)
            AmarLogLevel.ERROR -> Log.e(event.tag, event.message)
        }
    }
}

class AmarLogger(
    private val sink: AmarLogSink = AndroidAmarLogSink(),
    private val clockMs: () -> Long = System::currentTimeMillis
) {
    fun debug(tag: String, message: String) = emit(AmarLogLevel.DEBUG, tag, message, null)
    fun info(tag: String, message: String) = emit(AmarLogLevel.INFO, tag, message, null)
    fun warn(tag: String, message: String) = emit(AmarLogLevel.WARN, tag, message, null)
    fun error(tag: String, message: String, throwable: Throwable? = null) =
        emit(AmarLogLevel.ERROR, tag, message, throwable)

    private fun emit(level: AmarLogLevel, tag: String, message: String, throwable: Throwable?) {
        val safeTag = tag.take(23)
        val safeMessage = sanitize(message)
        sink.write(
            AmarLogEvent(
                level = level,
                tag = safeTag,
                message = safeMessage,
                epochMs = clockMs(),
                throwableType = throwable?.javaClass?.name
            )
        )
    }

    private fun sanitize(message: String): String {
        var safe = message
        val patterns = listOf(
            Regex("(?i)(password|token|secret|credential)\\s*[:=]\\s*[^\\s,;]+"),
            Regex("(?i)(authorization)\\s*[:=]\\s*[^\\s,;]+")
        )
        patterns.forEach { safe = it.replace(safe) { match -> match.value.substringBefore(':').substringBefore('=') + ": [REDACTED]" } }
        return safe
    }
}
