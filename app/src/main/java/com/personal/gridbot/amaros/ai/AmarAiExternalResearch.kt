package com.personal.gridbot.amaros.ai

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Keyless external research layer. Sources are explicit, public and independently labelled.
 * It is research evidence only; it cannot mutate strategies or execute trades.
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
            val url = "https://en.wikipedia.org/w/api.php?action=query&list=search&format=json&utf8=1&srlimit=$maxResults&srsearch=$encoded"
            val raw = get(url)
            val items = JSONObject(raw).getJSONObject("query").getJSONArray("search")
            for (i in 0 until minOf(items.length(), maxResults)) {
                val item = items.getJSONObject(i)
                val title = item.optString("title")
                results += SourceResult("Wikipedia", title, "https://en.wikipedia.org/wiki/${title.replace(' ', '_')}", item.optString("snippet").replace(Regex("<[^>]*>"), ""))
            }
        }
        runCatching {
            val url = "https://api.github.com/search/repositories?q=$encoded&per_page=$maxResults"
            val raw = get(url)
            val items = JSONObject(raw).getJSONArray("items")
            for (i in 0 until minOf(items.length(), maxResults)) {
                val item = items.getJSONObject(i)
                results += SourceResult("GitHub", item.optString("full_name"), item.optString("html_url"), item.optString("description"))
            }
        }
        results
    }

    private fun get(url: String): String {
        val request = Request.Builder().url(url).get().addHeader("Accept", "application/json").build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Research HTTP ${response.code}")
            return response.body?.string().orEmpty()
        }
    }
}
