package com.personal.gridbot.amaros.library

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Durable repository for the approved in-app catalog of bots, indicators and strategies. */
class SharedPreferencesAmarTradingLibraryRepository(
    context: Context,
    private val prefsName: String = "amar_trading_library_v1"
) : AmarTradingLibraryRepository {
    private val prefs = context.applicationContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<AmarLibraryItem>>() {}.type

    override suspend fun upsert(item: AmarLibraryItem) = synchronized(this) {
        val current = load().associateBy { it.id }.toMutableMap()
        current[item.id] = item
        save(current.values.toList())
    }

    override suspend fun get(id: String): AmarLibraryItem? = synchronized(this) { load().firstOrNull { it.id == id } }

    override suspend fun list(type: AmarLibraryItemType?): List<AmarLibraryItem> = synchronized(this) {
        load().filter { type == null || it.type == type }
    }

    override suspend fun delete(id: String): Boolean = synchronized(this) {
        val current = load().toMutableList()
        val removed = current.removeIf { it.id == id }
        if (removed) save(current)
        removed
    }

    private fun load(): List<AmarLibraryItem> =
        prefs.getString(KEY, null)?.let { gson.fromJson<List<AmarLibraryItem>>(it, type) } ?: emptyList()

    private fun save(items: List<AmarLibraryItem>) {
        prefs.edit().putString(KEY, gson.toJson(items, type)).apply()
    }

    private companion object { const val KEY = "items" }
}
