package com.personal.gridbot.amaros.agent

/**
 * AMAR-native request understanding layer.
 *
 * This is an offline linguistic parser, not a hosted model. It normalizes
 * Arabic/English text, detects the question form, scores multiple intents,
 * extracts salient entities, and exposes ambiguity instead of guessing.
 */
class AmarIntentUnderstanding {
    fun understand(text: String): AmarIntentAnalysis {
        require(text.isNotBlank())
        val normalized = normalize(text)
        if (isGreeting(normalized)) {
            return AmarIntentAnalysis(
                intent = AgentIntent.GENERAL,
                confidence = 1.0,
                signals = emptyList(),
                questionForm = "GREETING",
                entities = emptyList(),
                ambiguous = false
            )
        }

        val signals = mutableListOf<Pair<AgentIntent, Double>>()
        val trade = score(normalized, TRADE_CONCEPTS)
        val strategy = score(normalized, STRATEGY_CONCEPTS)
        val research = score(normalized, RESEARCH_CONCEPTS)

        if (trade > 0.0) signals += AgentIntent.TRADE_ANALYSIS to trade
        if (strategy > 0.0) signals += AgentIntent.STRATEGY_DESIGN to strategy
        if (research > 0.0) signals += AgentIntent.RESEARCH to research

        val chosen = signals.maxByOrNull { it.second } ?: (AgentIntent.RESEARCH to 0.20)
        val runnerUp = signals.filter { it.first != chosen.first }.maxOfOrNull { it.second } ?: 0.0
        val confidence = (chosen.second / (chosen.second + runnerUp + 0.25)).coerceIn(0.20, 0.98)
        val ambiguous = signals.size > 1 && (chosen.second - runnerUp) < 0.25

        return AmarIntentAnalysis(
            intent = chosen.first,
            confidence = confidence,
            signals = signals.sortedByDescending { it.second }.map { pair ->
                pair.first.name + ":" + String.format(java.util.Locale.US, "%.2f", pair.second)
            },
            questionForm = questionForm(normalized),
            entities = extractEntities(normalized),
            ambiguous = ambiguous
        )
    }

    private fun score(text: String, concepts: Set<String>): Double {
        val hits = concepts.count { text.contains(it) }
        val questionBoost = if (text.contains("?") || text.contains("؟")) 0.10 else 0.0
        return hits * 0.35 + questionBoost
    }

    private fun questionForm(text: String): String = when {
        text.startsWith("لماذا") || text.startsWith("ليش") || text.startsWith("why") -> "WHY"
        text.startsWith("كيف") || text.startsWith("شلون") || text.startsWith("how") -> "HOW"
        text.startsWith("هل") || text.startsWith("is ") || text.startsWith("are ") ||
            text.startsWith("can ") || text.startsWith("do ") -> "YES_NO"
        text.contains("متى") || text.contains("when") -> "WHEN"
        text.contains("كم") || text.contains("how much") || text.contains("how many") -> "QUANTITY"
        else -> "OPEN"
    }

    private fun extractEntities(text: String): List<String> {
        val entities = linkedSetOf<String>()
        val entityConcepts = listOf(
            "ذهب" to "GOLD", "gold" to "GOLD",
            "فوركس" to "FOREX", "forex" to "FOREX",
            "mt5" to "MT5", "mt4" to "MT4",
            "بيتكوين" to "BITCOIN", "bitcoin" to "BITCOIN",
            "دولار" to "USD", "usd" to "USD",
            "يورو" to "EUR", "eur" to "EUR",
            "باوند" to "GBP", "gbp" to "GBP",
            "مؤشر" to "INDICATOR", "indicator" to "INDICATOR",
            "روبوت" to "BOT", "bot" to "BOT"
        )
        entityConcepts.forEach { (needle, value) -> if (text.contains(needle)) entities += value }
        return entities.toList()
    }

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ة', 'ه')
            .replace(Regex("[،؛,:!\\.\\(\\)\\[\\]{}"']"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun isGreeting(text: String): Boolean =
        GREETINGS.any { text == it || text.startsWith("$it ") }

    companion object {
        private val GREETINGS = setOf(
            "هلو", "مرحبا", "السلام عليكم", "اهلا", "اهلين",
            "hello", "hi", "hey"
        )
        private val TRADE_CONCEPTS = setOf(
            "تداول", "صفقه", "mt5", "mt4", "trade", "gold", "ذهب",
            "forex", "فوركس", "سوق", "market", "شراء", "بيع", "سعر"
        )
        private val STRATEGY_CONCEPTS = setOf(
            "استراتيجيه", "strategy", "روبوت", "bot", "خطه", "قواعد",
            "مؤشر", "indicator", "backtest", "اختبار", "كود"
        )
        private val RESEARCH_CONCEPTS = setOf(
            "ابحث", "بحث", "research", "مصادر", "دليل", "دراسه",
            "اشرح", "ما هو", "لماذا", "كيف", "تحقق", "verify",
            "معلومه", "اعطني", "ما معنى", "شرح"
        )
    }
}

data class AmarIntentAnalysis(
    val intent: AgentIntent,
    val confidence: Double,
    val signals: List<String>,
    val questionForm: String,
    val entities: List<String> = emptyList(),
    val ambiguous: Boolean = false
)
