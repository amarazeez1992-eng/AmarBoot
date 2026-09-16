package com.personal.gridbot.amaros.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val AgentBg = Color(0xFF03070B)
private val AgentPanel = Color(0xFF0A1118)
private val AgentAccent = Color(0xFF20E6FF)
private val AgentMuted = Color(0xFF8FA4B2)

@Composable
fun AmarAiExperienceScreenV2() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val agent = remember(context) { AmarAiAgentEngine(context) }
    val conversation = remember { AmarAiConversationState() }
    var request by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var attachment by remember { mutableStateOf<String?>(null) }
    var showEvidence by remember { mutableStateOf(true) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        attachment = uri?.lastPathSegment
        if (uri != null) conversation.addSystem("تم استلام مدخل مستخدم: ${uri.lastPathSegment}. لا يُعلن تحليل المحتوى كمؤكد إلا بعد تشغيل محركه فعليًا.")
    }

    fun send() {
        val prompt = request.trim()
        if (prompt.isBlank() || busy) return
        conversation.addUser(prompt)
        request = ""
        busy = true
        scope.launch {
            runCatching { agent.ask(prompt) }
                .onSuccess { result ->
                    conversation.addAi(result.answer)
                    if (result.proposedActions.isNotEmpty()) conversation.addSystem(result.proposedActions.joinToString("\n"))
                    if (result.toolEvidence.isNotEmpty()) conversation.addSystem("دورة الوكيل:\n${result.toolEvidence.takeLast(12).joinToString("\n")}")
                }
                .onFailure { conversation.addSystem("FAIL_CLOSED: ${it.message ?: "خطأ غير معروف"}") }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(AgentBg)) {
        Surface(color = AgentPanel, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("AMAR AI AGENT", color = AgentAccent, style = MaterialTheme.typography.headlineSmall)
                        Text("العقل المركزي • محلي • FAIL-CLOSED", color = AgentMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    AssistChip(onClick = { showEvidence = !showEvidence }, label = { Text(if (showEvidence) "الأدلة ON" else "الأدلة OFF") })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(onClick = { request = "حلل هذا الطلب خطوة بخطوة، تحقق من الأدلة، واذكر ما هو مؤكد وما هو مجهول." }, label = { Text("تحليل") })
                    AssistChip(onClick = { request = "ابحث وقارن الأدلة ثم أعطني تقريرًا موثقًا مع مصادر واضحة." }, label = { Text("بحث") })
                    AssistChip(onClick = { request = "اختبر الفكرة وحدد المخاطر والافتراضات قبل أي قرار." }, label = { Text("اختبار") })
                    IconButton(onClick = { conversation.clear(); attachment = null }) { Icon(Icons.Default.Delete, "جلسة جديدة", tint = AgentMuted) }
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            items(conversation.messages) { message ->
                val accent = when (message.role) {
                    AmarAiConversationState.Role.USER -> AgentAccent
                    AmarAiConversationState.Role.AI -> Color.White
                    AmarAiConversationState.Role.SYSTEM -> AgentMuted
                }
                Surface(color = AgentPanel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(message.role.name, color = accent, style = MaterialTheme.typography.labelSmall)
                        Text(message.text, color = Color.White)
                    }
                }
            }
        }

        if (showEvidence) {
            Surface(color = Color(0xFF071018), modifier = Modifier.fillMaxWidth()) {
                Text("سجل الأدلة ظاهر • أي قدرة غير منفذة فعليًا تبقى غير مؤكدة.", color = AgentMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
        }

        attachment?.let { Text("📎 $it", color = AgentAccent, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) }
        Surface(color = AgentPanel, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = { picker.launch("*/*") }) { Icon(Icons.Default.Add, "إرفاق", tint = AgentAccent) }
                OutlinedTextField(
                    request,
                    { request = it },
                    Modifier.weight(1f),
                    placeholder = { Text(if (busy) "الوكيل يعمل…" else "اكتب طلبك للوكيل…", color = AgentMuted) },
                    singleLine = true,
                    enabled = !busy
                )
                FilledIconButton(onClick = ::send, enabled = request.isNotBlank() && !busy) { Icon(Icons.Default.Send, "إرسال") }
            }
        }
    }
}
