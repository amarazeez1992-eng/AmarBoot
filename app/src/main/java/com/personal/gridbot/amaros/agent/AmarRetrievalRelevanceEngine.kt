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
        val questionTerms = tokensForMatching(question)
        if (questionTerms.isEmpty()) return ScoredResult(0.0, emptySet())

        val titleTerms = tokenize(title)
        val bodyTerms = tokenize(excerpt)
        val evidenceTerms = titleTerms + bodyTerms

        fun termMatches(term: String, terms: Set<String>): Boolean =
            term in terms || aliasesFor(term).any { it in terms }

        val matched = questionTerms.filter { termMatches(it, evidenceTerms) }.toSet()
        val titleMatched = questionTerms.filter { termMatches(it, titleTerms) }.toSet()

        val coverage = matched.size.toDouble() / questionTerms.size.toDouble()
        val titleCoverage = titleMatched.size.toDouble() / questionTerms.size.toDouble()
        val exactPhrase = normalized(question).let { q ->
            q.length >= 5 &&
                (normalized(title).contains(q) || normalized(excerpt).contains(q))
        }

        val entityTerms = questionTerms.filter { it.length >= 4 }.toSet()
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

    fun accept(question: String, title: String, excerpt: String): Boolean {
        val result = score(question, title, excerpt)
        if (result.score < MIN_RELEVANCE_SCORE) return false

        val questionTerms = tokensForMatching(question)
        if (questionTerms.size <= 1) return result.matchedTerms.size == 1

        // A multi-facet question cannot be admitted merely because one broad
        // entity token matched. At least two independent question terms must
        // be represented in the evidence before source verification.
        return result.matchedTerms.size >= minOf(2, questionTerms.size)
    }

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

    private fun tokenize(value: String): Set<String> =
        normalized(value)
            .split(Regex("[^\p{L}\p{N}]+"))
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
            .replace(Regex("\s+"), " ")
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
