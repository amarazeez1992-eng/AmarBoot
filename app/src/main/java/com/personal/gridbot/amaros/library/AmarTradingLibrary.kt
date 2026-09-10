package com.personal.gridbot.amaros.library

import java.util.UUID

/** Future in-app catalog for bots, indicators and later strategy packages. */
enum class AmarLibraryItemType { BOT, INDICATOR, STRATEGY, PACKAGE }

data class AmarLibraryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: AmarLibraryItemType,
    val version: String = "1.0.0",
    val description: String = "",
    val enabled: Boolean = false,
    val metadata: Map<String, String> = emptyMap()
)

interface AmarTradingLibraryRepository {
    suspend fun upsert(item: AmarLibraryItem)
    suspend fun get(id: String): AmarLibraryItem?
    suspend fun list(type: AmarLibraryItemType? = null): List<AmarLibraryItem>
    suspend fun delete(id: String): Boolean
}

class InMemoryAmarTradingLibraryRepository : AmarTradingLibraryRepository {
    private val items = LinkedHashMap<String, AmarLibraryItem>()
    override suspend fun upsert(item: AmarLibraryItem) = synchronized(this) { items[item.id] = item }
    override suspend fun get(id: String): AmarLibraryItem? = synchronized(this) { items[id] }
    override suspend fun list(type: AmarLibraryItemType?): List<AmarLibraryItem> = synchronized(this) { items.values.filter { type == null || it.type == type } }
    override suspend fun delete(id: String): Boolean = synchronized(this) { items.remove(id) != null }
}
