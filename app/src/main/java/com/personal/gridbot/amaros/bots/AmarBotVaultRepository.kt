package com.personal.gridbot.amaros.bots

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.personal.gridbot.amaros.sync.AmarSyncManager
import org.json.JSONArray
import org.json.JSONObject

class AmarBotVaultRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("amar_bot_vault_v1", Context.MODE_PRIVATE)
    init { AmarSyncManager.schedule(appContext) }
    fun load(): List<AmarSavedBot> {
        val raw = prefs.getString(KEY_BOTS, null) ?: return defaultBots()
        return runCatching {
            val array = JSONArray(raw)
            buildList { for (i in 0 until array.length()) runCatching { add(AmarSavedBot.fromJson(array.getJSONObject(i)).normalized()) } }
                .distinctBy { it.botNumber }.sortedBy { it.botNumber }
        }.getOrElse { defaultBots() }
    }
    fun save(bots: List<AmarSavedBot>) = persist(bots)
    fun exportSnapshotJson(): String = JSONArray().apply { load().forEach { put(it.toJson()) } }.toString()
    fun replaceSnapshotJson(snapshot: String): Boolean = runCatching {
        val array = JSONArray(snapshot)
        val normalized = buildList { for (i in 0 until array.length()) runCatching { add(AmarSavedBot.fromJson(array.getJSONObject(i)).normalized()) } }
            .filter { it.botNumber > 0 }.distinctBy { it.botNumber }.sortedBy { it.botNumber }
        val out = JSONArray(); normalized.forEach { out.put(it.toJson()) }
        prefs.edit().putString(KEY_BOTS, out.toString()).commit()
    }.getOrDefault(false)
    fun addBot(name: String? = null): AmarSavedBot { val bots = load().toMutableList(); val next = generateSequence(1) { it + 1 }.first { n -> bots.none { it.botNumber == n } }; val bot = AmarSavedBot(next, name?.trim().takeUnless { it.isNullOrBlank() } ?: "بوت $next"); bots += bot; persist(bots); return bot }
    fun upsertBot(bot: AmarSavedBot): AmarSavedBot { val normalized = bot.normalized(); val bots = load().filterNot { it.botNumber == normalized.botNumber }.toMutableList(); bots += normalized; persist(bots); return normalized }
    fun renameBot(botNumber: Int, name: String): AmarSavedBot? { val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null; return upsertBot(bot.copy(name = name.trim().ifBlank { bot.name })) }
    fun deleteBot(botNumber: Int): Boolean { if (botNumber == 1) return false; val bots = load(); if (bots.none { it.botNumber == botNumber }) return false; persist(bots.filterNot { it.botNumber == botNumber }); return true }
    fun resetBot(botNumber: Int): AmarSavedBot? { val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null; return upsertBot(bot.copy(strategies = emptyList())) }
    fun saveStrategy(botNumber: Int, strategy: AmarSavedStrategy): AmarSavedBot? { if (strategy.number !in 1..10) return null; val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null; return upsertBot(bot.copy(strategies = bot.strategies.filterNot { it.number == strategy.number } + strategy)) }
    fun deleteStrategy(botNumber: Int, strategyNumber: Int): AmarSavedBot? { val bot = load().firstOrNull { it.botNumber == botNumber } ?: return null; return upsertBot(bot.copy(strategies = bot.strategies.filterNot { it.number == strategyNumber })) }
    fun resetStrategy(botNumber: Int, strategyNumber: Int): AmarSavedBot? = deleteStrategy(botNumber, strategyNumber)
    fun isStrategySaved(botNumber: Int, strategyNumber: Int): Boolean = load().firstOrNull { it.botNumber == botNumber }?.strategies?.any { it.number == strategyNumber } == true
    private fun persist(bots: List<AmarSavedBot>) { val array = JSONArray(); bots.map { it.normalized() }.filter { it.botNumber > 0 }.distinctBy { it.botNumber }.sortedBy { it.botNumber }.forEach { array.put(it.toJson()) }; prefs.edit().putString(KEY_BOTS, array.toString()).commit(); AmarSyncManager.requestNow(appContext) }
    private fun defaultBots(): List<AmarSavedBot> = (1..10).map { AmarSavedBot(it, "بوت $it") }
    companion object { private const val KEY_BOTS = "bots" }
}

