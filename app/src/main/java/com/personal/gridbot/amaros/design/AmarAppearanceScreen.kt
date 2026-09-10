package com.personal.gridbot.amaros.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.personal.gridbot.ui.theme.AmarThemeMode

@Composable
fun AmarAppearanceScreen(mode: AmarThemeMode, onModeChange: (AmarThemeMode) -> Unit) {
    val presets = listOf(
        AppearancePreset(0, "أسود ذهبي سماوي", listOf(Color(0xFF07121B), Color(0xFFFFC84D), Color(0xFF19E6FF))),
        AppearancePreset(1, "نهاري أبيض سماوي", listOf(Color(0xFFF4F8FA), Color(0xFF009FC0), Color(0xFFFFC84D))),
        AppearancePreset(2, "ليل سماوي", listOf(Color(0xFF06121A), Color(0xFF00D9FF), Color(0xFF4D7CFF))),
        AppearancePreset(3, "أسود أخضر", listOf(Color(0xFF040A08), Color(0xFF00E6A0), Color(0xFFB7FF4A)))
    )
    LazyColumn(Modifier.fillMaxSize().background(Color(0xFF07121B)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("غرفة المظهر", color = Color(0xFFFFE69A), fontSize = 27.sp, fontWeight = FontWeight.Black)
            Text("تغيير شامل للتطبيق بين الوضع الليلي المزرق والوضع النهاري الأبيض.", color = Color(0xFF9BB6BE), fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ModeCard("الوضع الليلي", "أسود + سماوي + ذهبي", mode == AmarThemeMode.DARK, Color(0xFF19E6FF), Modifier.weight(1f)) { onModeChange(AmarThemeMode.DARK) }
                ModeCard("الوضع النهاري", "أبيض + سماوي + ذهبي", mode == AmarThemeMode.LIGHT, Color(0xFFFFC84D), Modifier.weight(1f)) { onModeChange(AmarThemeMode.LIGHT) }
            }
        }
        item { Text("مظاهر مستقبلية", color = Color(0xFF19E6FF), fontWeight = FontWeight.Black, fontSize = 15.sp) }
        items(presets) { p -> AppearanceCard(p, (p.id == 0 && mode == AmarThemeMode.DARK) || (p.id == 1 && mode == AmarThemeMode.LIGHT)) { if (p.id == 1) onModeChange(AmarThemeMode.LIGHT) else onModeChange(AmarThemeMode.DARK) } }
        item {
            Text("قابلية التوسع", color = Color(0xFFFFC84D), fontWeight = FontWeight.Black, fontSize = 15.sp)
            Text("هذه الغرفة مصممة لاستقبال 10 أو 20 أو 50 أو 100+ مظهر وخلفية كوحدات مستقلة لاحقًا.", color = Color(0xFF8CA8B1), fontSize = 10.sp)
        }
    }
}

private data class AppearancePreset(val id: Int, val name: String, val colors: List<Color>)

@Composable private fun ModeCard(title: String, subtitle: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.background(Color(0xFF0D202B), RoundedCornerShape(18.dp)).border(1.dp, if (selected) accent else Color(0xFF214452), RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp)) {
        Text(title, color = if (selected) accent else Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(subtitle, color = Color(0xFF9BB6BE), fontSize = 9.sp)
    }
}

@Composable private fun AppearanceCard(p: AppearancePreset, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF0D202B), RoundedCornerShape(18.dp)).border(1.dp, if (selected) Color(0xFFFFC84D) else Color(0xFF214452), RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        p.colors.forEach { c -> Spacer(Modifier.size(38.dp).background(c, RoundedCornerShape(10.dp))) }
        Column { Text(p.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(if (selected) "المظهر الحالي" else "معاينة", color = if (selected) Color(0xFFFFC84D) else Color(0xFF7E9AA3), fontSize = 9.sp) }
    }
}
