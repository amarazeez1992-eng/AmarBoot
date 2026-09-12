package com.personal.gridbot.amaros.intelligence.advanced

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Durable decision memory with explicit human-approval state. */
class AmarDecisionMemoryRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    data class Record(
        val id: String,
        val decision: String,
        val context: String,
        val outcome: String?,
        val approved: Boolean,
        val timestamp: Long
    )

    fun remember(decision: String, context: String, outcome: String? = null, approved: Boolean = false): Record {
        require(decision.isNotBlank())
        val now = System.currentTimeMillis()
        val record = Record("DM-$now-${decision.hashCode().toUInt().toString(16)}", decision.trim(), context.trim(), outcome?.trim(), approved, now)
        val records = list().toMutableList().apply { add(record) }.takeLast(1000)
        prefs.edit().putString(KEY, encode(records)).commit()
        return record
    }

    fun list(limit: Int = 100): List<Record> = listInternal().takeLast(limit.coerceAtLeast(1))

    fun search(query: String, limit: Int = 50): List<Record> = listInternal()
        .filter { query.isBlank() || it.decision.contains(query, true) || it.context.contains(query, true) || it.outcome.orEmpty().contains(query, true) }
        .takeLast(limit.coerceAtLeast(1))

    private fun listInternal(): List<Record> = runCatching {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        buildList {
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                add(Record(o.optString("id"), o.optString("decision"), o.optString("context"), o.optString("outcome").ifBlank { null }, o.optBoolean("approved"), o.optLong("timestamp")))
            }
        }
    }.getOrDefault(emptyList())

    private fun list(): List<Record> = listInternal()

    private fun encode(records: List<Record>): String = JSONArray().apply {
        records.forEach { r -> put(JSONObject().put("id", r.id).put("decision", r.decision).put("context", r.context).put("outcome", r.outcome).put("approved", r.approved).put("timestamp", r.timestamp)) }
    }.toString()

    companion object {
        private const val PREFS = "amar_ai_decision_memory_v1"
        private const val KEY = "records"
    }
}
