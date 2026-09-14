package com.personal.gridbot.amaros.agent

/** Versioned institutional knowledge vault. It stores discoveries, decisions, opinions and failures. */
enum class AmarKnowledgeRecordType { SOURCE, ENGINE, STRATEGY, INDICATOR, RESEARCH, DECISION, OPINION, ERROR, BENCHMARK, UPDATE }

enum class AmarAdmissionStatus { DISCOVERED, VERIFIED, ADMITTED, REJECTED, DEPRECATED }

data class AmarKnowledgeRecord(
    val id: String,
    val type: AmarKnowledgeRecordType,
    val title: String,
    val version: String,
    val status: AmarAdmissionStatus,
    val sourceId: String?,
    val provenanceFingerprint: String,
    val content: String,
    val confidence: Double,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val tags: Set<String> = emptySet()
) {
    init {
        require(id.isNotBlank() && title.isNotBlank() && version.isNotBlank())
        require(provenanceFingerprint.isNotBlank())
        require(confidence.isFinite() && confidence in 0.0..1.0)
        require(createdAtEpochMs >= 0L && updatedAtEpochMs >= createdAtEpochMs)
    }
}

class AmarKnowledgeVault {
    private val records = linkedMapOf<String, AmarKnowledgeRecord>()

    @Synchronized fun upsert(record: AmarKnowledgeRecord) {
        records[record.id] = record
    }

    @Synchronized fun get(id: String): AmarKnowledgeRecord? = records[id]

    @Synchronized fun all(): List<AmarKnowledgeRecord> = records.values.toList()

    @Synchronized fun byType(type: AmarKnowledgeRecordType): List<AmarKnowledgeRecord> =
        records.values.filter { it.type == type }

    @Synchronized fun failures(): List<AmarKnowledgeRecord> =
        records.values.filter { it.type == AmarKnowledgeRecordType.ERROR || it.status == AmarAdmissionStatus.REJECTED }

    @Synchronized fun search(query: String): List<AmarKnowledgeRecord> {
        val q = query.trim().lowercase()
        require(q.isNotEmpty())
        return records.values.filter {
            it.title.lowercase().contains(q) || it.content.lowercase().contains(q) ||
                it.tags.any { tag -> tag.lowercase().contains(q) }
        }
    }
}
