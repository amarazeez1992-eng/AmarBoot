package com.personal.gridbot.amaros.agent

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarRetrievalRelevanceEngineTest {

    private val engine = AmarRetrievalRelevanceEngine()

    @Test
    fun capital_question_rejects_unrelated_person() {
        assertFalse(engine.accept(
            "ما عاصمة امريكا",
            "Emma Goldman",
            "Biography and historical activism in the United States."
        ))
    }

    @Test
    fun capital_question_accepts_matching_capital_evidence() {
        assertTrue(engine.accept(
            "ما عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        ))
    }

    @Test
    fun iraq_capital_question_rejects_estonia() {
        assertFalse(engine.accept(
            "ما عاصمة العراق",
            "Estonia",
            "Estonia is a country in Northern Europe."
        ))
    }

    @Test
    fun letter_question_rejects_unrelated_country() {
        assertFalse(engine.accept(
            "كم عدد الاحرف العربية والانكليزية",
            "Estonia",
            "Estonia is a country in Northern Europe."
        ))
    }

    @Test
    fun letter_question_requires_all_requested_facets() {
        assertFalse(engine.accept(
            "كم عدد الاحرف العربية والانكليزية",
            "Arabic alphabet",
            "The Arabic alphabet contains letters used in Arabic writing."
        ))
    }

    @Test
    fun age_question_accepts_matching_person_and_age_facet() {
        assertTrue(engine.accept(
            "كم عمر الفنانة شيرين",
            "شيرين عبد الوهاب — السيرة الذاتية",
            "شيرين عبد الوهاب مغنية مصرية، ولدت عام 1980."
        ))
    }

    @Test
    fun age_question_rejects_person_page_without_age_facet() {
        assertFalse(engine.accept(
            "كم عمر الفنانة شيرين",
            "شيرين عبد الوهاب — ألبومات وأعمال",
            "شيرين عبد الوهاب مغنية مصرية لها أعمال فنية متعددة."
        ))
    }

    @Test
    fun current_value_requires_entity_and_current_value_facet() {
        assertFalse(engine.accept(
            "ما سعر الذهب الآن",
            "تاريخ الذهب",
            "Gold has been used as money for thousands of years."
        ))
    }
}
