package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmarAiExperienceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("أنا جاهز للتحليل والبحث والمحاكاة — بدون تنفيذ تداول مباشر.") }
    var busy by remember { mutableStateOf(false) }
    val transition = rememberInfiniteTransition(label = "ai3d")
    val phase by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(7000, easing = LinearEasing)), label = "phase")
    val pulse by transition.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")
    val palette = listOf(Color(0xFF00E5FF), Color(0xFF7C4DFF), Color(0xFFFF3CAC), Color(0xFFFFC857), Color(0xFF151022))
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(250.dp).background(Brush.linearGradient(palette), RoundedCornerShape(34.dp)).shadow(28.dp, RoundedCornerShape(34.dp)).border(1.dp, Color.White.copy(.28f), RoundedCornerShape(34.dp))) {
            Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.White.copy(.20f), Color.Transparent), radius = 500f), RoundedCornerShape(34.dp)))
            Box(Modifier.align(Alignment.Center).size(132.dp).graphicsLayer { scaleX = pulse; scaleY = pulse; rotationZ = phase / 12f }.shadow(30.dp, CircleShape).background(Brush.radialGradient(listOf(Color.White, palette[0], palette[1], palette[2], Color.Transparent)), CircleShape))
            Box(Modifier.align(Alignment.Center).size(174.dp).graphicsLayer { rotationZ = -phase / 8f }.border(2.dp, Color.White.copy(.32f), CircleShape))
            Text("AMAR AI", Modifier.align(Alignment.Center), color = Color.White, fontSize = 23.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
            Text("LIVE ANALYSIS • ADVISORY", Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp), color = Color.White.copy(.86f), fontSize = 11.sp)
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF171022).copy(.96f)), shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("لوحة الذكاء التفاعلية", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC857))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("تحليل السوق", "اختبار", "المكتبة").forEachIndexed { i, label ->
                        Box(Modifier.weight(1f).background(palette[i + 1].copy(.16f), RoundedCornerShape(16.dp)).border(1.dp, palette[i + 1].copy(.55f), RoundedCornerShape(16.dp)).padding(10.dp)) { Text(label, color = Color.White, fontSize = 11.sp) }
                    }
                }
                OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }, singleLine = true)
                OutlinedTextField(request, { request = it }, Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("ماذا تريد من AMAR AI؟") }, placeholder = { Text("حلل السوق الحالي، ابحث عن استراتيجية مناسبة، أو جهز اختباراً…") })
                Button(onClick = {
                    AmarAiKeyStore.save(context, key); busy = true
                    scope.launch { runCatching { AmarAiAgentEngine().ask(key, model.trim(), request.trim()) }.onSuccess { answer = it.answer }.onFailure { answer = it.message ?: "حدث خطأ" }; busy = false }
                }, Modifier.fillMaxWidth(), enabled = key.isNotBlank() && request.isNotBlank() && !busy) { Text(if (busy) "AI يعمل…" else "تشغيل التحليل التفاعلي") }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0A17))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("نتيجة AMAR AI", color = Color(0xFF00E5FF), fontSize = 18.sp); Text(answer, color = Color.White); Text("⚠️ الذكاء الاصطناعي استشاري؛ لا يضع أو يعدّل أو يغلق أوامر الوسيط.", color = Color(0xFFFFC857), fontSize = 11.sp) }
        }
    }
}
