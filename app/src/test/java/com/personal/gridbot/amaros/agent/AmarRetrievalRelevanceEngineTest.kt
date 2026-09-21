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
        assertFalse(result.score >= AmarRetrievalRelevanceEngine.MIN_RELEVANCE_SCORE)
    }

    @Test
    fun relevant_capital_result_is_accepted() {
        val result = engine.score(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        )
        assertTrue(engine.accept(
            "اريد معرفة عاصمة امريكا",
            "Washington, D.C. — Capital of the United States",
            "Washington, D.C. is the capital city of the United States."
        ))
        assertEquals(null, result.rejectionReason)
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
            "Sherine — age and biography",
            "Sherine was born in 1980 and is an Egyptian singer."
        )
        assertTrue(engine.accept(
            "كم عمر الفنانة شيرين",
            "Sherine — age and biography",
            "Sherine was born in 1980 and is an Egyptian singer."
        ))
        assertEquals(null, result.rejectionReason)
    }

    @Test
    fun capital_question_form_matches_capital_evidence() {
        val r = engine.score("ما عاصمة امريكا", "Washington capital", "Capital of the United States.")
        assertTrue(r.formMatched)
    }

    @Test
    fun capital_question_rejects_age_evidence_by_form() {
        val r = engine.score("ما عاصمة امريكا", "America", "America was born in a historical account.")
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.QUESTION_FORM_MISMATCH, r.rejectionReason)
    }

    @Test
    fun age_question_form_matches_age_evidence() {
        val r = engine.score("كم عمر شيرين", "Sherine age", "Age and biography.")
        assertTrue(r.formMatched)
    }

    @Test
    fun age_question_rejects_capital_evidence_by_form() {
        val r = engine.score("كم عمر شيرين", "Sherine", "The capital city is discussed here.")
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.QUESTION_FORM_MISMATCH, r.rejectionReason)
    }

    @Test
    fun quantity_question_passes_quantity_facet() {
        val r = engine.score("كم عدد الاحرف", "Number of letters", "The count is 26.")
        assertTrue(r.facetsMatched)
    }

    @Test
    fun current_value_question_rejects_when_value_facet_is_missing() {
        val r = engine.score("السعر الحالي للذهب", "السعر الذهب current", "Current gold information.")
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, r.rejectionReason)
    }

    @Test
    fun entity_anchor_matches_exact_entity() {
        val r = engine.score("كم عمر شيرين", "Sherine age", "Sherine was born in 1980.")
        assertTrue(r.entityAnchorMatched)
    }

    @Test
    fun entity_anchor_rejects_unrelated_entity() {
        val r = engine.score("كم عمر شيرين", "Adele age", "Adele was born in 1988.")
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.ENTITY_ANCHOR_MISMATCH, r.rejectionReason)
    }

    @Test
    fun entity_anchor_uses_existing_aliases() {
        val r = engine.score("ما عاصمة امريكا", "Capital of the USA", "The capital is Washington.")
        assertTrue(r.entityAnchorMatched)
    }

    @Test
    fun temporal_gate_accepts_current_evidence() {
        val r = engine.score("السعر الحالي للذهب", "Gold current price", "The current price is updated now.")
        assertTrue(r.temporalMatched)
        assertTrue(engine.accept("السعر الحالي للذهب", "Gold current price", "The current price is updated now."))
    }

    @Test
    fun temporal_gate_rejects_non_current_evidence_without_overriding_facet_priority() {
        val r = engine.score("السعر الحالي للذهب", "السعر الذهب price", "Historical gold price data.")
        assertFalse(r.temporalMatched)
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.REQUIRED_FACET_MISSING, r.rejectionReason)
    }

    @Test
    fun scoring_remains_deterministic() {
        val q = "ما عاصمة امريكا"
        val t = "Washington capital of the United States"
        val e = "Washington is the capital of the United States."
        assertEquals(engine.score(q, t, e), engine.score(q, t, e))
    }

    @Test
    fun rejection_reason_priority_prefers_score_threshold() {
        val r = engine.score("كم عمر شيرين", "Unrelated", "Unrelated text.")
        assertEquals(AmarRetrievalRelevanceEngine.RejectionReason.SCORE_BELOW_THRESHOLD, r.rejectionReason)
    }
}
