package com.personal.gridbot.amaros.workspace

import java.security.MessageDigest

/** Signed-release metadata consumed by the in-app update flow. */
data class AmarUpdateManifest(
    val versionCode: Long,
    val versionName: String,
    val packageName: String,
    val apkUrl: String,
    val sha256: String,
    val changelog: String,
    val minSupportedVersionCode: Long,
    val generatedAtEpochMs: Long
)

data class AmarUpdatePolicy(
    val checkAutomatically: Boolean = true,
    val requireUserConfirmation: Boolean = true,
    val allowDowngrade: Boolean = false,
    val maxManifestAgeMs: Long = 7L * 24L * 60L * 60L * 1000L
)

enum class AmarUpdateStatus { UP_TO_DATE, UPDATE_AVAILABLE, BLOCKED, FAILED }

data class AmarUpdateState(
    val status: AmarUpdateStatus,
    val manifest: AmarUpdateManifest? = null,
    val reason: String = ""
)

/** Download/install boundary. Implementations must never uninstall the current app first. */
interface AmarUpdateInstaller {
    fun install(apkBytes: ByteArray, manifest: AmarUpdateManifest, userConfirmed: Boolean): Boolean
}

/** Deterministic, fail-closed update verification. No third-party AI dependency. */
class AmarUpdateEngine(
    private val expectedPackageName: String,
    private val currentVersionCode: Long,
    private val policy: AmarUpdatePolicy = AmarUpdatePolicy(),
    private val nowEpochMs: () -> Long = { System.currentTimeMillis() }
) {
    init {
        require(expectedPackageName.isNotBlank())
        require(currentVersionCode >= 0L)
        require(policy.maxManifestAgeMs >= 0L)
    }

    fun check(manifest: AmarUpdateManifest?): AmarUpdateState {
        if (manifest == null) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "missing_manifest")
        if (!isValidManifest(manifest)) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "invalid_manifest")
        if (manifest.packageName != expectedPackageName) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "package_mismatch")
        val now = nowEpochMs()
        if (now < 0L || manifest.generatedAtEpochMs > now) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "invalid_manifest_time")
        if (now - manifest.generatedAtEpochMs > policy.maxManifestAgeMs) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "stale_manifest")
        if (manifest.minSupportedVersionCode > currentVersionCode) return AmarUpdateState(AmarUpdateStatus.BLOCKED, reason = "incompatible_current_version")
        if (!policy.allowDowngrade && manifest.versionCode <= currentVersionCode) {
            return AmarUpdateState(AmarUpdateStatus.UP_TO_DATE, manifest = manifest, reason = "no_newer_version")
        }
        return AmarUpdateState(AmarUpdateStatus.UPDATE_AVAILABLE, manifest = manifest, reason = "new_version_available")
    }

    fun verifyApk(manifest: AmarUpdateManifest, apkBytes: ByteArray): Boolean {
        if (apkBytes.isEmpty() || !isValidManifest(manifest)) return false
        val expected = normalizeSha256(manifest.sha256) ?: return false
        val actual = MessageDigest.getInstance("SHA-256").digest(apkBytes).toHex()
        return MessageDigest.isEqual(expected.toByteArray(Charsets.US_ASCII), actual.toByteArray(Charsets.US_ASCII))
    }

    fun install(
        state: AmarUpdateState,
        apkBytes: ByteArray,
        userConfirmed: Boolean,
        installer: AmarUpdateInstaller
    ): Boolean {
        val manifest = state.manifest ?: return false
        if (state.status != AmarUpdateStatus.UPDATE_AVAILABLE) return false
        if (policy.requireUserConfirmation && !userConfirmed) return false
        if (!verifyApk(manifest, apkBytes)) return false
        return installer.install(apkBytes.copyOf(), manifest, userConfirmed)
    }

    private fun isValidManifest(m: AmarUpdateManifest): Boolean =
        m.versionCode >= 0L &&
            m.versionName.isNotBlank() &&
            m.packageName.isNotBlank() &&
            m.apkUrl.startsWith("https://") &&
            normalizeSha256(m.sha256) != null &&
            m.changelog.isNotBlank() &&
            m.minSupportedVersionCode >= 0L &&
            m.generatedAtEpochMs >= 0L

    private fun normalizeSha256(value: String): String? {
        val normalized = value.trim().lowercase()
        return if (normalized.matches(Regex("[0-9a-f]{64}"))) normalized else null
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
