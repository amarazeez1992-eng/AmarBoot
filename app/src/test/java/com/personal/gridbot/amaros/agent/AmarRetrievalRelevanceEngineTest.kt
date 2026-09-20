package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertEquals
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

        assertFalse(engine.accept(
            "اريد معرفة عاصمة امريكا",
            "Emma Goldman",
            "Biography and historical activism in the United States."
        ))
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun entity_only_match_is_rejected_for_multi_facet_question() {
        assertFalse(
            engine.accept(
                "اريد معرفة عاصمة امريكا",
                "United States",
                "Country profile and geography of the United States."
            )
        )
    }

    @Test
    fun relevant_capital_result_is_accepted() {
        assertTrue(
            engine.accept(
                "اريد معرفة عاصمة امريكا",
                "Washington, D.C. — Capital of the United States",
                "Washington, D.C. is the capital city of the United States."
            )
        )
    }

    @Test
    fun unrelated_country_result_is_rejected_for_letter_question() {
        assertFalse(
            engine.accept(
                "كم عدد الاحرف العربية والانكليزية",
                "Estonia",
                "Estonia is a country in Northern Europe."
            )
        )
    }

    @Test
    fun relevant_age_result_is_accepted() {
        assertTrue(
            engine.accept(
                "كم عمر الفنانة شيرين",
                "Sherine — age and biography",
                "Sherine was born in 1980 and is an Egyptian singer."
            )
        )
    }

    @Test
    fun determinism_is_stable_across_repeated_runs() {
        val question = "كم عمر الفنانة شيرين"
        val title = "Sherine — age and biography"
        val excerpt = "Sherine was born in 1980 and is an Egyptian singer."

        val first = engine.score(question, title, excerpt)
        repeat(10) {
            assertEquals(first, engine.score(question, title, excerpt))
        }
    }
}
