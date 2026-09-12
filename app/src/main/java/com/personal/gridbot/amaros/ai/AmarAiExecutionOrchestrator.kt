package com.personal.gridbot.amaros.ai

import android.content.Context
import com.personal.gridbot.amaros.runtime.AmarBotCommandEngine

/** App-side execution contract. Durable now; MT5/Bridge adapter is the final execution stage. */
object AmarAiExecutionOrchestrator {
    enum class Type { OPEN_MARKET, CLOSE_ALL, SET_STOP_LOSS, SET_PROFIT_TRIGGER, START_GRID, STOP_GRID, START_TRACKING, STOP_TRACKING }
    data class Intent(val type: Type, val symbol: String? = null, val side: String? = null, val volume: Double? = null, val amountUsd: Double? = null, val botNumber: Int = 1)
    data class Result(val accepted: Boolean, val status: String, val message: String, val command: String = "")

    suspend fun submit(context: Context, intent: Intent): Result {
        val validation = validate(intent)
        if (!validation.accepted) return validation
        val command = canonical(intent)
        val id = AmarBotCommandEngine(context).queue(intent.botNumber, command)
        return Result(true, "PENDING_MT5", "تم تجهيز أمر تنفيذ برقم $id. بانتظار مرحلة MT5/Bridge.", command)
    }

    fun validate(intent: Intent): Result {
        if (intent.botNumber !in 1..10) return Result(false, "REJECTED", "رقم البوت غير صالح")
        when (intent.type) {
            Type.OPEN_MARKET -> {
                if (intent.symbol.isNullOrBlank()) return Result(false, "REJECTED", "الرمز مطلوب")
                if (intent.side !in listOf("BUY", "SELL")) return Result(false, "REJECTED", "الاتجاه يجب أن يكون BUY أو SELL")
                if (intent.volume == null || intent.volume <= 0.0) return Result(false, "REJECTED", "حجم الصفقة مطلوب ولا يمكن تخمينه")
            }
            Type.SET_STOP_LOSS, Type.SET_PROFIT_TRIGGER -> if (intent.amountUsd == null || intent.amountUsd <= 0.0) return Result(false, "REJECTED", "قيمة الدولار مطلوبة")
            else -> Unit
        }
        return Result(true, "VALIDATED", "الأمر صالح للإرسال")
    }

    /** JVM-safe canonical JSON: no Android org.json dependency is required by unit tests. */
    fun canonical(intent: Intent): String {
        fun q(value: String?): String = if (value == null) "null" else "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        fun n(value: Double?): String = value?.toString() ?: "null"
        return "{" +
            "\"schema\":\"AMAR_EXECUTION_INTENT_V1\"," +
            "\"type\":\"${intent.type.name}\"," +
            "\"symbol\":${q(intent.symbol)}," +
            "\"side\":${q(intent.side)}," +
            "\"volume\":${n(intent.volume)}," +
            "\"amountUsd\":${n(intent.amountUsd)}," +
            "\"botNumber\":${intent.botNumber}," +
            "\"status\":\"PENDING_MT5\"" +
            "}"
    }
}
