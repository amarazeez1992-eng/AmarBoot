package com.personal.gridbot.amaros.settings

/** خيارات تطوير مستقبلية. افتراضياً مغلقة ولا تمنح صلاحية تداول. */
data class AmarDeveloperOptions(
    val showDiagnostics: Boolean = false,
    val showEventTrace: Boolean = false,
    val showPerformanceCounters: Boolean = false,
    val enableSimulationTools: Boolean = false,
    val allowExperimentalUi: Boolean = false,
    val allowFutureAgentSuggestions: Boolean = false,
    val allowFutureAgentTools: Boolean = false,
    val exposeAdapterDiagnostics: Boolean = false,
    val developerMode: Boolean = false
) {
    fun safeForCurrentPhase(): AmarDeveloperOptions = copy(
        allowFutureAgentTools = false,
        developerMode = false
    )
}

/** عقد اقتراحات مستقبلي: الذكاء الاصطناعي يقترح فقط، والاعتماد والتنفيذ يظلان بيد المستخدم. */
data class AmarFutureSuggestion(
    val id: String,
    val titleArabic: String,
    val reasonArabic: String,
    val priority: Int,
    val requiresApproval: Boolean = true
)

interface AmarFutureSuggestionProvider {
    fun suggest(context: Map<String, String>): List<AmarFutureSuggestion>
}
