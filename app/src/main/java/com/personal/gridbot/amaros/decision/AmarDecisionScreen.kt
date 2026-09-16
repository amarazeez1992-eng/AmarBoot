package com.personal.gridbot.amaros.decision

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.ai.AmarAiAgentEngine
import kotlinx.coroutines.launch

/** Decision room: presentation only; every analytical request is delegated to the central AMAR AI agent. */
@Composable
fun AmarDecisionScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val agent = remember(context) { AmarAiAgentEngine(context) }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("لا يوجد قرار مؤكد بعد. اطلب من AMAR AI تحليل الحالة بالأدلة.") }
    var busy by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Color(0xFF050608)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("DECISION ROOM", color = Color(0xFFFFE8A0), style = MaterialTheme.typography.headlineMedium)
                Text("AMAR AI • EVIDENCE-GOVERNED DECISION", color = Color(0xFF8FA4B2), style = MaterialTheme.typography.labelSmall)
            }
            Text("● AGENT", color = Color(0xFF69F6C0), style = MaterialTheme.typography.labelMedium)
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1118))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("AMAR AI", color = Color(0xFFFFE7A1), style = MaterialTheme.typography.titleLarge)
                Text("غرفة القرار لا تصدر تنفيذًا مباشرًا. تجمع الطلب، الأدلة، عدم اليقين والبدائل عبر الوكيل المركزي.", color = Color(0xFFD8E2E7))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    OutlinedButton(onClick = { request = "حلل الحالة الحالية وحدد البدائل والافتراضات والأدلة والتناقضات وعدم اليقين." }, Modifier.weight(1f)) { Text("تحليل") }
                    OutlinedButton(onClick = { request = "اختبر القرار المقترح وابحث عن الأدلة الناقصة والمخاطر قبل اعتماده." }, Modifier.weight(1f)) { Text("اختبار") }
                }
            }
        }

        TextField(
            value = request,
            onValueChange = { request = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
            placeholder = { Text("اكتب سؤال القرار…") },
            singleLine = false
        )
        Button(
            onClick = {
                val prompt = request.trim()
                if (prompt.isBlank() || busy) return@Button
                busy = true
                scope.launch {
                    runCatching { agent.ask(prompt) }
                        .onSuccess { answer = it.answer }
                        .onFailure { answer = "FAIL_CLOSED: ${it.message ?: "تعذر تشغيل الوكيل"}" }
                    busy = false
                }
            },
            enabled = request.isNotBlank() && !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (busy) "AMAR AI يعمل…" else "حلّل عبر AMAR AI") }

        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F15))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("نتيجة الوكيل", color = Color(0xFF7EEAFF), style = MaterialTheme.typography.titleMedium)
                        Text(answer, color = Color.White)
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F15))) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("حوكمة القرار", color = Color(0xFFFFE7A1), style = MaterialTheme.typography.titleMedium)
                        Text("مؤكد • محتمل • مجهول", color = Color(0xFFD8E2E7))
                        Text("لا تنفيذ حساس دون الصلاحية والتأكيد المطلوبين.", color = Color(0xFF8FA4B2))
                    }
                }
            }
        }
    }
}
