package com.personal.gridbot.amaros.broker

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

/** Durable device-sequence storage boundary used by the remote-command security layer. */
interface AmarDeviceSequenceStore {
    fun read(deviceId: String): Long
    fun write(deviceId: String, sequence: Long)
}

/** Production store: SharedPreferences survives Android process/application restarts. */
class AmarSharedPreferencesDeviceSequenceStore(
    context: Context,
    private val preferencesName: String = "amar_device_security"
) : AmarDeviceSequenceStore {
    private val preferences = context.applicationContext
        .getSharedPreferences(preferencesName, Context.MODE_PRIVATE)

    override fun read(deviceId: String): Long =
        preferences.getLong(key(deviceId), 0L)

    override fun write(deviceId: String, sequence: Long) {
        require(sequence > 0L) { "AMAR device sequence must be positive" }
        check(preferences.edit().putLong(key(deviceId), sequence).commit()) {
            "AMAR device sequence persistence failed"
        }
    }

    private fun key(deviceId: String): String =
        "device_sequence_${deviceId.take(128)}"
}

/** JVM/test-safe store; production should use AmarSharedPreferencesDeviceSequenceStore. */
class AmarInMemoryDeviceSequenceStore : AmarDeviceSequenceStore {
    private val values = ConcurrentHashMap<String, Long>()

    override fun read(deviceId: String): Long = values[deviceId] ?: 0L

    override fun write(deviceId: String, sequence: Long) {
        require(sequence > 0L)
        values[deviceId] = sequence
    }
}
