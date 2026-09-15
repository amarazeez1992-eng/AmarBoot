package com.personal.gridbot.amaros.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.visual.AmarVisualEffectsPreference
import com.personal.gridbot.ui.theme.AmarThemeMode

@Composable
fun AmarSettingsOverviewScreen(
    mode: AmarThemeMode,
    onModeChange: (AmarThemeMode) -> Unit,
    layout: Int,
    onLayoutChange: (Int) -> Unit,
    visualEffectsEnabled: Boolean,
    onVisualEffectsChange: (Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        item {
            Text("مركز إعدادات عمار", style = MaterialTheme.typography.headlineSmall)
            Text("تحكم مركزي بالمظهر والواجهة والوكيل دون تغيير منطق التداول.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            SettingCard("المظهر") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button({ onModeChange(AmarThemeMode.DARK) }, Modifier.weight(1f)) { Text("ليلي") }
                    Button({ onModeChange(AmarThemeMode.LIGHT) }, Modifier.weight(1f)) { Text("نهاري") }
                    Button({ onModeChange(AmarThemeMode.AUTO) }, Modifier.weight(1f)) { Text("تلقائي") }
                }
                Spacer(Modifier.height(6.dp))
                Text("الوضع الحالي: ${mode.name}", style = MaterialTheme.typography.labelSmall)
            }
        }
        item {
            SettingCard("الخلفية والمؤثرات") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("الخلفية الديناميكية والمؤثرات", style = MaterialTheme.typography.titleSmall)
                        Text("يمكن إيقاف المؤثرات للأداء، مع بقاء الواجهة سليمة.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = visualEffectsEnabled, onCheckedChange = onVisualEffectsChange)
                }
            }
        }
        item {
            SettingCard("تصميم الرئيسية") {
                Text("الواجهة الحالية: V$layout", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button({ onLayoutChange((layout - 1).coerceAtLeast(1)) }, Modifier.weight(1f)) { Text("السابق") }
                    Button({ onLayoutChange((layout + 1).coerceAtMost(10)) }, Modifier.weight(1f)) { Text("التالي") }
                }
                Button({ onLayoutChange(10) }, Modifier.fillMaxWidth()) { Text("V10 — ثلاثي الأبعاد") }
            }
        }
        item {
            SettingCard("وكيل AI") {
                Text("تحكم كامل بالواجهة والتنقل", style = MaterialTheme.typography.titleSmall)
                Text("الوكيل يستطيع فتح غرف التطبيق وتغيير المظهر والتصميم والمؤثرات عبر قناة UI محكومة. صلاحية تنفيذ التداول ليست ضمن صلاحية الواجهة.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingCard("وحدات المشروع") {
                Text("السوق · الشارت · البوتات · المخاطر · الصفقات · الأداء · المؤشرات · التحليل · القرار · الاختبار · الأدوات · التنبيهات · المكتبة · الحسابات · الأخبار والجلسات", style = MaterialTheme.typography.bodySmall)
                Text("الوحدات غير الجاهزة تبقى معلّمة كغير متاحة ولا يتم اختلاق وظائف لها.", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        content()
    }
}
