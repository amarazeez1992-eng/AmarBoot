package com.personal.gridbot.amaros.intelligence.trading

import android.content.Context
import android.content.Intent
import android.net.Uri

/** Opens official research destinations; it never grants browser pages execution authority. */
object AmarTradingResearchBrowser {
    const val CHROME_PACKAGE = "com.android.chrome"

    fun open(context: Context, url: String, preferChrome: Boolean = true) {
        require(url.startsWith("https://")) { "Only HTTPS research URLs are allowed" }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (preferChrome) intent.setPackage(CHROME_PACKAGE)
        runCatching { context.startActivity(intent) }.getOrElse {
            context.startActivity(intent.setPackage(null))
        }
    }

    fun tradingViewSearchUrl(query: String): String = "https://www.tradingview.com/scripts/?q=${Uri.encode(query.trim())}"
    fun luxAlgoLibrarySearchUrl(query: String): String = "https://www.luxalgo.com/library/?q=${Uri.encode(query.trim())}"
    fun googleSearchUrl(query: String): String = "https://www.google.com/search?q=${Uri.encode(query.trim())}"
}
