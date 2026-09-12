package com.personal.gridbot.amaros.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/** Native Android speech-to-text adapter. It only converts speech to text; AI decides nothing here. */
class AmarAiVoiceInput(private val context: Context) {
    interface Listener {
        fun onPartial(text: String)
        fun onFinal(text: String)
        fun onError(code: Int)
    }

    private var recognizer: SpeechRecognizer? = null

    fun start(listener: Listener, locale: Locale = Locale.getDefault()) {
        stop()
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            listener.onError(-1)
            return
        }
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { sr ->
            sr.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
                override fun onPartialResults(results: Bundle?) = listener.onPartial(best(results))
                override fun onResults(results: Bundle?) = listener.onFinal(best(results))
                override fun onError(error: Int) = listener.onError(error)
            })
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            sr.startListening(intent)
        }
    }

    fun stop() {
        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null
    }

    private fun best(bundle: Bundle?): String = bundle
        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        ?.firstOrNull()
        .orEmpty()
}
