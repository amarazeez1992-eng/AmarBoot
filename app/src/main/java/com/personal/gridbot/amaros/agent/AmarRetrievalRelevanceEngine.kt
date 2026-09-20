package com.personal.gridbot.amaros.agent

/**
 * Canonical question-aware evidence admission gate.
 *
 * Relevance is not source authority. A source may be trustworthy and still be
 * unusable for the user's exact question. This gate therefore models the
 * question form and required semantic facets before evidence can be admitted.
 */
class AmarRetrievalRelevanceEngine {

    data class ScoredResult(
        val score: Double,
        val matchedTerms: Set<String>,
        val questionForm: QuestionForm,
        val requiredFacetsSatisfied: Boolean
    )

    enum class QuestionForm { CAPITAL, AGE, QUANTITY, CURRENT_VALUE, DEFINITION, WHO, WHEN, WHY, HOW, YES_NO, OPEN }

    fun score(question: String, title: String, excerpt: String): ScoredResult {
        val q = normalize(question)
        val titleText = normalize(title)
        val bodyText = normalize(excerpt)
        val qTokens = tokenize(q)
        if (qTokens.isEmpty() || (titleText.isBlank() && bodyText.isBlank())) {
            return ScoredResult(0.0, emptySet(), QuestionForm.OPEN, false)
        }

        val form = questionForm(q)
        val titleTokens = tokenize(titleText)
        val bodyTokens = tokenize(bodyText)
        val evidenceTokens = titleTokens + bodyTokens
        val matched = qTokens.intersect(evidenceTokens)
        val lexicalCoverage = matched.size.toDouble() / qTokens.size.toDouble()

        val semanticGroups = semanticGroups(form, qTokens)
        val satisfied = semanticGroups.all { group -> group.any { it in evidenceTokens } }
        val groupCoverage = if (semanticGroups.isEmpty()) 1.0
        else semanticGroups.count { group -> group.any { it in evidenceTokens } }.toDouble() / semanticGroups.size

        val titleCoverage = if (qTokens.isEmpty()) 0.0
        else qTokens.count { it in titleTokens }.toDouble() / qTokens.size

        val phrase = normalizedPhraseMatch(q, titleText) || normalizedPhraseMatch(q, bodyText)
        val entityAnchor = entityAnchorSatisfied(qTokens, evidenceTokens)

        // Required semantic facets are a hard admission boundary. Lexical overlap
        // alone can never promote an unrelated page.
        val score = (
            lexicalCoverage * 0.30 +
                titleCoverage * 0.20 +
                groupCoverage * 0.25 +
                if (entityAnchor) 0.15 else 0.0 +
                if (phrase) 0.10 else 0.0
            ).coerceIn(0.0, 1.0)

        val accepted = satisfied && entityAnchor && score >= MIN_RELEVANCE_SCORE
        return ScoredResult(if (accepted) score else 0.0, matched, form, satisfied && entityAnchor)
    }

    fun accept(question: String, title: String, excerpt: String): Boolean =
        score(question, title, excerpt).score >= MIN_RELEVANCE_SCORE

    private fun questionForm(q: String): QuestionForm = when {
        containsAny(q, "عاصمه", "عاصمة", "capital") -> QuestionForm.CAPITAL
        containsAny(q, "عمر", "age", "born", "birth") -> QuestionForm.AGE
        containsAny(q, "كم", "عدد", "number", "count", "how many", "how much") -> QuestionForm.QUANTITY
        containsAny(q, "سعر", "price", "الان", "حاليا", "today", "current", "latest") -> QuestionForm.CURRENT_VALUE
        containsAny(q, "من هو", "من هي", "who") -> QuestionForm.WHO
        containsAny(q, "متى", "when") -> QuestionForm.WHEN
        containsAny(q, "لماذا", "ليش", "why") -> QuestionForm.WHY
        containsAny(q, "كيف", "شلون", "how") -> QuestionForm.HOW
        containsAny(q, "هل", "is", "are", "can", "do") -> QuestionForm.YES_NO
        containsAny(q, "ما هو", "ما هي", "ما معنى", "what is", "define") -> QuestionForm.DEFINITION
        else -> QuestionForm.OPEN
    }

