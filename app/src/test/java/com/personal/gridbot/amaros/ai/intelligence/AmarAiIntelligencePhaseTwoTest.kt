package com.personal.gridbot.amaros.ai.intelligence

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmarAiIntelligencePhaseTwoTest {
    @Test
    fun modelManager_recommends_supported_fast_model_within_memory() {
        val models = listOf(
            AmarAiModelSpec("small", "Small", 3.0, "Q4", 4096),
            AmarAiModelSpec("large", "Large", 7.0, "Q4", 8192)
        )
        val manager = AmarAiModelManager(models)
        val result = manager.recommend(
            listOf(
                AmarAiBenchmarkResult("small", 18.0, 120, 1800, true),
                AmarAiBenchmarkResult("large", 30.0, 100, 5200, true)
            ),
            maxMemoryMb = 2000
        )
        assertEquals("small", result?.id)
    }

    @Test
    fun deterministicRuntime_is_streaming_and_key_free() {
        val chunks = AmarAiDeterministicRuntime().generate("test", 20).toList()
        assertTrue(chunks.isNotEmpty())
        assertTrue(chunks.joinToString(" ").contains("LOCAL_FALLBACK"))
    }

    @Test
    fun rag_chunk_retrieve_and_rerank_preserves_citations() {
        val engine = AmarAiRagEngine()
        val chunks = engine.chunk("gold-guide", "gold breakout trend strategy risk management", 2)
        val evidence = engine.retrieve("gold strategy", chunks)
        assertTrue(evidence.isNotEmpty())
        val ranked = engine.rerank(evidence)
        assertNotNull(ranked.first().citation)
        assertTrue(ranked.first().score > 0.0)
    }

    @Test
    fun ontology_resolves_aliases_without_duplicates() {
        val ontology = AmarTradingOntology(
            listOf(AmarAiOntologyTerm("xauusd", "gold", setOf("xauusd", "xau")))
        )
        assertEquals(listOf("xauusd"), ontology.resolve("Gold XAUUSD XAU").map { it.id })
    }

    @Test
    fun evidenceLedger_rejects_unknown_citations() {
        val ledger = AmarAiEvidenceLedger()
        ledger.record(AmarAiEvidence("s1", "c1", 0.5, "s1#0"))
        assertTrue(ledger.requireEvidenceFor(listOf("s1#0")))
        assertTrue(!ledger.requireEvidenceFor(listOf("missing#0")))
    }
}
