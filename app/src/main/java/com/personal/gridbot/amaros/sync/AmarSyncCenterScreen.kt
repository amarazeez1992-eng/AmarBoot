package com.personal.gridbot.amaros.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun AmarSyncCenterScreen() {
    val context = LocalContext.current
    val current = AmarSyncConfig.load(context)
    var endpoint by remember { mutableStateOf(current?.endpoint.orEmpty()) }
    var token by remember { mutableStateOf(current?.token.orEmpty()) }
    var deviceName by remember { mutableStateOf("AMAR-ANDROID") }
    var autoSync by remember { mutableStateOf(current != null) }
    var message by remember { mutableStateOf(if (current != null) "المزامنة مهيأة" else "لم يتم ربط خادم مزامنة بعد") }
    Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF08101D), Color(0xFF31205F), Color(0xFF5B174D))), RoundedCornerShape(28.dp)).padding(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AMAR SYNC CORE", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("مزامنة Offline-first مع الإنترنت عند توفره", color = Color(0xFFE9D5FF))
                Text(if (current != null) "● متصل بإعداد مزامنة" else "○ محلي فقط", color = Color(0xFFFFD166))
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(deviceName, { deviceName = it }, Modifier.fillMaxWidth(), label = { Text("اسم الجهاز") }, singleLine = true)
                OutlinedTextField(endpoint, { endpoint = it }, Modifier.fillMaxWidth(), label = { Text("عنوان خادم AMAR / اللابتوب") }, singleLine = true, placeholder = { Text("https://server.example أو http://192.168.x.x:port") })
                OutlinedTextField(token, { token = it }, Modifier.fillMaxWidth(), label = { Text("مفتاح المزامنة") }, singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text("المزامنة التلقائية"); Text("تعمل فقط عند توفر الشبكة", style = MaterialTheme.typography.labelSmall) }
                    Switch(autoSync, { autoSync = it })
                }
                Button(onClick = {
                    runCatching {
                        if (autoSync) AmarSyncConfig.configure(context, endpoint.trim(), token.trim())
                        message = if (autoSync) "تم تشغيل المزامنة الدورية + طلب مزامنة فوري." else "المزامنة التلقائية متوقفة." 
                    }.onFailure { message = "تعذر حفظ الإعداد: ${it.message}" }
                }, Modifier.fillMaxWidth(), enabled = !autoSync || (endpoint.isNotBlank() && token.isNotBlank())) { Text("حفظ وتشغيل") }
                OutlinedButton(onClick = { AmarSyncManager.requestNow(context); message = "تم وضع طلب مزامنة فوري؛ سيعمل فقط مع شبكة متاحة." }, Modifier.fillMaxWidth(), enabled = current != null) { Text("مزامنة الآن") }
            }
        }
        Text("ما تتم مزامنته يمر عبر طبقة المزامنة الحالية؛ أسرار التداول لا تُضمّن في Bot Vault snapshot. المزامنة السحابية العامة تحتاج خادم AMAR متوافقاً مع /sync.", style = MaterialTheme.typography.labelSmall)
    }
}
