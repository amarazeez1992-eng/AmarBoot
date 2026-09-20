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
import com.personal.gridbot.amaros.agent.AmarRetrievalRelevanceEngine

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
                                add(
                                    SourceResult(
                                        source = "Wikipedia",
                                        title = title,
                                        url = "https://$wikipediaLanguage.wikipedia.org/wiki/" + title.replace(' ', '_'),
                                        excerpt = excerpt
                                    )
                                )
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }
            val github = async {
                if (!isCodeOrRepositoryQuery(query)) {
                    emptyList()
                } else {
                    runCatching {
                        val url = "https://api.github.com/search/repositories?q=$encoded&per_page=$limit"
                        val raw = get(url, "application/json")
                        val items = JSONObject(raw).getJSONArray("items")
                        buildList {
                            for (i in 0 until minOf(items.length(), limit)) {
                                val item = items.getJSONObject(i)
                                val title = item.optString("full_name")
                                val repositoryUrl = item.optString("html_url")
                                if (title.isNotBlank() && repositoryUrl.isNotBlank()) {
                                    add(
                                        SourceResult(
                                            source = "GitHub",
                                            title = title,
                                            url = repositoryUrl,
                                            excerpt = item.optString("description")
                                        )
                                    )
                                }
                            }
                        }
                    }.getOrDefault(emptyList())
                }
            }

            (duck.await() + wikipedia.await() + github.await())
                .filter { it.url.startsWith("http") && it.title.isNotBlank() }
                .map { result ->
                    val scored = relevance.score(query, result.title, result.excerpt)
                    result.copy(relevanceScore = scored.score)
                }
                .filter { it.relevanceScore >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE }
                .distinctBy { canonicalKey(it.url) }
                .sortedByDescending { it.relevanceScore }
                .take(limit)
        }
    }

    private fun isCodeOrRepositoryQuery(query: String): Boolean {
        val q = query.lowercase()
        return listOf(
            "github", "repository", "repo", "code", "source code", "sdk", "api",
            "kotlin", "android", "python", "javascript", "typescript", "pine script",
            "مستودع", "كود", "برمجة", "برمجي", "شفرة", "github"
        ).any { it in q }
    }

    private fun parseDuckDuckGo(html: String, limit: Int): List<SourceResult> {
        if (html.isBlank()) return emptyList()
        val anchorPattern = Regex(
            "<a\\b[^>]*>(.*?)</a>",
            RegexOption.IGNORE_CASE or RegexOption.DOT_MATCHES_ALL
        )
        val anchors = anchorPattern.findAll(html).toList()
        val results = mutableListOf<SourceResult>()

        anchors.forEachIndexed { index, match ->
            if (results.size >= limit) return@forEachIndexed
            val tag = match.value
            if (!hasCssClass(tag, "result__a")) return@forEachIndexed

            val href = attribute(tag, "href")
            val title = stripMarkup(match.groupValues[1])
            if (href.isBlank() || title.isBlank()) return@forEachIndexed

            val nextResultStart = anchors.asSequence()
                .drop(index + 1)
                .firstOrNull { hasCssClass(it.value, "result__a") }
                ?.range?.first ?: html.length
            val snippet = anchors.asSequence()
                .drop(index + 1)
                .firstOrNull { hasCssClass(it.value, "result__snippet") && it.range.first < nextResultStart }
                ?.groupValues?.getOrNull(1)
                ?.let(::stripMarkup)
                .orEmpty()

            val evidence = snippet.ifBlank { title }
            val url = resolveDuckDuckGoUrl(decodeHtml(href))
            if (url.startsWith("http") && evidence.isNotBlank()) {
                results += SourceResult("Public Web", title, url, evidence)
            }
        }

        return results
    }

    private fun hasCssClass(tag: String, className: String): Boolean {
        val classes = attribute(tag, "class")
        return classes.split(Regex("\\s+")).any { it == className }
    }

    private fun attribute(tag: String, name: String): String =
        Regex("\\b$name\\s*=\\s*[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE)
            .find(tag)?.groupValues?.getOrNull(1).orEmpty()

    private fun resolveDuckDuckGoUrl(url: String): String {
        if (!url.startsWith("//duckduckgo.com/l/?uddg=")) return url
        val query = url.substringAfter("?")
        val encodedTarget = query.split("&")
            .firstOrNull { it.startsWith("uddg=") }
            ?.substringAfter("=")
            ?: return url
        return runCatching {
            java.net.URLDecoder.decode(encodedTarget, StandardCharsets.UTF_8.name())
        }.getOrDefault(url)
    }

    private fun canonicalKey(url: String): String =
        runCatching {
            val uri = java.net.URI(url)
            (uri.host.orEmpty().lowercase() + uri.path.orEmpty()).trimEnd('/')
        }.getOrDefault(url.trim())

    private fun stripMarkup(value: String): String =
        decodeHtml(value.replace(Regex("<[^>]*>"), "")).trim()

    private fun decodeHtml(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#x27;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&#39;", "'")

    private fun get(url: String, accept: String): String {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", accept)
            .addHeader("User-Agent", "AmarBoot-PublicResearch/2.0")
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Research HTTP " + response.code)
            return response.body?.string().orEmpty()
        }
    }
}
