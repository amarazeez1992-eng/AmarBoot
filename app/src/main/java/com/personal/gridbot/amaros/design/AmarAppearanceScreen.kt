package com.personal.gridbot.amaros.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.ui.theme.AmarThemeMode

@Composable
fun AmarAppearanceScreen(mode: AmarThemeMode, onModeChange: (AmarThemeMode) -> Unit) {
    val presets = listOf(
        AppearancePreset(1, "المظهر 01 — عمار الذهبي", listOf(Color(0xFF07121B), Color(0xFFFFC84D), Color(0xFF19E6FF))),
        AppearancePreset(2, "المظهر 02 — نهاري أبيض", listOf(Color(0xFFF4F8FA), Color(0xFF009FC0), Color(0xFFFFC84D))),
        AppearancePreset(3, "المظهر 03 — ليل سماوي", listOf(Color(0xFF06121A), Color(0xFF00D9FF), Color(0xFF4D7CFF))),
        AppearancePreset(4, "المظهر 04 — أخضر رقمي", listOf(Color(0xFF040A08), Color(0xFF00E6A0), Color(0xFFB7FF4A))),
        AppearancePreset(5, "المظهر 05 — فضي", listOf(Color(0xFF11151A), Color(0xFFD9E2E8), Color(0xFF6FD8FF))),
        AppearancePreset(6, "المظهر 06 — أزرق عميق", listOf(Color(0xFF050B18), Color(0xFF4DA3FF), Color(0xFFB57CFF))),
        AppearancePreset(7, "المظهر 07 — أحمر تحذيري", listOf(Color(0xFF100609), Color(0xFFFF5364), Color(0xFFFFC84D))),
        AppearancePreset(8, "المظهر 08 — بنفسجي تقني", listOf(Color(0xFF0C0715), Color(0xFFB46CFF), Color(0xFF35E0FF))),
        AppearancePreset(9, "المظهر 09 — فحمي", listOf(Color(0xFF090909), Color(0xFFE7E7E7), Color(0xFFFFB83D))),
        AppearancePreset(10, "المظهر 10 — سماوي زجاجي", listOf(Color(0xFF06151A), Color(0xFF7BE8FF), Color(0xFFFFFFFF)))
    )
    LazyColumn(Modifier.fillMaxSize().background(Color(0xFF07121B)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("الخلفيات والمظهر", color = Color(0xFFFFE69A), fontSize = 27.sp, fontWeight = FontWeight.Black); Text("نظام مظهر مستقل قابل للتوسع إلى V20 وما بعده.", color = Color(0xFF9BB6BE), fontSize = 11.sp) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ModeCard("ليلي", mode == AmarThemeMode.DARK, Color(0xFF19E6FF), Modifier.weight(1f)) { onModeChange(AmarThemeMode.DARK) }
            ModeCard("نهاري", mode == AmarThemeMode.LIGHT, Color(0xFFFFC84D), Modifier.weight(1f)) { onModeChange(AmarThemeMode.LIGHT) }
            ModeCard("تلقائي", mode == AmarThemeMode.AUTO, Color(0xFFB46CFF), Modifier.weight(1f)) { onModeChange(AmarThemeMode.AUTO) }
        } }
        item { Text("الخلفيات / التصاميم", color = Color(0xFF19E6FF), fontWeight = FontWeight.Black, fontSize = 15.sp) }
        items(presets) { p -> AppearanceCard(p, p.id == 1 && mode == AmarThemeMode.DARK || p.id == 2 && mode == AmarThemeMode.LIGHT) { if (p.id == 2) onModeChange(AmarThemeMode.LIGHT) else onModeChange(AmarThemeMode.DARK) } }
        item { Text("التوسعة الآمنة", color = Color(0xFFFFC84D), fontWeight = FontWeight.Black, fontSize = 15.sp); Text("كل خلفية/تصميم مستقبلي وحدة مستقلة؛ فشل وحدة لا يوقف بقية التطبيق.", color = Color(0xFF8CA8B1), fontSize = 10.sp) }
    }
}

private data class AppearancePreset(val id: Int, val name: String, val colors: List<Color>)
@Composable private fun ModeCard(title: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit) { Column(modifier.background(Color(0xFF0D202B), RoundedCornerShape(16.dp)).border(1.dp, if (selected) accent else Color(0xFF214452), RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(9.dp)) { Text(title, color = if (selected) accent else Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp) } }
@Composable private fun AppearanceCard(p: AppearancePreset, selected: Boolean, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().background(Color(0xFF0D202B), RoundedCornerShape(18.dp)).border(1.dp, if (selected) Color(0xFFFFC84D) else Color(0xFF214452), RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) { p.colors.forEach { c -> Spacer(Modifier.size(34.dp).background(c, RoundedCornerShape(9.dp))) }; Column { Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(if (selected) "المظهر الحالي" else "معاينة", color = if (selected) Color(0xFFFFC84D) else Color(0xFF7E9AA3), fontSize = 9.sp) } } }
