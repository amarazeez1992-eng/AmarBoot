package com.personal.gridbot.amaros.ai

import android.webkit.JavascriptInterface
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Trusted local WebView -> AMAR AI boundary.
 * The web UI can request analysis/research, but it never receives execution authority.
 */
class AmarAiWebEngineBridge(
    private val webView: WebView,
    private val engine: AmarAiAgentEngine = AmarAiAgentEngine()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @JavascriptInterface
    fun request(payload: String) {
        val parsed = runCatching { JSONObject(payload) }.getOrElse {
            reply(payload, false, error = "INVALID_REQUEST")
            return
        }
        val requestId = parsed.optString("requestId").ifBlank { return }
        val action = parsed.optString("action")
        when (action) {
            "agent.request" -> {
                val request = parsed.optJSONObject("request")
                val prompt = request?.optString("prompt").orEmpty()
                if (prompt.isBlank()) {
                    reply(requestId, false, error = "EMPTY_PROMPT")
                    return
                }
                scope.launch {
                    runCatching { engine.ask("", "", prompt) }
                        .onSuccess { result ->
                            val response = JSONObject()
                                .put("id", request?.optString("id").orEmpty().ifBlank { requestId })
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
                if (query.isBlank()) {
                    reply(requestId, false, error = "EMPTY_QUERY")
                    return
                }
                scope.launch {
                    runCatching { AmarAiExternalResearch().search(query, 8) }
                        .onSuccess { results ->
                            val sources = JSONArray()
                            results.forEachIndexed { index, source ->
                                sources.put(
                                    JSONObject()
                                        .put("id", "research-$index")
                                        .put("title", source.title)
                                        .put("url", source.url)
                                        .put("status", "checking")
                                        .put("excerpt", source.excerpt)
                                )
                            }
                            reply(requestId, true, sources = sources)
                        }
                        .onFailure { error -> reply(requestId, false, error = error.message ?: "RESEARCH_FAILURE") }
                }
            }
            else -> reply(requestId, false, error = "UNSUPPORTED_ACTION:$action")
        }
    }

    fun destroy() {
        scope.coroutineContext.cancel()
    }

    private fun reply(requestId: String, ok: Boolean, response: JSONObject? = null, sources: JSONArray? = null, error: String? = null) {
        val message = JSONObject().put("requestId", requestId).put("ok", ok)
        response?.let { message.put("response", it) }
        sources?.let { message.put("sources", it) }
        error?.let { message.put("error", it) }
        val script = "window.dispatchEvent(new MessageEvent('message',{data:${message}}));"
        webView.post { webView.evaluateJavascript(script, null) }
    }
}
