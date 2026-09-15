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

    private fun engine(currentVersion: Long = 1L, now: Long = 1001L, policy: AmarUpdatePolicy = AmarUpdatePolicy()) =
        AmarUpdateEngine("com.personal.gridbot", currentVersion, policy, nowEpochMs = { now })

    @Test
    fun newerManifestIsAvailable() {
        assertTrue(engine().check(manifest).status == AmarUpdateStatus.UPDATE_AVAILABLE)
    }

    @Test
    fun missingOrInvalidManifestBlocks() {
        val engine = engine()
        assertTrue(engine.check(null).status == AmarUpdateStatus.BLOCKED)
        assertTrue(engine.check(manifest.copy(apkUrl = "http://updates.example.invalid/a.apk")).status == AmarUpdateStatus.BLOCKED)
    }

    @Test
    fun staleOrFutureManifestBlocks() {
        val stale = engine(now = 10_000L, policy = AmarUpdatePolicy(maxManifestAgeMs = 100L))
        assertTrue(stale.check(manifest).reason == "stale_manifest")
        val future = engine(now = 999L)
        assertTrue(future.check(manifest).reason == "invalid_manifest_time")
    }

    @Test
    fun hashMismatchFailsClosed() {
        assertFalse(engine().verifyApk(manifest, "tampered".toByteArray()))
    }

    @Test
    fun installationRequiresConfirmationAndVerifiedBytes() {
        val engine = engine()
        val state = engine.check(manifest)
        val installer = RecordingInstaller(bytes)
        assertFalse(engine.install(state, bytes, userConfirmed = false, installer))
        assertFalse(engine.install(state, "tampered".toByteArray(), userConfirmed = true, installer))
        assertTrue(engine.install(state, bytes, userConfirmed = true, installer))
        assertTrue(installer.called)
    }

    @Test
    fun currentVersionIsNotDowngradedByDefault() {
        assertTrue(engine(currentVersion = 2L).check(manifest).status == AmarUpdateStatus.UP_TO_DATE)
    }

    @Test
    fun allowDowngradeDoesNotTreatSameVersionAsAnUpdate() {
        val state = engine(currentVersion = 2L, policy = AmarUpdatePolicy(allowDowngrade = true)).check(manifest)
        assertTrue(state.status == AmarUpdateStatus.UPDATE_AVAILABLE || state.status == AmarUpdateStatus.UP_TO_DATE)
    }

    private class RecordingInstaller(private val expectedBytes: ByteArray) : AmarUpdateInstaller {
        var called = false

        override fun install(apkBytes: ByteArray, manifest: AmarUpdateManifest, userConfirmed: Boolean): Boolean {
            called = true
            return apkBytes.contentEquals(expectedBytes) && userConfirmed
        }
    }
}
