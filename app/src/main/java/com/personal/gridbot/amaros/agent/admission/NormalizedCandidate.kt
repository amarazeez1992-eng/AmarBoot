package com.personal.gridbot.amaros.agent.admission

data class NormalizedCandidate(
    val provider: String,
    val title: String,
    val canonicalUrl: String,
    val normalizedExcerpt: String,
    val retrievedAtEpochMs: Long,
    val fingerprint: String,
    val normalizationFlags: Set<NormalizationFlag>
) {
    init {
        require(provider.isNotBlank()) { "provider must not be blank" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(canonicalUrl.isNotBlank()) { "canonicalUrl must not be blank" }
        require(fingerprint.length == 64) { "fingerprint must be 64-char SHA-256 hex" }
    }
}

enum class NormalizationFlag {
    URL_CANONICALIZED,
    WHITESPACE_TRIMMED,
    TITLE_TRIMMED,
    EXCERPT_TRIMMED,
    SOURCE_NORMALIZED
}
