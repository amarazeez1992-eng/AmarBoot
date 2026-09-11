package com.personal.gridbot.amaros.broker

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.util.Base64
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/** B42: device-bound identity. Private material never leaves Android Keystore. */
class AmarDeviceSecurity(
    private val alias: String = "amar_bot1_device_key"
) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val sequence = AtomicLong(System.currentTimeMillis())

    init {
        ensureKeyPair()
    }

    val deviceId: String by lazy {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(publicKeyBytes())
        digest.joinToString("") { "%02x".format(it) }
    }

    fun nextSequence(): Long = sequence.incrementAndGet()

    fun publicKeyBase64(): String = Base64.getEncoder().encodeToString(publicKeyBytes())

    fun sign(value: String): String {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey())
        signature.update(value.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    private fun ensureKeyPair() {
        if (keyStore.containsAlias(alias)) return
        val generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore")
        generator.initialize(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setKeySize(2048)
                .build()
        )
        generator.generateKeyPair()
    }

    private fun privateKey(): PrivateKey =
        (keyStore.getKey(alias, null) as? PrivateKey)
            ?: error("AMAR device private key unavailable")

    private fun publicKeyBytes(): ByteArray =
        keyStore.getCertificate(alias)?.publicKey?.encoded
            ?: error("AMAR device public key unavailable")
}
