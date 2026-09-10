package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF07121B)
private val Panel = Color(0xFF0D202B)
private val Panel2 = Color(0xFF102A37)
private val Ink = Color(0xFFE9FBFF)
private val Muted = Color(0xFF8CA9B5)
private val Cyan = Color(0xFF19E6FF)
private val Green = Color(0xFF00E6A0)
private val Red = Color(0xFFFF5364)
private val Gold = Color(0xFFFFC84D)
private val Line = Color(0xFF214452)

private data class Frame(val label: String, val code: String)

/**
 * واجهة حالة السوق جاهزة للبيانات الحقيقية.
 * النسب الحالية متعمدة أن تكون غير معروضة كأرقام حتى لا نصنع بيانات وهمية.
 */
@Composable
fun Bot1MarketStatus() {
    val frames = listOf(
        Frame("1 دقيقة", "M1"), Frame("2 دقيقة", "M2"), Frame("3 دقائق", "M3"), Frame("4 دقائق", "M4"),
        Frame("5 دقائق", "M5"), Frame("6 دقائق", "M6"), Frame("10 دقائق", "M10"), Frame("12 دقيقة", "M12"),
        Frame("15 دقيقة", "M15"), Frame("20 دقيقة", "M20"), Frame("30 دقيقة", "M30"), Frame("ساعة", "H1"),
        Frame("ساعتان", "H2"), Frame("3 ساعات", "H3"), Frame("4 ساعات", "H4"), Frame("6 ساعات", "H6"),
        Frame("8 ساعات", "H8"), Frame("12 ساعة", "H12"), Frame("يومي", "D1"), Frame("أسبوعي", "W1"), Frame("شهري", "MN1")
    )
    var selected by remember { mutableStateOf("H4") }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Bg)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("حالة السوق", color = Ink, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text("تحليل متعدد الفريمات • جاهز للربط مع MT5", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("● مباشر لاحقًا", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                frames.forEach { frame ->
                    FrameChip(frame, frame.code == selected) { selected = frame.code }
                }
            }

            MarketDirectionCard()

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("قراءة الفريمات", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Black)
                frames.forEach { frame -> FrameStatusRow(frame, frame.code == selected) }
            }

            Text("النسب والإشارة ستُحسب من بيانات MT5 الحقيقية عند تفعيل موصل السوق؛ لا توجد أرقام وهمية في مرحلة التجهيز.", color = Muted, fontSize = 9.sp)
        }
    }
}

@Composable private fun FrameChip(frame: Frame, selected: Boolean, onClick: () -> Unit) {
    BoxChip(
        text = frame.label,
        accent = if (selected) Cyan else Line,
        selected = selected,
        onClick = onClick
    )
}

@Composable private fun BoxChip(text: String, accent: Color, selected: Boolean, onClick: () -> Unit) {
    Text(text, color = if (selected) accent else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.background(if (selected) Panel2 else Panel, RoundedCornerShape(12.dp))
            .border(1.dp, if (selected) accent else Line, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 8.dp))
}

@Composable private fun MarketDirectionCard() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        DirectionMetric("شرائي", "— %", Green, Modifier.weight(1f))
        DirectionMetric("محايد", "— %", Gold, Modifier.weight(1f))
        DirectionMetric("بيعي", "— %", Red, Modifier.weight(1f))
    }
}

@Composable private fun DirectionMetric(title: String, value: String, accent: Color, modifier: Modifier) {
    Column(modifier.background(Panel, RoundedCornerShape(15.dp)).border(1.dp, accent.copy(alpha = .45f), RoundedCornerShape(15.dp)).padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(value, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Black)
        Text("بانتظار البيانات", color = Muted, fontSize = 7.sp)
    }
}

@Composable private fun FrameStatusRow(frame: Frame, selected: Boolean) {
    Row(Modifier.fillMaxWidth().background(if (selected) Panel2 else Panel, RoundedCornerShape(12.dp)).border(1.dp, if (selected) Cyan else Line, RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(frame.label, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(frame.code, color = Cyan, fontSize = 8.sp)
        }
        Text("شرائي —", color = Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(9.dp))
        Text("بيعي —", color = Red, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(9.dp))
        Text("قوة —", color = Gold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
