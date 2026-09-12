package com.personal.gridbot.amaros.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AmarAiExperienceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("أنا جاهز للتحليل والبحث والمحاكاة — بدون تنفيذ تداول مباشر.") }
    var busy by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AmarAiPremiumCoreVisual(Modifier.fillMaxWidth())
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF171022).copy(.96f))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("لوحة الذكاء التفاعلية", style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC857))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("تحليل السوق" to Color(0xFF31E7FF), "اختبار" to Color(0xFFFF2FAE), "المكتبة" to Color(0xFF7C4DFF)).forEach { (label, color) ->
                        Box(Modifier.weight(1f).background(color.copy(.16f), RoundedCornerShape(16.dp)).border(1.dp, color.copy(.55f), RoundedCornerShape(16.dp)).padding(10.dp)) { Text(label, color = Color.White) }
                    }
                }
                OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                OutlinedTextField(model, { model = it }, Modifier.fillMaxWidth(), label = { Text("Model") }, singleLine = true)
                OutlinedTextField(request, { request = it }, Modifier.fillMaxWidth().heightIn(min = 100.dp), label = { Text("ماذا تريد من AMAR AI؟") })
                Button(onClick = {
                    AmarAiKeyStore.save(context, key); busy = true
                    scope.launch { runCatching { AmarAiAgentEngine().ask(key, model.trim(), request.trim()) }.onSuccess { answer = it.answer }.onFailure { answer = it.message ?: "حدث خطأ" }; busy = false }
                }, Modifier.fillMaxWidth(), enabled = key.isNotBlank() && request.isNotBlank() && !busy) { Text(if (busy) "AI يعمل…" else "تشغيل التحليل التفاعلي") }
            }
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0A17))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نتيجة AMAR AI", color = Color(0xFF31E7FF), style = MaterialTheme.typography.titleLarge)
                Text(answer, color = Color.White)
                Text("⚠️ الذكاء الاصطناعي استشاري؛ لا يضع أو يعدّل أو يغلق أوامر الوسيط.", color = Color(0xFFFFC857))
            }
        }
    }
}
