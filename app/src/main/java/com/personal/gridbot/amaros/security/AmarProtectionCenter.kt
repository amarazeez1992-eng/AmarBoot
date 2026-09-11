package com.personal.gridbot.amaros.security

import android.content.Context
import java.io.File
import java.security.MessageDigest
import java.time.Instant

object AmarProtectionCenter {
    private const val PREFS = "amar_protection"
    private const val LAST_ERROR = "last_error"
    private const val LAST_TIME = "last_error_time"
    private const val VAULT_HASH = "vault_hash"

    fun recordFailure(context: Context, module: String, error: Throwable) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(LAST_ERROR, "الوحدة: $module\n${error.javaClass.simpleName}: ${error.message ?: "خطأ غير معروف"}")
            .putString(LAST_TIME, Instant.now().toString()).apply()
    }

    fun clearLastFailure(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(LAST_ERROR).remove(LAST_TIME).apply() }
    fun lastFailure(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(LAST_ERROR, null)
    fun lastFailureTime(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(LAST_TIME, null)

    fun verifyFile(file: File, expectedSha256: String): Boolean = file.exists() && sha256(file) == expectedSha256
    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) { val n = input.read(buffer); if (n <= 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun saveVaultHash(context: Context, hash: String) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(VAULT_HASH, hash).apply()
    fun savedVaultHash(context: Context): String? = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(VAULT_HASH, null)
}
