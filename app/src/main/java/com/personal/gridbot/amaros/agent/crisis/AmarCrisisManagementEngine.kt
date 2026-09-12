package com.personal.gridbot.amaros.agent.crisis

/** Crisis response proposes containment and recovery; it does not directly trade. */
class AmarCrisisManagementEngine {
    fun assess(snapshot: AmarCrisisSnapshot): AmarCrisisPlan {
        val actions = mutableListOf<String>()
        if (snapshot.drawdownPercent >= snapshot.maxDrawdownPercent) actions += "وقف المخاطرة الجديدة وابدأ مراجعة السبب"
        if (snapshot.connectionUnstable) actions += "تعطيل أوامر جديدة حتى استقرار الاتصال"
        if (snapshot.dataStale) actions += "رفض القرارات المعتمدة على بيانات قديمة"
        if (actions.isEmpty()) actions += "استمرار المراقبة مع عدم تغيير الخطة دون دليل"
        return AmarCrisisPlan(snapshot, actions, requiresHumanOrExecutionGate = true)
    }
}

data class AmarCrisisSnapshot(
    val drawdownPercent: Double,
    val maxDrawdownPercent: Double = 5.0,
    val connectionUnstable: Boolean = false,
    val dataStale: Boolean = false,
    val openRiskAmount: Double = 0.0
)

data class AmarCrisisPlan(
    val snapshot: AmarCrisisSnapshot,
    val actions: List<String>,
    val requiresHumanOrExecutionGate: Boolean
)
