package com.personal.gridbot.amaros.ai

import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

/** Trusted local WebView -> AMAR AI boundary. UI receives evidence, never execution authority. */
class AmarAiWebEngineBridge(
    private val webView: WebView,
    private val engine: AmarAiAgentEngine = AmarAiAgentEngine()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @JavascriptInterface
    fun request(payload: String) {
        val parsed = runCatching { JSONObject(payload) }.getOrElse {
            return
        }
        val requestId = parsed.optString("requestId").ifBlank { return }
        when (parsed.optString("action")) {
            "agent.request" -> {
                val request = parsed.optJSONObject("request")
                val prompt = request?.optString("prompt").orEmpty()
                if (prompt.isBlank()) return reply(requestId, false, error = "EMPTY_PROMPT")
                scope.launch {
                    runCatching { engine.ask("", "", prompt) }
                        .onSuccess { result ->
                            val response = JSONObject()
                                .put("id", request.optString("id").ifBlank { requestId })
                                .put("text", result.answer)
                                .put("status", "complete")
                                .put("verification", if (result.toolEvidence.isEmpty()) "uncertain" else "confirmed")
                                .put("confidence", if (result.toolEvidence.isEmpty()) 0.5 else 0.9)
                            val evidence = JSONArray()
                            result.toolEvidence.forEachIndexed { index, item ->
                                evidence.put(JSONObject().put("id", "engine-$index").put("title", item).put("status", "verified"))
                            }
                            response.put("sources", evidence)
                            reply(requestId, true, response = response)
                        }
                        .onFailure { error -> reply(requestId, false, error = error.message ?: "ENGINE_FAILURE") }
                }
            }
            "research.search" -> {
                val query = parsed.optString("query")
                if (query.isBlank()) return reply(requestId, false, error = "EMPTY_QUERY")
                scope.launch {
                    runCatching { AmarAiExternalResearch().search(query, 8) }
                        .onSuccess { results ->
                            val sources = JSONArray()
                            results.forEachIndexed { index, source ->
                                sources.put(JSONObject().put("id", "research-$index").put("title", source.title).put("url", source.url).put("status", "checking").put("excerpt", source.excerpt))
                            }
                            reply(requestId, true, sources = sources)
                        }
                        .onFailure { error -> reply(requestId, false, error = error.message ?: "RESEARCH_FAILURE") }
                }
            }
            "multimodal.analyze" -> reply(requestId, false, error = "MULTIMODAL_NATIVE_TRANSFER_REQUIRES_SECURE_MEDIA_CHANNEL")
            "screen.share.start", "screen.share.stop" -> reply(requestId, false, error = "SCREEN_SHARE_REQUIRES_ANDROID_MEDIA_PROJECTION")
            else -> reply(requestId, false, error = "UNSUPPORTED_ACTION")
        }
    }

    fun destroy() = scope.cancel()

    private fun reply(requestId: String, ok: Boolean, response: JSONObject? = null, sources: JSONArray? = null, error: String? = null) {
        val message = JSONObject().put("requestId", requestId).put("ok", ok)
        response?.let { message.put("response", it) }
        sources?.let { message.put("sources", it) }
        error?.let { message.put("error", it) }
        val script = "window.dispatchEvent(new MessageEvent('message',{data:${message}}));"
        webView.post { webView.evaluateJavascript(script, null) }
    }
}
