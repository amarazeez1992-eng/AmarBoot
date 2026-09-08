package com.personal.gridbot.amaros.navigation

/** غرف AMAR الأساسية. إضافة غرفة جديدة لا تتطلب تعديل منطق التداول. */
enum class AmarRoom(
    val titleAr: String,
    val emoji: String
) {
    COMMAND_CENTER("مركز القيادة", "🏠"),
    MARKET("غرفة السوق", "📈"),
    CHART("غرفة الرسم البياني", "📊"),
    BOT_LAB("مختبر البوتات", "🤖"),
    RISK("غرفة المخاطر", "🛡️"),
    POSITIONS("الصفقات والأوامر", "📋"),
    PERFORMANCE("الأداء", "💎"),
    INDICATORS("المؤشرات", "📐"),
    ANALYSIS("التحليل", "🧠"),
    TESTING("الاختبار والمحاكاة", "🧪"),
    TOOLS("الأدوات", "🧰"),
    ALERTS("التنبيهات", "🔔"),
    LIBRARY("مكتبة AMAR", "🧩"),
    SETTINGS("الإعدادات", "⚙️")
}
