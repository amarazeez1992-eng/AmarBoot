package com.personal.gridbot.amaros.security

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class AmarProtectionCenterTest {
    @Test
    fun recordFailure_savesModuleErrorTypeAndTimestamp() {
        val preferences = InMemorySharedPreferences()
        val context = object : ContextWrapper(null) {
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = preferences
        }
        val error = IllegalStateException("boom")

        AmarProtectionCenter.recordFailure(context, "test-module", error)

        val saved = AmarProtectionCenter.lastFailure(context)
        val timestamp = AmarProtectionCenter.lastFailureTime(context)
        assertNotNull(saved)
        assertTrue(saved!!.contains("test-module"))
        assertTrue(saved.contains("IllegalStateException"))
        assertNotNull(timestamp)
        Instant.parse(timestamp)
    }

    private class InMemorySharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values.toMutableMap()
        override fun getString(key: String, defValue: String?): String? = values[key] as? String ?: defValue
        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? =
            (values[key] as? Set<String>)?.toMutableSet() ?: defValues
        override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = values[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = values.containsKey(key)
        override fun edit(): SharedPreferences.Editor = Editor()

        private inner class Editor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removals = mutableSetOf<String>()

            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun putStringSet(key: String, value: MutableSet<String>?): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun putInt(key: String, value: Int): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
                pending[key] = value
                return this
            }
            override fun remove(key: String): SharedPreferences.Editor {
                removals.add(key)
                return this
            }
            override fun clear(): SharedPreferences.Editor {
                values.clear()
                pending.clear()
                removals.clear()
                return this
            }
            override fun commit(): Boolean {
                apply()
                return true
            }
            override fun apply() {
                removals.forEach(values::remove)
                values.putAll(pending)
            }
        }

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {}
    }
}
