package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRetrievalRelevanceEngineTest {

    private val engine = AmarRetrievalRelevanceEngine()

    @Test
    fun unrelated_result_is_rejected() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Emma Goldman",
            "Biography and historical activism in the United States."
        )
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun relevant_capital_result_is_accepted() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        )
        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun unrelated_country_result_is_rejected_for_letter_question() {
        val result = engine.score(
            "كم عدد الاحرف العربية والانكليزية",
            "Estonia",
            "Estonia is a country in Northern Europe."
        )
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun relevant_age_result_is_accepted() {
        val result = engine.score(
            "كم عمر الفنانة شيرين",
            "شيرين عبد الوهاب — السيرة الذاتية",
            "شيرين عبد الوهاب مغنية مصرية، ولدت عام 1980."
        )
        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun iraq_capital_result_is_accepted() {
        val result = engine.score(
            "ما عاصمة العراق",
            "عاصمة العراق",
            "بغداد هي عاصمة العراق."
        )
        assertTrue(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun mixed_unrelated_evidence_is_rejected() {
        val result = engine.score(
            "ما عاصمة العراق",
            "Estonia",
            "Estonia is a country in Northern Europe."
        )
        assertFalse(engine.accept("ما عاصمة العراق", "Estonia", "Estonia is a country in Northern Europe."))
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }
}
