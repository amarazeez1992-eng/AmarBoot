package com.personal.gridbot.amaros.logging

data class AmarLogEvent(
    val level: AmarLogLevel,
    val tag: String,
    val message: String,
    val epochMs: Long,
    val throwableType: String? = null
)
