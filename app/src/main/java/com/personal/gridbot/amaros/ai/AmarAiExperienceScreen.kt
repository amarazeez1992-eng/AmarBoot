package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
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
    val control = remember { AmarAiControlCenter(context) }
    val notes = remember { AmarAiStrategyNotesRepository(context) }
    val motion = rememberInfiniteTransition(label = "ai-room-motion")
    val drift by motion.animateFloat(-18f, 18f, infiniteRepeatable(tween(3600), RepeatMode.Reverse), label = "ai-drift")
    val pulse by motion.animateFloat(.82f, 1.12f, infiniteRepeatable(tween(1800), RepeatMode.Reverse), label = "ai-pulse")
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(key.isNotBlank()) }
    var aiEnabled by remember { mutableStateOf(control.aiEnabled) }
    var emergency by remember { mutableStateOf(control.emergencyStopped) }
    var strategyName by remember { mutableStateOf("") }
    var controlRevision by remember { mutableIntStateOf(0) }

    fun refreshControl() {
        aiEnabled = control.aiEnabled
        emergency = control.emergencyStopped
        controlRevision++
    }

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
            runCatching { AmarAiAgentEngine(context).ask(key.trim(), model.trim(), prompt) }
                .onSuccess { conversation.addAi(it.answer) }
                .onFailure { conversation.addSystem(it.message ?: "حدث خطأ غير معروف"); error = true }
            busy = false
        }
    }

    fun saveConversation() {
        val name = strategyName.trim()
        if (name.isBlank()) return
        val content = conversation.messages.joinToString("\n\n") { message ->
            val role = when (message.role) {
                AmarAiConversationState.Role.USER -> "عمار"
                AmarAiConversationState.Role.AI -> "AMAR AI"
                AmarAiConversationState.Role.SYSTEM -> "النظام"
            }
            "$role: ${message.text}"
        }
        notes.save(name, content, "DRAFT")
        conversation.addSystem("تم حفظ الاستراتيجية $name في دفتر استراتيجيات AMAR AI. الاعتماد النهائي يبقى بقرارك.")
        strategyName = ""
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF101A46), Color(0xFF32114F), Color(0xFF5B184F), Color(0xFF123F59))))) {
        Box(Modifier.offset(x = drift.dp, y = (-drift * .55f).dp).size(240.dp).alpha(.22f * pulse).blur(50.dp).background(Brush.radialGradient(listOf(Color(0xFF31E7FF), Color.Transparent)), CircleShape))
        Box(Modifier.offset(x = (-drift * .7f).dp, y = (drift * .8f).dp).size(260.dp).alpha(.18f).blur(55.dp).background(Brush.radialGradient(listOf(Color(0xFFFF4FA3), Color.Transparent)), CircleShape))

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AmarAiPremiumCoreVisual(Modifier.fillMaxWidth())

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF17265A).copy(.86f))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("AMAR AI — مركز الصلاحية", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFD66B))
                    Text("اليدوي يبقى مستقلاً. تشغيل AI يمنح غرفة AI صلاحية العمل ضمن الحدود المعتمدة.", color = Color(0xFFC9F6FF), style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (aiEnabled && !emergency) "AI: مفعّل" else "AI: متوقف", color = if (aiEnabled && !emergency) Color(0xFF65F5C0) else Color(0xFFFF7F8E))
                        Switch(checked = aiEnabled && !emergency, onCheckedChange = {
                            if (it) control.enableAi() else control.disableAi()
                            refreshControl()
                        }, enabled = !emergency)
                    }
                    Button(
                        onClick = {
                            if (emergency) control.clearEmergencyStop() else control.emergencyStop()
                            refreshControl()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (emergency) Color(0xFF1FAF68) else Color(0xFFB3263A))
                    ) {
                        Text(if (emergency) "استئناف النظام" else "إيقاف طوارئ — أوقف صلاحية AI")
                    }
                    Text(
                        if (emergency) "وضع الطوارئ: AI مقفول. التشغيل اليدوي لا يعتمد على AI."
                        else "زر الطوارئ مستقل عن AI ولا يغلق صفقات MT5 تلقائياً.",
                        color = Color(0xFFFFD66B), style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF17265A).copy(.78f))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("AMAR AI — غرفة المحادثة", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFD66B))
                            Text("تحليل، نقد استراتيجية، اختبار، اقتراح، وحفظ — بدون ادعاء تنفيذ MT5 في هذه المرحلة.", color = Color(0xFFC9F6FF), style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { conversation.clear() }, enabled = !busy) { Text("جلسة جديدة", color = Color(0xFF8FEFFF)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        AssistChip(onClick = { runPrompt(AmarAiShortcutContract.MARKET_ANALYSIS) }, label = { Text("تحليل السوق") }, modifier = Modifier.weight(1f), enabled = !busy)
                        AssistChip(onClick = { runPrompt(AmarAiShortcutContract.STRATEGY_TEST) }, label = { Text("اختبار") }, modifier = Modifier.weight(1f), enabled = !busy)
                        AssistChip(onClick = { runPrompt(AmarAiShortcutContract.LIBRARY_SEARCH) }, label = { Text("المكتبة") }, modifier = Modifier.weight(1f), enabled = !busy)
                    }
                    OutlinedTextField(key, { key = it; saved = false }, Modifier.fillMaxWidth(), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (saved) "✓ المفتاح محفوظ محليًا" else "المفتاح غير محفوظ", color = if (saved) Color(0xFF65F5C0) else Color(0xFFFFD66B), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { AmarAiKeyStore.clear(context); key = ""; saved = false }) { Text("مسح", color = Color(0xFFFF9BBE)) }
                    }
                    OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }, singleLine = true)
                    OutlinedTextField(request, { request = it; error = false }, Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("اكتب رسالتك إلى AMAR AI") })
                    Button(onClick = { runPrompt(request) }, Modifier.fillMaxWidth(), enabled = key.isNotBlank() && model.isNotBlank() && request.isNotBlank() && !busy) {
                        if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text("إرسال إلى AMAR AI")
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(strategyName, { strategyName = it }, Modifier.weight(1f), label = { Text("اسم الاستراتيجية") }, singleLine = true)
                        Button(onClick = { saveConversation() }, enabled = strategyName.isNotBlank()) { Text("حفظ") }
                    }
                    if (controlRevision >= 0) {
                        Text("الاستراتيجيات تحفظ بإصدارات مستقلة في دفتر AMAR AI، دون استبدال Bot Vault.", color = Color(0xFF9EDCFF), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2450).copy(.82f))) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("المحادثة", color = Color(0xFF65F5FF), style = MaterialTheme.typography.titleLarge)
                    conversation.messages.forEach { message ->
                        val bg = when (message.role) {
                            AmarAiConversationState.Role.USER -> Color(0xFF204B6D).copy(.92f)
                            AmarAiConversationState.Role.AI -> Color(0xFF4A276A).copy(.92f)
                            AmarAiConversationState.Role.SYSTEM -> Color(0xFF563C4D).copy(.90f)
                        }
                        val label = when (message.role) {
                            AmarAiConversationState.Role.USER -> "أنت"
                            AmarAiConversationState.Role.AI -> "AMAR AI"
                            AmarAiConversationState.Role.SYSTEM -> "النظام"
                        }
                        Surface(shape = RoundedCornerShape(16.dp), color = bg, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(label, color = if (message.role == AmarAiConversationState.Role.AI) Color(0xFF65F5FF) else Color(0xFFFFD66B), style = MaterialTheme.typography.labelMedium)
                                Text(message.text, color = Color.White)
                            }
                        }
                    }
                    Text("⚠️ التنفيذ عبر MT5 غير موصول في هذه المرحلة. عند ربطه لاحقاً، تمر الأوامر عبر طبقة الصلاحية والإيقاف الطارئ والتحقق بعد التنفيذ.", color = Color(0xFFFFD66B), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
