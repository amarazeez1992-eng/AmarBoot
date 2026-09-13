package com.personal.gridbot.amaros.ai.intelligence

import kotlin.math.max

/** Phase 2 provider-neutral intelligence contracts. No broker execution is exposed here. */

data class AmarAiModelSpec(
    val id: String,
    val displayName: String,
    val parameterBillions: Double,
    val quantization: String,
    val contextTokens: Int,
    val localOnly: Boolean = true
) {
    init {
        require(id.isNotBlank())
        require(displayName.isNotBlank())
        require(parameterBillions > 0.0)
        require(contextTokens > 0)
    }
}

data class AmarAiBenchmarkResult(
    val modelId: String,
    val tokensPerSecond: Double,
    val firstTokenMs: Long,
    val memoryMb: Long,
    val supported: Boolean
) {
    init {
        require(modelId.isNotBlank())
        require(tokensPerSecond.isFinite() && tokensPerSecond >= 0.0)
        require(firstTokenMs >= 0)
        require(memoryMb >= 0)
    }
}

class AmarAiModelManager(private val models: List<AmarAiModelSpec>) {
    fun available(): List<AmarAiModelSpec> = models.toList()

    fun recommend(
        benchmark: List<AmarAiBenchmarkResult>,
        maxMemoryMb: Long,
        minTokensPerSecond: Double = 0.0
    ): AmarAiModelSpec? {
        require(maxMemoryMb >= 0)
        require(minTokensPerSecond.isFinite() && minTokensPerSecond >= 0.0)
        val candidates = benchmark
            .filter { it.supported && it.memoryMb <= maxMemoryMb && it.tokensPerSecond >= minTokensPerSecond }
            .sortedWith(compareByDescending<AmarAiBenchmarkResult> { it.tokensPerSecond }
                .thenBy { it.firstTokenMs })
        return candidates.firstOrNull()?.let { result -> models.firstOrNull { it.id == result.modelId } }
    }
}

interface AmarAiLocalRuntime {
    fun generate(prompt: String, maxTokens: Int = 256): Sequence<String>
}

class AmarAiDeterministicRuntime : AmarAiLocalRuntime {
    override fun generate(prompt: String, maxTokens: Int): Sequence<String> {
        require(prompt.isNotBlank())
        require(maxTokens > 0)
        val response = "LOCAL_FALLBACK: analysis requires a configured local model."
        return response.split(" ").asSequence().take(maxTokens)
    }
}

data class AmarAiRagChunk(
    val id: String,
    val sourceId: String,
    val text: String,
    val ordinal: Int
) {
    init {
        require(id.isNotBlank() && sourceId.isNotBlank() && text.isNotBlank())
        require(ordinal >= 0)
    }
}

data class AmarAiEvidence(
    val sourceId: String,
    val chunkId: String,
    val score: Double,
    val citation: String
) {
    init {
        require(sourceId.isNotBlank() && chunkId.isNotBlank())
        require(score.isFinite() && score >= 0.0)
        require(citation.isNotBlank())
    }
}

class AmarAiRagEngine {
    fun chunk(sourceId: String, text: String, chunkSize: Int = 600): List<AmarAiRagChunk> {
        require(sourceId.isNotBlank())
        require(text.isNotBlank())
        require(chunkSize > 0)
        val words = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.chunked(chunkSize).mapIndexed { index, wordsChunk ->
            AmarAiRagChunk("$sourceId:$index", sourceId, wordsChunk.joinToString(" "), index)
        }
    }

    fun retrieve(query: String, chunks: List<AmarAiRagChunk>, limit: Int = 5): List<AmarAiEvidence> {
        require(query.isNotBlank())
        require(limit > 0)
        val terms = query.lowercase().split(Regex("\\W+")).filter { it.length > 1 }.toSet()
        return chunks.map { chunk ->
            val words = chunk.text.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
            val score = if (terms.isEmpty()) 0.0 else terms.intersect(words).size.toDouble() / terms.size
            AmarAiEvidence(chunk.sourceId, chunk.id, score, "${chunk.sourceId}#${chunk.ordinal}")
        }.filter { it.score > 0.0 }
            .sortedByDescending { it.score }
            .take(limit)
    }

    fun rerank(evidence: List<AmarAiEvidence>, limit: Int = 5): List<AmarAiEvidence> {
        require(limit > 0)
        return evidence.sortedWith(compareByDescending<AmarAiEvidence> { it.score }
            .thenBy { it.sourceId }
            .thenBy { it.chunkId }).take(limit)
    }
}

data class AmarAiOntologyTerm(val id: String, val label: String, val aliases: Set<String> = emptySet()) {
    init { require(id.isNotBlank() && label.isNotBlank()) }
}

class AmarTradingOntology(terms: List<AmarAiOntologyTerm>) {
    private val index = terms.flatMap { term ->
        (setOf(term.label) + term.aliases).map { it.lowercase() to term }
    }.toMap()

    fun resolve(text: String): List<AmarAiOntologyTerm> = text.lowercase()
        .split(Regex("\\W+"))
        .mapNotNull { index[it] }
        .distinctBy { it.id }
}

class AmarAiEvidenceLedger {
    private val entries = linkedMapOf<String, AmarAiEvidence>()

    fun record(evidence: AmarAiEvidence) {
        entries[evidence.chunkId] = evidence
    }

    fun all(): List<AmarAiEvidence> = entries.values.toList()

    fun requireEvidenceFor(citations: Collection<String>): Boolean = citations.all { citation ->
        entries.values.any { it.citation == citation }
    }
}

fun recommendBenchmark(results: List<AmarAiBenchmarkResult>): AmarAiBenchmarkResult? = results
    .filter { it.supported && it.tokensPerSecond.isFinite() }
    .maxWithOrNull(compareBy<AmarAiBenchmarkResult> { it.tokensPerSecond }
        .thenBy { -max(0L, it.firstTokenMs) })
