package com.personal.gridbot.amaros.security

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Local bearer-token storage backed by Android Keystore and AES-GCM. */
object AmarSecureTokenStore {
    private const val STORE = "AndroidKeyStore"
    private const val ALIAS = "amar_secure_token_key_v1"
    private const val PREFS = "amar_secure_tokens_v1"
    private const val TOKEN = "sync_token"
    private const val IV_BYTES = 12

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(STORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance("AES", STORE)
        generator.init(256)
        return generator.generateKey()
    }

    fun save(context: Context, value: String) {
        require(value.isNotBlank()) { "Secure token is required" }
        val iv = ByteArray(IV_BYTES).also { java.security.SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(value.trim().toByteArray(Charsets.UTF_8))
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(TOKEN, Base64.encodeToString(iv + encrypted, Base64.NO_WRAP))
            .apply()
    }

    fun load(context: Context): String? = runCatching {
        val raw = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(TOKEN, null)
            ?: return null
        val bytes = Base64.decode(raw, Base64.NO_WRAP)
        require(bytes.size > IV_BYTES)
        val iv = bytes.copyOfRange(0, IV_BYTES)
        val encrypted = bytes.copyOfRange(IV_BYTES, bytes.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }.getOrNull()

    fun clear(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(TOKEN).apply()
    }
}
