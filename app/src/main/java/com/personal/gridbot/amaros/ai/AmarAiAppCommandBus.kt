package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** Canonical application-control bridge. Amar AI Agent is the sole command authority. */
object AmarAiAppCommandBus {
    sealed interface Command {
        data class OpenRoom(val room: AmarRoom) : Command
        data class SetVisualEffects(val enabled: Boolean) : Command
        data class SetTheme(val mode: com.personal.gridbot.ui.theme.AmarThemeMode) : Command
        data class SetBackground(val background: String) : Command
        data class SetAccentColor(val color: String) : Command
        data object RequestUpdate : Command
        data class QueueBotCommand(val botNumber: Int, val command: String) : Command
    }

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 32)
    val commands: SharedFlow<Command> = _commands

    fun openRoom(room: AmarRoom) { _commands.tryEmit(Command.OpenRoom(room)) }
    fun setVisualEffects(enabled: Boolean) { _commands.tryEmit(Command.SetVisualEffects(enabled)) }
    fun setTheme(mode: com.personal.gridbot.ui.theme.AmarThemeMode) { _commands.tryEmit(Command.SetTheme(mode)) }
    fun setBackground(background: String) { _commands.tryEmit(Command.SetBackground(background)) }
    fun setAccentColor(color: String) { _commands.tryEmit(Command.SetAccentColor(color)) }
    fun requestUpdate() { _commands.tryEmit(Command.RequestUpdate) }
    fun queueBotCommand(botNumber: Int, command: String) { _commands.tryEmit(Command.QueueBotCommand(botNumber, command)) }

    fun queueExecutionIntent(intent: AmarAiExecutionOrchestrator.Intent) {
        val validation = AmarAiExecutionOrchestrator.validate(intent)
        if (validation.accepted) queueBotCommand(intent.botNumber, AmarAiExecutionOrchestrator.canonical(intent))
    }
}

/** Deterministic local actions. Trading values are never guessed. */
object AmarAiActionEngine {
    data class Result(val handled: Boolean, val response: String)

    private val numberPattern = Regex("[-+]?\\d+(?:\\.\\d+)?")
    private val botPattern = Regex("(?:البوت|bot)\\s*(\\d+)", RegexOption.IGNORE_CASE)
    /** Accepts natural Arabic word order: "لوت البوت 2 إلى 0.03" as well as "البوت 2 لوت 0.03". */
    private val lotCommandPattern = Regex(
        "(?:لوت\\s+(?:البوت\\s*)?(\\d+)\\s*(?:إلى|الى|to|=)?\\s*(\\d+(?:\\.\\d+)?)|البوت\\s*(\\d+)\\s+لوت\\s*(?:إلى|الى|to|=)?\\s*(\\d+(?:\\.\\d+)?))",
        RegexOption.IGNORE_CASE
    )
    private val lotValuePattern = Regex("(?:لوت|lot)\\s*(?:إلى|الى|to|=)?\\s*(\\d+(?:\\.\\d+)?)", RegexOption.IGNORE_CASE)
    private val symbolPattern = Regex("\\b[A-Za-z]{3,12}\\b")