    private fun semanticGroups(form: QuestionForm, tokens: Set<String>): List<Set<String>> = when (form) {
        QuestionForm.CAPITAL -> listOf(
            setOf("capital", "عاصمه", "عاصمة"),
            tokens.filter { it.length >= 4 && it !in QUESTION_WORDS }.toSet()
        )
        QuestionForm.AGE -> listOf(
            setOf("age", "born", "birth", "مواليد", "عمر"),
            tokens.filter { it.length >= 4 && it !in QUESTION_WORDS && it !in setOf("فنانه", "فنانة") }.toSet()
        )
        QuestionForm.QUANTITY -> {
            val groups = mutableListOf<Set<String>>()
            if (tokens.any { it in setOf("احرف", "الحروف", "الأحرف", "letters", "alphabet") }) {
                groups += setOf("letters", "alphabet", "احرف", "الحروف", "الأحرف")
            }
            if (tokens.any { it in setOf("عربيه", "العربيه", "العربية", "arabic") }) {
                groups += setOf("arabic", "عربيه", "العربيه", "العربية")
            }
            if (tokens.any { it in setOf("انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english") }) {
                groups += setOf("english", "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية")
            }
            if (groups.isEmpty()) groups += tokens.filter { it.length >= 4 && it !in QUESTION_WORDS }.toSet()
            groups
        }
        QuestionForm.CURRENT_VALUE -> listOf(
            setOf("price", "سعر", "current", "latest", "today", "الان", "حاليا"),
            tokens.filter { it.length >= 4 && it !in QUESTION_WORDS }.toSet()
        )
        else -> listOf(tokens.filter { it.length >= 4 && it !in QUESTION_WORDS }.toSet())
    }.filter { it.isNotEmpty() }

    private fun entityAnchorSatisfied(questionTokens: Set<String>, evidenceTokens: Set<String>): Boolean {
        val salient = questionTokens.filter { it.length >= 4 && it !in QUESTION_WORDS }
        if (salient.isEmpty()) return false
        return salient.any { it in evidenceTokens } ||
            (salient.any { it in setOf("امريكا", "america", "usa") } &&
                evidenceTokens.any { it in setOf("united", "states", "america", "usa") }) ||
            (salient.any { it in setOf("عربيه", "العربيه", "العربية", "arabic") } &&
                evidenceTokens.any { it in setOf("arabic", "عربيه", "العربيه", "العربية") }) ||
            (salient.any { it in setOf("انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english") } &&
                evidenceTokens.any { it in setOf("english", "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية") })
    }

    private fun tokenize(value: String): Set<String> =
        value.split(Regex("[^\\p{L}\\p{N}]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in QUESTION_WORDS }
            .toSet()

    private fun normalizedPhraseMatch(q: String, candidate: String): Boolean =
        q.length >= 6 && candidate.contains(q)

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(normalize(it)) }

    private fun normalize(value: String): String =
        value.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ى', 'ي')
            .replace(Regex("\\s+"), " ")
            .trim()

    companion object {
        const val MIN_RELEVANCE_SCORE = 0.60
        private val QUESTION_WORDS = setOf(
            "اريد", "أريد", "معرفه", "معرفة", "عن", "ما", "هو", "هي", "هل",
            "من", "في", "الى", "إلى", "على", "هذا", "هذه", "ذلك", "تلك",
            "كم", "كيف", "متى", "لماذا", "ليش", "the", "a", "an", "is",
            "are", "of", "to", "in", "on", "what", "who", "how", "many",
            "please", "tell", "me", "about"
        )
    }
}
