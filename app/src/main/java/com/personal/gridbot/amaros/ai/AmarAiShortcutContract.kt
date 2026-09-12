package com.personal.gridbot.amaros.ai

/**
 * Central contract for AI advisory shortcuts.
 * UI layers may present these actions, while the agent remains the execution boundary.
 */
object AmarAiShortcutContract {
    const val MARKET_ANALYSIS = "حلل حالة السوق الحالية اعتمادًا على بيانات AmarMarketStateStore، واذكر جودة البيانات والمخاطر ولا تخترع أي قيمة."
    const val STRATEGY_TEST = "صمم اقتراح اختبار حتمي للاستراتيجية الحالية باستخدام B21/B22، واذكر خطوات الاختبار والنتيجة المتوقعة، ولا تدّعِ أن الاختبار تم تشغيله."
    const val LIBRARY_SEARCH = "ابحث في مكتبة عمار عن الموارد والاستراتيجيات المناسبة للطلب الحالي، ثم لخص أفضل الخيارات مع سبب الاختيار."
}
