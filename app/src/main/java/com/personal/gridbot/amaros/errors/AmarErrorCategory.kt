package com.personal.gridbot.amaros.errors

enum class AmarErrorCategory {
    VALIDATION,
    NETWORK,
    STORAGE,
    PROVIDER,
    SECURITY,
    UNEXPECTED
}

data class AmarError(
    val category: AmarErrorCategory,
    val source: String,
    val cause: Throwable?
)

object AmarErrorClassifier {
    fun classify(source: String, throwable: Throwable): AmarError {
        val category = when (throwable) {
            is IllegalArgumentException -> AmarErrorCategory.VALIDATION
            is IllegalStateException -> AmarErrorCategory.VALIDATION
            is java.io.IOException -> AmarErrorCategory.NETWORK
            is android.database.SQLException -> AmarErrorCategory.STORAGE
            is SecurityException -> AmarErrorCategory.SECURITY
            else -> AmarErrorCategory.UNEXPECTED
        }
        return AmarError(category, source, throwable)
    }
}
