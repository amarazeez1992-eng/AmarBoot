package com.personal.gridbot.amaros.agent

/**
 * Query-aware retrieval gate.
 *
 * Retrieval relevance is a pre-verification boundary: evidence that does not
 * materially match the user's question must not enter authority/consensus
 * scoring merely because its publisher is trustworthy.
 *
 * Matching is measured against the original query tokens. Aliases may satisfy
 * one query token, but an alias expansion never creates additional matched
 * query terms. This prevents one entity alias such as "United States" from
 * inflating relevance for a question that also requires a specific property
 * such as "capital".
 */
class AmarRetrievalRelevanceEngine {

    data class ScoredResult(
        val score: Double,
        val matchedTerms: Set<String>
    )

    fun score(question: String, title: String, excerpt: String): ScoredResult {
        val queryTerms = tokenize(question)
        if (queryTerms.isEmpty()) return ScoredResult(0.0, emptySet())

        val titleTerms = tokenize(title)
        val bodyTerms = tokenize(excerpt)
        val evidenceTerms = titleTerms + bodyTerms

        fun termMatches(term: String, terms: Set<String>): Boolean =
            term in terms || aliasesFor(term).any { it in terms }

        val matched = queryTerms.filter { termMatches(it, evidenceTerms) }.toSet()
        val titleMatched = queryTerms.filter { termMatches(it, titleTerms) }.toSet()

        val coverage = matched.size.toDouble() / queryTerms.size.toDouble()
        val titleCoverage = titleMatched.size.toDouble() / queryTerms.size.toDouble()

        val normalizedQuestion = normalized(question)
        val exactPhrase = normalizedQuestion.length >= 5 &&
            (normalized(title).contains(normalizedQuestion) ||
                normalized(excerpt).contains(normalizedQuestion))

        val entityTerms = queryTerms.filter { it.length >= 4 }.toSet()
        val entityMatched = entityTerms.count { termMatches(it, evidenceTerms) }
        val entityCoverage = if (entityTerms.isEmpty()) 0.0 else {
            entityMatched.toDouble() / entityTerms.size.toDouble()
        }

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
