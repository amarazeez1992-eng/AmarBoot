package com.personal.gridbot.amaros.core

/**
 * وضع التشغيل يمنع خلط المحاكاة مع التداول الحقيقي.
 * LIVE موجود كعقد معماري فقط؛ لا يتم تفعيل تداول حقيقي في هذه المرحلة.
 */
enum class AmarOperatingMode {
    SIMULATION,
    DEMO,
    READ_ONLY,
    LIVE,
    EMERGENCY
}
