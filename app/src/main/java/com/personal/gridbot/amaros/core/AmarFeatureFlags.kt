package com.personal.gridbot.amaros.core

/** مفاتيح تشغيل مستقلة للوحدات الجديدة. */
data class AmarFeatureFlags(
    val auroraShell: Boolean = true,
    val roomNavigation: Boolean = true,
    val motionEngine: Boolean = true,
    val eventBus: Boolean = true,
    val centralizedState: Boolean = true,
    val liveTrading: Boolean = false
)
