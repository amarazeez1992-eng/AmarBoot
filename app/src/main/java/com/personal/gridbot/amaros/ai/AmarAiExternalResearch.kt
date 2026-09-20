package com.personal.gridbot.amaros.ai

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Keyless public-web research layer.
 *
 * Results are evidence only; this layer cannot mutate strategies or execute trades.
 * Retrieval is bounded, concurrent, deduplicated and fail-soft so one provider
 * cannot silently become the Agent's sole source of truth.
 */
class AmarAiExternalResearch(
    private val http: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(8, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()
) {
    data class SourceResult(
        val source: String,
        val title: String,
        val url: String,
        val excerpt: String,
        val relevanceScore: Double = 0.0
    )

    suspend fun search(query: String, maxResults: Int = 80): List<SourceResult> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Research query is required" }
        val limit = maxResults.coerceIn(1, 80)
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        val wikipediaLanguage = if (query.any { it in '\u0600'..'\u06FF' }) "ar" else "en"
        val relevance = com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine()

        coroutineScope {
            val duck = async {
                runCatching {
                    parseDuckDuckGo(
                        get("https://html.duckduckgo.com/html/?q=$encoded", "text/html"),
                        limit
                    )
                }.getOrDefault(emptyList())
            }
            val wikipedia = async {
                runCatching {
                    val url = "https://$wikipediaLanguage.wikipedia.org/w/api.php?action=query&generator=search&format=json&utf8=1&gsrnamespace=0&gsrlimit=$limit&gsrsearch=$encoded&prop=extracts&exintro=1&explaintext=1&exchars=1200"
                    val raw = get(url, "application/json")
                    val pages = JSONObject(raw).optJSONObject("query")?.optJSONObject("pages") ?: JSONObject()
                    buildList {
                        val keys = pages.keys()
                        while (keys.hasNext() && size < limit) {
                            val item = pages.getJSONObject(keys.next())
                            val title = item.optString("title")
                            val excerpt = item.optString("extract").ifBlank { stripMarkup(item.optString("snippet")) }
                            if (title.isNotBlank() && excerpt.isNotBlank()) {
                                add(SourceResult("Wikipedia", title, "https://$wikipediaLanguage.wikipedia.org/wiki/" + title.replace(' ', '_'), excerpt))
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }
            val github = async {
                if (!isCodeOrRepositoryQuery(query)) emptyList()
                else runCatching {
                    val url = "https://api.github.com/search/repositories?q=$encoded&per_page=$limit"
                    val raw = get(url, "application/json")
                    val items = JSONObject(raw).getJSONArray("items")
                    buildList {
                        for (i in 0 until minOf(items.length(), limit)) {
                            val item = items.getJSONObject(i)
                            val title = item.optString("full_name")
                            val repositoryUrl = item.optString("html_url")
                            val excerpt = item.optString("description")
                            if (title.isNotBlank() && repositoryUrl.isNotBlank() && excerpt.isNotBlank()) {
                                add(SourceResult("GitHub", title, repositoryUrl, excerpt))
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }

            (duck.await() + wikipedia.await() + github.await())
                .filter { it.url.startsWith("http") && it.title.isNotBlank() && it.excerpt.isNotBlank() }
                .mapNotNull { result ->
                    val scored = relevance.score(query, result.title, result.excerpt)
                    if (scored.score >= com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE) {
                        result to scored.score
                    } else null
                }
                .distinctBy { canonicalKey(it.first.url) }
                .sortedByDescending { it.second }
                .take(limit)
                .map { it.first.copy(relevanceScore = it.second) }
        }
    }


