package com.personal.gridbot.amaros.ai

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Local-only Gemini key storage using Android Keystore. No key is hard-coded in the APK. */
object AmarAiKeyStore {
    private const val STORE = "AndroidKeyStore"
    private const val ALIAS = "amar_gemini_api_key"
    private const val PREFS = "amar_ai_secure"
    private const val KEY = "gemini_key"

    private fun secretKey(): SecretKey {
        val ks = KeyStore.getInstance(STORE).apply { load(null) }
        val existing = ks.getKey(ALIAS, null)
        if (existing is SecretKey) return existing
        val generator = KeyGenerator.getInstance("AES", STORE)
        generator.init(256)
        return generator.generateKey()
    }

    fun save(context: Context, value: String) {
        if (value.isBlank()) return
        val iv = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(value.trim().toByteArray(Charsets.UTF_8))
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)).apply()
    }

    fun load(context: Context): String? = runCatching {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return null
        val bytes = Base64.decode(raw, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, 12)
        val encrypted = bytes.copyOfRange(12, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }.getOrNull()

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
