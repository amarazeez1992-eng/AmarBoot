package com.personal.gridbot.amaros.ai

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

/** Reads user-selected visual evidence without granting it execution authority or binding it to a vendor. */
class AmarAiImageInput(private val context: Context) {
    data class ImagePayload(val mimeType: String, val base64: String)

    fun read(uri: Uri): ImagePayload {
        val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            val out = ByteArrayOutputStream()
            input.copyTo(out)
            out.toByteArray()
        } ?: error("Unable to read image")
        require(bytes.isNotEmpty()) { "Image is empty" }
        require(bytes.size <= 8 * 1024 * 1024) { "Image exceeds 8 MB AI input limit" }
        return ImagePayload(mime, Base64.encodeToString(bytes, Base64.NO_WRAP))
    }
}