    fun route(text: String): Result {
        val q = text.trim().lowercase()
        if (q.isBlank()) return Result(false, "")

        parseTradingCommand(q)?.let { return it }

        val room = when {
            q.contains("الإعدادات") || q.contains("settings") -> AmarRoom.SETTINGS
            q.contains("السوق") || q.contains("market") -> AmarRoom.MARKET
            q.contains("الشارت") || q.contains("الرسم") || q.contains("الشمعة") || q.contains("chart") -> AmarRoom.CHART
            q.contains("البوت") || q.contains("bot") -> AmarRoom.BOT_LAB
            q.contains("المخاطر") || q.contains("risk") -> AmarRoom.RISK
            q.contains("الصفقات") || q.contains("الأوامر") || q.contains("positions") -> AmarRoom.POSITIONS
            q.contains("الأداء") || q.contains("performance") -> AmarRoom.PERFORMANCE
            q.contains("التحليل") || q.contains("analysis") -> AmarRoom.ANALYSIS
            q.contains("الاختبار") || q.contains("simulation") -> AmarRoom.TESTING
            q.contains("المكتبة") || q.contains("library") -> AmarRoom.LIBRARY
            q.contains("الأخبار") || q.contains("news") -> AmarRoom.NEWS_SESSIONS
            else -> null
        }
        if (room != null && (q.contains("اذهب") || q.contains("افتح") || q.contains("روح") || q.contains("go") || q.contains("open") || q.contains("اعرض"))) {
            AmarAiAppCommandBus.openRoom(room)
            return Result(true, "فتحت ${room.titleAr}.")
        }
        if (q.contains("الوضع الداكن") || q.contains("dark mode")) {
            AmarAiAppCommandBus.setTheme(com.personal.gridbot.ui.theme.AmarThemeMode.DARK)
            return Result(true, "فعّلت الوضع الداكن.")
        }
        if (q.contains("الوضع الفاتح") || q.contains("light mode")) {
            AmarAiAppCommandBus.setTheme(com.personal.gridbot.ui.theme.AmarThemeMode.LIGHT)
            return Result(true, "فعّلت الوضع الفاتح.")
        }
        if (q.contains("تحديث التطبيق") || q.contains("حدّث التطبيق") || q.contains("update app")) {
            AmarAiAppCommandBus.requestUpdate()
            return Result(true, "أرسلت طلب التحديث إلى نظام التطبيق.")
        }
        if (q.contains("الخلفية") || q.contains("background")) {
            AmarAiAppCommandBus.setBackground("default")
            return Result(true, "أرسلت أمر إدارة الخلفية إلى نظام التطبيق.")
        }
        if (q.contains("اللون") || q.contains("accent color")) {
            AmarAiAppCommandBus.setAccentColor("default")
            return Result(true, "أرسلت أمر إدارة اللون إلى نظام التطبيق.")
        }
        if (q.contains("حلل السوق") || q.contains("حلل السوق الآن") || q.contains("analyze market")) return Result(true, AmarAiEngineBinding.market())
        if (q.contains("راجع المخاطر") || q.contains("risk gate") || q.contains("تحقق من المخاطر")) return Result(true, AmarAiEngineBinding.riskGate())
        if (q.contains("راجع الاستراتيجية") || q.contains("validate strategy") || q.contains("تحقق من الاستراتيجية")) {
            val values = numberPattern.findAll(q).mapNotNull { it.value.toDoubleOrNull() }.toList()
            return Result(true, AmarAiEngineBinding.validate(values))
        }
        if (q.contains("حالة التتبع") || q.contains("tracking status") || q.contains("راقب الصفقات")) {
            return Result(true, "ENGINE_TRACKING|status=PENDING_RUNTIME_QUERY|لا يتم اختلاق بيانات التتبع؛ ستقرأ من MT5 Runtime عند توفره.")
        }
        if (q.contains("الإضاءة") || q.contains("الاضاءة") || q.contains("visual effects") || q.contains("المؤثرات")) {
            val enable = !(q.contains("أوقف") || q.contains("اطف") || q.contains("إيقاف") || q.contains("off"))
            AmarAiAppCommandBus.setVisualEffects(enable)
            return Result(true, if (enable) "فعّلت المؤثرات والإضاءة البصرية." else "أوقفت المؤثرات والإضاءة البصرية.")
        }
        return Result(false, "")
    }

