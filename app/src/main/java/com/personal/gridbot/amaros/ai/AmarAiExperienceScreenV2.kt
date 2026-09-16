package com.personal.gridbot.amaros.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
private val AgentPanel2 = Color(0xFF071018)
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
    var showWorkspaceControls by remember { mutableStateOf(true) }
    var compactMessages by remember { mutableStateOf(false) }
    var autoEvidence by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf("تحليل") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        attachment = uri?.lastPathSegment
        if (uri != null) conversation.addSystem("تم استلام مدخل مستخدم: ${uri.lastPathSegment}. التحليل لا يصبح مؤكدًا إلا بعد تشغيل المحرك المناسب فعليًا.")
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
                    if (autoEvidence && result.toolEvidence.isNotEmpty()) conversation.addSystem("دورة الوكيل:\n${result.toolEvidence.takeLast(12).joinToString("\n")}")
                }
                .onFailure { conversation.addSystem("FAIL_CLOSED: ${it.message ?: "خطأ غير معروف"}") }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(AgentBg)) {
        Surface(color = AgentPanel, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("AMAR AI AGENT", color = AgentAccent, style = MaterialTheme.typography.headlineSmall)
                        Text("CENTRAL INTELLIGENCE • AUTHORITY CONTROLLED", color = AgentMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    AssistChip(onClick = { showWorkspaceControls = !showWorkspaceControls }, label = { Text(if (showWorkspaceControls) "التحكم ON" else "التحكم OFF") })
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("تحليل", "بحث", "اختبار", "نقد", "قرار").forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = {
                                selectedMode = mode
                                request = when (mode) {
                                    "بحث" -> "ابحث وقارن الأدلة ثم أعطني تقريرًا موثقًا مع مصادر واضحة."
                                    "اختبار" -> "اختبر الفكرة وحدد المخاطر والافتراضات والثغرات قبل أي قرار."
                                    "نقد" -> "راجع الإجابة نقديًا، ابحث عن الأدلة الناقصة والتناقضات والادعاءات غير المدعومة."
                                    "قرار" -> "حلل البدائل والافتراضات والنتائج وعدم اليقين ثم قدم قرارًا محكومًا بالأدلة."
                                    else -> "حلل هذا الطلب خطوة بخطوة، تحقق من الأدلة، واذكر ما هو مؤكد وما هو مجهول."
                                }
                            },
                            label = { Text(mode) }
                        )
                    }
                    IconButton(onClick = { conversation.clear(); attachment = null }) { Icon(Icons.Default.Delete, "جلسة جديدة", tint = AgentMuted) }
                }
                if (showWorkspaceControls) {
                    Card(colors = CardDefaults.cardColors(containerColor = AgentPanel2)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text("لوحة التحكم القابلة للتعديل", color = AgentAccent, style = MaterialTheme.typography.titleSmall)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إظهار الأدلة")
                                Switch(checked = showEvidence, onCheckedChange = { showEvidence = it })
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("إضافة الأدلة تلقائيًا")
                                Switch(checked = autoEvidence, onCheckedChange = { autoEvidence = it })
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رسائل مختصرة")
                                Switch(checked = compactMessages, onCheckedChange = { compactMessages = it })
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(if (compactMessages) 4.dp else 8.dp)
        ) {
            items(conversation.messages) { message ->
                val accent = when (message.role) {
                    AmarAiConversationState.Role.USER -> AgentAccent
                    AmarAiConversationState.Role.AI -> Color.White
                    AmarAiConversationState.Role.SYSTEM -> AgentMuted
                }
                Surface(color = AgentPanel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(if (compactMessages) 8.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(message.role.name, color = accent, style = MaterialTheme.typography.labelSmall)
                        Text(message.text, color = Color.White)
                    }
                }
            }
        }

        if (showEvidence) {
            Surface(color = AgentPanel2, modifier = Modifier.fillMaxWidth()) {
                Text("EVIDENCE STREAM • مؤكد / محتمل / مجهول • لا ادعاء بقدرة لم تُنفذ فعليًا.", color = AgentMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
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
