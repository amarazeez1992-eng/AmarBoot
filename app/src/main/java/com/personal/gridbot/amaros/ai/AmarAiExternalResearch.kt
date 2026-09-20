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
        val excerpt: String
    )

    suspend fun search(query: String, maxResults: Int = 80): List<SourceResult> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Research query is required" }
        val limit = maxResults.coerceIn(1, 80)
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())

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
                    val url = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&utf8=1&srlimit=$limit&srsearch=$encoded"
                    val raw = get(url, "application/json")
                    val items = JSONObject(raw).getJSONObject("query").getJSONArray("search")
                    buildList {
                        for (i in 0 until minOf(items.length(), limit)) {
                            val item = items.getJSONObject(i)
                            val title = item.optString("title")
                            if (title.isNotBlank()) {
                                add(
                                    SourceResult(
                                        source = "Wikipedia",
                                        title = title,
                                        url = "https://en.wikipedia.org/wiki/\${title.replace(' ', '_')}",
                                        excerpt = stripMarkup(item.optString("snippet"))
                                    )
                                )
                            }
                        }
                    }
                }.getOrDefault(emptyList())
            }
            val github = async {
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

            (duck.await() + wikipedia.await() + github.await())
                .filter { it.url.startsWith("http") && it.title.isNotBlank() }
                .distinctBy { canonicalKey(it.url) }
                .take(limit)
        }
    }

    private fun parseDuckDuckGo(html: String, limit: Int): List<SourceResult> {
        val pattern = Regex(
            """<a[^>]+class=["']result__a["'][^>]+href=["']([^"']+)["'][^>]*>(.*?)</a>""",
            RegexOption.IGNORE_CASE
        )
        return pattern.findAll(html)
            .take(limit)
            .mapNotNull { match ->
                val url = decodeHtml(match.groupValues[1])
                val title = stripMarkup(match.groupValues[2])
                if (url.startsWith("http") && title.isNotBlank()) {
                    SourceResult("Public Web", title, url, title)
                } else null
            }
            .toList()
    }

    private fun canonicalKey(url: String): String =
        runCatching {
            val uri = java.net.URI(url)
            "\${uri.host.orEmpty().lowercase()}\${uri.path.orEmpty()}".trimEnd('/')
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
            if (!response.isSuccessful) error("Research HTTP \${response.code}")
            return response.body?.string().orEmpty()
        }
    }
}
