package com.personal.gridbot.amaros.workspace

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Android installation boundary. The OS installer remains the authority for package/signing checks. */
class AmarAndroidUpdateInstaller(
    private val context: Context,
    private val authoritySuffix: String = ".update-file-provider"
) : AmarUpdateInstaller {
    override fun install(apkBytes: ByteArray, manifest: AmarUpdateManifest, userConfirmed: Boolean): Boolean {
        if (!userConfirmed || apkBytes.isEmpty()) return false
        if (manifest.packageName != context.packageName) return false
        return runCatching {
            val directory = File(context.cacheDir, "amar-updates").apply { mkdirs() }
            val apk = File(directory, "amar-${manifest.versionCode}.apk")
            apk.outputStream().use { it.write(apkBytes) }
            val uri = FileProvider.getUriForFile(context, context.packageName + authoritySuffix, apk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        }.getOrElse { false }
    }
}
