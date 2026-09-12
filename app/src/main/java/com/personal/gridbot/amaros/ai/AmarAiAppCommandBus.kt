package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/** UI/application command bridge. AI never owns broker execution. */
object AmarAiAppCommandBus {
    sealed interface Command {
        data class OpenRoom(val room: AmarRoom) : Command
        data class SetVisualEffects(val enabled: Boolean) : Command
        data class QueueBotCommand(val botNumber: Int, val command: String) : Command
        data class QueueExecutionIntent(val intent: AmarAiExecutionOrchestrator.Intent) : Command
    }

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 32)
    val commands: SharedFlow<Command> = _commands

    fun openRoom(room: AmarRoom) { _commands.tryEmit(Command.OpenRoom(room)) }
    fun setVisualEffects(enabled: Boolean) { _commands.tryEmit(Command.SetVisualEffects(enabled)) }
    fun queueBotCommand(botNumber: Int, command: String) { _commands.tryEmit(Command.QueueBotCommand(botNumber, command)) }
    fun queueExecutionIntent(intent: AmarAiExecutionOrchestrator.Intent) { _commands.tryEmit(Command.QueueExecutionIntent(intent)) }
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
            AmarAiAppCommandBus.openRoom(room)
            return Result(true, "فتحت ${room.titleAr}.")
        }

        if (q.contains("الإضاءة") || q.contains("الاضاءة") || q.contains("visual effects") || q.contains("المؤثرات")) {
            val enable = !(q.contains("أوقف") || q.contains("اطف") || q.contains("إيقاف") || q.contains("off"))
            AmarAiAppCommandBus.setVisualEffects(enable)
            return Result(true, if (enable) "فعّلت المؤثرات والإضاءة البصرية." else "أوقفت المؤثرات والإضاءة البصرية.")
        }

        val botMatch = Regex("(?:بوت|bot)\\s*(\\d+)").find(q)
        val bot = botMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val symbol = Regex("(?:على|في|for|on)\\s*([a-z0-9._-]+)").find(q)?.groupValues?.getOrNull(1)?.uppercase()
        val volume = Regex("(?:لوت|lot)\\s*(?:إلى|الى|to)?\\s*(0?\\.\\d+|\\d+(?:\\.\\d+)?)").find(q)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        val usd = Regex("(?:\\$|دولار|usd)\\s*(\\d+(?:\\.\\d+)?)").find(q)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        if ((q.contains("افتح") || q.contains("فتح") || q.contains("open")) && (q.contains("شراء") || q.contains("buy") || q.contains("بيع") || q.contains("sell"))) {
            val side = if (q.contains("شراء") || q.contains("buy")) "BUY" else "SELL"
            if (symbol == null || volume == null) return Result(true, "أحتاج الرمز واللوت صراحةً. مثال: افتح شراء XAUUSD لوت 0.01")
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.OPEN_MARKET, symbol, side, volume, botNumber = bot))
            return Result(true, "جهزت أمر $side على $symbol بحجم $volume. الحالة PENDING_MT5 حتى تصل مرحلة الجسر.")
        }

        if (q.contains("اغلق الكل") || q.contains("أغلق الكل") || q.contains("close all")) {
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.CLOSE_ALL, botNumber = bot))
            return Result(true, "جهزت أمر إغلاق شامل. التنفيذ الفعلي ينتظر MT5/Bridge.")
        }

        if (q.contains("عند الخسارة") || q.contains("stop loss") || q.contains("حد الخسارة")) {
            if (usd == null) return Result(true, "أعطني قيمة الخسارة بالدولار، مثلاً: عند الخسارة 30 دولار أغلق.")
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.SET_STOP_LOSS, amountUsd = usd, botNumber = bot))
            return Result(true, "جهزت حد خسارة ${usd}$ للتنفيذ عبر المحرك عند اكتمال MT5/Bridge.")
        }

        if (q.contains("عند الربح") || q.contains("profit trigger") || q.contains("take profit")) {
            if (usd == null) return Result(true, "أعطني قيمة الربح بالدولار، مثلاً: عند الربح 2 دولار نفذ الأمر التالي.")
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.SET_PROFIT_TRIGGER, amountUsd = usd, botNumber = bot))
            return Result(true, "جهزت شرط ربح ${usd}$ للمحرك. التنفيذ الفعلي ينتظر MT5/Bridge.")
        }

        if (volume != null && (q.contains("لوت") || q.contains("lot"))) {
            AmarAiAppCommandBus.queueBotCommand(bot, "SET_LOT:$volume")
            return Result(true, "سجلت تغيير لوت البوت $bot إلى $volume، والحالة PENDING_MT5.")
        }
        if (q.contains("ارفع مستوى اللوت") || q.contains("ارفع اللوت") || q.contains("ارفع لوت") || q.contains("increase lot")) return Result(true, "أستطيع رفع اللوت، لكن أعطني القيمة المطلوبة ولن أخمّن قيمة مالية.")

        if (q.contains("شغل الشبكة") || q.contains("شغّل الشبكة") || q.contains("start grid")) {
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.START_GRID, botNumber = bot))
            return Result(true, "جهزت تشغيل نظام الشبكة. التنفيذ ينتظر MT5/Bridge.")
        }
        if (q.contains("أوقف الشبكة") || q.contains("اوقف الشبكة") || q.contains("stop grid")) {
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.STOP_GRID, botNumber = bot))
            return Result(true, "جهزت إيقاف نظام الشبكة.")
        }
        if (q.contains("شغل التتبع") || q.contains("شغّل التتبع") || q.contains("start tracking")) {
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.START_TRACKING, botNumber = bot))
            return Result(true, "جهزت تشغيل نظام التتبع.")
        }
        if (q.contains("أوقف التتبع") || q.contains("اوقف التتبع") || q.contains("stop tracking")) {
            AmarAiAppCommandBus.queueExecutionIntent(AmarAiExecutionOrchestrator.Intent(AmarAiExecutionOrchestrator.Type.STOP_TRACKING, botNumber = bot))
            return Result(true, "جهزت إيقاف نظام التتبع.")
        }
        return Result(false, "")
    }
}
