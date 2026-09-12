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
    val selfImprovement = remember(context) { AmarAiSelfImprovementEngine(context) }
    var key by remember { mutableStateOf(AmarAiKeyStore.load(context).orEmpty()) }
    var model by remember { mutableStateOf("gemini-2.5-flash") }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("اكتب أمرًا للوكيل. مثال: ابحث في مكتبة المؤشرات عن أفضل الأدوات لهذه الحركة في السوق الحالي.") }
    var actions by remember { mutableStateOf<List<String>>(emptyList()) }
    var busy by remember { mutableStateOf(false) }
    var auditBusy by remember { mutableStateOf(false) }
    var audit by remember { mutableStateOf<AmarAiSelfImprovementEngine.Audit?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun runSelfAudit() {
        auditBusy = true
        error = null
        scope.launch {
            runCatching { selfImprovement.audit() }
                .onSuccess { audit = it }
                .onFailure { error = it.message ?: "فشل فحص التطوير" }
            auditBusy = false
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AMAR AI TRADING SUPERVISOR 🤖", style = MaterialTheme.typography.headlineSmall)
                Text("العقل المركزي للتداول: بحث، تحليل، اختبار، تحقق، مخاطر، مراقبة وتطوير ذاتي محكوم بموافقتك.")
                Text("AI يقترح ولا يغيّر الكود أو يعتمد استراتيجية تلقائيًا.", style = MaterialTheme.typography.labelMedium)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("التطوير الذاتي للتداول 🧠", style = MaterialTheme.typography.titleLarge)
                Text("يفحص AI مختبر البوتات ومحركات التداول ومصادر الأدلة، ثم يولد قائمة تحسينات مرتبة بالأولوية ومتطلبات التحقق.")
                Button(onClick = ::runSelfAudit, enabled = !auditBusy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (auditBusy) "جاري الفحص العميق…" else "🔎 اكتشف ما يحتاجه النظام")
                }
                audit?.let { a ->
                    Text("درجة الجاهزية الحالية: ${"%.1f".format(a.score)}/10", style = MaterialTheme.typography.titleMedium)
                    a.warnings.forEach { Text("⚠️ $it", style = MaterialTheme.typography.bodySmall) }
                    a.proposals.forEach { proposal ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("P${proposal.priority} • ${proposal.area} • ${proposal.title}", style = MaterialTheme.typography.titleMedium)
                                Text(proposal.reason)
                                Text("التطوير المقترح: ${proposal.action}")
                                Text("شرط الإثبات: ${proposal.evidenceRequirement}", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { selfImprovement.decide(proposal.id, AmarAiSelfImprovementEngine.Status.APPROVED); audit = selfImprovement.audit() }) { Text("موافقة") }
                                    OutlinedButton(onClick = { selfImprovement.decide(proposal.id, AmarAiSelfImprovementEngine.Status.REJECTED); audit = selfImprovement.audit() }) { Text("رفض") }
                                }
                            }
                        }
                    }
                }
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
            placeholder = { Text("مثال: حلل السوق، ثم ابحث في مكتبة المؤشرات عن الأدوات المناسبة، واختبر الفكرة وأعطني تقريرًا بالأدلة.") }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = key.isNotBlank() && request.isNotBlank() && !busy,
            onClick = {
                AmarAiKeyStore.save(context, key)
                busy = true; error = null
                scope.launch {
                    runCatching { AmarAiAgentEngine(context).ask(key, model.trim(), request.trim()) }
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
