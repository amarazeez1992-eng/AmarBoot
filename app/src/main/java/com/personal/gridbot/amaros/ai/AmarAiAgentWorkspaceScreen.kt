package com.personal.gridbot.amaros.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.personal.gridbot.amaros.workspace.Item10CaptureCoordinator
import kotlinx.coroutines.launch
import java.util.Locale

private val Bg = Color(0xFF03070C)
private val Panel = Color(0xFF0B121B)
private val Panel2 = Color(0xFF101A25)
private val Gold = Color(0xFFE6BE67)
private val Cyan = Color(0xFF73E7FF)
private val Violet = Color(0xFFB89CFF)
private val Muted = Color(0xFF91A5B3)

@Composable
fun AmarAiAgentWorkspaceScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val agent = remember(context) { AmarAiAgentEngine(context) }
    val conversation = remember { AmarAiConversationState() }
    var text by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var controlsOpen by remember { mutableStateOf(false) }
    var voiceMode by remember { mutableStateOf(false) }
    var evidenceOpen by remember { mutableStateOf(true) }
    var attachment by remember { mutableStateOf<String?>(null) }
    var selectedTool by remember { mutableStateOf<String?>(null) }
    var lastEvidence by remember { mutableStateOf<Item10CaptureCoordinator.Evidence?>(null) }
    var cameraActive by remember { mutableStateOf(false) }
    var screenActive by remember { mutableStateOf(false) }
    var voiceRecording by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }

    val capture = remember(activity) {
        activity?.let { host ->
            Item10CaptureCoordinator(host) { evidence ->
                lastEvidence = evidence
                when (evidence.channel) {
                    Item10CaptureCoordinator.Channel.VOICE -> conversation.addSystem("VOICE EVIDENCE • تم تسجيل بصمة صوتية محلية: ${evidence.byteCount} bytes")
                    Item10CaptureCoordinator.Channel.CAMERA -> cameraActive = true
                    Item10CaptureCoordinator.Channel.SCREEN -> screenActive = true
                }
            }
        }
    }

    val pulse = rememberInfiniteTransition(label = "agent-pulse")
    val orbScale by pulse.animateFloat(0.96f, 1.04f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "orb")

    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) engine?.language = Locale("ar", "IQ") }
        tts = engine
        onDispose { engine?.stop(); engine?.shutdown(); tts = null; capture?.stopAll() }
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            cameraActive = capture?.startCamera() == true
            selectedTool = if (cameraActive) "الكاميرا — ACTIVE • evidence capture" else "الكاميرا — FAIL_CLOSED: تعذر بدء الالتقاط"
        } else conversation.addSystem("FAIL_CLOSED: لم يُمنح إذن الكاميرا.")
    }
    val screenPermission = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val ok = capture?.startScreen(result.resultCode, result.data) == true
        screenActive = ok
        selectedTool = if (ok) "مشاركة الشاشة — ACTIVE • MediaProjection evidence" else "الشاشة — FAIL_CLOSED: لم تُمنح جلسة الالتقاط"
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        attachment = uri?.lastPathSegment
        if (uri != null) conversation.addSystem("مرفق مستخدم: ${uri.lastPathSegment}. الإدخال محفوظ كدليل؛ التفسير البصري/المستندي لا يُدّعى دون محركه.")
    }

    fun toggleVoiceRecording() {
        if (voiceRecording) {
            val evidence = capture?.stopVoice()
            voiceRecording = false
            if (evidence == null) conversation.addSystem("FAIL_CLOSED: تعذر حفظ التسجيل الصوتي.") else lastEvidence = evidence
        } else {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                conversation.addSystem("FAIL_CLOSED: امنح إذن الميكروفون لتسجيل Voice Message.")
                return
            }
            voiceRecording = capture?.startVoice() == true
            if (!voiceRecording) conversation.addSystem("FAIL_CLOSED: تعذر تشغيل مسجل الصوت.")
        }
    }

    fun toggleCamera() {
        if (cameraActive) { capture?.stopCamera(); cameraActive = false; selectedTool = "الكاميرا — STOPPED"; return }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermission.launch(Manifest.permission.CAMERA)
        } else {
            cameraActive = capture?.startCamera() == true
            selectedTool = if (cameraActive) "الكاميرا — ACTIVE • evidence capture" else "الكاميرا — FAIL_CLOSED"
        }
    }

    fun toggleScreen() {
        if (screenActive) { capture?.stopScreen(); screenActive = false; selectedTool = "مشاركة الشاشة — STOPPED"; return }
        val intent = capture?.screenPermissionIntent()
        if (intent == null) { selectedTool = "الشاشة — FAIL_CLOSED: MediaProjection غير متاح"; return }
        screenPermission.launch(intent)
    }

    fun send() {
        val prompt = text.trim(); if (prompt.isBlank() || busy) return
        conversation.addUser(prompt); text = ""; busy = true
        scope.launch {
            runCatching { agent.ask(prompt) }
                .onSuccess { result ->
                    conversation.addAi(result.answer)
                    if (voiceMode) tts?.speak(result.answer, TextToSpeech.QUEUE_FLUSH, null, "amar-agent")
                    if (result.proposedActions.isNotEmpty()) conversation.addSystem(result.proposedActions.joinToString("\n"))
                    if (result.toolEvidence.isNotEmpty()) conversation.addSystem("EVIDENCE\n${result.toolEvidence.takeLast(10).joinToString("\n")}")
                }
                .onFailure { conversation.addSystem("FAIL_CLOSED: ${it.message ?: "فشل غير معروف"}") }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Bg, Color(0xFF07121B), Bg)))) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(58.dp).scale(orbScale).background(Brush.radialGradient(listOf(Color.White, Gold, Color(0xFF5A4218), Bg)), CircleShape), contentAlignment = Alignment.Center) { Text("A", color = Color.White, fontWeight = FontWeight.Black, style = MaterialTheme.typography.headlineMedium) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) { Text("AMAR AI", color = Gold, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge); Text("CENTRAL INTELLIGENCE • AGENT", color = Muted, style = MaterialTheme.typography.labelSmall) }
            AssistChip(onClick = { controlsOpen = !controlsOpen }, label = { Text(if (controlsOpen) "إخفاء" else "تحكم") })
        }

        Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("غرفة الوكيل المركزية", color = Color.White, fontWeight = FontWeight.Bold); Text(if (busy) "AMAR يعمل الآن" else "جاهز • القرار داخل الوكيل", color = if (busy) Cyan else Muted, style = MaterialTheme.typography.labelSmall) }
                    Surface(color = Color(0xFF143A31), shape = CircleShape) { Text("● ONLINE", color = Color(0xFF79F0C2), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall) }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("تحليل", "بحث", "اختبار", "نقد", "قرار").forEach { mode -> FilterChip(selected = false, onClick = { text = mode }, label = { Text(mode) }) }
                }
                if (controlsOpen) {
                    SettingRow("الأدلة", evidenceOpen) { evidenceOpen = it }
                    SettingRow("المحادثة الصوتية / TTS", voiceMode) { voiceMode = it }
                    SettingRow("الكاميرا", cameraActive) { toggleCamera() }
                    SettingRow("مشاركة الشاشة", screenActive) { toggleScreen() }
                }
            }
        }

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(conversation.messages) { message ->
                val isAi = message.role == AmarAiConversationState.Role.AI
                val color = when (message.role) { AmarAiConversationState.Role.USER -> Cyan; AmarAiConversationState.Role.AI -> Color.White; else -> Muted }
                Card(colors = CardDefaults.cardColors(containerColor = if (isAi) Panel2 else Panel), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) { Text(if (isAi) "AMAR AI" else message.role.name, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold); Text(message.text, color = Color.White) }
                }
            }
        }

        if (evidenceOpen) Text("EVIDENCE • capture: ${if (lastEvidence == null) "none" else lastEvidence!!.channel.name} • hash/provenance محفوظ • interpretation remains Agent-gated", color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
        attachment?.let { Text("📎 $it", color = Cyan, modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)) }

        if (selectedTool != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Panel2), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 5.dp)) {
                Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Text(selectedTool!!, color = Gold, modifier = Modifier.weight(1f)); TextButton(onClick = { selectedTool = null }) { Text("إغلاق") } }
            }
        }

        Surface(color = Panel, tonalElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
            if (menuOpen) {
                Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("إضافات الوكيل", color = Gold, fontWeight = FontWeight.Bold)
                    Tool("🖼️", "إرسال صورة", true) { picker.launch("image/*"); selectedTool = "إرسال صورة"; menuOpen = false }
                    Tool("📁", "إرسال ملف", true) { picker.launch("*/*"); selectedTool = "إرسال ملف"; menuOpen = false }
                    Tool("📄", "إرسال مستند", true) { picker.launch("application/pdf"); selectedTool = "إرسال مستند"; menuOpen = false }
                    Tool("🗣️", "التحدث مع الوكيل / TTS", true) { voiceMode = !voiceMode; selectedTool = if (voiceMode) "التحدث مع الوكيل — TTS ACTIVE" else "التحدث مع الوكيل — TTS OFF"; menuOpen = false }
                    Tool("🎙️", if (voiceRecording) "إيقاف Voice Message" else "تسجيل Voice Message", true) { toggleVoiceRecording(); selectedTool = if (voiceRecording) "Voice Message — RECORDING" else "Voice Message — STOPPED"; menuOpen = false }
                    Tool("📷", if (cameraActive) "إيقاف الكاميرا" else "مشاركة الكاميرا", true) { toggleCamera(); menuOpen = false }
                    Tool("🖥️", if (screenActive) "إيقاف مشاركة الشاشة" else "مشاركة الشاشة", true) { toggleScreen(); menuOpen = false }
                }
            }
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalIconButton(onClick = { menuOpen = !menuOpen }, modifier = Modifier.size(48.dp)) { Icon(if (menuOpen) Icons.Default.Close else Icons.Default.Add, "الإضافات") }
                FilledTonalButton(onClick = { toggleVoiceRecording() }, modifier = Modifier.size(48.dp), contentPadding = PaddingValues(0.dp)) { Text(if (voiceRecording) "■" else "🎙️", color = if (voiceRecording) Violet else Color.White) }
                OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.weight(1f), singleLine = true, enabled = !busy, placeholder = { Text(if (busy) "AMAR يعمل…" else "راسل AMAR AI…") }, shape = RoundedCornerShape(18.dp))
                FilledIconButton(onClick = ::send, enabled = text.isNotBlank() && !busy, modifier = Modifier.size(48.dp)) { Icon(Icons.Default.Send, "إرسال") }
            }
        }
    }
}

@Composable private fun SettingRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(label, color = Color.White); Switch(checked, onChange) } }

@Composable private fun Tool(symbol: String, label: String, enabled: Boolean, onClick: () -> Unit) { OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text("$symbol  $label") } }
