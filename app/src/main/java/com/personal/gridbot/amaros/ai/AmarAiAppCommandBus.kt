package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** UI/application command bridge. AI can request navigation and visual changes without owning broker execution. */
object AmarAiAppCommandBus {
    sealed interface Command {
        data class OpenRoom(val room: AmarRoom) : Command
        data class SetVisualEffects(val enabled: Boolean) : Command
        data class QueueBotCommand(val botNumber: Int, val command: String) : Command
    }

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 32)
    val commands: SharedFlow<Command> = _commands

    fun openRoom(room: AmarRoom) { _commands.tryEmit(Command.OpenRoom(room)) }
    fun setVisualEffects(enabled: Boolean) { _commands.tryEmit(Command.SetVisualEffects(enabled)) }
    fun queueBotCommand(botNumber: Int, command: String) { _commands.tryEmit(Command.QueueBotCommand(botNumber, command)) }
}

/** Natural-language local command router. It performs only deterministic, reversible app actions. */
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
            AmarAiAppCommandBus.openRoom(room)
            return Result(true, "فتحت ${room.titleAr}.")
        }

        if (q.contains("الإضاءة") || q.contains("الاضاءة") || q.contains("visual effects") || q.contains("المؤثرات")) {
            val enable = !(q.contains("أوقف") || q.contains("اطف") || q.contains("إيقاف") || q.contains("off"))
            AmarAiAppCommandBus.setVisualEffects(enable)
            return Result(true, if (enable) "فعّلت المؤثرات والإضاءة البصرية." else "أوقفت المؤثرات والإضاءة البصرية.")
        }

        val botMatch = Regex("(?:بوت|bot)\\s*(\\d+)").find(q)
        val explicitLot = Regex("(?:lot|لوت)\\s*(?:إلى|الى|to)?\\s*(0?\\.\\d+|\\d+(?:\\.\\d+)?)").find(q)
        val targetLot = explicitLot?.groupValues?.getOrNull(1) ?: Regex("(?:إلى|الى|to)\\s*(0?\\.\\d+|\\d+(?:\\.\\d+)?)").find(q)?.groupValues?.getOrNull(1)
        if (targetLot != null && (q.contains("لوت") || q.contains("lot"))) {
            val bot = botMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
            AmarAiAppCommandBus.queueBotCommand(bot, "SET_LOT:$targetLot")
            return Result(true, "سجلت أمر تغيير اللوت للبوت $bot إلى $targetLot، والحالة ستبقى PENDING_MT5 حتى يؤكد الجسر التنفيذ.")
        }
        if (q.contains("ارفع مستوى اللوت") || q.contains("ارفع اللوت") || q.contains("ارفع لوت") || q.contains("increase lot")) {
            return Result(true, "أستطيع رفع اللوت، لكن أعطني القيمة المطلوبة مثلاً: لوت 0.02 للبوت 1، ولن أخمّن قيمة مالية.")
        }

        return Result(false, "")
    }
}
