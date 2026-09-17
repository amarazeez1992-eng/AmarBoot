package com.personal.gridbot.amaros.agent

import java.net.URI

/**
 * Stage 11 Item 3 Point 2: deterministic source-quality analysis.
 *
 * This point evaluates source metadata and address quality only. Authority scoring,
 * freshness, independence, uniqueness and fingerprint integrity remain owned by the
 * later canonical evidence-quality policy/engine points.
 */
class AmarSourceQualityAnalyzer {
    fun analyze(findings: List<ResearchFinding>): AmarSourceQualityReport {
        val items = findings.map(::analyzeItem)
        val usable = items.filter { it.quality == AmarSourceQualityLevel.USABLE }
        val score = if (items.isEmpty()) 0.0 else items.map { it.score }.average().coerceIn(0.0, 1.0)
        return AmarSourceQualityReport(
            score = score,
            usableSourceCount = usable.size,
            items = items,
            status = when {
                findings.isEmpty() -> AmarSourceQualityStatus.UNVERIFIABLE
                usable.isEmpty() -> AmarSourceQualityStatus.UNVERIFIABLE
                items.any { it.quality == AmarSourceQualityLevel.DEGRADED } -> AmarSourceQualityStatus.DEGRADED
                else -> AmarSourceQualityStatus.ACCEPTABLE
            }
        )
    }

    private fun analyzeItem(finding: ResearchFinding): AmarSourceQualityItem {
        val titlePresent = finding.sourceTitle.trim().isNotEmpty()
        val uri = finding.sourceUri.trim()
        val validUri = isHttpUri(uri)
        val evidencePresent = finding.evidence.trim().isNotEmpty()
        val publisherPresent = finding.publisher.trim().isNotEmpty()

        val score = listOf(titlePresent, validUri, evidencePresent, publisherPresent)
            .count { it }
            .toDouble() / 4.0

        val quality = when {
            !validUri || !evidencePresent -> AmarSourceQualityLevel.INVALID
            score >= 0.75 -> AmarSourceQualityLevel.USABLE
            else -> AmarSourceQualityLevel.DEGRADED
        }

        val issues = buildList {
            if (!titlePresent) add("missing_source_title")
            if (!validUri) add("invalid_source_uri")
            if (!evidencePresent) add("missing_evidence")
            if (!publisherPresent) add("missing_publisher")
        }

        return AmarSourceQualityItem(
            sourceUri = uri,
            metadataScore = score,
            titlePresent = titlePresent,
            validHttpUri = validUri,
            evidencePresent = evidencePresent,
            publisherPresent = publisherPresent,
            score = score,
            quality = quality,
            issues = issues
        )
    }

    private fun isHttpUri(value: String): Boolean = runCatching {
        val uri = URI(value)
        (uri.scheme.equals("https", true) || uri.scheme.equals("http", true)) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}

data class AmarSourceQualityItem(
    val sourceUri: String,
    val metadataScore: Double,
    val titlePresent: Boolean,
    val validHttpUri: Boolean,
    val evidencePresent: Boolean,
    val publisherPresent: Boolean,
    val score: Double,
    val quality: AmarSourceQualityLevel,
    val issues: List<String>
)

enum class AmarSourceQualityLevel { USABLE, DEGRADED, INVALID }
enum class AmarSourceQualityStatus { ACCEPTABLE, DEGRADED, UNVERIFIABLE }

data class AmarSourceQualityReport(
    val score: Double,
    val usableSourceCount: Int,
    val items: List<AmarSourceQualityItem>,
    val status: AmarSourceQualityStatus
)
