package com.personal.gridbot.amaros.agent

/**
 * Canonical question-to-evidence admission gate.
 *
 * This gate runs before source authority/consensus processing and again before
 * final synthesis. Authority proves trustworthiness; this class proves that
 * the evidence is about the question actually asked.
 */
class AmarRetrievalRelevanceEngine {

    data class ScoredResult(
        val score: Double,
        val matchedTerms: Set<String>,
        val questionForm: QuestionForm,
        val requiredFacetsSatisfied: Boolean
    )

    enum class QuestionForm {
        CAPITAL, AGE, QUANTITY, CURRENT_VALUE, DEFINITION, WHO, WHEN, WHY, HOW, YES_NO, OPEN
    }

    fun score(question: String, title: String, excerpt: String): ScoredResult {
        val q = normalize(question)
        val titleText = normalize(title)
        val bodyText = normalize(excerpt)
        val questionTokens = tokenize(q)
        val evidenceTokens = tokenize("$titleText $bodyText")

        if (questionTokens.isEmpty() || evidenceTokens.isEmpty()) {
            return ScoredResult(0.0, emptySet(), QuestionForm.OPEN, false)
        }

        val form = detectQuestionForm(q)
        val requiredFacets = requiredFacets(form, questionTokens)
        val satisfied = requiredFacets.all { facet ->
            facet.any { it in evidenceTokens }
        }

        val matched = questionTokens.filter { it in evidenceTokens }.toSet()
        val lexicalCoverage = matched.size.toDouble() / questionTokens.size.toDouble()
        val titleTokens = tokenize(titleText)
        val titleCoverage = matched.count { it in titleTokens }.toDouble() /
            questionTokens.size.toDouble()
        val facetCoverage = if (requiredFacets.isEmpty()) 1.0 else
            requiredFacets.count { facet -> facet.any { it in evidenceTokens } }.toDouble() /
                requiredFacets.size.toDouble()

        val entityAnchor = entityAnchorSatisfied(questionTokens, evidenceTokens)
        val phrase = normalizedPhraseMatch(q, titleText) || normalizedPhraseMatch(q, bodyText)

        val score = (
            lexicalCoverage * 0.30 +
                titleCoverage * 0.15 +
                facetCoverage * 0.30 +
                if (entityAnchor) 0.15 else 0.0 +
                if (phrase) 0.10 else 0.0
            ).coerceIn(0.0, 1.0)

        val admitted = satisfied && entityAnchor && score >= MIN_RELEVANCE_SCORE
        return ScoredResult(
            score = if (admitted) score else 0.0,
            matchedTerms = matched,
            questionForm = form,
            requiredFacetsSatisfied = satisfied && entityAnchor
        )
    }

    fun accept(question: String, title: String, excerpt: String): Boolean =
        score(question, title, excerpt).score >= MIN_RELEVANCE_SCORE

    private fun detectQuestionForm(q: String): QuestionForm = when {
        containsAny(q, "عاصمة", "عاصمه", "capital") -> QuestionForm.CAPITAL
        containsAny(q, "سعر", "price", "الان", "حاليا", "today", "current", "latest") ->
            QuestionForm.CURRENT_VALUE
        containsAny(q, "عمر", "age", "born", "birth", "مواليد") -> QuestionForm.AGE
        containsAny(q, "كم", "عدد", "number", "count", "how many", "how much") ->
            QuestionForm.QUANTITY
        containsAny(q, "من هو", "من هي", "who") -> QuestionForm.WHO
        containsAny(q, "متى", "when") -> QuestionForm.WHEN
        containsAny(q, "لماذا", "ليش", "why") -> QuestionForm.WHY
        containsAny(q, "كيف", "شلون", "how") -> QuestionForm.HOW
        containsAny(q, "هل", "is", "are", "can", "do") -> QuestionForm.YES_NO
        containsAny(q, "ما هو", "ما هي", "ما معنى", "what is", "define") ->
            QuestionForm.DEFINITION
        else -> QuestionForm.OPEN
    }

    private fun requiredFacets(form: QuestionForm, tokens: Set<String>): List<Set<String>> =
        when (form) {
            QuestionForm.CAPITAL -> listOf(
                setOf("capital", "عاصمة", "عاصمه"),
                entityFacet(tokens)
            )
            QuestionForm.AGE -> listOf(
                setOf("age", "born", "birth", "مواليد", "عمر"),
                entityFacet(tokens)
            )
            QuestionForm.QUANTITY -> {
                val groups = mutableListOf<Set<String>>()
                if (containsAny(tokens, "احرف", "الحروف", "الأحرف", "letters", "alphabet")) {
                    groups += setOf("letters", "alphabet", "احرف", "الحروف", "الأحرف")
                }
                if (containsAny(tokens, "عربيه", "العربيه", "العربية", "arabic")) {
                    groups += setOf("arabic", "عربيه", "العربيه", "العربية")
                }
                if (containsAny(tokens, "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english")) {
                    groups += setOf("english", "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية")
                }
                if (groups.isEmpty()) groups += entityFacet(tokens)
                groups
            }
            QuestionForm.CURRENT_VALUE -> listOf(
                setOf("price", "سعر", "current", "latest", "today", "الان", "حاليا"),
                entityFacet(tokens)
            )
            else -> listOf(entityFacet(tokens))
        }.filter { it.isNotEmpty() }

    private fun entityFacet(tokens: Set<String>): Set<String> {
        val salient = tokens.filter { it.length >= 3 && it !in QUESTION_WORDS }.toSet()
        val result = salient.toMutableSet()

        when {
            salient.any { it in setOf("امريكا", "america", "usa") } ->
                result += setOf("امريكا", "america", "usa", "united", "states")
            salient.any { it in setOf("عربيه", "العربيه", "العربية", "arabic") } ->
                result += setOf("عربيه", "العربيه", "العربية", "arabic")
            salient.any { it in setOf("انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english") } ->
                result += setOf("انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english")
        }

        return result
    }

    private fun entityAnchorSatisfied(questionTokens: Set<String>, evidenceTokens: Set<String>): Boolean {
        val salient = questionTokens.filter { it.length >= 3 && it !in QUESTION_WORDS }
        if (salient.isEmpty()) return false

        if (salient.any { it in evidenceTokens }) return true

        if (salient.any { it in setOf("امريكا", "america", "usa") }) {
            return evidenceTokens.any { it in setOf("america", "usa", "united", "states") }
        }
        if (salient.any { it in setOf("عربيه", "العربيه", "العربية", "arabic") }) {
            return evidenceTokens.any { it in setOf("arabic", "عربيه", "العربيه", "العربية") }
        }
        if (salient.any { it in setOf("انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية", "english") }) {
            return evidenceTokens.any { it in setOf("english", "انكليزيه", "الانكليزيه", "الإنجليزية", "انجليزية") }
        }

        return false
    }

    private fun tokenize(value: String): Set<String> =
        value.split(Regex("[^\\p{L}\\p{N}]+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in QUESTION_WORDS }
            .toSet()

    private fun normalizedPhraseMatch(question: String, candidate: String): Boolean =
        question.length >= 6 && candidate.contains(question)

    private fun containsAny(text: String, vararg terms: String): Boolean =
        terms.any { text.contains(normalize(it)) }

    private fun containsAny(tokens: Set<String>, vararg terms: String): Boolean =
        terms.any { normalize(it) in tokens }

    private fun normalize(value: String): String =
        value.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('آ', 'ا')
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
