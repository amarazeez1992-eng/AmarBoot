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

/** Decision room shell. It is intentionally read-only until the confluence engines are connected. */
@Composable
fun AmarDecisionScreen() {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("القرار", style = MaterialTheme.typography.headlineMedium)
        Text("محرك تجميع الأدلة والتوافق — واجهة القرار المستقبلية")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("القرار الحالي", style = MaterialTheme.typography.titleLarge)
                Text("غير متاح بعد — بانتظار ربط محركات التحليل والتوافق")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("شراء: —")
                    Text("بيع: —")
                    Text("ثقة: —")
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
                Text("هنا ستُحفظ القراءات والأدلة والتوافقات وسبب القرار وسجل تغيّره.")
            }
        }
    }
}