data class AmarSavedBot(val botNumber: Int, val name: String, val strategies: List<AmarSavedStrategy> = emptyList()) {
    fun normalized(): AmarSavedBot = copy(botNumber = botNumber.coerceAtLeast(1), name = name.ifBlank { "بوت ${botNumber.coerceAtLeast(1)}" }, strategies = strategies.filter { it.number in 1..10 }.distinctBy { it.number }.sortedBy { it.number })
    fun toJson(): JSONObject = JSONObject().apply { put("botNumber", botNumber); put("name", name); put("strategies", JSONArray().apply { strategies.forEach { put(it.toJson()) } }) }
    companion object { fun fromJson(o: JSONObject): AmarSavedBot { val s = o.optJSONArray("strategies") ?: JSONArray(); val strategies = buildList { for (i in 0 until s.length()) runCatching { add(AmarSavedStrategy.fromJson(s.getJSONObject(i))) } }; return AmarSavedBot(o.optInt("botNumber"), o.optString("name", "بوت"), strategies) } }
}

data class AmarSavedStrategy(val number: Int, val name: String, val profile: AmarBot1RuntimeConfig, val riskProfile: String = "قياسي", val rebuildRule: String = "يدوي", val entryRule: String = "أساسي", val metadata: String = "") {
    fun toJsonString(): String = Gson().toJson(toJsonObject())
    fun toJson(): JSONObject = JSONObject(toJsonString())
    private fun toJsonObject(): JsonObject = JsonObject().apply { addProperty("schemaVersion", 2); addProperty("number", number); addProperty("name", name); addProperty("risk", riskProfile); addProperty("rebuildRule", rebuildRule); addProperty("entryRule", entryRule); addProperty("metadata", metadata); addProperty("lot", profile.lot); addProperty("step", profile.gridStep); addProperty("max", profile.maxOrders); addProperty("multiplier", profile.multiplier); addProperty("tp", profile.basketTp); addProperty("sl", profile.basketSl); addProperty("trailing", profile.trailing); addProperty("buy", profile.buyEnabled); addProperty("sell", profile.sellEnabled) }
    companion object {
        fun fromJsonString(json: String): AmarSavedStrategy = runCatching {
            val o = JsonParser.parseString(json).asJsonObject
            fromGson(o)
        }.getOrElse { AmarSavedStrategy(1, "استراتيجية", AmarBot1RuntimeConfig()) }
        fun fromJson(o: JSONObject): AmarSavedStrategy = runCatching { fromGson(JsonParser.parseString(o.toString()).asJsonObject) }.getOrElse { AmarSavedStrategy(1, "استراتيجية", AmarBot1RuntimeConfig()) }
        private fun fromGson(o: JsonObject): AmarSavedStrategy {
            fun positive(key: String, fallback: Double): Double { val v = o.get(key)?.takeUnless { it.isJsonNull }?.asDouble ?: fallback; return if (v.isFinite() && v > 0.0) v else fallback }
            fun finite(key: String, fallback: Double): Double { val v = o.get(key)?.takeUnless { it.isJsonNull }?.asDouble ?: fallback; return if (v.isFinite()) v else fallback }
            val lot = positive("lot", 0.01); val step = positive("step", 30.0); val max = (o.get("max")?.takeUnless { it.isJsonNull }?.asInt ?: 10).coerceAtLeast(1); val multiplier = positive("multiplier", 2.0)
            val tp = finite("tp", 50.0); val sl = finite("sl", -30.0); val trailing = finite("trailing", 0.0).coerceAtLeast(0.0)
            val profile = runCatching { AmarBot1RuntimeConfig(lot, step, max, multiplier, tp, sl, trailing, o.get("buy")?.asBoolean ?: true, o.get("sell")?.asBoolean ?: true) }.getOrElse { AmarBot1RuntimeConfig() }
            return AmarSavedStrategy(
                number = (o.get("number")?.takeUnless { it.isJsonNull }?.asInt ?: 1).coerceIn(1, 10),
                name = (o.get("name")?.takeUnless { it.isJsonNull }?.asString ?: "استراتيجية").ifBlank { "استراتيجية" },
                profile = profile,
                riskProfile = (o.get("risk")?.takeUnless { it.isJsonNull }?.asString ?: "قياسي").ifBlank { "قياسي" },
                rebuildRule = (o.get("rebuildRule")?.takeUnless { it.isJsonNull }?.asString ?: "يدوي").ifBlank { "يدوي" },
                entryRule = (o.get("entryRule")?.takeUnless { it.isJsonNull }?.asString ?: "أساسي").ifBlank { "أساسي" },
                metadata = o.get("metadata")?.takeUnless { it.isJsonNull }?.asString ?: ""
            )
        }
    }
}
