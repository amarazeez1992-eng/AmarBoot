package com.personal.gridbot.amaros.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/** Reads user-selected visual evidence without granting it execution authority. */
class AmarAiImageInput(private val context: Context) {
    data class ImagePayload(val mimeType: String, val base64: String)

    fun read(uri: Uri): ImagePayload {
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = ByteArrayOutputStream()
            input.copyTo(out)
            out.toByteArray()
        } ?: error("Unable to read image")
        require(bytes.isNotEmpty()) { "Image is empty" }
        require(bytes.size <= 8 * 1024 * 1024) { "Image exceeds 8 MB AI input limit" }
        return ImagePayload(mime, Base64.encodeToString(bytes, Base64.NO_WRAP))
    }
}

/** Multimodal Gemini adapter kept separate from the text client for additive architecture. */
class AmarGeminiVisionClient(
    private val http: OkHttpClient = OkHttpClient()
) {
    suspend fun analyze(
        apiKey: String,
        model: String,
        system: String,
        prompt: String,
        image: AmarAiImageInput.ImagePayload
    ): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", JSONArray()
                .put(JSONObject().put("text", prompt))
                .put(JSONObject().put("inlineData", JSONObject().put("mimeType", image.mimeType).put("data", image.base64))))))
            .put("generationConfig", JSONObject().put("temperature", 0.15).put("responseMimeType", "application/json"))
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/${model.trim()}:generateContent")
            .addHeader("x-goog-api-key", apiKey.trim())
            .addHeader("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()
        http.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Gemini Vision HTTP ${response.code}")
            JSONObject(raw).getJSONArray("candidates")
                .getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text")
        }
    }
}
