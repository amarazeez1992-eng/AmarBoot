package com.personal.gridbot.amaros.workspace

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertEquals(AmarUpdateStatus.UPDATE_AVAILABLE, engine().check(manifest).status)
    }

    @Test
    fun missingOrInvalidManifestBlocks() {
        val engine = engine()
        assertEquals(AmarUpdateStatus.BLOCKED, engine.check(null).status)
        assertEquals(AmarUpdateStatus.BLOCKED, engine.check(manifest.copy(apkUrl = "http://updates.example.invalid/a.apk")).status)
    }

    @Test
    fun staleOrFutureManifestBlocks() {
        val stale = engine(now = 10_000L, policy = AmarUpdatePolicy(maxManifestAgeMs = 100L))
        assertEquals("stale_manifest", stale.check(manifest).reason)
        val future = engine(now = 999L)
        assertEquals("invalid_manifest_time", future.check(manifest).reason)
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
        assertEquals(AmarUpdateStatus.UP_TO_DATE, engine(currentVersion = 2L).check(manifest).status)
        assertEquals("downgrade_blocked", engine(currentVersion = 3L).check(manifest).reason)
    }

    @Test
    fun allowDowngradeEnablesExplicitLowerVersionOnly() {
        val policy = AmarUpdatePolicy(allowDowngrade = true)
        assertEquals(AmarUpdateStatus.UPDATE_AVAILABLE, engine(currentVersion = 3L, policy = policy).check(manifest).status)
        assertEquals(AmarUpdateStatus.UP_TO_DATE, engine(currentVersion = 2L, policy = policy).check(manifest).status)
    }

    private class RecordingInstaller(private val expectedBytes: ByteArray) : AmarUpdateInstaller {
        var called = false

        override fun install(apkBytes: ByteArray, manifest: AmarUpdateManifest, userConfirmed: Boolean): Boolean {
            called = true
            return apkBytes.contentEquals(expectedBytes) && userConfirmed
        }
    }
}