    private fun parseTradingCommand(q: String): Result? {
        if ((q.contains("مستوى اللوت") || q.contains("قيمة اللوت")) && lotValuePattern.find(q) == null) {
            return Result(true, "أعطني القيمة المطلوبة للوت، ولا يتم تخمينها.")
        }

        if (q.contains("لوت") || q.contains("lot")) {
            val match = lotCommandPattern.find(q)
            if (q.contains("ارفع") || q.contains("خفض") || q.contains("غير") || q.contains("غيّر")) {
                if (match == null) return Result(true, "أعطني رقم البوت وقيمة اللوت المطلوبة، ولا يتم تخمين أي قيمة.")
                val bot = match.groupValues[1].ifBlank { match.groupValues[3] }.toIntOrNull()
                val lot = match.groupValues[2].ifBlank { match.groupValues[4] }.toDoubleOrNull()
                if (bot == null || lot == null || lot <= 0.0) return Result(true, "أعطني رقم البوت وقيمة اللوت المطلوبة، ولا يتم تخمين أي قيمة.")
                if (bot !in 1..10) return Result(true, "رقم البوت غير صالح؛ المسموح من 1 إلى 10.")
                val command = "SET_LOT|bot=$bot|lot=$lot"
                AmarAiAppCommandBus.queueBotCommand(bot, command)
                return Result(true, "PENDING_MT5|تم تجهيز تغيير لوت البوت $bot إلى $lot، بانتظار MT5/Bridge.")
            }
        }

        if (q.contains("افتح") && (q.contains("شراء") || q.contains("بيع"))) {
            val side = if (q.contains("شراء")) "BUY" else "SELL"
            val symbol = symbolPattern.find(q)?.value?.uppercase()
            val lot = lotValuePattern.find(q)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
            if (symbol == null || lot == null || lot <= 0.0) return Result(true, "أحتاج الرمز وقيمة اللوت الصريحة قبل تجهيز أمر السوق.")
            val intent = AmarAiExecutionOrchestrator.Intent(type = AmarAiExecutionOrchestrator.Type.OPEN_MARKET, symbol = symbol, side = side, volume = lot)
            val validation = AmarAiExecutionOrchestrator.validate(intent)
            if (!validation.accepted) return Result(true, validation.message)
            AmarAiAppCommandBus.queueExecutionIntent(intent)
            return Result(true, AmarAiExecutionOrchestrator.canonical(intent))
        }

        if (q.contains("عند الخسارة") && q.contains("دولار") && q.contains("أغلق")) {
            val amount = numberPattern.findAll(q).lastOrNull()?.value?.toDoubleOrNull()
            if (amount == null || amount <= 0.0) return Result(true, "أعطني قيمة الخسارة بالدولار.")
            val intent = AmarAiExecutionOrchestrator.Intent(type = AmarAiExecutionOrchestrator.Type.SET_STOP_LOSS, amountUsd = amount)
            AmarAiAppCommandBus.queueExecutionIntent(intent)
            return Result(true, AmarAiExecutionOrchestrator.canonical(intent))
        }

        if (q.contains("عند الربح") && q.contains("دولار")) {
            val amount = numberPattern.findAll(q).lastOrNull()?.value?.toDoubleOrNull()
            if (amount == null || amount <= 0.0) return Result(true, "أعطني قيمة الربح بالدولار.")
            val intent = AmarAiExecutionOrchestrator.Intent(type = AmarAiExecutionOrchestrator.Type.SET_PROFIT_TRIGGER, amountUsd = amount)
            AmarAiAppCommandBus.queueExecutionIntent(intent)
            return Result(true, AmarAiExecutionOrchestrator.canonical(intent))
        }

        if (q.contains("شغل الشبكة") || q.contains("شغّل الشبكة") || q.contains("ابدأ الشبكة")) {
            val intent = AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.START_GRID)
            AmarAiAppCommandBus.queueExecutionIntent(intent)
            return Result(true, AmarAiExecutionOrchestrator.canonical(intent))
        }

        if (q.contains("شغل التتبع") || q.contains("شغّل التتبع") || q.contains("ابدأ التتبع")) {
            val intent = AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.START_TRACKING)
            AmarAiAppCommandBus.queueExecutionIntent(intent)
            return Result(true, AmarAiExecutionOrchestrator.canonical(intent))
        }

        return null
    }
}
