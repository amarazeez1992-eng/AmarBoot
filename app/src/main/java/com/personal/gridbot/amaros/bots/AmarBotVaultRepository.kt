package com.personal.gridbot.amaros.bots

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Persistent configuration vault. It stores profiles only; it never authorizes live execution. */
class AmarBotVaultRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("amar_bot_vault_v1", Context.MODE_PRIVATE)

    fun load(): List<AmarSavedBot> {
        val raw = prefs.getString(KEY_BOTS, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(AmarSavedBot.fromJson(o))
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(bots: List<AmarSavedBot>) {
        val array = JSONArray()
        bots.sortedBy { it.botNumber }.forEach { array.put(it.toJson()) }
        prefs.edit().putString(KEY_BOTS, array.toString()).apply()
    }

    companion object { private const val KEY_BOTS = "bots" }
}

data class AmarSavedBot(
    val botNumber: Int,
    val name: String,
    val strategies: List<AmarSavedStrategy> = emptyList()
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("botNumber", botNumber)
        put("name", name)
        put("strategies", JSONArray().apply { strategies.sortedBy { it.number }.forEach { put(it.toJson()) } })
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
    val riskProfile: String = "قياسي"
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("number", number); put("name", name); put("risk", riskProfile)
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
            ), riskProfile = o.optString("risk", "قياسي")
        )
    }
}
