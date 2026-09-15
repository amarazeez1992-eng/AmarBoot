package com.personal.gridbot.amaros.workspace

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Item10UpdateEngineTest {
    private val bytes = "verified-apk".toByteArray()
    private val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
    private val manifest = AmarUpdateManifest(
        versionCode = 2L,
        versionName = "2.0",
        packageName = "com.personal.gridbot",
        apkUrl = "https://updates.example.invalid/amar-ai-2.apk",
        sha256 = hash,
        changelog = "Item 10 update engine",
        minSupportedVersionCode = 1L,
        generatedAtEpochMs = 1000L
    )

    @Test
    fun newerManifestIsAvailable() {
        val engine = AmarUpdateEngine("com.personal.gridbot", 1L)
        assertTrue(engine.check(manifest).status == AmarUpdateStatus.UPDATE_AVAILABLE)
    }

    @Test
    fun missingOrInvalidManifestBlocks() {
        val engine = AmarUpdateEngine("com.personal.gridbot", 1L)
        assertTrue(engine.check(null).status == AmarUpdateStatus.BLOCKED)
        assertTrue(engine.check(manifest.copy(apkUrl = "http://updates.example.invalid/a.apk")).status == AmarUpdateStatus.BLOCKED)
    }

    @Test
    fun hashMismatchFailsClosed() {
        val engine = AmarUpdateEngine("com.personal.gridbot", 1L)
        assertFalse(engine.verifyApk(manifest, "tampered".toByteArray()))
    }

    @Test
    fun installationRequiresConfirmationAndVerifiedBytes() {
        val engine = AmarUpdateEngine("com.personal.gridbot", 1L)
        val state = engine.check(manifest)
        val installer = RecordingInstaller(bytes)
        assertFalse(engine.install(state, bytes, userConfirmed = false, installer))
        assertFalse(engine.install(state, "tampered".toByteArray(), userConfirmed = true, installer))
        assertTrue(engine.install(state, bytes, userConfirmed = true, installer))
        assertTrue(installer.called)
    }

    @Test
    fun currentVersionIsNotDowngradedByDefault() {
        val engine = AmarUpdateEngine("com.personal.gridbot", 2L)
        assertTrue(engine.check(manifest).status == AmarUpdateStatus.UP_TO_DATE)
    }

    private class RecordingInstaller(private val expectedBytes: ByteArray) : AmarUpdateInstaller {
        var called = false

        override fun install(apkBytes: ByteArray, manifest: AmarUpdateManifest, userConfirmed: Boolean): Boolean {
            called = true
            return apkBytes.contentEquals(expectedBytes) && userConfirmed
        }
    }
}
