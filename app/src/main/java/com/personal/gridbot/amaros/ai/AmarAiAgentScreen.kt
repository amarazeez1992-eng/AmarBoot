package com.personal.gridbot.amaros.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AmarAiAgentScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("اكتب أمرًا للوكيل. مثال: ابحث في مكتبة المؤشرات عن أفضل الأدوات لهذه الحركة في السوق الحالي.") }
    var actions by remember { mutableStateOf<List<String>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AMAR AI SUPERVISOR 🤖", style = MaterialTheme.typography.headlineSmall)
                Text("تحليل + بحث داخل الوحدات + إعداد خطط الاختبار + مقترحات تنفيذ. لا يوجد تداول مباشر من AI.")
            }
        }
        OutlinedTextField(key, { key = it }, Modifier.fillMaxWidth(), label = { Text("Gemini API Key") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(model, { model = it }, Modifier.weight(1f), label = { Text("Model") }, singleLine = true)
            Button(onClick = { AmarAiKeyStore.save(context, key) }, enabled = key.isNotBlank()) { Text("حفظ") }
        }
        Text("المفتاح يُخزّن محليًا مشفّرًا عبر Android Keystore.", style = MaterialTheme.typography.labelSmall)
        OutlinedTextField(
            request, { request = it }, Modifier.fillMaxWidth().heightIn(min = 120.dp),
            label = { Text("أمر Amar AI") },
            placeholder = { Text("مثال: حلل السوق، ثم ابحث في مكتبة المؤشرات عن الأدوات المناسبة، وأعطني قرارًا مع الأسباب.") }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = key.isNotBlank() && request.isNotBlank() && !busy,
            onClick = {
                AmarAiKeyStore.save(context, key)
                busy = true; error = null
                scope.launch {
                    runCatching { AmarAiAgentEngine().ask(key, model.trim(), request.trim()) }
                        .onSuccess { result -> answer = result.answer; actions = result.proposedActions }
                        .onFailure { error = it.message ?: "حدث خطأ" }
                    busy = false
                }
            }
        ) { Text(if (busy) "الوكيل يعمل…" else "تشغيل Amar AI") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("النتيجة", style = MaterialTheme.typography.titleLarge)
                Text(answer)
                if (actions.isNotEmpty()) {
                    Text("المقترحات / الأدوات المطلوبة", style = MaterialTheme.typography.titleMedium)
                    actions.forEach { Text("• $it") }
                    Text("⚠️ لم يتم تنفيذ أي أمر تداول. هذه المقترحات تنتظر قرارك.", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
