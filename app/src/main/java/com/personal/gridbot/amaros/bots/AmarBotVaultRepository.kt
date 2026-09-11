package com.personal.gridbot.amaros.bots

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persistent configuration vault. It stores profiles only; it never authorizes live execution. */
class AmarBotVaultRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("amar_bot_vault_v1", Context.MODE_PRIVATE)

    fun load(): List<AmarSavedBot> {
        val raw = prefs.getString(KEY_BOTS, null) ?: return defaultBots()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) add(AmarSavedBot.fromJson(array.getJSONObject(i)).normalized())
            }.distinctBy { it.botNumber }.sortedBy { it.botNumber }
        }.getOrElse { defaultBots() }
    }

    fun save(bots: List<AmarSavedBot>) {
        persist(bots)
    }

    /** Adds a configuration slot only; it never creates a trading engine. */
    fun addBot(name: String? = null): AmarSavedBot {
        val bots = load().toMutableList()
        val next = generateSequence(1) { it + 1 }.first { n -> bots.none { it.botNumber == n } }
        val bot = AmarSavedBot(next, name?.trim().takeUnless { it.isNullOrBlank() } ?: "بوت $next")
        bots += bot
        persist(bots)
        return bot
    }

    fun upsertBot(bot: AmarSavedBot): AmarSavedBot {
        val normalized = bot.normalized()
        val bots = load().filterNot { it.botNumber == normalized.botNumber }.toMutableList()
        bots += normalized
        persist(bots)
        return normalized
    }

    fun renameBot(botNumber: Int, name: String): AmarSavedBot? {
        val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null
        return upsertBot(bot.copy(name = name.trim().ifBlank { bot.name }))
    }

    /** Deletes only the saved configuration slot. Live MT5 state is untouched. */
    fun deleteBot(botNumber: Int): Boolean {
        if (botNumber == 1) return false
        val bots = load()
        if (bots.none { it.botNumber == botNumber }) return false
        persist(bots.filterNot { it.botNumber == botNumber })
        return true
    }

    /** Clears a saved bot configuration without touching MT5. */
    fun resetBot(botNumber: Int): AmarSavedBot? {
        val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null
        return upsertBot(bot.copy(strategies = emptyList()))
    }

    fun saveStrategy(botNumber: Int, strategy: AmarSavedStrategy): AmarSavedBot? {
        if (strategy.number !in 1..10) return null
        val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null
        return upsertBot(bot.copy(strategies = bot.strategies.filterNot { it.number == strategy.number } + strategy))
    }

    fun deleteStrategy(botNumber: Int, strategyNumber: Int): AmarSavedBot? {
        val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null
        return upsertBot(bot.copy(strategies = bot.strategies.filterNot { it.number == strategyNumber }))
    }

    fun resetStrategy(botNumber: Int, strategyNumber: Int): AmarSavedBot? = deleteStrategy(botNumber, strategyNumber)

    fun isStrategySaved(botNumber: Int, strategyNumber: Int): Boolean =
        load().firstOrNull { it.botNumber == botNumber }?.strategies?.any { it.number == strategyNumber } == true

    private fun persist(bots: List<AmarSavedBot>) {
        val array = JSONArray()
        bots.map { it.normalized() }
            .filter { it.botNumber > 0 }
            .distinctBy { it.botNumber }
            .sortedBy { it.botNumber }
            .forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_BOTS, array.toString()).apply()
    }

    private fun defaultBots(): List<AmarSavedBot> = (1..4).map { AmarSavedBot(it, "بوت $it") }

    companion object { private const val KEY_BOTS = "bots" }
}

data class AmarSavedBot(
    val botNumber: Int,
    val name: String,
    val strategies: List<AmarSavedStrategy> = emptyList()
) {
    fun normalized(): AmarSavedBot = copy(
        botNumber = botNumber.coerceAtLeast(1),
        name = name.ifBlank { "بوت ${botNumber.coerceAtLeast(1)}" },
        strategies = strategies.filter { it.number in 1..10 }.distinctBy { it.number }.sortedBy { it.number }
    )

    fun toJson(): JSONObject = JSONObject().apply {
        put("botNumber", botNumber)
        put("name", name)
        put("strategies", JSONArray().apply { strategies.forEach { put(it.toJson()) } })
    }

    companion object {
        fun fromJson(o: JSONObject): AmarSavedBot {
            val s = o.optJSONArray("strategies") ?: JSONArray()
            val strategies = buildList {
                for (i in 0 until s.length()) add(AmarSavedStrategy.fromJson(s.getJSONObject(i)))
            }
            return AmarSavedBot(o.optInt("botNumber"), o.optString("name", "بوت"), strategies)
        }
    }
}

data class AmarSavedStrategy(
    val number: Int,
    val name: String,
    val profile: AmarBot1RuntimeConfig,
    val riskProfile: String = "قياسي",
    val rebuildRule: String = "يدوي",
    val entryRule: String = "أساسي",
    val metadata: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("number", number); put("name", name); put("risk", riskProfile)
        put("rebuildRule", rebuildRule); put("entryRule", entryRule); put("metadata", metadata)
        put("lot", profile.lot); put("step", profile.gridStep); put("max", profile.maxOrders)
        put("multiplier", profile.multiplier); put("tp", profile.basketTp); put("sl", profile.basketSl)
        put("trailing", profile.trailing); put("buy", profile.buyEnabled); put("sell", profile.sellEnabled)
    }

    companion object {
        fun fromJson(o: JSONObject): AmarSavedStrategy = AmarSavedStrategy(
            number = o.optInt("number"), name = o.optString("name", "استراتيجية"),
            profile = AmarBot1RuntimeConfig(
                lot = o.optDouble("lot", 0.01), gridStep = o.optDouble("step", 30.0),
                maxOrders = o.optInt("max", 10), multiplier = o.optDouble("multiplier", 2.0),
                basketTp = o.optDouble("tp", 50.0), basketSl = o.optDouble("sl", -30.0),
                trailing = o.optDouble("trailing", 0.0), buyEnabled = o.optBoolean("buy", true),
                sellEnabled = o.optBoolean("sell", true)
            ),
            riskProfile = o.optString("risk", "قياسي"),
            rebuildRule = o.optString("rebuildRule", "يدوي"),
            entryRule = o.optString("entryRule", "أساسي"),
            metadata = o.optString("metadata", "")
        )
    }
}
