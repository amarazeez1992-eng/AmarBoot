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

/** Reliable, retryable, offline-first Bot Vault synchronization. Trading secrets are never included. */
class AmarSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val config = AmarSyncConfig.load(applicationContext) ?: return@withContext Result.success()
        val repo = AmarBotVaultRepository(applicationContext)
        val local = repo.exportSnapshotJson()
        var base = config.baseSnapshot
        var revision = config.revision
        var candidate = local

        repeat(2) { attempt ->
            val requestBase = base
            val requestRevision = revision
            val body = JsonObject().apply {
                addProperty("schemaVersion", 1)
                addProperty("deviceId", config.deviceId)
                addProperty("revision", requestRevision)
                addProperty("baseHash", sha256(requestBase))
                addProperty("snapshotHash", sha256(candidate))
                add("snapshot", JsonParser.parseString(candidate))
            }
            val request = Request.Builder()
                .url(config.endpoint.trimEnd('/') + "/sync")
                .header("Authorization", "Bearer ${config.token}")
                .header("X-AMAR-DEVICE", config.deviceId)
                .post(Gson().toJson(body).toRequestBody("application/json".toMediaType()))
                .build()
            try {
                client(config).newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (response.code == 409 && attempt == 0) {
                        val conflict = JsonParser.parseString(raw).asJsonObject
                        val remote = conflict.get("snapshot")?.takeIf { it.isJsonArray }?.toString()
                            ?: return@use
                        candidate = if (requestRevision == 0L) remote else AmarSyncMerge.merge(requestBase, candidate, remote)
                        base = remote
                        revision = conflict.get("revision")?.asLong ?: requestRevision
                        return@use
                    }
                    if (!response.isSuccessful) return@withContext if (response.code in 408..599) Result.retry() else Result.failure()
                    val json = JsonParser.parseString(raw).asJsonObject
                    if (!json.optBoolean("ok", false)) return@withContext Result.retry()
                    val remote = json.get("snapshot")?.takeIf { it.isJsonArray }?.toString() ?: candidate
                    val remoteHash = json.optString("snapshotHash", sha256(remote))
                    if (remoteHash != sha256(remote)) return@withContext Result.retry()
                    val newRevision = json.optLong("revision", requestRevision + 1L)
                    AmarSyncConfig.saveState(applicationContext, newRevision, remote)
                    repo.replaceSnapshotJson(remote)
                    return@withContext Result.success()
                }
            } catch (_: Exception) {
                return@withContext Result.retry()
            }
        }
        Result.retry()
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

private fun JsonObject.optBoolean(name: String, fallback: Boolean): Boolean = if (has(name) && !get(name).isJsonNull) get(name).asBoolean else fallback
private fun JsonObject.optString(name: String, fallback: String): String = if (has(name) && !get(name).isJsonNull) get(name).asString else fallback
private fun JsonObject.optLong(name: String, fallback: Long): Long = if (has(name) && !get(name).isJsonNull) get(name).asLong else fallback

data class AmarSyncConfig(
    val endpoint: String,
    val token: String,
    val deviceId: String,
    val revision: Long,
    val baseSnapshot: String,
    val timeoutMs: Long = 10_000L,
) {
    companion object {
        private const val PREFS = "amar_sync_config_v1"
        private const val ENDPOINT = "endpoint"
        private const val TOKEN = "token"
        private const val DEVICE = "device"
        private const val REVISION = "revision"
        private const val BASE = "baseSnapshot"

        fun configure(context: Context, endpoint: String, token: String) {
            require(endpoint.startsWith("https://") || endpoint.startsWith("http://10.") || endpoint.startsWith("http://192.168."))
            require(token.isNotBlank())
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val device = prefs.getString(DEVICE, null) ?: UUID.randomUUID().toString()
            prefs.edit().putString(ENDPOINT, endpoint.trim()).putString(TOKEN, token).putString(DEVICE, device).apply()
            AmarSyncManager.schedule(context)
            AmarSyncManager.requestNow(context)
        }

        fun load(context: Context): AmarSyncConfig? {
            val p = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val endpoint = p.getString(ENDPOINT, null)?.trim().orEmpty()
            val token = p.getString(TOKEN, null).orEmpty()
            val device = p.getString(DEVICE, null).orEmpty()
            if (endpoint.isBlank() || token.isBlank() || device.isBlank()) return null
            return AmarSyncConfig(endpoint, token, device, p.getLong(REVISION, 0L), p.getString(BASE, "[]") ?: "[]")
        }

        fun saveState(context: Context, revision: Long, snapshot: String) {
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(REVISION, revision)
                .putString(BASE, snapshot)
                .apply()
        }
    }
}
