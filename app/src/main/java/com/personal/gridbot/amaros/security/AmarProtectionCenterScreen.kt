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
        Text("مراقبة الأعطال وسلامة بيانات التطبيق. المركز يسجل الأعطال ويمنع بيانات الحماية من التحول إلى أوامر تداول.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (error == null) "● لا توجد أعطال مسجلة" else "⚠ عطل مسجل يحتاج مراجعة", style = MaterialTheme.typography.titleMedium)
            if (error != null) { Text(error); Text("وقت التسجيل: ${time ?: "غير معروف"}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("سياسة الحماية", style = MaterialTheme.typography.titleMedium)
            Text("عند حدوث عطل غير معالج، يسجل النظام الوحدة والخطأ قبل تمرير العطل إلى معالج النظام. لا ندّعي اعتراض أخطاء Compose داخل الاستدعاءات البرمجية؛ عزل الواجهة يتم فقط في حدود الاستضافة التي يمكن حمايتها بأمان.")
        } }
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("سلامة البيانات", style = MaterialTheme.typography.titleMedium)
            Text("يمكن التحقق من الملفات باستخدام SHA-256، بينما تبقى أسرار التداول والاعتماديات الحية خارج طبقة الحماية.")
        } }
        if (error != null) Button(onClick = { AmarProtectionCenter.clearLastFailure(context) }) { Text("مسح التنبيه بعد المعالجة") }
    }
}
