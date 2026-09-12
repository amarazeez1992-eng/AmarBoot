package com.personal.gridbot.amaros.agent

/**
 * Local-first RAG engine. The deterministic lexical path works without external services.
 * Embeddings/vector databases can be attached later through provider-neutral interfaces.
 */
class AmarRagEngine {
    private val chunks = mutableListOf<AmarRagChunk>()

    fun index(
        documentId: String,
        text: String,
        source: String,
        chunkSize: Int = 800,
        publisher: String = "",
        sourceType: AmarSourceType = AmarSourceType.UNKNOWN,
        license: String? = null
    ) {
        require(documentId.isNotBlank())
        require(source.isNotBlank())
        require(chunkSize >= 128)
        text.chunked(chunkSize).forEachIndexed { index, chunk ->
            if (chunk.isNotBlank()) {
                chunks += AmarRagChunk(
                    id = "$documentId:$index",
                    documentId = documentId,
                    text = chunk,
                    source = source,
                    publisher = publisher,
                    sourceType = sourceType,
                    license = license,
                    retrievedAtEpochMs = System.currentTimeMillis()
                )
            }
        }
    }

    fun retrieve(query: String, limit: Int = 8): List<AmarRagResult> {
        if (query.isBlank() || limit <= 0) return emptyList()
        val terms = query.lowercase().split(Regex("\\W+")).filter { it.length >= 2 }.toSet()
        return chunks.asSequence().map { chunk ->
            val haystack = "${chunk.text} ${chunk.publisher} ${chunk.source}".lowercase()
            val score = terms.count(haystack::contains).toDouble() / terms.size.coerceAtLeast(1)
            AmarRagResult(chunk, score)
        }.filter { it.score > 0.0 }
            .distinctBy { it.chunk.source + "|" + it.chunk.documentId }
            .sortedByDescending { it.score }
            .take(limit)
            .toList()
    }
}

data class AmarRagChunk(
    val id: String,
    val documentId: String,
    val text: String,
    val source: String,
    val publisher: String = "",
    val sourceType: AmarSourceType = AmarSourceType.UNKNOWN,
    val license: String? = null,
    val retrievedAtEpochMs: Long = System.currentTimeMillis()
)

data class AmarRagResult(val chunk: AmarRagChunk, val score: Double)

enum class AmarSourceType {
    OFFICIAL_API, MARKET_DATA, NEWS, SOCIAL, COMMUNITY, RESEARCH, DOCUMENT, WEB, UNKNOWN
}

interface AmarEmbeddingProvider { suspend fun embed(text: String): FloatArray }
interface AmarReranker {
    suspend fun rerank(query: String, candidates: List<AmarRagResult>): List<AmarRagResult>
}
