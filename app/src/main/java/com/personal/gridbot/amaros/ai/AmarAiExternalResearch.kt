package com.personal.gridbot.amaros.ai

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Keyless public-web research layer. Results are evidence only; it cannot mutate strategies or execute trades.
 * Direct primary-source URLs remain preferred when the research question identifies one.
 */
class AmarAiExternalResearch(
    private val http: OkHttpClient = OkHttpClient()
) {
    data class SourceResult(val source: String, val title: String, val url: String, val excerpt: String)

    suspend fun search(query: String, maxResults: Int = 6): List<SourceResult> = withContext(Dispatchers.IO) {
        require(query.isNotBlank()) { "Research query is required" }
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        val results = mutableListOf<SourceResult>()

        runCatching {
            val url = "https://html.duckduckgo.com/html/?q=$encoded"
            val html = get(url, "text/html")
            results += parseDuckDuckGo(html, maxResults)
        }

        runCatching {
            val url = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&utf8=1&srlimit=$maxResults&srsearch=$encoded"
            val raw = get(url, "application/json")
            val items = JSONObject(raw).getJSONObject("query").getJSONArray("search")
            for (i in 0 until minOf(items.length(), maxResults)) {
                val item = items.getJSONObject(i)
                val title = item.optString("title")
                results += SourceResult("Wikipedia", title, "https://en.wikipedia.org/wiki/${title.replace(' ', '_')}", item.optString("snippet").replace(Regex("<[^>]*>"), ""))
            }
        }

        runCatching {
            val url = "https://api.github.com/search/repositories?q=$encoded&per_page=$maxResults"
            val raw = get(url, "application/json")
            val items = JSONObject(raw).getJSONArray("items")
            for (i in 0 until minOf(items.length(), maxResults)) {
                val item = items.getJSONObject(i)
                results += SourceResult("GitHub", item.optString("full_name"), item.optString("html_url"), item.optString("description"))
            }
        }

        results.distinctBy { "${it.source}|${it.url}" }.take(maxResults * 3)
    }

    private fun parseDuckDuckGo(html: String, limit: Int): List<SourceResult> {
        val pattern = Regex("<a[^>]+class=\\\"result__a\\\"[^>]+href=\\\"([^\\\"]+)\\\"[^>]*>(.*?)</a>", RegexOption.IGNORE_CASE)
        val results = mutableListOf<SourceResult>()
        for (match in pattern.findAll(html).take(limit)) {
            val url = decodeHtml(match.groupValues[1])
            val title = decodeHtml(match.groupValues[2].replace(Regex("<[^>]*>"), ""))
            if (url.startsWith("http")) results += SourceResult("Public Web", title, url, title)
        }
        return results
    }

    private fun decodeHtml(value: String): String = value
        .replace("&amp;", "&")
        .replace("&quot;", "\"")
        .replace("&#x27;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")

    private fun get(url: String, accept: String): String {
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", accept)
            .addHeader("User-Agent", "AmarBoot-PublicResearch/1.0")
            .build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Research HTTP ${response.code}")
            return response.body?.string().orEmpty()
        }
    }
}
