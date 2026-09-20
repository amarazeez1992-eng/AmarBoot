package com.personal.gridbot.amaros.agent

/**
 * Single candidate-to-answerability boundary.
 * It does not rank sources and does not decide authority.
 */
class AmarQuestionAnswerabilityGate {
    data class Decision(val admitted: Boolean, val reason: String)

    fun decide(question: String, title: String, evidence: String): Decision {
        if (question.isBlank() || title.isBlank() || evidence.isBlank()) {
            return Decision(false, "missing_question_or_candidate_content")
        }
        val profile = profile(question)
        var candidate = tokens(title + " " + evidence).toMutableSet()
        if ("united" in candidate && "states" in candidate) candidate += "america"
        if ("usa" in candidate) candidate += "america"
        if ("sherin" in candidate) candidate += "sherine"
        if ("arabic" in candidate) candidate += "عربيه"
        if ("english" in candidate) candidate += "انكليزيه"

        if (profile.subject.any { it !in candidate }) {
            return Decision(false, "candidate_subject_mismatch")
        }
        if (profile.facets.any { facet -> !facetSatisfied(facet, candidate) }) {
            return Decision(false, "requested_facet_not_answered")
        }
        if (profile.subject.isEmpty() && profile.facets.isEmpty()) {
            return Decision(false, "question_profile_not_answerable")
        }
        return Decision(true, "subject_and_requested_facet_same_candidate")
    }

    private fun facetSatisfied(facet: String, candidate: Set<String>): Boolean = when (facet) {
        "capital" -> "capital" in candidate
        "age" -> "age" in candidate || "born" in candidate || "birth" in candidate
        "letters" -> "letters" in candidate || "alphabet" in candidate
        "number" -> "number" in candidate || "count" in candidate || "how" in candidate
        "when" -> candidate.any { it in setOf("when", "date", "year", "born") }
        "why" -> candidate.any { it in setOf("why", "because", "reason") }
        "how" -> candidate.any { it in setOf("how", "method", "process") }
        else -> facet in candidate
    }

    private fun profile(question: String): Profile {
        val q = tokens(question)
        val facets = linkedSetOf<String>()
        if (q.any { it in QUANTITY_WORDS }) {
            when {
                "عمر" in q -> facets += "age"
                "احرف" in q || "حروف" in q -> facets += "letters"
                else -> facets += "number"
            }
        }
        if (q.any { it in setOf("عاصمه", "capital") }) facets += "capital"
        if (q.any { it in setOf("متى", "when") }) facets += "when"
        if (q.any { it in setOf("لماذا", "ليش", "why") }) facets += "why"
        if (q.any { it in setOf("كيف", "شلون", "how") }) facets += "how"

        val subject = q.filter {
            it !in STOP_WORDS && it !in FACETS && it !in QUANTITY_WORDS &&
                it !in SUBJECT_TYPE_WORDS
        }.map { normalizeAlias(it) }.toSet()

        return Profile(subject, facets)
    }

    private fun tokens(value: String): Set<String> =
        value.lowercase()
            .replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
            .replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
            .replace('ى', 'ي').replace('ة', 'ه')
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.length >= 2 }
            .map { normalizeAlias(it) }
            .toSet()

    private fun normalizeAlias(token: String): String = when (token) {
        "عاصمه", "عاصمة" -> "capital"
        "امريكا", "أمريكا" -> "america"
        "شيرين", "شيرن" -> "sherine"
        "عمر" -> "age"
        "احرف", "الأحرف", "الحروف", "حروف" -> "letters"
        "عربيه", "العربيه", "العربية" -> "arabic"
        "انكليزيه", "الانكليزيه", "الانجليزية", "انجليزية" -> "english"
        else -> token
    }

    private data class Profile(val subject: Set<String>, val facets: Set<String>)

    companion object {
        private val QUANTITY_WORDS = setOf("كم", "عدد", "many", "much", "number", "count")
        private val STOP_WORDS = setOf(
            "ما","ماذا","هو","هي","هل","من","في","عن","الى","إلى","على","مع",
            "the","a","an","is","are","of","to","in","on","what","who","please","tell","me"
        )
        private val SUBJECT_TYPE_WORDS = setOf(
            "فنان","الفنان","فنانة","الفنانه","artist","actress","singer"
        )
        private val FACETS = setOf(
            "capital","عاصمه","عاصمة","age","عمر","born","birth",
            "letters","alphabet","احرف","الأحرف","الحروف","حروف",
            "price","value","rate","time","date","year",
            "reason","why","because","method","process","how"
        )
    }
}
