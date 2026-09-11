package com.personal.gridbot.amaros.sync

import org.json.JSONArray
import org.json.JSONObject

/** Three-way merge for the Bot Vault. Non-overlapping offline edits are preserved. */
object AmarSyncMerge {
    fun merge(base: String, local: String, remote: String): String {
        val b = bots(JSONArray(base))
        val l = bots(JSONArray(local))
        val r = bots(JSONArray(remote))
        val numbers = (b.keys + l.keys + r.keys).toSortedSet()
        val out = JSONArray()
        numbers.forEach { number ->
            val merged = mergeBot(b[number], l[number], r[number])
            if (merged != null) out.put(merged)
        }
        return out.toString()
    }

    private fun mergeBot(base: JSONObject?, local: JSONObject?, remote: JSONObject?): JSONObject? {
        if (local == null && remote == null) return null
        if (base == null) return local ?: remote
        if (same(local, base)) return remote
        if (same(remote, base)) return local
        if (local == null) return remote
        if (remote == null) return local
        val out = JSONObject(remote.toString())
        if (local.optString("name") != base.optString("name")) out.put("name", local.optString("name"))
        val bs = strategies(base); val ls = strategies(local); val rs = strategies(remote)
        val slots = (bs.keys + ls.keys + rs.keys).toSortedSet()
        val mergedStrategies = JSONArray()
        slots.forEach { slot ->
            val m = mergeStrategy(bs[slot], ls[slot], rs[slot])
            if (m != null) mergedStrategies.put(m)
        }
        out.put("strategies", mergedStrategies)
        return out
    }

    private fun mergeStrategy(base: JSONObject?, local: JSONObject?, remote: JSONObject?): JSONObject? {
        if (local == null && remote == null) return null
        if (base == null) return local ?: remote
        if (same(local, base)) return remote
        if (same(remote, base)) return local
        if (local == null) return remote
        if (remote == null) return local
        // Same strategy slot changed on both sides: keep the local edit as the user's latest explicit action.
        return local
    }

    private fun bots(a: JSONArray): Map<Int, JSONObject> = buildMap {
        for (i in 0 until a.length()) {
            val o = a.optJSONObject(i) ?: continue
            put(o.optInt("botNumber", 0), o)
        }
    }

    private fun strategies(bot: JSONObject): Map<Int, JSONObject> {
        val a = bot.optJSONArray("strategies") ?: JSONArray()
        return buildMap {
            for (i in 0 until a.length()) {
                val o = a.optJSONObject(i) ?: continue
                put(o.optInt("number", 0), o)
            }
        }
    }

    private fun same(a: JSONObject?, b: JSONObject?): Boolean = a?.toString() == b?.toString()
}
