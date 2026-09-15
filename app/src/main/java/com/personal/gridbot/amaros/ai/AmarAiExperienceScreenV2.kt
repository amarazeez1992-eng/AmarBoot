package com.personal.gridbot.amaros.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val AiBg = Color(0xFF03070B)
private val AiPanel = Color(0xFF0A1118)
private val AiPanel2 = Color(0xFF0D1720)
private val AiCyan = Color(0xFF20E6FF)
private val AiGold = Color(0xFFFFC84A)
private val AiGreen = Color(0xFF38E29B)
private val AiRed = Color(0xFFFF5C72)
private val AiText = Color(0xFFF2F7FA)
private val AiMuted = Color(0xFF8FA4B2)

@Composable
fun AmarAiExperienceScreenV2() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val conversation = remember { AmarAiConversationState() }
    var request by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var textColor by remember { mutableStateOf("AUTO") }
    var liveState by remember { mutableStateOf(AmarAiLiveConversationEngine.State.IDLE) }
    var transcript by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<AmarAiImageInput.ImagePayload?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var externalActivated by remember { mutableStateOf(AmarAiKeyStore.load(context)?.isNotBlank() == true) }
    var testing by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    val dark = isSystemInDarkTheme()
    val fg = when (textColor) {
        "GOLD" -> AiGold
        "CYAN" -> AiCyan
        "WHITE" -> AiText
        else -> if (dark) AiText else Color(0xFFEAF1F5)
    }

    val live = remember {
        AmarAiLiveConversationEngine(context, scope, { liveState = it }, { transcript = it }, { conversation.addAi(it) }, { conversation.addSystem(it) })
    }
    DisposableEffect(Unit) { onDispose { live.release() } }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri).orEmpty()
            if (type.startsWith("image/")) runCatching { AmarAiImageInput(context).read(uri) }
                .onSuccess { image = it; attachmentName = "صورة" }
                .onFailure { conversation.addSystem("تعذر قراءة الصورة") }
            else attachmentName = uri.lastPathSegment ?: "ملف"
        }
    }

    fun activateExternal() {
        val trimmed = key.trim()
        if (trimmed.isBlank()) {
            conversation.addSystem("AMAR AI المحلي يعمل بدون مفتاح. Gemini اختياري فقط.")
            externalActivated = false
            return
        }
        AmarAiKeyStore.save(context, trimmed)
        key = trimmed
        externalActivated = true
        conversation.addSystem("تم تفعيل Gemini كمزود اختياري. AMAR AI ومحركاته المحلية تبقى السلطة الأساسية.")
    }

    fun deactivateExternal() {
        AmarAiKeyStore.clear(context)
        key = ""
        externalActivated = false
        conversation.addSystem("تم إيقاف Gemini. AMAR AI المحلي ما زال يعمل.")
    }

    fun testConnection() {
        val trimmed = key.trim()
        if (trimmed.isBlank() || testing || busy) return
        testing = true
        scope.launch {
            runCatching { AmarGeminiClient().generate(trimmed, model.trim(), "أنت AMAR AI Supervisor. أجب بالعربية بجملة قصيرة.", "اختبار اتصال فقط. قل: الاتصال يعمل.") }
                .onSuccess { conversation.addSystem("اختبار المزود الخارجي ناجح: $it") }
                .onFailure { conversation.addSystem("فشل المزود الخارجي: ${it.message ?: "غير معروف"}. AMAR AI المحلي لم يتوقف.") }
            testing = false
        }
    }

    fun send() {
        val prompt = request.trim()
        if (prompt.isBlank() || busy) return
        val local = AmarAiActionEngine.route(prompt)
        conversation.addUser(prompt)
        request = ""
        if (local.handled) { conversation.addAi(local.response); return }
        busy = true
        scope.launch {
            runCatching {
                if (image != null) {
                    if (key.isBlank()) error("تحليل الصور يحتاج مزوداً متعدد الوسائط مفعلاً؛ لم يتم اختلاق تحليل.")
                    AmarGeminiVisionClient().analyze(key.trim(), model.trim(), "أنت AMAR AI Supervisor. استخدم الأدلة المرئية فقط ولا تخترع بيانات.", prompt, image!!)
                } else {
                    AmarAiUiEngineBridge(context, externalProviderEnabled = externalActivated).ask(key.trim(), model.trim(), prompt).answer
                }
            }.onSuccess { conversation.addAi(it) }
                .onFailure { conversation.addSystem("فشل الطلب: ${it.message ?: "غير معروف"}") }
            image = null
            attachmentName = null
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(AiBg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AMAR AI", color = AiCyan, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("SUPERVISOR CONSOLE / AMAR CORE • NEWS • MARKET", color = AiMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Surface(color = AiGreen.copy(alpha = .12f), shape = RoundedCornerShape(20.dp)) {
                Text("AMAR CORE ACTIVE", color = AiGreen, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
            }
            Spacer(Modifier.width(4.dp))
            IconButton({ showAdvanced = !showAdvanced }) { Icon(if (showAdvanced) Icons.Default.Close else Icons.Default.Settings, "الإعدادات", tint = AiCyan) }
        }

        AmarAi3DOrbitalStage(Modifier.fillMaxWidth())

        if (showAdvanced) {
            Surface(Modifier.fillMaxWidth().padding(horizontal = 12.dp), color = AiPanel, shape = RoundedCornerShape(16.dp), tonalElevation = 4.dp) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("AI CONTROL CENTER", color = AiText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("AMAR Core • Gemini اختياري • النموذج", color = AiMuted, fontSize = 8.sp)
                        }
                        Icon(Icons.Default.CheckCircle, "AMAR مفعل", tint = AiGreen)
                    }
                    OutlinedTextField(value = key, onValueChange = { key = it; externalActivated = false }, modifier = Modifier.fillMaxWidth(), label = { Text("Optional Gemini API Key") }, supportingText = { Text("اختياري فقط. AMAR AI لا يتوقف بدونه.", fontSize = 8.sp) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AiCyan, unfocusedBorderColor = Color(0xFF263642), focusedLabelColor = AiCyan, cursorColor = AiCyan, focusedTextColor = AiText, unfocusedTextColor = AiText))
                    OutlinedTextField(value = model, onValueChange = { model = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Optional external model") }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AiCyan, unfocusedBorderColor = Color(0xFF263642), focusedLabelColor = AiCyan, cursorColor = AiCyan, focusedTextColor = AiText, unfocusedTextColor = AiText))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button({ activateExternal() }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AiCyan, contentColor = Color.Black)) { Text("تفعيل اختياري", fontSize = 10.sp, fontWeight = FontWeight.Black) }
                        OutlinedButton({ testConnection() }, enabled = key.isNotBlank() && !testing && !busy, Modifier.weight(1f)) { Text(if (testing) "جارٍ الاختبار…" else "اختبار المزود", fontSize = 10.sp) }
                        OutlinedButton({ deactivateExternal() }, enabled = externalActivated, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("إيقاف", fontSize = 10.sp, color = AiRed) }
                    }
                    Text(if (externalActivated) "AMAR CORE + مزود خارجي اختياري" else "AMAR CORE يعمل مستقلاً", color = AiMuted, fontSize = 9.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("النص", color = AiMuted, fontSize = 8.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        listOf("AUTO" to "تلقائي", "WHITE" to "أبيض", "GOLD" to "ذهبي", "CYAN" to "سماوي").forEach { (value, label) -> FilterChip(selected = textColor == value, onClick = { textColor = value }, label = { Text(label, fontSize = 8.sp) }) }
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
        }

        if (attachmentName != null) Surface(Modifier.padding(horizontal = 12.dp), color = AiPanel2, shape = RoundedCornerShape(8.dp)) { Text("📎 $attachmentName", color = AiCyan, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) }
        if (transcript.isNotBlank()) Text("LIVE • $transcript", color = AiMuted, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))

        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp), reverseLayout = true) {
            items(conversation.messages.asReversed()) { message ->
                val user = message.role == AmarAiConversationState.Role.USER
                Surface(color = if (user) AiPanel2 else AiPanel, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().border(1.dp, if (user) AiCyan.copy(alpha = .15f) else Color(0xFF162530), RoundedCornerShape(12.dp))) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(if (user) "COMMAND / YOU" else if (message.role == AmarAiConversationState.Role.SYSTEM) "SYSTEM" else "AMAR AI", color = if (message.role == AmarAiConversationState.Role.SYSTEM) AiGold else AiCyan, fontSize = 7.sp, fontWeight = FontWeight.Black)
                        Text(message.text, color = fg, fontSize = 12.sp, lineHeight = 17.sp)
                        if (!user) TextButton({ clipboard.setText(AnnotatedString(message.text)) }, contentPadding = PaddingValues(0.dp)) { Text("نسخ", color = AiMuted, fontSize = 8.sp) }
                    }
                }
            }
        }

        Surface(color = AiPanel, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                IconButton({ picker.launch("*/*") }) { Icon(Icons.Default.Add, "إرفاق صورة أو ملف", tint = AiCyan) }
                IconButton({ if (liveState == AmarAiLiveConversationEngine.State.IDLE) { live.configure(key, model); live.start() } else live.stop() }) { Text(if (liveState == AmarAiLiveConversationEngine.State.IDLE) "🎙" else "■", color = AiCyan, fontSize = 18.sp) }
                OutlinedTextField(value = request, onValueChange = { request = it }, modifier = Modifier.weight(1f), placeholder = { Text("اكتب الأمر أو السؤال…", color = AiMuted) }, singleLine = true, colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFF263642), focusedBorderColor = AiCyan, cursorColor = AiCyan, focusedTextColor = AiText, unfocusedTextColor = AiText))
                FilledIconButton({ send() }, enabled = request.isNotBlank() && !busy, colors = IconButtonDefaults.filledIconButtonColors(containerColor = AiCyan, contentColor = Color.Black)) { Icon(Icons.Default.Send, "إرسال") }
            }
        }
    }
}
