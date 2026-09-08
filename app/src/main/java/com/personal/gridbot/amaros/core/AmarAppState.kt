package com.personal.gridbot.amaros.core

import com.personal.gridbot.amaros.navigation.AmarRoom

/** الحالة العامة للـ Shell فقط؛ منطق التداول يبقى خارج طبقة العرض. */
data class AmarAppState(
    val selectedRoom: AmarRoom = AmarRoom.COMMAND_CENTER,
    val mode: AmarOperatingMode = AmarOperatingMode.DEMO,
    val isConnected: Boolean = false,
    val alertsCount: Int = 0,
    val featureFlags: AmarFeatureFlags = AmarFeatureFlags()
)
