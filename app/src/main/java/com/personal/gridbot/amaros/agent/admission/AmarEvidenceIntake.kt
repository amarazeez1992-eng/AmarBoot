package com.personal.gridbot.amaros.agent.admission

import java.net.URI
import java.security.MessageDigest

class AmarEvidenceIntake : AmarEvidenceIntakeContract {

    override fun intake(
        question: String,
        candidates: List<EvidenceCandidate>
    ): EvidenceIntakeResult {
        val accepted = mutableListOf<NormalizedCandidate>()
        val rejected = mutableListOf<IntakeRejectedCandidate>()
        val seenCanonicalUrls = mutableSetOf<String>()
        val seenFingerprints = mutableSetOf<String>()

        candidates.forEach { candidate ->
            val validationError = validate(candidate)
            if (validationError != null) {
                rejected += IntakeRejectedCandidate(candidate, validationError, validationError.name)
                return@forEach
            }

            val provider = normalizeProvider(candidate.provider)
            val title = normalizeText(candidate.title)
            val excerpt = normalizeText(candidate.excerpt)
            val canonicalUrl = canonicalizeUrl(candidate.url)
            val fingerprint = computeFingerprint(provider, title, excerpt)

            if (!seenCanonicalUrls.add(canonicalUrl)) {
                rejected += IntakeRejectedCandidate(
                    candidate,
                    IntakeRejectionReason.DUPLICATE_URL,
                    "canonical URL is already present"
                )
                return@forEach
            }

            if (!seenFingerprints.add(fingerprint)) {
                rejected += IntakeRejectedCandidate(
                    candidate,
                    IntakeRejectionReason.DUPLICATE_FINGERPRINT,
                    "technical fingerprint is already present"
                )
                seenCanonicalUrls.remove(canonicalUrl)
                return@forEach
            }

            accepted += NormalizedCandidate(
                provider = provider,
                title = title,
                canonicalUrl = canonicalUrl,
                normalizedExcerpt = excerpt,
                retrievedAtEpochMs = candidate.retrievedAtEpochMs,
                fingerprint = fingerprint,
                normalizationFlags = normalizationFlags(candidate, provider, title, excerpt, canonicalUrl)
            )
        }

        return EvidenceIntakeResult(accepted, rejected)
    }

    private fun validate(candidate: EvidenceCandidate): IntakeRejectionReason? {
        if (candidate.provider.isBlank()) return IntakeRejectionReason.BLANK_PROVIDER
        if (candidate.title.isBlank()) return IntakeRejectionReason.BLANK_TITLE
        if (candidate.excerpt.isBlank()) return IntakeRejectionReason.BLANK_EVIDENCE
        if (!isValidUrl(candidate.url)) return IntakeRejectionReason.INVALID_URL
        return null
    }

    private fun isValidUrl(url: String): Boolean = try {
        val uri = URI(url.trim())
        !uri.scheme.isNullOrBlank() &&
            !uri.host.isNullOrBlank() &&
            (uri.scheme.equals("http", true) || uri.scheme.equals("https", true))
    } catch (_: Exception) {
        false
    }

    private fun canonicalizeUrl(url: String): String {
        val uri = URI(url.trim())
        val scheme = uri.scheme.lowercase()
        val host = uri.host.lowercase()
        val path = uri.path.trimEnd('/')
        val query = uri.rawQuery?.let { "?$it" } ?: ""
        return "$scheme://$host$path$query"
    }

    private fun normalizeText(text: String): String = text.trim().replace(Regex("\\s+"), " ")

    private fun normalizeProvider(provider: String): String = provider.trim().lowercase()

    private fun computeFingerprint(
        provider: String,
        title: String,
        excerpt: String
    ): String {
        val input = "$provider\n$title\n$excerpt"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun normalizationFlags(
        candidate: EvidenceCandidate,
        provider: String,
        title: String,
        excerpt: String,
        canonicalUrl: String
    ): Set<NormalizationFlag> {
        val flags = mutableSetOf<NormalizationFlag>()
        if (candidate.url.trim() != canonicalUrl) flags += NormalizationFlag.URL_CANONICALIZED
        if (candidate.provider != provider) flags += NormalizationFlag.SOURCE_NORMALIZED
        if (candidate.title.trim() != title) flags += NormalizationFlag.TITLE_TRIMMED
        if (candidate.excerpt.trim() != excerpt) flags += NormalizationFlag.EXCERPT_TRIMMED
        if (candidate.title != candidate.title.trim() || candidate.excerpt != candidate.excerpt.trim()) {
            flags += NormalizationFlag.WHITESPACE_TRIMMED
        }
        return flags
    }
}
