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
import androidx.compose.ui.graphics.Brush
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
private val AiBlue = Color(0xFF397BFF)
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
    var accent by remember { mutableStateOf("CYAN") }
    var liveState by remember { mutableStateOf(AmarAiLiveConversationEngine.State.IDLE) }
    var transcript by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<AmarAiImageInput.ImagePayload?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }
    var activated by remember { mutableStateOf(AmarAiKeyStore.load(context)?.isNotBlank() == true) }
    var testing by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }

    val dark = isSystemInDarkTheme()
    val fg = when (textColor) {
        "GOLD" -> AiGold
        "CYAN" -> AiCyan
        "WHITE" -> AiText
        else -> if (dark) AiText else Color(0xFFEAF1F5)
    }
    val accentColor = if (accent == "GOLD") AiGold else AiCyan

    val live = remember {
        AmarAiLiveConversationEngine(
            context,
            scope,
            { liveState = it },
            { transcript = it },
            { answer -> conversation.addAi(answer) },
            { conversation.addSystem(it) }
        )
    }
    DisposableEffect(Unit) { onDispose { live.release() } }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val type = context.contentResolver.getType(uri).orEmpty()
            if (type.startsWith("image/")) {
                runCatching { AmarAiImageInput(context).read(uri) }
                    .onSuccess { image = it; attachmentName = "صورة" }
                    .onFailure { conversation.addSystem("تعذر قراءة الصورة") }
            } else {
                attachmentName = uri.lastPathSegment ?: "ملف"
            }
        }
    }

    fun activate() {
        val trimmed = key.trim()
        if (trimmed.isBlank()) {
            activated = false
            conversation.addSystem("أدخل مفتاح Gemini المجاني أولاً، ثم اضغط حفظ وتفعيل.")
            return
        }
        AmarAiKeyStore.save(context, trimmed)
        key = trimmed
        activated = true
        conversation.addSystem("تم حفظ مفتاح Gemini محلياً وتفعيل قناة الذكاء. لم يتم إرسال المفتاح إلى المحادثة.")
    }

    fun deactivate() {
        AmarAiKeyStore.clear(context)
        key = ""
        activated = false
        conversation.addSystem("تم إيقاف قناة Gemini وحذف المفتاح المخزن من مساحة التطبيق.")
    }

    fun testConnection() {
        val trimmed = key.trim()
        if (trimmed.isBlank() || testing || busy) return
        AmarAiKeyStore.save(context, trimmed)
        activated = true
        testing = true
        scope.launch {
            runCatching {
                AmarGeminiClient().generate(
                    trimmed,
                    model.trim(),
                    "أنت AMAR AI Supervisor. أجب بالعربية بجملة قصيرة لتأكيد اتصال API.",
                    "اختبار اتصال فقط. قل: الاتصال يعمل."
                )
            }.onSuccess {
                conversation.addSystem("اختبار Gemini ناجح: $it")
            }.onFailure {
                conversation.addSystem("فشل اختبار Gemini: ${it.message ?: "خطأ غير معروف"}")
            }
            testing = false
        }
    }

    fun send() {
        val prompt = request.trim()
        if (prompt.isBlank() || busy) return
        val local = AmarAiActionEngine.route(prompt)
        conversation.addUser(prompt)
        request = ""
        if (local.handled) {
            conversation.addAi(local.response)
            return
        }
        if (key.isBlank()) {
            conversation.addSystem("القناة الخارجية غير مفعلة. أدخل مفتاح Gemini المجاني واضغط حفظ وتفعيل.")
            return
        }
        AmarAiKeyStore.save(context, key.trim())
        busy = true
        scope.launch {
            runCatching {
                if (image != null) {
                    AmarGeminiVisionClient().analyze(
                        key.trim(), model.trim(),
                        "أنت AMAR AI Supervisor. استخدم الأدلة المتاحة فقط وأجب بالعربية.",
                        prompt, image!!
                    )
                } else {
                    AmarAiAgentEngine(context).ask(key.trim(), model.trim(), prompt).answer
                }
            }.onSuccess { conversation.addAi(it) }
                .onFailure { conversation.addSystem("فشل الذكاء الاصطناعي: ${it.message ?: "غير معروف"}") }
            image = null
            attachmentName = null
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(AiBg)) {
        // Professional command header: identity, channel state and controls are always visible.
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("AMAR AI", color = accentColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("SUPERVISOR CONSOLE  /  AI • NEWS • MARKET", color = AiMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
            Surface(
                color = if (activated) AiGreen.copy(alpha = .12f) else AiRed.copy(alpha = .10f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(Modifier.padding(horizontal = 9.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(7.dp).background(if (activated) AiGreen else AiRed, RoundedCornerShape(50)))
                    Spacer(Modifier.width(5.dp))
                    Text(if (activated) "AI ACTIVE" else "AI OFF", color = if (activated) AiGreen else AiRed, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton({ showAdvanced = !showAdvanced }) {
                Icon(if (showAdvanced) Icons.Default.Close else Icons.Default.Settings, "الإعدادات", tint = accentColor)
            }
        }

        // The AI/news/market convoy is isolated inside this room; it cannot consume another room's layout.
        AmarAi3DOrbitalStage(Modifier.fillMaxWidth())

        if (showAdvanced) {
            Surface(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                color = AiPanel,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("AI CONTROL CENTER", color = AiText, fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("مفتاح Gemini • النموذج • قناة الاتصال", color = AiMuted, fontSize = 8.sp)
                        }
                        if (activated) Icon(Icons.Default.CheckCircle, "مفعل", tint = AiGreen)
                    }
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it; activated = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gemini API Key") },
                        supportingText = { Text("مفتاحك يبقى داخل التخزين المشفر للتطبيق ولا تضعه في GitHub.", fontSize = 8.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF263642),
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor,
                            focusedTextColor = AiText,
                            unfocusedTextColor = AiText
                        )
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Gemini model") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFF263642),
                            focusedLabelColor = accentColor,
                            cursorColor = accentColor,
                            focusedTextColor = AiText,
                            unfocusedTextColor = AiText
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(onClick = { activate() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)) {
                            Text("حفظ وتفعيل", fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                        OutlinedButton(onClick = { testConnection() }, enabled = key.isNotBlank() && !testing && !busy, modifier = Modifier.weight(1f)) {
                            Text(if (testing) "جارٍ الاختبار…" else "اختبار الاتصال", fontSize = 10.sp)
                        }
                        OutlinedButton(onClick = { deactivate() }, enabled = activated, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Text("إيقاف", fontSize = 10.sp, color = AiRed)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("النص", color = AiMuted, fontSize = 8.sp, modifier = Modifier.align(Alignment.CenterVertically))
                        listOf("AUTO" to "تلقائي", "WHITE" to "أبيض", "GOLD" to "ذهبي", "CYAN" to "سماوي").forEach { (value, label) ->
                            FilterChip(selected = textColor == value, onClick = { textColor = value }, label = { Text(label, fontSize = 8.sp) })
                        }
                    }
                }
            }
            Spacer(Modifier.height(7.dp))
        }

        if (attachmentName != null) {
            Surface(Modifier.padding(horizontal = 12.dp), color = AiPanel2, shape = RoundedCornerShape(8.dp)) {
                Text("📎 $attachmentName", color = accentColor, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
            }
        }
        if (transcript.isNotBlank()) {
            Text("LIVE  •  $transcript", color = AiMuted, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp))
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            reverseLayout = true
        ) {
            items(conversation.messages.asReversed()) { message ->
                val user = message.role == AmarAiConversationState.Role.USER
                Surface(
                    color = if (user) AiPanel2 else AiPanel,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, if (user) accentColor.copy(alpha = .15f) else Color(0xFF162530), RoundedCornerShape(12.dp))
                ) {
                    Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                        Text(if (user) "COMMAND / YOU" else if (message.role == AmarAiConversationState.Role.SYSTEM) "SYSTEM" else "AMAR AI", color = if (message.role == AmarAiConversationState.Role.SYSTEM) AiGold else accentColor, fontSize = 7.sp, fontWeight = FontWeight.Black)
                        Text(message.text, color = fg, fontSize = 12.sp, lineHeight = 17.sp)
                        if (!user) {
                            TextButton({ clipboard.setText(AnnotatedString(message.text)) }, contentPadding = PaddingValues(0.dp)) {
                                Text("نسخ", color = AiMuted, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bottom command deck: attachment, voice, text command and a real send button.
        Surface(color = AiPanel, modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                IconButton({ picker.launch("*/*") }) {
                    Icon(Icons.Default.Add, "إرفاق صورة أو ملف", tint = accentColor)
                }
                IconButton({
                    if (liveState == AmarAiLiveConversationEngine.State.IDLE) {
                        live.configure(key, model)
                        live.start()
                    } else {
                        live.stop()
                    }
                }) {
                    Text(if (liveState == AmarAiLiveConversationEngine.State.IDLE) "🎙" else "■", color = accentColor, fontSize = 18.sp)
                }
                OutlinedTextField(
                    value = request,
                    onValueChange = { request = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("اكتب الأمر أو السؤال…", color = AiMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF263642),
                        focusedBorderColor = accentColor,
                        cursorColor = accentColor,
                        focusedTextColor = AiText,
                        unfocusedTextColor = AiText
                    )
                )
                FilledIconButton(
                    onClick = { send() },
                    enabled = request.isNotBlank() && !busy,
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Send, "إرسال")
                }
            }
        }
    }
}
