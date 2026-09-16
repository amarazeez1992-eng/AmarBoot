package com.personal.gridbot.amaros.ai

import com.personal.gridbot.amaros.navigation.AmarRoom
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Application command transport owned by the AMAR Agent authority.
 * UI/action parsing can request capabilities, but it does not execute them directly.
 */
object AmarAiAppCommandBus {
    sealed interface Command {
        data class OpenRoom(val room: AmarRoom) : Command
        data class SetVisualEffects(val enabled: Boolean) : Command
        data class QueueBotCommand(val botNumber: Int, val command: String) : Command
    }

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 32)
    val commands: SharedFlow<Command> = _commands

    internal fun emitFromAgent(command: Command): Boolean = _commands.tryEmit(command)

    fun openRoom(room: AmarRoom) = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.OpenRoom(room))

    fun setVisualEffects(enabled: Boolean) = AmarAgentApplicationAuthority.request(
        AmarAgentApplicationAuthority.Request.SetVisualEffects(enabled)
    )

    fun queueBotCommand(botNumber: Int, command: String) = AmarAgentApplicationAuthority.request(
        AmarAgentApplicationAuthority.Request.QueueBotCommand(botNumber, command)
    )

    fun queueExecutionIntent(intent: AmarAiExecutionOrchestrator.Intent) {
        val validation = AmarAiExecutionOrchestrator.validate(intent)
        if (validation.accepted) {
            queueBotCommand(intent.botNumber, AmarAiExecutionOrchestrator.canonical(intent))
        }
    }
}

/**
 * Single application authority for commands originating from the AMAR intelligence layer.
 * Sensitive broker execution remains fail-closed until the explicitly deferred MT5 stage.
 */
object AmarAgentApplicationAuthority {
    var agentEnabled: Boolean = true
        private set
    var allowApplicationControl: Boolean = true
        private set
    var allowBrokerExecution: Boolean = false
        private set

    sealed interface Request {
        data class OpenRoom(val room: AmarRoom) : Request
        data class SetVisualEffects(val enabled: Boolean) : Request
        data class QueueBotCommand(val botNumber: Int, val command: String) : Request
    }

    fun request(request: Request): Boolean {
        if (!agentEnabled || !allowApplicationControl) return false
        return when (request) {
            is Request.OpenRoom -> AmarAiAppCommandBus.emitFromAgent(
                AmarAiAppCommandBus.Command.OpenRoom(request.room)
            )
            is Request.SetVisualEffects -> AmarAiAppCommandBus.emitFromAgent(
                AmarAiAppCommandBus.Command.SetVisualEffects(request.enabled)
            )
            is Request.QueueBotCommand -> {
                if (request.botNumber !in 1..10 || request.command.isBlank()) return false
                AmarAiAppCommandBus.emitFromAgent(
                    AmarAiAppCommandBus.Command.QueueBotCommand(request.botNumber, request.command)
                )
            }
        }
    }

    fun setApplicationControlEnabled(enabled: Boolean) {
        allowApplicationControl = enabled
    }

    fun setBrokerExecutionEnabled(enabled: Boolean) {
        allowBrokerExecution = enabled
    }
}

