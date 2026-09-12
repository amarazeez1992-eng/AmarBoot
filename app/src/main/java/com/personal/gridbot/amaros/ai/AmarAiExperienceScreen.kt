package com.personal.gridbot.amaros.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AmarAiExperienceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val conversation = remember { AmarAiConversationState() }
    val control = remember { AmarAiControlCenter(context) }
    val notes = remember { AmarAiStrategyNotesRepository(context) }
    val motion = rememberInfiniteTransition(label = "amar-ai-motion")
    val ringRotation by motion.animateFloat(0f, 360f, infiniteRepeatable(tween(4200), RepeatMode.Restart), label = "ring")
    val pulse by motion.animateFloat(.90f, 1.10f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")

    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var liveState by remember { mutableStateOf(AmarAiLiveConversationEngine.State.IDLE) }
    var liveTranscript by remember { mutableStateOf("") }
    var lastAnswer by remember { mutableStateOf("") }
    var strategyName by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf<AmarAiImageInput.ImagePayload?>(null) }
    var aiEnabled by remember { mutableStateOf(control.aiEnabled) }
    var emergency by remember { mutableStateOf(control.emergencyStopped) }

    val liveEngine = remember {
        AmarAiLiveConversationEngine(
            context = context,
            scope = scope,
            onState = { liveState = it },
            onTranscript = { liveTranscript = it },
            onAnswer = { answer -> lastAnswer = answer; conversation.addAi(answer) },
            onError = { conversation.addSystem(it) }
        )
    }
    DisposableEffect(liveEngine) { onDispose { liveEngine.release() } }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching { AmarAiImageInput(context).read(uri) }
            .onSuccess { selectedImage = it; conversation.addSystem("تم إرفاق الصورة. أرسل سؤالك عنها.") }
            .onFailure { conversation.addSystem("تعذر قراءة الصورة: ${it.message}") }
    }

    fun refreshControl() {
        aiEnabled = control.aiEnabled
        emergency = control.emergencyStopped
    }

    fun submit(raw: String) {
        val prompt = raw.trim()
        if (prompt.isBlank() || busy) return
        val local = AmarAiActionEngine.route(prompt)
        if (local.handled) {
            conversation.addUser(prompt)
            conversation.addAi(local.response)
            request = ""
            return
        }
        if (key.isBlank()) { conversation.addSystem("أدخل Gemini API Key أولاً."); return }
        AmarAiKeyStore.save(context, key.trim())
        busy = true
        conversation.addUser(prompt)
        request = ""
        scope.launch {
            runCatching {
                if (selectedImage != null) {
                    AmarGeminiVisionClient().analyze(
                        key.trim(), model.trim(),
                        "أنت AMAR AI. حلل الصورة كدليل بصري فقط ولا تخترع بيانات غير ظاهرة.",
                        prompt, selectedImage!!
                    )
                } else AmarAiAgentEngine(context).ask(key.trim(), model.trim(), prompt).answer
            }.onSuccess { answer -> lastAnswer = answer; conversation.addAi(answer) }
                .onFailure { conversation.addSystem("خطأ AI: ${it.message ?: "غير معروف"}") }
            selectedImage = null
            busy = false
        }
    }

    fun toggleLive() {
        if (liveState == AmarAiLiveConversationEngine.State.IDLE) {
            liveEngine.configure(key, model)
            liveEngine.start()
        } else liveEngine.stop()
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF7F8FB))) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("AMAR AI", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF171923))
                    Text("مساعدك الداخلي — صوت، صورة، محادثة وتحكم", color = Color(0xFF626775), style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = CircleShape, color = if (aiEnabled && !emergency) Color(0xFFE3FFF3) else Color(0xFFFFE6E9)) {
                    Text(if (aiEnabled && !emergency) "AI ON" else "AI OFF", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = if (aiEnabled && !emergency) Color(0xFF087443) else Color(0xFFAE2638))
                }
            }

            AvatarPanel(liveState = liveState, rotation = ringRotation, pulse = pulse, transcript = liveTranscript, answer = lastAnswer)

            Card(Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp), reverseLayout = true) {
                    items(conversation.messages.asReversed()) { message ->
                        val mine = message.role == AmarAiConversationState.Role.USER
                        Surface(shape = RoundedCornerShape(18.dp), color = if (mine) Color(0xFFEAF5FF) else Color(0xFFF2F1FF), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(if (mine) "أنت" else if (message.role == AmarAiConversationState.Role.AI) "AMAR AI" else "النظام", style = MaterialTheme.typography.labelMedium, color = Color(0xFF4D4F5B))
                                Text(message.text, color = Color(0xFF20222A))
                                if (!mine && message.text.isNotBlank()) {
                                    IconButton(onClick = { clipboard.setText(androidx.compose.ui.text.AnnotatedString(message.text)) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Default.ContentCopy, "نسخ") }
                                }
                            }
                        }
                    }
                }
            }

            // ChatGPT/Gemini-like composer with an animated perimeter.
            Box(Modifier.fillMaxWidth().padding(horizontal = 1.dp)) {
                Box(
                    Modifier.matchParentSize().padding(1.dp).clip(RoundedCornerShape(25.dp)).rotate(ringRotation / 18f)
                        .background(Brush.sweepGradient(listOf(Color(0xFFFFD21F), Color(0xFFFF3D3D), Color(0xFFFF8A00), Color(0xFFFFD21F))))
                )
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth().padding(2.dp)) {
                    Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (selectedImage != null) {
                            Text("📷 صورة جاهزة للتحليل", color = Color(0xFF6D4AFF), style = MaterialTheme.typography.labelMedium)
                        }
                        OutlinedTextField(
                            value = request,
                            onValueChange = { request = it },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                            placeholder = { Text("اكتب لـ AMAR AI أو اضغط الاتصال للتحدث مباشرة…") },
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFFFFB000))
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(onClick = { imagePicker.launch("image/*") }) { Icon(Icons.Default.AddPhotoAlternate, "إرسال صورة", tint = Color(0xFF6254D9)) }
                                IconButton(onClick = { request = ""; selectedImage = null }) { Icon(Icons.Default.Delete, "حذف", tint = Color(0xFFE05252)) }
                                IconButton(onClick = { clipboard.getText()?.text?.let { request = it } }) { Icon(Icons.Default.ContentCopy, "لصق", tint = Color(0xFF536274)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(onClick = { toggleLive() }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = if (liveState == AmarAiLiveConversationEngine.State.IDLE) Color(0xFFFFE5B3) else Color(0xFFFFD1D1))) {
                                    Icon(if (liveState == AmarAiLiveConversationEngine.State.IDLE) Icons.Default.Mic else Icons.Default.Stop, "محادثة مباشرة")
                                }
                                FilledIconButton(onClick = { submit(request) }, enabled = request.isNotBlank() && !busy) { Icon(Icons.Default.Send, "إرسال") }
                            }
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = { submit("حلل السوق الآن") }, label = { Text("تحليل السوق") }, modifier = Modifier.weight(1f))
                AssistChip(onClick = { submit("اذهب إلى الإعدادات") }, label = { Text("الإعدادات") }, modifier = Modifier.weight(1f))
                AssistChip(onClick = { submit("افتح الرسم البياني") }, label = { Text("الشارت") }, modifier = Modifier.weight(1f))
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("الصلاحية: ${if (aiEnabled && !emergency) "مفعّلة" else "متوقفة"}", color = Color(0xFF606571), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { if (emergency) control.clearEmergencyStop() else control.emergencyStop(); refreshControl() }) {
                    Text(if (emergency) "استئناف" else "إيقاف طوارئ", color = if (emergency) Color(0xFF087443) else Color(0xFFB3263A))
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedTextField(key, { key = it }, Modifier.weight(1f), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(model, { model = it }, Modifier.width(155.dp), label = { Text("Model") }, singleLine = true)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedTextField(strategyName, { strategyName = it }, Modifier.weight(1f), label = { Text("اسم الاستراتيجية") }, singleLine = true)
                Button(onClick = {
                    if (strategyName.isNotBlank()) {
                        notes.save(strategyName.trim(), conversation.messages.joinToString("\n\n") { it.text }, "DRAFT")
                        strategyName = ""
                    }
                }, enabled = strategyName.isNotBlank()) { Text("حفظ") }
            }
        }
    }
}

