package com.personal.gridbot.amaros.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AmarDeveloperOptionsScreen() {
    var options by remember { mutableStateOf(AmarDeveloperOptions()) }
    val items = listOf(
        "عرض التشخيص" to options.showDiagnostics,
        "عرض تسلسل الأحداث" to options.showEventTrace,
        "عدادات الأداء" to options.showPerformanceCounters,
        "أدوات المحاكاة" to options.enableSimulationTools,
        "واجهة تجريبية" to options.allowExperimentalUi,
        "اقتراحات الذكاء الاصطناعي المستقبلية" to options.allowFutureAgentSuggestions,
        "أدوات الذكاء الاصطناعي المستقبلية" to options.allowFutureAgentTools,
        "تشخيص الموصلات" to options.exposeAdapterDiagnostics
    )
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("خيارات المطور", style = MaterialTheme.typography.headlineMedium)
                Text("خيارات مخصصة للتطوير والاختبار المستقبلي. لا تمنح أي صلاحية للتداول الحقيقي.")
            }
        }
        items(items) { (title, enabled) ->
            Card(Modifier.fillMaxWidth()) {
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, modifier = Modifier.weight(1f))
                    Switch(enabled, onCheckedChange = { value ->
                        options = when (title) {
                            "عرض التشخيص" -> options.copy(showDiagnostics = value)
                            "عرض تسلسل الأحداث" -> options.copy(showEventTrace = value)
                            "عدادات الأداء" -> options.copy(showPerformanceCounters = value)
                            "أدوات المحاكاة" -> options.copy(enableSimulationTools = value)
                            "واجهة تجريبية" -> options.copy(allowExperimentalUi = value)
                            "اقتراحات الذكاء الاصطناعي المستقبلية" -> options.copy(allowFutureAgentSuggestions = value)
                            "أدوات الذكاء الاصطناعي المستقبلية" -> options.copy(allowFutureAgentTools = value)
                            else -> options.copy(exposeAdapterDiagnostics = value)
                        }.safeForCurrentPhase()
                    })
                }
            }
        }
        item { Text("التداول الحقيقي مغلق افتراضياً وممنوع في هذه المرحلة.") }
    }
}
