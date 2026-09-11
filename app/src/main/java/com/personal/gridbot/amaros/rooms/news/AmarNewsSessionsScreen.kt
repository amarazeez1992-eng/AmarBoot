package com.personal.gridbot.amaros.rooms.news

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private enum class NewsTab(val title: String) { NEWS("A — الأخبار"), SESSIONS("B — السوق والسيولة"), DAILY("C — التقرير اليومي") }
private data class Session(val name: String, val zone: String, val openHour: Int, val closeHour: Int)

@Composable
fun AmarNewsSessionsScreen() {
    var tab by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NewsTab.values().forEachIndexed { index, item ->
                FilterChip(tab == index, { tab = index }, label = { Text(item.title) }, modifier = Modifier.weight(1f))
            }
        }
        when (NewsTab.values()[tab]) {
            NewsTab.NEWS -> NewsPanel()
            NewsTab.SESSIONS -> SessionsPanel()
            NewsTab.DAILY -> DailyPanel()
        }
    }
}

@Composable private fun NewsPanel() = LazyColumn(Modifier.fillMaxSize().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item { Header("الأخبار", "المصادر الحية لم تُفعّل بعد؛ لا يتم عرض بيانات مُختلقة.") }
    item { InfoCard("المصادر الموثوقة", "سيتم ربط المصادر الرسمية ومصادر الأخبار المرخّصة مع ترجمة عربية وذاكرة مؤقتة.") }
    item { InfoCard("التقويم الاقتصادي", "الفائدة والتضخم والوظائف والبنوك المركزية ستظهر مع الوقت والتأثير والمصدر.") }
    item { InfoCard("الذهب والفوركس والسلع", "أي تحديث سوقي سيُعرض من بيانات فعلية فقط، مع حالة المصدر ووقت التحديث.") }
}

@Composable private fun SessionsPanel() {
    val now = ZonedDateTime.now()
    val sessions = listOf(Session("آسيا", "Asia/Tokyo", 9, 18), Session("لندن", "Europe/London", 8, 17), Session("نيويورك", "America/New_York", 8, 17))
    LazyColumn(Modifier.fillMaxSize().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("السوق والسيولة", "الوقت ديناميكي حسب المنطقة الزمنية. السيولة الفعلية تحتاج مصدر سوق حي.") }
        items(sessions) { SessionCard(it, now) }
        item { InfoCard("التداخلات", "لندن ↔ نيويورك تداخل رئيسي. قوة السيولة هنا وصف اعتيادي وليست قراءة لحظية.") }
    }
}

@Composable private fun DailyPanel() {
    val previous = ZonedDateTime.now().minusDays(1).toLocalDate()
    LazyColumn(Modifier.fillMaxSize().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Header("التقرير اليومي", "تقرير يوم ${previous.format(DateTimeFormatter.ISO_DATE)}") }
        item { InfoCard("بيانات OHLC", "سيتم حساب الافتتاح والأعلى والأدنى والإغلاق والتغير والنسبة من مصدر أسعار موثوق.") }
        item { InfoCard("الفجوة اليومية", "سيتم حساب اتجاه وحجم الفجوة عند توفر إغلاق اليوم السابق وافتتاح اليوم التالي.") }
        item { InfoCard("الخط الزمني", "الافتتاح → آسيا → لندن → تداخل لندن/نيويورك → نيويورك → الإغلاق. التحليل الفعلي يعتمد على البيانات.") }
    }
}

@Composable private fun SessionCard(session: Session, now: ZonedDateTime) {
    val zone = ZoneId.of(session.zone)
    val local = now.withZoneSameInstant(zone)
    val open = local.toLocalDate().atTime(session.openHour, 0).atZone(zone)
    val close = local.toLocalDate().atTime(session.closeHour, 0).atZone(zone)
    val active = !local.isBefore(open) && local.isBefore(close)
    val target = if (active) close else if (local.isBefore(open)) open else open.plusDays(1)
    val seconds = Duration.between(local, target).seconds.coerceAtLeast(0)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(session.name, style = MaterialTheme.typography.titleMedium)
                Text(if (active) "● مفتوحة" else "○ مغلقة", color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("المرجع: ${local.format(DateTimeFormatter.ofPattern("HH:mm:ss"))} — ${zone.id}")
            Text(if (active) "الإغلاق بعد ${hours}س ${minutes}د" else "الافتتاح بعد ${hours}س ${minutes}د")
            Text("السيولة: لا توجد قراءة لحظية متصلة بعد", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun Header(title: String, subtitle: String) { Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.headlineSmall); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun InfoCard(title: String, body: String) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleMedium); Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
