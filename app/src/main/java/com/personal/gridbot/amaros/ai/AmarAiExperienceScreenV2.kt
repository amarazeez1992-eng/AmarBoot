package com.personal.gridbot.amaros.ai

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

private val Black = Color(0xFF05070A)
private val Gold = Color(0xFFFFC84A)
private val Cyan = Color(0xFF1DE5FF)
private val White = Color(0xFFF4F7FA)
private val Panel = Color(0xFF0B1117)
private val Line = Color(0xFF263642)

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
    var showSettings by remember { mutableStateOf(false) }
    var textColor by remember { mutableStateOf("AUTO") }
    var accent by remember { mutableStateOf("CYAN") }
    var liveState by remember { mutableStateOf(AmarAiLiveConversationEngine.State.IDLE) }
    var transcript by remember { mutableStateOf("") }
    var image by remember { mutableStateOf<AmarAiImageInput.ImagePayload?>(null) }
    var attachmentName by remember { mutableStateOf<String?>(null) }

    val dark = isSystemInDarkTheme()
    val bg = Black
    val panel = Panel
    val fg = when (textColor) {
        "GOLD" -> Gold
        "CYAN" -> Cyan
        "WHITE" -> White
        "BLACK" -> Color.White
        else -> if (dark) White else Color(0xFFEAF1F5)
    }
    val accentColor = if (accent == "GOLD") Gold else Cyan

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
            conversation.addSystem("الذكاء الاصطناعي الخارجي يحتاج مفتاح Gemini من الإعدادات. الأوامر المحلية تعمل دون مفتاح.")
            return
        }
        AmarAiKeyStore.save(context, key.trim())
        busy = true
        scope.launch {
            runCatching {
                if (image != null) {
                    AmarGeminiVisionClient().analyze(key.trim(), model.trim(), "أنت AMAR AI Supervisor. استخدم الأدلة المتاحة فقط وأجب بالعربية.", prompt, image!!)
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

    Column(Modifier.fillMaxSize().background(bg)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AMAR AI", color = accentColor, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(if (liveState == AmarAiLiveConversationEngine.State.IDLE) "المشرف الذكي • جاهز" else "الذكاء يعمل • ${liveState.name}", color = fg.copy(alpha = .7f), fontSize = 9.sp)
            }
            IconButton({ showSettings = !showSettings }) { Icon(Icons.Default.Settings, "الإعدادات", tint = accentColor) }
        }

        AmarAi3DOrbitalStage(Modifier.fillMaxWidth())

        if (showSettings) {
            Card(Modifier.fillMaxWidth().padding(horizontal = 10.dp), colors = CardDefaults.cardColors(panel), shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("إعدادات الذكاء الاصطناعي", color = fg, fontWeight = FontWeight.Black)
                    OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("مفتاح Gemini") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("النموذج") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("AUTO" to "تلقائي", "WHITE" to "أبيض", "GOLD" to "ذهبي", "CYAN" to "سماوي", "BLACK" to "فاتح").forEach { (value, label) ->
                            FilterChip(selected = textColor == value, onClick = { textColor = value }, label = { Text(label, fontSize = 8.sp) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        FilterChip(selected = accent == "CYAN", onClick = { accent = "CYAN" }, label = { Text("سماوي") })
                        FilterChip(selected = accent == "GOLD", onClick = { accent = "GOLD" }, label = { Text("ذهبي") })
                    }
                }
            }
        }

        if (attachmentName != null) Text("📎 $attachmentName", color = accentColor, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 14.dp))

        if (transcript.isNotBlank()) Text(transcript, color = fg.copy(alpha = .55f), fontSize = 8.sp, modifier = Modifier.padding(horizontal = 14.dp))

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            reverseLayout = true
        ) {
            items(conversation.messages.asReversed()) { message ->
                val user = message.role == AmarAiConversationState.Role.USER
                Surface(color = if (user) panel else panel.copy(alpha = .82f), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(10.dp)) {
                        Text(if (user) "أنت" else "AMAR AI", color = accentColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(message.text, color = fg, fontSize = 12.sp)
                        if (!user) TextButton({ clipboard.setText(AnnotatedString(message.text)) }) { Text("نسخ", fontSize = 8.sp) }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            IconButton({ picker.launch("*/*") }) { Icon(Icons.Default.Add, "إرفاق صورة أو ملف", tint = accentColor) }
            IconButton({
                if (liveState == AmarAiLiveConversationEngine.State.IDLE) { live.configure(key, model); live.start() } else live.stop()
            }) { Text(if (liveState == AmarAiLiveConversationEngine.State.IDLE) "🎙" else "■", color = accentColor, fontSize = 18.sp) }
            OutlinedTextField(
                request,
                { request = it },
                Modifier.weight(1f),
                placeholder = { Text("اكتب سؤالك أو أمرك…", color = fg.copy(alpha = .45f)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Line, focusedBorderColor = accentColor, cursorColor = accentColor, focusedTextColor = fg, unfocusedTextColor = fg)
            )
            FilledIconButton(onClick = { send() }, enabled = request.isNotBlank() && !busy, colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor, contentColor = Color.Black)) {
                Icon(Icons.Default.Send, "إرسال")
            }
        }
    }
}
