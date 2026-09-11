package com.personal.gridbot.amaros.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/** Free-tier Gemini REST adapter. The user supplies their own API key. */
class AmarGeminiClient(private val http: OkHttpClient = OkHttpClient()) {
    data class Result(val text: String, val raw: String)

    suspend fun generate(apiKey: String, model: String, system: String, prompt: String): Result = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) { "Gemini API key is required" }
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray().put(JSONObject().put("text", prompt)))))
            .put("generationConfig", JSONObject().put("temperature", 0.2).put("responseMimeType", "application/json"))
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            .addHeader("x-goog-api-key", apiKey.trim())
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Gemini HTTP ${response.code}: ${extractError(raw)}")
            val text = runCatching {
                JSONObject(raw).getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            }.getOrElse { error("Gemini response could not be parsed") }
            Result(text, raw)
        }
    }

    private fun extractError(raw: String): String = runCatching {
        JSONObject(raw).getJSONObject("error").optString("message", raw)
    }.getOrDefault(raw.take(300))
}
