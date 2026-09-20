package com.personal.gridbot.amaros.agent

/**
 * Query-aware retrieval gate.
 *
 * Retrieval relevance is a pre-verification boundary: evidence that does not
 * materially match the user's question must not enter authority/consensus
 * scoring merely because its publisher is trustworthy.
 *
 * This is a deterministic lexical/entity-aware gate, not the final intelligence
 * layer. It is deliberately conservative: when relevance cannot be established,
 * the result is rejected rather than promoted as evidence.
 */
class AmarRetrievalRelevanceEngine {

    data class ScoredResult(
        val score: Double,
        val matchedTerms: Set<String>
    )

    fun score(question: String, title: String, excerpt: String): ScoredResult {
        val expanded = expandTerms(tokenize(question))
        if (expanded.isEmpty()) return ScoredResult(0.0, emptySet())

        val titleTerms = tokenize(title)
        val bodyTerms = tokenize(excerpt)
        val evidenceTerms = titleTerms + bodyTerms

        fun termMatches(term: String, terms: Set<String>): Boolean =
            term == term && (
                term in terms ||
                    aliasesFor(term).any { it in terms }
                )

        val matched = tokensForMatching(question).filter { termMatches(it, evidenceTerms) }.toSet()
        val titleMatched = tokensForMatching(question).filter { termMatches(it, titleTerms) }.toSet()

        val coverage = matched.size.toDouble() / tokensForMatching(question).size.toDouble()
        val titleCoverage = titleMatched.size.toDouble() / tokensForMatching(question).size.toDouble()
        val exactPhrase = normalized(question).let { q ->
            q.length >= 5 && (
                normalized(title).contains(q) ||
                    normalized(excerpt).contains(q)
                )
        }

        val entityTerms = tokensForMatching(question).filter { it.length >= 4 }.toSet()
        val entityMatched = entityTerms.count { termMatches(it, evidenceTerms) }
        val entityCoverage = if (entityTerms.isEmpty()) 0.0 else
            entityMatched.toDouble() / entityTerms.size.toDouble()

        val score = (
            coverage * 0.45 +
                titleCoverage * 0.25 +
                entityCoverage * 0.25 +
                if (exactPhrase) 0.05 else 0.0
            ).coerceIn(0.0, 1.0)

        return ScoredResult(score, matched)
    }

    fun accept(question: String, title: String, excerpt: String): Boolean =
        score(question, title, excerpt).score >= MIN_RELEVANCE_SCORE

    private fun tokensForMatching(question: String): Set<String> =
        tokenize(question)

    private fun aliasesFor(token: String): Set<String> =
        when (token) {
            "عاصمه" -> setOf("capital")
            "امريكا" -> setOf("america", "united", "states", "usa")
            "الفنانه" -> setOf("artist", "actress", "singer")
            "عمر" -> setOf("age", "born", "birth")
            "شيرين" -> setOf("sherine", "sherin")
            "احرف", "الاحرف", "الحروف" -> setOf("letters", "alphabet")
            "انكليزيه", "الانكليزيه", "الانجليزية", "انجليزية" -> setOf("english")
            "عربيه", "العربيه", "العربية" -> setOf("arabic")
            "عدد", "كم" -> setOf("number", "count", "how")
            else -> emptySet()
        }

    private fun expandTerms(tokens: Set<String>): Set<String> {
        val result = tokens.toMutableSet()
        tokens.forEach { token ->
            when (token) {
                "عاصمه" -> result += setOf("capital")
                "عاصمة" -> result += setOf("capital", "عاصمه")
                "امريكا", "أمريكا" -> result += setOf("america", "united", "states", "usa")
                "الفنانه", "الفنانة" -> result += setOf("artist", "actress", "singer")
                "عمر" -> result += setOf("age", "born", "birth")
                "احرف", "الأحرف", "الحروف" -> result += setOf("letters", "alphabet")
                "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية" ->
                    result += setOf("english")
                "عربيه", "العربيه", "العربية" -> result += setOf("arabic")
                "عدد", "كم" -> result += setOf("number", "count", "how")
            }
        }
        return result.filter { it.length >= 2 && it !in STOP_WORDS }.toSet()
    }

    private fun tokenize(value: String): Set<String> =
        normalized(value)
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in STOP_WORDS }
            .toSet()

    private fun normalized(value: String): String =
        value.lowercase()
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace('ة', 'ه')
            .replace('ؤ', 'و')
            .replace('ئ', 'ي')
            .replace(Regex("\\s+"), " ")
            .trim()

    companion object {
        const val MIN_RELEVANCE_SCORE = 0.45

        private val STOP_WORDS = setOf(
            "اريد", "أريد", "معرفه", "معرفة", "عن", "ما", "هو", "هي", "هل",
            "من", "في", "الى", "إلى", "على", "هذا", "هذه", "ذلك", "تلك",
            "the", "a", "an", "is", "are", "of", "to", "in", "on", "what",
            "who", "how", "many", "please", "tell", "me", "about"
        )
    }
}
