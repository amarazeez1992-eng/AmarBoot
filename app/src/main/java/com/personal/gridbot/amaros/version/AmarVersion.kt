package com.personal.gridbot.amaros.version

object AmarVersion {
    const val APP_VERSION: String = BuildConfig.VERSION_NAME
    const val SCHEMA_VERSION: Int = 1
    const val CONTRACT_VERSION: Int = 1

    fun isCompatible(schemaVersion: Int, contractVersion: Int): Boolean =
        schemaVersion == SCHEMA_VERSION && contractVersion == CONTRACT_VERSION
}
