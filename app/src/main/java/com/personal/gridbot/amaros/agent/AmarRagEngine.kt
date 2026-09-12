package com.personal.gridbot.amaros.agent

/** Local-first RAG contracts. Retrieval is lexical and deterministic until an embedding backend is added. */
class AmarRagEngine {
    private val chunks = mutableListOf<AmarRagChunk>()

    fun index(documentId: String, text: String, source: String, chunkSize: Int = 800) {
        require(documentId.isNotBlank())
        require(chunkSize >= 128)
        text.chunked(chunkSize).forEachIndexed { index, chunk ->
            if (chunk.isNotBlank()) chunks += AmarRagChunk("$documentId:$index", documentId, chunk, source)
        }
    }

    fun retrieve(query: String, limit: Int = 8): List<AmarRagResult> {
        if (query.isBlank() || limit <= 0) return emptyList()
        val terms = query.lowercase().split(Regex("\\W+")).filter { it.length >= 2 }.toSet()
        return chunks.map { chunk ->
            val haystack = chunk.text.lowercase()
            val score = terms.count(haystack::contains).toDouble() / terms.size.coerceAtLeast(1)
            AmarRagResult(chunk, score)
        }.filter { it.score > 0.0 }
            .sortedByDescending { it.score }
            .take(limit)
    }
}

data class AmarRagChunk(val id: String, val documentId: String, val text: String, val source: String)
data class AmarRagResult(val chunk: AmarRagChunk, val score: Double)

interface AmarEmbeddingProvider { suspend fun embed(text: String): FloatArray }
interface AmarReranker { suspend fun rerank(query: String, candidates: List<AmarRagResult>): List<AmarRagResult> }
