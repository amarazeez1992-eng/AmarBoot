package com.personal.gridbot.amaros.api

import com.personal.gridbot.amaros.core.AmarOperatingMode
import com.personal.gridbot.amaros.grid.AmarGridEngine

/** B24: stable application API. Commands become intents; no UI code can reach a broker directly. */
interface AmarTradingApi {
    fun status(): AmarApiStatus
    fun planGrid(request: AmarGridRequest): AmarGridResponse
}

data class AmarApiStatus(val mode: AmarOperatingMode, val connected: Boolean, val executionEnabled: Boolean)
data class AmarGridRequest(val symbol: String, val anchorPrice: Double, val levels: Int, val distance: Double, val quantity: Double, val direction: AmarGridEngine.Direction)
data class AmarGridResponse(val accepted: Boolean, val plan: AmarGridEngine.GridPlan?, val message: String)

class DemoAmarTradingApi : AmarTradingApi {
    private val grid = AmarGridEngine()
    override fun status(): AmarApiStatus = AmarApiStatus(AmarOperatingMode.DEMO, connected = false, executionEnabled = false)
    override fun planGrid(request: AmarGridRequest): AmarGridResponse = try {
        val plan = grid.build(AmarGridEngine.GridConfig(request.symbol, request.anchorPrice, request.levels, request.distance, request.quantity, request.direction))
        AmarGridResponse(true, plan, "تم إنشاء خطة شبكة تجريبية دون تنفيذ")
    } catch (error: IllegalArgumentException) {
        AmarGridResponse(false, null, "تعذر إنشاء الخطة: ${error.message ?: "بيانات غير صالحة"}")
    }
}
