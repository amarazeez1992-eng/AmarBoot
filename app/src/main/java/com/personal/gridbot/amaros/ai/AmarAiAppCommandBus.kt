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

/** Deterministic local natural-language actions. Values are never guessed. */
object AmarAiActionEngine {
    data class Result(val handled: Boolean, val response: String)
    fun route(text: String): Result {
        val q = text.trim().lowercase()
        if (q.isBlank()) return Result(false, "")
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
            AmarAiAppCommandBus.openRoom(room); return Result(true, "فتحت ${room.titleAr}.")
        }
        if (q.contains("الوضع الداكن") || q.contains("dark mode")) { AmarAiAppCommandBus.setTheme(com.personal.gridbot.ui.theme.AmarThemeMode.DARK); return Result(true,"فعّلت الوضع الداكن.") }
        if (q.contains("الوضع الفاتح") || q.contains("light mode")) { AmarAiAppCommandBus.setTheme(com.personal.gridbot.ui.theme.AmarThemeMode.LIGHT); return Result(true,"فعّلت الوضع الفاتح.") }
        if (q.contains("تحديث التطبيق") || q.contains("حدّث التطبيق") || q.contains("update app")) { AmarAiAppCommandBus.requestUpdate(); return Result(true,"أرسلت طلب التحديث إلى نظام التطبيق.") }
        if (q.contains("الخلفية") || q.contains("background")) { AmarAiAppCommandBus.setBackground("default"); return Result(true,"أرسلت أمر إدارة الخلفية إلى نظام التطبيق.") }
        if (q.contains("اللون") || q.contains("accent color")) { AmarAiAppCommandBus.setAccentColor("default"); return Result(true,"أرسلت أمر إدارة اللون إلى نظام التطبيق.") }
        if (q.contains("حلل السوق") || q.contains("حلل السوق الآن") || q.contains("analyze market")) return Result(true, AmarAiEngineBinding.market())
        if (q.contains("راجع المخاطر") || q.contains("risk gate") || q.contains("تحقق من المخاطر")) return Result(true, AmarAiEngineBinding.riskGate())
        if (q.contains("راجع الاستراتيجية") || q.contains("validate strategy") || q.contains("تحقق من الاستراتيجية")) {
            val values = Regex("[-+]?\\d+(?:\\.\\d+)?").findAll(q).mapNotNull { it.value.toDoubleOrNull() }.toList()
            return Result(true, AmarAiEngineBinding.validate(values))
        }
        if (q.contains("حالة التتبع") || q.contains("tracking status") || q.contains("راقب الصفقات")) return Result(true, "ENGINE_TRACKING|status=PENDING_RUNTIME_QUERY|لا يتم اختلاق بيانات التتبع؛ ستقرأ من MT5 Runtime عند توفره.")
        if (q.contains("الإضاءة") || q.contains("الاضاءة") || q.contains("visual effects") || q.contains("المؤثرات")) {
            val enable = !(q.contains("أوقف") || q.contains("اطف") || q.contains("إيقاف") || q.contains("off"))
            AmarAiAppCommandBus.setVisualEffects(enable); return Result(true, if (enable) "فعّلت المؤثرات والإضاءة البصرية." else "أوقفت المؤثرات والإضاءة البصرية.")
        }
        return Result(false, "")
    }
}