package com.personal.gridbot.amaros.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AmarAiExperienceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val conversation = remember { AmarAiConversationState() }
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(key.isNotBlank()) }

    fun runPrompt(raw: String) {
        val prompt = raw.trim()
        if (prompt.isBlank() || busy) return
        if (key.isBlank() || model.isBlank()) {
            request = prompt
            error = false
            return
        }
        AmarAiKeyStore.save(context, key.trim())
        saved = true
        busy = true
        error = false
        conversation.addUser(prompt)
        request = ""
        scope.launch {
            runCatching { AmarAiAgentEngine().ask(key.trim(), model.trim(), prompt) }
                .onSuccess { conversation.addAi(it.answer) }
                .onFailure { conversation.addSystem(it.message ?: "حدث خطأ غير معروف"); error = true }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AmarAiPremiumCoreVisual(Modifier.fillMaxWidth())
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF171022).copy(.96f))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("AMAR AI — غرفة المحادثة", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC857))
                        Text("مستشار فعلي يقرأ الأدلة المتاحة ويقترح، ولا ينفذ أوامر الوسيط.", color = Color(0xFFB7D9E2), style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = { conversation.clear() }, enabled = !busy) { Text("جلسة جديدة") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    AssistChip(onClick = { runPrompt(AmarAiShortcutContract.MARKET_ANALYSIS) }, label = { Text("تحليل السوق") }, modifier = Modifier.weight(1f), enabled = !busy)
                    AssistChip(onClick = { runPrompt(AmarAiShortcutContract.STRATEGY_TEST) }, label = { Text("اختبار") }, modifier = Modifier.weight(1f), enabled = !busy)
                    AssistChip(onClick = { runPrompt(AmarAiShortcutContract.LIBRARY_SEARCH) }, label = { Text("المكتبة") }, modifier = Modifier.weight(1f), enabled = !busy)
                }
                OutlinedTextField(key, { key = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (saved) "✓ المفتاح محفوظ محليًا" else "المفتاح غير محفوظ", color = if (saved) Color(0xFF65F5C0) else Color(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { AmarAiKeyStore.clear(context); key = ""; saved = false }) { Text("مسح") }
                }
                OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }, singleLine = true)
                OutlinedTextField(request, { request = it; error = false }, Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("اكتب رسالتك إلى AMAR AI") })
                Button(onClick = { runPrompt(request) }, Modifier.fillMaxWidth(), enabled = key.isNotBlank() && model.isNotBlank() && request.isNotBlank() && !busy) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("إرسال إلى AMAR AI")
                }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0A17))) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("المحادثة", color = Color(0xFF31E7FF), style = MaterialTheme.typography.titleLarge)
                conversation.messages.forEach { message ->
                    val bg = when (message.role) {
                        AmarAiConversationState.Role.USER -> Color(0xFF1E3150)
                        AmarAiConversationState.Role.AI -> Color(0xFF25143B)
                        AmarAiConversationState.Role.SYSTEM -> Color(0xFF202020)
                    }
                    val label = when (message.role) {
                        AmarAiConversationState.Role.USER -> "أنت"
                        AmarAiConversationState.Role.AI -> "AMAR AI"
                        AmarAiConversationState.Role.SYSTEM -> "النظام"
                    }
                    Surface(shape = RoundedCornerShape(16.dp), color = bg, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(label, color = if (message.role == AmarAiConversationState.Role.AI) Color(0xFF31E7FF) else Color(0xFFFFC857), style = MaterialTheme.typography.labelMedium)
                            Text(message.text, color = if (message.role == AmarAiConversationState.Role.SYSTEM && error) Color(0xFFFF7B85) else Color.White)
                        }
                    }
                }
                Text("⚠️ الذكاء الاصطناعي استشاري؛ لا يضع أو يعدّل أو يغلق أوامر الوسيط.", color = Color(0xFFFFC857), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
