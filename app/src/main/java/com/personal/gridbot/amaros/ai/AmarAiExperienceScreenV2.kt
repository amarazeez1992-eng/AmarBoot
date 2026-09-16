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
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("AMAR AI AGENT", color = AgentAccent, style = MaterialTheme.typography.headlineSmall)
                Text("CENTRAL AUTHORITY • LOCAL • FAIL-CLOSED", color = AgentMuted, style = MaterialTheme.typography.labelSmall)
            }
            AssistChip(onClick = {}, label = { Text("MT5: OFF") })
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            items(conversation.messages) { message ->
                Surface(color = AgentPanel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(11.dp)) {
                        Text(message.role.name, color = AgentAccent, style = MaterialTheme.typography.labelSmall)
                        Text(message.text, color = Color.White)
                    }
                }
            }
        }
        attachment?.let { Text("📎 $it", color = AgentAccent, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) }
        Surface(color = AgentPanel, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = { picker.launch("*/*") }) { Icon(Icons.Default.Add, "إرفاق", tint = AgentAccent) }
                OutlinedTextField(request, { request = it }, Modifier.weight(1f), placeholder = { Text("اكتب طلبك للوكيل…", color = AgentMuted) }, singleLine = true)
                FilledIconButton(onClick = ::send, enabled = request.isNotBlank() && !busy) { Icon(Icons.Default.Send, "إرسال") }
            }
        }
    }
}
