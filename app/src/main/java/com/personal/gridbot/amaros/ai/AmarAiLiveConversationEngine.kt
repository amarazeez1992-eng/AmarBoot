package com.personal.gridbot.amaros.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Voice-first conversation loop: speech -> AI -> speech, with no text input required. */
class AmarAiLiveConversationEngine(
    private val context: Context,
    private val scope: CoroutineScope,
    private val onState: (State) -> Unit,
    private val onTranscript: (String) -> Unit,
    private val onAnswer: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    enum class State { IDLE, LISTENING, THINKING, SPEAKING }

    private val voice = AmarAiVoiceInput(context)
    private var tts: TextToSpeech? = null
    private var active = false
    private var requestJob: Job? = null
    private var apiKey = ""
    private var model = "gemini-2.5-flash"

    fun configure(apiKey: String, model: String) {
        this.apiKey = apiKey.trim()
        this.model = model.trim()
    }

    fun start() {
        if (active) return
        if (apiKey.isBlank()) {
            onError("أدخل مفتاح Gemini أولاً لتشغيل المحادثة المباشرة.")
            return
        }
        active = true
        ensureTts()
        listen()
    }

    fun stop() {
        active = false
        requestJob?.cancel()
        voice.stop()
        tts?.stop()
        onState(State.IDLE)
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        ensureTts()
        onState(State.SPEAKING)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "amar-ai-answer")
    }

    private fun listen() {
        if (!active) return
        onState(State.LISTENING)
        voice.start(object : AmarAiVoiceInput.Listener {
            override fun onPartial(text: String) { if (text.isNotBlank()) onTranscript(text) }
            override fun onFinal(text: String) {
                if (text.isBlank() || !active) { if (active) listen(); return }
                onTranscript(text)
                requestJob?.cancel()
                requestJob = scope.launch(Dispatchers.Main.immediate) {
                    onState(State.THINKING)
                    val local = AmarAiActionEngine.route(text)
                    val answer = if (local.handled) local.response else {
                        runCatching { AmarAiAgentEngine(context).ask(apiKey, model, text).answer }
                            .getOrElse { "تعذر الرد الآن: ${it.message ?: "خطأ غير معروف"}" }
                    }
                    onAnswer(answer)
                    if (active) {
                        speak(answer)
                        // TTS is intentionally followed by a fresh listening turn.
                        listen()
                    }
                }
            }
            override fun onError(code: Int) {
                if (!active) return
                onError("تعذر التقاط الصوت (رمز $code). سأحاول الاستماع مرة أخرى.")
                listen()
            }
        }, Locale("ar"))
    }

    private fun ensureTts() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("ar")) ?: TextToSpeech.ERROR
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.getDefault())
                }
            }
        }
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
