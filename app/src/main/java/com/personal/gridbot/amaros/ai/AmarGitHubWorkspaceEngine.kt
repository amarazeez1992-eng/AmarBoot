package com.personal.gridbot.amaros.ai

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Owner-authorized GitHub workspace boundary for AMAR AI. */
class AmarGitHubWorkspaceEngine(context: Context? = null) {
    data class GitHubResult(
        val ok: Boolean,
        val operation: String,
        val status: Int,
        val message: String,
        val data: String = ""
    )

    private val prefs: SharedPreferences? = context?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder().build()

    suspend fun searchRepositories(query: String, limit: Int = 10): GitHubResult =
        request("GET", "/search/repositories?q=${enc(query)}&per_page=${limit.coerceIn(1, 50)}", operation = "repo_search")

    suspend fun inspectRepository(owner: String, repo: String): GitHubResult =
        request("GET", "/repos/${enc(owner)}/${enc(repo)}", operation = "repo_inspect")

    suspend fun searchCode(owner: String?, repo: String?, query: String, limit: Int = 20): GitHubResult {
        val scopedQuery = buildString {
            append(query)
            if (!owner.isNullOrBlank() && !repo.isNullOrBlank()) append(" repo:$owner/$repo")
        }
        return request("GET", "/search/code?q=${enc(scopedQuery)}&per_page=${limit.coerceIn(1, 50)}", operation = "code_search")
    }

    suspend fun readFile(owner: String, repo: String, path: String, ref: String? = null): GitHubResult {
        val suffix = ref?.let { "?ref=${enc(it)}" } ?: ""
        return request("GET", "/repos/${enc(owner)}/${enc(repo)}/contents/${path.trimStart('/')}$suffix", operation = "file_read")
    }

    suspend fun inspectLicense(owner: String, repo: String): GitHubResult =
        request("GET", "/repos/${enc(owner)}/${enc(repo)}/license", operation = "license_inspect")

    suspend fun createBranch(owner: String, repo: String, branch: String, fromRef: String): GitHubResult {
        val base = request("GET", "/repos/${enc(owner)}/${enc(repo)}/git/ref/heads/${enc(fromRef)}", operation = "branch_base")
        if (!base.ok) return base
        val sha = runCatching { JSONObject(base.data).getJSONObject("object").getString("sha") }.getOrNull()
            ?: return GitHubResult(false, "branch_create", base.status, "تعذر استخراج SHA للفرع الأساسي")
        val body = JSONObject().put("ref", "refs/heads/$branch").put("sha", sha)
        return request("POST", "/repos/${enc(owner)}/${enc(repo)}/git/refs", body.toString(), "branch_create")
    }

    suspend fun writeFile(owner: String, repo: String, path: String, content: String, message: String, branch: String? = null): GitHubResult {
        val existing = readFile(owner, repo, path, branch)
        val encoded = Base64.encodeToString(content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val body = JSONObject().put("message", message).put("content", encoded)
        branch?.let { body.put("branch", it) }
        if (existing.ok) {
            val sha = runCatching { JSONObject(existing.data).getString("sha") }.getOrNull()
                ?: return GitHubResult(false, "file_update", existing.status, "تعذر استخراج SHA للملف الحالي")
            body.put("sha", sha)
            return request("PUT", "/repos/${enc(owner)}/${enc(repo)}/contents/${path.trimStart('/')}", body.toString(), "file_update")
        }
        if (existing.status != 404) return existing
        return request("PUT", "/repos/${enc(owner)}/${enc(repo)}/contents/${path.trimStart('/')}", body.toString(), "file_create")
    }

    suspend fun deleteFile(owner: String, repo: String, path: String, message: String, branch: String? = null): GitHubResult {
        val existing = readFile(owner, repo, path, branch)
        if (!existing.ok) return existing
        val body = JSONObject().put("message", message).put("sha", JSONObject(existing.data).getString("sha"))
        branch?.let { body.put("branch", it) }
        return request("DELETE", "/repos/${enc(owner)}/${enc(repo)}/contents/${path.trimStart('/')}", body.toString(), "file_delete")
    }

    suspend fun createPullRequest(owner: String, repo: String, title: String, head: String, base: String, bodyText: String = ""): GitHubResult {
        val body = JSONObject().put("title", title).put("head", head).put("base", base).put("body", bodyText)
        return request("POST", "/repos/${enc(owner)}/${enc(repo)}/pulls", body.toString(), "pull_request_create")
    }

    /** Must only be called after an explicit user approval for the merge. */
    suspend fun mergePullRequest(owner: String, repo: String, number: Int, method: String = "squash"): GitHubResult {
        val body = JSONObject().put("merge_method", method)
        return request("PUT", "/repos/${enc(owner)}/${enc(repo)}/pulls/$number/merge", body.toString(), "pull_request_merge")
    }

    suspend fun setAccessToken(token: String): Boolean = withContext(Dispatchers.IO) {
        if (token.isBlank()) return@withContext false
        runCatching { prefs?.edit()?.putString(TOKEN_KEY, encryptForKeystore(token.trim()))?.apply(); true }.getOrDefault(false)
    }

    fun clearAccessToken(): Boolean = prefs?.edit()?.remove(TOKEN_KEY)?.commit() ?: false
    fun hasAccessToken(): Boolean = !prefs?.getString(TOKEN_KEY, null).isNullOrBlank()

    private suspend fun request(method: String, path: String, body: String? = null, operation: String): GitHubResult = withContext(Dispatchers.IO) {
        val builder = Request.Builder().url("https://api.github.com$path")
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .header("User-Agent", "AMAR-AI")
        readToken()?.let { builder.header("Authorization", "Bearer $it") }
        if (body != null) builder.method(method, body.toRequestBody("application/json; charset=utf-8".toMediaType())) else builder.method(method, null)
        try {
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                GitHubResult(response.isSuccessful, operation, response.code, if (response.isSuccessful) "تمت العملية" else "GitHub رفض العملية: HTTP ${response.code}", text)
            }
        } catch (e: IOException) {
            GitHubResult(false, operation, 0, "تعذر الاتصال بـ GitHub: ${e.message ?: "network error"}")
        }
    }

    private fun readToken(): String? = prefs?.getString(TOKEN_KEY, null)?.let { runCatching { decryptForKeystore(it) }.getOrNull() }
    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

    companion object {
        private const val PREFS = "amar_github"
        private const val TOKEN_KEY = "access_token_encrypted"
        private const val KEY_ALIAS = "amar_github_token_key"

        fun keystoreKey(): SecretKey {
            val ks = java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build())
            return generator.generateKey()
        }

        private fun encryptForKeystore(value: String): String {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
            val payload = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(payload, Base64.NO_WRAP)
        }

        private fun decryptForKeystore(value: String): String {
            val raw = Base64.decode(value, Base64.NO_WRAP)
            require(raw.size > 12) { "invalid encrypted token" }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, raw.copyOfRange(0, 12)))
            return cipher.doFinal(raw.copyOfRange(12, raw.size)).toString(Charsets.UTF_8)
        }
    }
}
