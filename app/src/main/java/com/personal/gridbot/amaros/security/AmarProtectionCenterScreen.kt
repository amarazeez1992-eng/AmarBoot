package com.personal.gridbot.amaros.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.content.Context

@Composable
fun AmarProtectionCenterScreen(context: Context) {
    val error = AmarProtectionCenter.lastFailure(context)
    val time = AmarProtectionCenter.lastFailureTime(context)
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("مركز الحماية", style = MaterialTheme.typography.headlineSmall)
        Text("مراقبة الأعطال وسلامة بيانات التطبيق. الحماية لا تعدّل ملفات المشروع أو تتداول تلقائياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (error == null) "● لا توجد أعطال مسجلة" else "⚠ عطل يحتاج مراجعة", style = MaterialTheme.typography.titleMedium)
            if (error != null) { Text(error); Text("وقت التسجيل: ${time ?: "غير معروف"}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("سياسة العزل", style = MaterialTheme.typography.titleMedium)
            Text("عند فشل وحدة محمية، يتم عزلها في واجهة الاستضافة وتسجيل اسم الوحدة والخطأ بدلاً من إسقاط التطبيق كله.")
        } }
        if (error != null) Button(onClick = { AmarProtectionCenter.clearLastFailure(context) }) { Text("مسح التنبيه بعد المعالجة") }
    }
}
