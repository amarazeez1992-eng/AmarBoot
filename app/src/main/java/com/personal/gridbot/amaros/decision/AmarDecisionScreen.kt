package com.personal.gridbot.amaros.decision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.core.AmarRuntimeController

/** Read-only decision room fed by the canonical demo runtime. */
@Composable
fun AmarDecisionScreen(state: AmarRuntimeController.RuntimeState) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("القرار", style = MaterialTheme.typography.headlineMedium)
        Text("محرك تجميع الأدلة والتوافق — واجهة القرار المستقبلية")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("القرار الحالي", style = MaterialTheme.typography.titleLarge)
                Text(state.decision?.direction?.name ?: "بانتظار أول دورة")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("شراء: ${if (state.decision?.direction?.name == "LONG_BIAS") "مفعّل" else "—"}")
                    Text("بيع: ${if (state.decision?.direction?.name == "SHORT_BIAS") "مفعّل" else "—"}")
                    Text("ثقة: ${state.decision?.confidence?.let { "%.2f".format(it) } ?: "—"}")
                }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("مصادر القرار", style = MaterialTheme.typography.titleMedium)
                Text("هيكل السوق • القمم والقيعان • السيولة • المناطق • Fibonacci")
                Text("التشبع • الاتجاه • الزخم • التذبذب • الفريمات المتعددة")
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("الذاكرة المستقبلية", style = MaterialTheme.typography.titleMedium)
                Text("الدورة: ${state.cycleNumber} • الحالة: ${state.context?.regime ?: "—"}")
                Text("الأدلة: ${state.context?.evidence?.size ?: 0} • المخاطر: ${state.risk?.allowed?.let { if (it) "مسموح" else "محجوب" } ?: "—"}")
                Text("التنفيذ: ${state.execution?.let { if (it.executed) "EXECUTED" else "DEMO_GUARDED" } ?: "—"}")
            }
        }
    }
}
