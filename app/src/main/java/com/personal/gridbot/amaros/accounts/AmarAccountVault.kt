package com.personal.gridbot.amaros.accounts

import android.content.Context
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** حساب تداول قابل للربط لاحقاً بموصل حقيقي. لا يُحفظ سر الدخول كنص مكشوف. */
data class AmarAccountProfile(
    val id: String,
    val brokerName: String,
    val accountType: AccountType,
    val server: String,
    val login: String,
    val credentialAlias: String,
    val enabled: Boolean = false,
)

enum class AccountType { DEMO, REAL }

/** مخزن محلي مشفر باستخدام مخزن مفاتيح أندرويد. */
class AmarAccountVault(private val context: Context) {
    private val preferences = context.getSharedPreferences("amar_account_vault", Context.MODE_PRIVATE)
    private val keyAlias = "amar_account_secret_key_v1"

    fun savePassword(accountId: String, password: CharArray) {
        require(accountId.isNotBlank())
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.doFinal(String(password).toByteArray(StandardCharsets.UTF_8))
        val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        preferences.edit().putString("secret_$accountId", payload).apply()
        password.fill('\u0000')
    }

    fun hasPassword(accountId: String): Boolean =
        preferences.contains("secret_$accountId")

    fun removePassword(accountId: String) {
        preferences.edit().remove("secret_$accountId").apply()
    }

    fun decryptPassword(accountId: String): CharArray {
        val payload = preferences.getString("secret_$accountId", null)
            ?: error("بيانات الدخول غير موجودة")
        val parts = payload.split(":", limit = 2)
        require(parts.size == 2)
        val iv = Base64.decode(parts[0], Base64.NO_WRAP)
        val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8).toCharArray()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                keyAlias,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return generator.generateKey()
    }
}