/** Deterministic local natural-language actions delegated to the Agent application authority. */
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
            return if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.OpenRoom(room))) {
                Result(true, "فتحت ${room.titleAr} عبر سلطة AMAR Agent.")
            } else Result(true, "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        }
        if (q.contains("حلل السوق") || q.contains("حلل السوق الآن") || q.contains("analyze market")) return Result(true, AmarAiEngineBinding.market())
        if (q.contains("راجع المخاطر") || q.contains("risk gate") || q.contains("تحقق من المخاطر")) return Result(true, AmarAiEngineBinding.riskGate())
        if (q.contains("راجع الاستراتيجية") || q.contains("validate strategy") || q.contains("تحقق من الاستراتيجية")) {
            val values = Regex("[-+]?\\d+(?:\\.\\d+)?").findAll(q).mapNotNull { it.value.toDoubleOrNull() }.toList()
            return Result(true, AmarAiEngineBinding.validate(values))
        }
        if (q.contains("حالة التتبع") || q.contains("tracking status") || q.contains("راقب الصفقات")) return Result(true, "ENGINE_TRACKING|status=PENDING_RUNTIME_QUERY|لا يتم اختلاق بيانات التتبع؛ ستقرأ من MT5 Runtime عند توفره.")
        if (q.contains("الشمعة") && (q.contains("ربع ساعة") || q.contains("15m") || q.contains("m15"))) return Result(true, "ENGINE_CANDLE|status=MT5_RUNTIME_REQUIRED|timeframe=M15|لا توجد نسبة مخترعة بدون بيانات شموع فعلية.")
        if (q.contains("الإضاءة") || q.contains("الاضاءة") || q.contains("visual effects") || q.contains("المؤثرات")) {
            val enable = !(q.contains("أوقف") || q.contains("اطف") || q.contains("إيقاف") || q.contains("off"))
            val accepted = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.SetVisualEffects(enable))
            return Result(true, if (accepted) "تم تغيير المؤثرات عبر سلطة AMAR Agent." else "تم رفض تغيير المؤثرات بواسطة سلطة AMAR Agent.")
        }

        val bot = Regex("(?:بوت|bot)\\s*(\\d+)").find(q)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 1
        val symbol = Regex("(?:على|في|for|on)\\s*([a-z0-9._-]+)").find(q)?.groupValues?.getOrNull(1)?.uppercase()
        val volume = Regex("(?:لوت|lot)(?:\\s+(?:البوت|bot)\\s*\\d+)?\\s*(?:إلى|الى|to)?\\s*(0?\\.\\d+|\\d+(?:\\.\\d+)?)").find(q)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        val usd = Regex("(?:\\$|دولار|usd)\\s*(\\d+(?:\\.\\d+)?)").find(q)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        val explicitBotLot = Regex("(?:ارفع|غيّر|غير|اضبط|set|increase)\\s+(?:لوت|lot)\\s+(?:البوت|bot)\\s*(\\d+)\\s*(?:إلى|الى|to)\\s*(0?\\.\\d+|\\d+(?:\\.\\d+)?)").find(q)
        if (explicitBotLot != null) {
            val targetBot = explicitBotLot.groupValues[1].toIntOrNull() ?: 1
            val targetLot = explicitBotLot.groupValues[2].toDoubleOrNull()
            if (targetBot !in 1..10 || targetLot == null || targetLot <= 0.0) return Result(true, "قيمة اللوت أو رقم البوت غير صالح.")
            return if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(targetBot, "SET_LOT:$targetLot"))) {
                Result(true, "سجلت تغيير لوت البوت $targetBot عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ المالي ما زال مغلقًا حتى MT5.")
            } else Result(true, "تم رفض أمر اللوت بواسطة سلطة AMAR Agent.")
        }

        if ((q.contains("افتح") || q.contains("فتح") || q.contains("open")) && (q.contains("شراء") || q.contains("buy") || q.contains("بيع") || q.contains("sell"))) {
            val side = if (q.contains("شراء") || q.contains("buy")) "BUY" else "SELL"
            if (symbol == null || volume == null) return Result(true, "أحتاج الرمز واللوت صراحةً. مثال: افتح شراء XAUUSD لوت 0.01")
            return if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "OPEN:$side:$symbol:$volume"))) {
                Result(true, "جهزت أمر $side على $symbol بحجم $volume عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ الفعلي ما زال مغلقًا حتى MT5.")
            } else Result(true, "تم رفض أمر التداول بواسطة سلطة AMAR Agent.")
        }
        if (q.contains("اغلق الكل") || q.contains("أغلق الكل") || q.contains("close all")) {
            val accepted = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "CLOSE_ALL"))
            return Result(true, if (accepted) "جهزت أمر الإغلاق عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ ينتظر MT5." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        }
        if (q.contains("عند الخسارة") || q.contains("stop loss") || q.contains("حد الخسارة")) {
            if (usd == null) return Result(true, "أعطني قيمة الخسارة بالدولار، مثلاً: عند الخسارة 30 دولار أغلق.")
            val accepted = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "SET_STOP_LOSS:$usd"))
            return Result(true, if (accepted) "جهزت حد خسارة ${usd}$ عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ ينتظر MT5." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        }
        if (q.contains("عند الربح") || q.contains("profit trigger") || q.contains("take profit")) {
            if (usd == null) return Result(true, "أعطني قيمة الربح بالدولار، مثلاً: عند الربح 2 دولار نفذ الأمر التالي.")
            val accepted = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "SET_PROFIT_TRIGGER:$usd"))
            return Result(true, if (accepted) "جهزت شرط ربح ${usd}$ عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ ينتظر MT5." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        }
        if (volume != null && (q.contains("لوت") || q.contains("lot"))) {
            val accepted = AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "SET_LOT:$volume"))
            return Result(true, if (accepted) "سجلت تغيير لوت البوت $bot عبر سلطة AMAR Agent؛ PENDING_MT5: التنفيذ المالي ينتظر MT5." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        }
        if (q.contains("ارفع مستوى اللوت") || q.contains("ارفع اللوت") || q.contains("ارفع لوت") || q.contains("increase lot")) return Result(true, "أستطيع رفع اللوت، لكن أعطني القيمة المطلوبة ولن أخمّن قيمة مالية.")
        if (q.contains("شغل الشبكة") || q.contains("شغّل الشبكة") || q.contains("start grid")) return Result(true, if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "START_GRID"))) "جهزت تشغيل الشبكة عبر سلطة AMAR Agent." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        if (q.contains("أوقف الشبكة") || q.contains("اوقف الشبكة") || q.contains("stop grid")) return Result(true, if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "STOP_GRID"))) "جهزت إيقاف الشبكة عبر سلطة AMAR Agent." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        if (q.contains("شغل التتبع") || q.contains("شغّل التتبع") || q.contains("start tracking")) return Result(true, if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "START_TRACKING"))) "جهزت التتبع عبر سلطة AMAR Agent." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        if (q.contains("أوقف التتبع") || q.contains("اوقف التتبع") || q.contains("stop tracking")) return Result(true, if (AmarAgentApplicationAuthority.request(AmarAgentApplicationAuthority.Request.QueueBotCommand(bot, "STOP_TRACKING"))) "جهزت إيقاف التتبع عبر سلطة AMAR Agent." else "تم رفض الأمر بواسطة سلطة AMAR Agent.")
        return Result(false, "")
    }
}
