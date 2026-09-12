package com.personal.gridbot.amaros.ai

/**
 * Local strategy-audit engine. It measures specification quality, not profitability.
 * No score from this class is a forecast of future returns.
 */
object AmarStrategyQualityEngine {
    data class Audit(
        val level: Int,
        val scorePct: Double,
        val strengths: List<String>,
        val missing: List<String>,
        val redFlags: List<String>,
        val verdict: String
    )

    private data class Criterion(val label: String, val weight: Int, val keys: List<String>)

    private val criteria = listOf(
        Criterion("Market/context", 8, listOf("market", "symbol", "asset", "session", "timeframe", "regime")),
        Criterion("Setup/conditions", 10, listOf("setup", "condition", "شرط", "نمط", "pattern")),
        Criterion("Entry", 10, listOf("entry", "دخول", "breakout", "trigger", "إشارة")),
        Criterion("Invalidation/stop", 12, listOf("stop", "sl", "invalidation", "إلغاء", "وقف")),
        Criterion("Target/exit", 10, listOf("target", "tp", "exit", "خروج", "هدف")),
        Criterion("Position sizing", 10, listOf("lot", "size", "risk", "risk %", "حجم", "مخاطرة")),
        Criterion("Execution constraints", 7, listOf("spread", "slippage", "spread limit", "execution", "تنفيذ")),
        Criterion("Regime filter", 6, listOf("trend", "range", "volatility", "regime", "اتجاه", "تذبذب")),
        Criterion("News/session policy", 5, listOf("news", "session", "خبر", "جلسة")),
        Criterion("Failure handling", 6, listOf("failure", "fallback", "cooldown", "fail", "فشل")),
        Criterion("Validation plan", 10, listOf("backtest", "forward", "walk-forward", "oos", "test", "اختبار")),
        Criterion("Overfit/leak controls", 6, listOf("overfit", "leak", "lookahead", "repaint", "تسرب", "إفراط"))
    )

    fun audit(strategy: String): Audit {
        val text = strategy.trim().lowercase()
        if (text.isBlank()) return Audit(1, 0.0, emptyList(), criteria.map { it.label }, listOf("Empty strategy specification"), "غير قابلة للتقييم")

        var weighted = 0
        var max = 0
        val strengths = mutableListOf<String>()
        val missing = mutableListOf<String>()
        criteria.forEach { c ->
            max += c.weight
            val found = c.keys.any { text.contains(it) }
            if (found) {
                weighted += c.weight
                strengths += c.label
            } else missing += c.label
        }

        val redFlags = mutableListOf<String>()
        if (Regex("\\b(100|99|95|90)\\s*%\\b").containsMatchIn(text) || text.contains("مضمون")) redFlags += "Unverified performance claim"
        if (listOf("no stop", "without stop", "بدون وقف", "بدون ستوب").any { text.contains(it) }) redFlags += "No explicit loss boundary"
        if (listOf("always wins", "never loses", "لا يخسر", "مضمون").any { text.contains(it) }) redFlags += "Absolute outcome claim"
        if (listOf("future", "next candle", "شمعة قادمة", "سيعرف").any { text.contains(it) }) redFlags += "Potential look-ahead wording"
        if (listOf("martingale", "مارتينجال", "multiply after loss", "مضاعفة بعد الخسارة").any { text.contains(it) }) redFlags += "Loss-progression risk requires explicit exposure limits"

        val score = if (max == 0) 0.0 else weighted * 100.0 / max
        val level = when {
            score >= 92 && redFlags.isEmpty() -> 7
            score >= 82 && redFlags.size <= 1 -> 6
            score >= 70 -> 5
            score >= 58 -> 4
            score >= 42 -> 3
            score >= 22 -> 2
            else -> 1
        }
        val verdict = when (level) {
            7 -> "مواصفة احترافية قابلة للدخول إلى مرحلة تحقق صارمة"
            6 -> "مواصفة قوية، تحتاج إغلاق فجوات محدودة قبل الاختبار"
            5 -> "جيدة، لكن توجد فجوات تؤثر في قابلية القياس"
            4 -> "متوسطة؛ لا تعتمد قبل استكمال القواعد"
            3 -> "ناقصة؛ تحتاج إعادة صياغة منهجية"
            2 -> "ضعيفة؛ لا تكفي لبناء اختبار موثوق"
            else -> "غير قابلة للاعتماد أو الاختبار بشكل مهني"
        }
        return Audit(level, score, strengths, missing, redFlags, verdict)
    }
}
