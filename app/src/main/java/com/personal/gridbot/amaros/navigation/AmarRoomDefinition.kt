package com.personal.gridbot.amaros.navigation

/**
 * تعريف قابل للتوسعة لمحتوى الغرفة.
 * هذا الملف يصف الهيكل فقط؛ لا يحتوي على منطق تداول.
 * NEWS_SESSIONS جزء مستقل من كتالوج الغرف حتى تبقى إضافة الأخبار معزولة.
 */
data class AmarRoomSection(
    val id: String,
    val titleAr: String,
    val icon: String,
    val descriptionAr: String
)

data class AmarRoomDefinition(
    val room: AmarRoom,
    val subtitleAr: String,
    val sections: List<AmarRoomSection>
)

object AmarRoomCatalog {
    fun definition(room: AmarRoom): AmarRoomDefinition = when (room) {
        AmarRoom.COMMAND_CENTER -> AmarRoomDefinition(room, "الصورة العامة والتحكم السريع", listOf(
            AmarRoomSection("overview", "نظرة عامة", "◈", "الحالة العامة للنظام"),
            AmarRoomSection("quick", "التحكم السريع", "⚡", "الأوامر والإجراءات السريعة"),
            AmarRoomSection("activity", "النشاط", "◉", "آخر الأحداث وحالة الوحدات")
        ))
        AmarRoom.MARKET -> AmarRoomDefinition(room, "السوق والمراقبة", listOf(
            AmarRoomSection("watchlist", "قائمة المراقبة", "★", "الأصول التي تتابعها"),
            AmarRoomSection("quotes", "الأسعار", "↕", "Bid / Ask / Spread"),
            AmarRoomSection("sessions", "الجلسات", "◷", "حالة جلسات السوق"),
            AmarRoomSection("market-state", "حالة السوق", "◎", "الاتجاه والقوة والتذبذب")
        ))
        AmarRoom.CHART -> AmarRoomDefinition(room, "الرسم والتحليل البصري", listOf(
            AmarRoomSection("chart", "الرسم البياني", "▥", "شموع وفريمات وتحكم بصري"),
            AmarRoomSection("orders", "الصفقات على الرسم", "↔", "دخول وTP وSL والأوامر المعلقة"),
            AmarRoomSection("drawings", "أدوات الرسم", "✎", "خطوط وقياسات ومناطق"),
            AmarRoomSection("layout", "تخطيط الشاشة", "▦", "تخطيط متعدد الرسوم")
        ))
        AmarRoom.BOT_LAB -> AmarRoomDefinition(room, "البوت الحالي والتحكم به", listOf(
            AmarRoomSection("overview", "الحالة", "◉", "حالة البوت والأرقام الأساسية"),
            AmarRoomSection("grid", "إعدادات الشبكة", "▦", "إعدادات Grid الحالية"),
            AmarRoomSection("trade", "الصفقة الفردية", "↗", "TP وSL والصفقة"),
            AmarRoomSection("basket", "السلة", "▤", "TP وSL وإدارة السلة"),
            AmarRoomSection("risk", "الحماية", "🛡", "حدود الحماية والإيقاف"),
            AmarRoomSection("trailing", "التتبع المتحرك", "⟿", "إعدادات Trailing"),
            AmarRoomSection("enhancement", "محرك التعزيز", "✦", "إعدادات التعزيز الحالية"),
            AmarRoomSection("rebuild", "إعادة البناء", "↻", "قواعد إعادة بناء الشبكة"),
            AmarRoomSection("manual", "التحكم اليدوي", "☝", "أوامر يدوية آمنة"),
            AmarRoomSection("orders", "الصفقات والأوامر", "☷", "المراكز والأوامر الحالية"),
            AmarRoomSection("performance", "الأداء", "◆", "إحصاءات البوت"),
            AmarRoomSection("simulation", "المحاكاة", "◌", "عرض تجريبي قبل التنفيذ")
        ))
        AmarRoom.RISK -> AmarRoomDefinition(room, "إدارة المخاطر والحماية", listOf(
            AmarRoomSection("limits", "حدود المخاطر", "!", "النسب والحدود القصوى"),
            AmarRoomSection("account", "حماية الحساب", "◉", "الرصيد والهامش والسحب"),
            AmarRoomSection("market", "حماية السوق", "≈", "Spread / Slippage / Sessions"),
            AmarRoomSection("emergency", "الإيقاف الطارئ", "■", "حالة الطوارئ والتحكم")
        ))
        AmarRoom.POSITIONS -> AmarRoomDefinition(room, "إدارة الصفقات والأوامر", listOf(
            AmarRoomSection("open", "الصفقات المفتوحة", "↔", "المراكز الحالية"),
            AmarRoomSection("pending", "الأوامر المعلقة", "⌁", "الأوامر المنتظرة"),
            AmarRoomSection("history", "السجل", "▤", "الصفقات السابقة"),
            AmarRoomSection("actions", "الإجراءات", "⚡", "إغلاق وإدارة المراكز")
        ))
        AmarRoom.PERFORMANCE -> AmarRoomDefinition(room, "قياس الأداء", listOf(
            AmarRoomSection("periods", "الفترات", "◷", "يومي / أسبوعي / شهري / سنوي"),
            AmarRoomSection("metrics", "المؤشرات", "◆", "Win Rate / PF / Expectancy"),
            AmarRoomSection("equity", "منحنى رأس المال", "⌁", "Equity Curve"),
            AmarRoomSection("drawdown", "السحب", "↓", "Drawdown وRecovery")
        ))
        AmarRoom.INDICATORS -> AmarRoomDefinition(room, "مكتبة المؤشرات", listOf(
            AmarRoomSection("trend", "الاتجاه", "↗", "EMA / SMA / Donchian"),
            AmarRoomSection("momentum", "الزخم", "⚡", "RSI / MACD / CCI / Stochastic"),
            AmarRoomSection("volatility", "التذبذب", "≈", "ATR / Bollinger"),
            AmarRoomSection("structure", "هيكل السوق", "⌁", "BOS / CHoCH / FVG / OB")
        ))
        AmarRoom.ANALYSIS -> AmarRoomDefinition(room, "محرك التحليل", listOf(
            AmarRoomSection("trend", "الاتجاه", "↗", "تحليل الاتجاه"),
            AmarRoomSection("momentum", "الزخم", "⚡", "تحليل الزخم"),
            AmarRoomSection("structure", "الهيكل", "⌁", "بنية السوق والسيولة"),
            AmarRoomSection("regime", "نظام السوق", "◎", "Market Regime"),
            AmarRoomSection("score", "AMAR Score", "✦", "النتيجة التجميعية")
        ))
        AmarRoom.TESTING -> AmarRoomDefinition(room, "الاختبار والمحاكاة", listOf(
            AmarRoomSection("backtest", "اختبار خلفي", "◀", "Backtest"),
            AmarRoomSection("forward", "اختبار أمامي", "▶", "Forward Test"),
            AmarRoomSection("optimization", "تحسين الإعدادات", "✦", "Parameter Testing"),
            AmarRoomSection("monte-carlo", "مونت كارلو", "∿", "تحليل احتمالي"),
            AmarRoomSection("results", "النتائج", "◆", "الإحصاءات والمنحنيات")
        ))
        AmarRoom.TOOLS -> AmarRoomDefinition(room, "أدوات التداول", listOf(
            AmarRoomSection("position-size", "حجم الصفقة", "▣", "حساب الحجم المناسب"),
            AmarRoomSection("risk", "حاسبة المخاطر", "!", "المخاطرة والنسبة"),
            AmarRoomSection("grid", "حاسبة الشبكة", "▦", "الشبكة والتدرج"),
            AmarRoomSection("profit", "حاسبة الربح", "◆", "TP / SL / Profit"),
            AmarRoomSection("sessions", "ساعة الجلسات", "◷", "مواعيد السوق")
        ))
        AmarRoom.ALERTS -> AmarRoomDefinition(room, "التنبيهات والأحداث", listOf(
            AmarRoomSection("active", "التنبيهات الحالية", "●", "التنبيهات النشطة"),
            AmarRoomSection("rules", "قواعد التنبيه", "⚙", "ما الذي يطلق التنبيه"),
            AmarRoomSection("history", "سجل التنبيهات", "▤", "الأحداث السابقة")
        ))
        AmarRoom.LIBRARY -> AmarRoomDefinition(room, "مكتبة AMAR والإضافات", listOf(
            AmarRoomSection("indicators", "المؤشرات", "📐", "مكونات المؤشرات"),
            AmarRoomSection("strategies", "الاستراتيجيات", "◆", "مكونات الاستراتيجيات"),
            AmarRoomSection("bots", "البوتات الحالية", "🤖", "الوحدات الموجودة فقط"),
            AmarRoomSection("extensions", "الإضافات", "🧩", "نظام الإضافات المستقبلي")
        ))
        AmarRoom.ACCOUNTS -> AmarRoomDefinition(room, "الحسابات والاتصال", listOf(
            AmarRoomSection("accounts", "حسابات التداول", "👤", "حفظ وإدارة حسابات التداول"),
            AmarRoomSection("security", "حماية بيانات الدخول", "🛡", "تخزين آمن للبيانات الحساسة"),
            AmarRoomSection("connection", "حالة الاتصال", "↔", "حالة موصل الوسيط مستقبلاً")
        ))
        AmarRoom.NEWS_SESSIONS -> AmarRoomDefinition(room, "الأخبار والسوق والسيولة والتقرير اليومي", listOf(
            AmarRoomSection("news", "A — الأخبار", "◉", "الأخبار والتقويم الاقتصادي"),
            AmarRoomSection("sessions", "B — السوق والسيولة", "◷", "الجلسات والتداخلات والنشاط"),
            AmarRoomSection("daily", "C — التقرير اليومي", "◆", "OHLC والفجوة وتحليل الجلسات")
        ))
        AmarRoom.SETTINGS -> AmarRoomDefinition(room, "إعدادات وتجهيز AMAR", listOf(
            AmarRoomSection("appearance", "المظهر", "✦", "الألوان والثيمات والحركة"),
            AmarRoomSection("workspace", "مساحة العمل", "▦", "التخطيط والتنقل"),
            AmarRoomSection("demo", "الوضع التجريبي", "◉", "إعدادات Demo"),
            AmarRoomSection("security", "الأمان", "🛡", "الصلاحيات والحماية"),
            AmarRoomSection("advanced", "متقدم", "⚙", "إعدادات النظام المتقدمة")
        ))
    }
}
