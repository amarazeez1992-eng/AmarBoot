package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.agent.AmarResearchEngine
import com.personal.gridbot.amaros.agent.AmarSourceType
import com.personal.gridbot.amaros.agent.Authority
import com.personal.gridbot.amaros.agent.ResearchFinding
import com.personal.gridbot.amaros.agent.ResearchReport
import com.personal.gridbot.amaros.agent.ResearchRequest

/**
 * Provider-neutral adapter from the existing keyless public-web retriever into the
 * canonical evidence contract. It retrieves evidence only; it never generates answers.
 */
class AmarExternalResearchAdapter(
    private val external: AmarAiExternalResearch = AmarAiExternalResearch()
) : AmarResearchEngine {
    override suspend fun research(request: ResearchRequest): ResearchReport {
        val raw = external.search(request.question, request.maxSources)
        val findings = raw.distinctBy { it.url } .map { item ->
            ResearchFinding(
                sourceTitle = item.title.ifBlank { item.source },
                sourceUri = item.url,
                evidence = item.excerpt.trim(),
                authority = authorityFor(item.url, item.source),
                publisher = item.source,
                sourceType = AmarSourceType.WEB
            )
        }.filter { it.sourceUri.startsWith("https://") && it.evidence.isNotBlank() }
        val independent = findings.mapNotNull { host(it.sourceUri) }.distinct().size
        val confidence = when {
            findings.isEmpty() -> 0.0
            independent >= 3 -> 0.75
            independent == 2 -> 0.60
            else -> 0.35
        }
        val conflicts = if (independent < request.targetIndependentSources.coerceAtMost(3))
            listOf("independent_source_target_not_reached") else emptyList()
        return ResearchReport(findings, conflicts, confidence)
    }

    private fun host(url: String): String = runCatching {
        java.net.URI(url).host.orEmpty().lowercase().removePrefix("www.")
    }.getOrDefault("")

    private fun authorityFor(url: String, source: String): Authority {
        val host = host(url)
        return when {
            host.endsWith(".gov") || host.endsWith(".gov.uk") || host.endsWith(".int") -> Authority.OFFICIAL
            source.equals("Wikipedia", true) -> Authority.REPUTABLE
            else -> Authority.UNKNOWN
        }
    }
}