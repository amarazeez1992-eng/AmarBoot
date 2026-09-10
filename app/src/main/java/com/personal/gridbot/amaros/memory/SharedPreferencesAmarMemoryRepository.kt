package com.personal.gridbot.amaros.memory

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Durable Android-backed implementation of the B13 repository contract. */
class SharedPreferencesAmarMemoryRepository(
    context: Context,
    private val prefsName: String = "amar_memory_v1"
) : AmarMemoryRepository {
    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<AmarMemoryRecord>>() {}.type

    override suspend fun put(record: AmarMemoryRecord) = synchronized(this) {
        val current = load().associateBy { compositeKey(it.namespace, it.key) }.toMutableMap()
        current[compositeKey(record.namespace, record.key)] = record
        save(current.values.toList())
    }

    override suspend fun get(namespace: String, key: String): AmarMemoryRecord? = synchronized(this) {
        load().firstOrNull { it.namespace == namespace && it.key == key }
    }

    override suspend fun query(namespace: String, tags: Set<String>, limit: Int): List<AmarMemoryRecord> = synchronized(this) {
        load().asSequence()
            .filter { it.namespace == namespace }
            .filter { tags.isEmpty() || tags.all(it.tags::contains) }
            .sortedByDescending { it.importance }
            .take(limit.coerceIn(1, 500))
            .toList()
    }

    override suspend fun delete(namespace: String, key: String): Boolean = synchronized(this) {
        val current = load().toMutableList()
        val removed = current.removeIf { it.namespace == namespace && it.key == key }
        if (removed) save(current)
        removed
    }

    override suspend fun clear(namespace: String?) = synchronized(this) {
        if (namespace == null) save(emptyList())
        else save(load().filterNot { it.namespace == namespace })
    }

    private fun load(): List<AmarMemoryRecord> =
        prefs.getString(KEY, null)?.let { gson.fromJson<List<AmarMemoryRecord>>(it, type) } ?: emptyList()

    private fun save(records: List<AmarMemoryRecord>) {
        prefs.edit().putString(KEY, gson.toJson(records, type)).apply()
    }

    private fun compositeKey(namespace: String, key: String) = "$namespace::$key"

    private companion object { const val KEY = "records" }
}
