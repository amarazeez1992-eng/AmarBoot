package com.personal.gridbot.amaros.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import java.util.Locale

private val AgentBg = Color(0xFF03070B)
private val AgentPanel = Color(0xFF0A1118)
private val AgentPanel2 = Color(0xFF071018)
private val AgentAccent = Color(0xFF20E6FF)
private val AgentAccent2 = Color(0xFFB86CFF)
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
    var toolsOpen by remember { mutableStateOf(false) }
    var voiceListening by remember { mutableStateOf(false) }
    var voiceConversation by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) engine?.language = Locale("ar", "IQ")
        }
        tts = engine
        onDispose { engine?.stop(); engine?.shutdown(); tts = null }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) conversation.addSystem("FAIL_CLOSED: لم يتم منح إذن الميكروفون؛ لم يبدأ تسجيل أو إرسال صوت.")
    }
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        voiceListening = false
        val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!spoken.isNullOrBlank()) request = spoken
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        attachment = uri?.lastPathSegment
        if (uri != null) conversation.addSystem("تم إرفاق مدخل مستخدم: ${uri.lastPathSegment}. التحليل لا يصبح مؤكدًا إلا بعد تشغيل المحرك المناسب فعليًا.")
    }

    fun startVoice() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO); return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            conversation.addSystem("FAIL_CLOSED: خدمة التعرف الصوتي غير متاحة على الجهاز."); return
        }
        voiceListening = true
        speechLauncher.launch(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-IQ")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث مع AMAR AI")
        })
    }

    fun send() {
        val prompt = request.trim()
        if (prompt.isBlank() || busy) return
        conversation.addUser(prompt); request = ""; busy = true
        scope.launch {
            runCatching { agent.ask(prompt) }
                .onSuccess { result ->
                    conversation.addAi(result.answer)
                    if (voiceConversation) tts?.speak(result.answer, TextToSpeech.QUEUE_FLUSH, null, "amar-agent")
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
                        FilterChip(selected = selectedMode == mode, onClick = {
                            selectedMode = mode
                            request = when (mode) {
                                "بحث" -> "ابحث وقارن الأدلة ثم أعطني تقريرًا موثقًا مع مصادر واضحة."
                                "اختبار" -> "اختبر الفكرة وحدد المخاطر والافتراضات والثغرات قبل أي قرار."
                                "نقد" -> "راجع الإجابة نقديًا، ابحث عن الأدلة الناقصة والتناقضات والادعاءات غير المدعومة."
                                "قرار" -> "حلل البدائل والافتراضات والنتائج وعدم اليقين ثم قدم قرارًا محكومًا بالأدلة."
                                else -> "حلل هذا الطلب خطوة بخطوة، تحقق من الأدلة، واذكر ما هو مؤكد وما هو مجهول."
                            }
                        }, label = { Text(mode) })
                    }
                    IconButton(onClick = { conversation.clear(); attachment = null }) { Icon(Icons.Default.Delete, "جلسة جديدة", tint = AgentMuted) }
                }
                if (showWorkspaceControls) Card(colors = CardDefaults.cardColors(containerColor = AgentPanel2)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text("لوحة التحكم", color = AgentAccent, style = MaterialTheme.typography.titleSmall)
                        SettingSwitch("إظهار الأدلة", showEvidence) { showEvidence = it }
                        SettingSwitch("إضافة الأدلة تلقائيًا", autoEvidence) { autoEvidence = it }
                        SettingSwitch("رسائل مختصرة", compactMessages) { compactMessages = it }
                        SettingSwitch("المحادثة الصوتية", voiceConversation) { voiceConversation = it }
                    }
                }
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(if (compactMessages) 4.dp else 8.dp)) {
            items(conversation.messages) { message ->
                val accent = when (message.role) { AmarAiConversationState.Role.USER -> AgentAccent; AmarAiConversationState.Role.AI -> Color.White; AmarAiConversationState.Role.SYSTEM -> AgentMuted }
                Surface(color = AgentPanel, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(if (compactMessages) 8.dp else 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(message.role.name, color = accent, style = MaterialTheme.typography.labelSmall)
                        Text(message.text, color = Color.White)
                    }
                }
            }
        }

        if (showEvidence) Surface(color = AgentPanel2, modifier = Modifier.fillMaxWidth()) {
            Text("EVIDENCE STREAM • مؤكد / محتمل / مجهول • لا ادعاء بقدرة لم تُنفذ فعليًا.", color = AgentMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
        }
        attachment?.let { Text("📎 $it", color = AgentAccent, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) }

        Surface(color = AgentPanel, tonalElevation = 6.dp, modifier = Modifier.fillMaxWidth()) {
            Column {
                if (toolsOpen) Card(colors = CardDefaults.cardColors(containerColor = AgentPanel2), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("إضافات الوكيل", color = AgentAccent, style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { toolsOpen = false }) { Icon(Icons.Default.Close, "إغلاق", tint = AgentMuted) }
                        }
                        ToolRow("🖼", "إرسال صورة") { picker.launch("image/*"); toolsOpen = false }
                        ToolRow("📁", "إرسال ملف") { picker.launch("*/*"); toolsOpen = false }
                        ToolRow("📄", "إرسال مستند") { picker.launch("application/pdf"); toolsOpen = false }
                        ToolRow("🎙", "بصمة صوتية / فويس") { startVoice(); toolsOpen = false }
                        ToolRow("🗣", if (voiceConversation) "التحدث مع الوكيل • ON" else "التحدث مع الوكيل") { voiceConversation = !voiceConversation; toolsOpen = false }
                        ToolRow("📷", "مشاركة الكاميرا") { conversation.addSystem("FAIL_CLOSED: مشاركة الكاميرا تحتاج قناة الكاميرا الفعلية قبل تفعيل التحليل."); toolsOpen = false }
                        ToolRow("▣", "مشاركة الشاشة") { conversation.addSystem("FAIL_CLOSED: مشاركة الشاشة تحتاج جلسة MediaProjection الفعلية قبل تفعيل الفهم."); toolsOpen = false }
                        ToolRow("🔊", "الصوت") { voiceConversation = true; toolsOpen = false }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalIconButton(onClick = { toolsOpen = !toolsOpen }, modifier = Modifier.size(48.dp)) { Icon(if (toolsOpen) Icons.Default.Close else Icons.Default.Add, "الإضافات") }
                    FilledTonalIconButton(onClick = { startVoice() }, modifier = Modifier.size(48.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (voiceListening) AgentAccent2 else AgentPanel2)) { Text(if (voiceListening) "●" else "🎙", color = Color.White) }
                    OutlinedTextField(request, { request = it }, Modifier.weight(1f), placeholder = { Text(if (busy) "الوكيل يعمل…" else "اكتب رسالتك…", color = AgentMuted) }, singleLine = true, enabled = !busy, shape = RoundedCornerShape(16.dp))
                    FilledIconButton(onClick = ::send, enabled = request.isNotBlank() && !busy, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Send, "إرسال") }
                }
            }
        }
    }
}

@Composable private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Switch(checked = checked, onCheckedChange = onCheckedChange) }
}

@Composable private fun ToolRow(symbol: String, label: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("$symbol  $label") }
}