@Composable
private fun AvatarPanel(liveState: AmarAiLiveConversationEngine.State, rotation: Float, pulse: Float, transcript: String, answer: String) {
    val active = liveState != AmarAiLiveConversationEngine.State.IDLE
    Card(Modifier.fillMaxWidth().height(190.dp), shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF10131C)), elevation = CardDefaults.cardElevation(5.dp)) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f; val cy = size.height * .46f
                drawCircle(Color(0xFF1D2638), radius = 48f * pulse, center = androidx.compose.ui.geometry.Offset(cx, cy))
                drawCircle(Color(0xFFFFC928), radius = 58f, center = androidx.compose.ui.geometry.Offset(cx, cy), style = Stroke(2.5f, cap = StrokeCap.Round))
                drawArc(Color(0xFFFF4D3D), 0f + rotation, 115f, false, androidx.compose.ui.geometry.Offset(cx - 76f, cy - 76f), androidx.compose.ui.geometry.Size(152f, 152f), style = Stroke(5f, cap = StrokeCap.Round))
                drawArc(Color(0xFFFFC928), 160f + rotation, 75f, false, androidx.compose.ui.geometry.Offset(cx - 88f, cy - 88f), androidx.compose.ui.geometry.Size(176f, 176f), style = Stroke(3f, cap = StrokeCap.Round))
                drawCircle(Color(0xFFFFD64A).copy(alpha = if (active) .95f else .55f), radius = 22f * pulse, center = androidx.compose.ui.geometry.Offset(cx, cy))
                drawCircle(Color.White.copy(alpha = if (active) .85f else .35f), radius = 8f, center = androidx.compose.ui.geometry.Offset(cx - 7f, cy - 7f))
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(when (liveState) {
                    AmarAiLiveConversationEngine.State.LISTENING -> "يستمع إليك…"
                    AmarAiLiveConversationEngine.State.THINKING -> "يفكر ويحلل…"
                    AmarAiLiveConversationEngine.State.SPEAKING -> "يتحدث معك…"
                    else -> "AMAR AI"
                }, color = Color(0xFFFFD75A), style = MaterialTheme.typography.labelLarge)
                if (active && transcript.isNotBlank()) Text(transcript.takeLast(90), color = Color(0xFFDDE5F2), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
