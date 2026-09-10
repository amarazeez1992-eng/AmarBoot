package com.personal.gridbot.amaros.accounts

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AmarAccountRepository(context: Context) {
    private val preferences = context.getSharedPreferences("amar_accounts", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val type = object : TypeToken<List<AmarAccountProfile>>() {}.type

    fun list(): List<AmarAccountProfile> =
        gson.fromJson<List<AmarAccountProfile>>(preferences.getString("profiles", "[]"), type) ?: emptyList()

    fun save(profile: AmarAccountProfile) {
        val next = list().filterNot { it.id == profile.id } + profile
        preferences.edit().putString("profiles", gson.toJson(next)).apply()
    }

    fun setEnabled(accountId: String, enabled: Boolean) {
        save(list().firstOrNull { it.id == accountId }?.copy(enabled = enabled) ?: return)
    }

    fun remove(accountId: String) {
        preferences.edit().putString("profiles", gson.toJson(list().filterNot { it.id == accountId })).apply()
    }
}
