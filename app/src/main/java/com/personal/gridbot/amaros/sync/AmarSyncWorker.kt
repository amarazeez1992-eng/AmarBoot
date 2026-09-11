package com.personal.gridbot.amaros.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.personal.gridbot.amaros.bots.AmarBotVaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Reliable, retryable vault synchronization. Trading credentials/secrets are never included. */
class AmarSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val config = AmarSyncConfig.load(applicationContext) ?: return@withContext Result.success()
        val repo = AmarBotVaultRepository(applicationContext)
        val snapshot = repo.exportSnapshotJson()
        val body = JsonObject().apply {
            addProperty("schemaVersion", 1)
            addProperty("deviceId", config.deviceId)
            addProperty("snapshotHash", sha256(snapshot))
            addProperty("snapshot", JsonParser.parseString(snapshot))
        }
        val request = Request.Builder()
            .url(config.endpoint.trimEnd('/') + "/sync")
            .header("Authorization", "Bearer ${config.token}")
            .header("X-AMAR-DEVICE", config.deviceId)
            .post(Gson().toJson(body).toRequestBody("application/json".toMediaType()))
            .build()
        return@withContext try {
            client(config).newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext if (response.code in 408..599) Result.retry() else Result.failure()
                val raw = response.body?.string().orEmpty()
                val json = JsonParser.parseString(raw).asJsonObject
                if (!json.optBoolean("ok", false)) return@withContext Result.retry()
                val remote = json.get("snapshot")
                if (remote != null && remote.isJsonArray) {
                    val remoteSnapshot = remote.toString()
                    val remoteHash = json.optString("snapshotHash", sha256(remoteSnapshot))
                    if (remoteHash == sha256(remoteSnapshot)) repo.replaceSnapshotJson(remoteSnapshot)
                }
                Result.success()
            }
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun client(config: AmarSyncConfig): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(config.timeoutMs, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

private fun JsonObject.optBoolean(name: String, fallback: Boolean): Boolean =
    if (has(name) && !get(name).isJsonNull) get(name).asBoolean else fallback

private fun JsonObject.optString(name: String, fallback: String): String =
    if (has(name) && !get(name).isJsonNull) get(name).asString else fallback

data class AmarSyncConfig(
    val endpoint: String,
    val token: String,
    val deviceId: String,
    val timeoutMs: Long = 10_000L,
) {
    companion object {
        private const val PREFS = "amar_sync_config_v1"
        private const val ENDPOINT = "endpoint"
        private const val TOKEN = "token"
        private const val DEVICE = "device"

        fun configure(context: Context, endpoint: String, token: String) {
            require(endpoint.startsWith("https://") || endpoint.startsWith("http://10.") || endpoint.startsWith("http://192.168."))
            require(token.isNotBlank())
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val device = prefs.getString(DEVICE, null) ?: UUID.randomUUID().toString()
            prefs.edit().putString(ENDPOINT, endpoint.trim()).putString(TOKEN, token).putString(DEVICE, device).commit()
            AmarSyncManager.schedule(context)
            AmarSyncManager.requestNow(context)
        }

        fun load(context: Context): AmarSyncConfig? {
            val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val endpoint = p.getString(ENDPOINT, null)?.trim().orEmpty()
            val token = p.getString(TOKEN, null).orEmpty()
            val device = p.getString(DEVICE, null).orEmpty()
            if (endpoint.isBlank() || token.isBlank() || device.isBlank()) return null
            return AmarSyncConfig(endpoint, token, device)
        }
    }
}
