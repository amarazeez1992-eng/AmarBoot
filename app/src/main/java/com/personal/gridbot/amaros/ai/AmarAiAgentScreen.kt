package com.personal.gridbot.amaros.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Internal Agent workspace. The screen only sends requests to the canonical Agent boundary. */
@Composable
fun AmarAiAgentScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selfImprovement = remember(context) { AmarAiSelfImprovementEngine(context) }
    val agent = remember(context) { AmarAiAgentEngine(context) }
    var request by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("أنا AMAR AI Agent. اكتب طلبك وسأخطط له، أتحقق من الأدلة، وأغلق أي مسار غير موثّق.") }
    var actions by remember { mutableStateOf<List<String>>(emptyList()) }
    var evidence by remember { mutableStateOf<List<String>>(emptyList()) }
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
                Text("AMAR AI AGENT — CENTRAL AUTHORITY", style = MaterialTheme.typography.headlineSmall)
                Text("العقل المركزي للتطبيق: التخطيط، الأدلة، البحث، التحقق، النقد، الذاكرة والقرارات المحكومة. واجهة التطبيق لا تملك سلطة مستقلة.")
                Text("التنفيذ الحساس وMT5 خارج هذه المرحلة ومغلقان افتراضيًا.", style = MaterialTheme.typography.labelMedium)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("التطوير الذاتي", style = MaterialTheme.typography.titleLarge)
                Text("يفحص الوكيل المكونات الحالية ويعرض مقترحات تحتاج إلى قرار ومبررات وإثبات.")
                Button(onClick = ::runSelfAudit, enabled = !auditBusy, modifier = Modifier.fillMaxWidth()) {
                    Text(if (auditBusy) "جاري الفحص…" else "🔎 فحص الوكيل والنظام")
                }
                audit?.let { a ->
                    Text("درجة الجاهزية المقاسة: ${"%.1f".format(a.score)}/10", style = MaterialTheme.typography.titleMedium)
                    a.warnings.forEach { Text("⚠️ $it", style = MaterialTheme.typography.bodySmall) }
                    a.proposals.forEach { proposal ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text("P${proposal.priority} • ${proposal.area} • ${proposal.title}", style = MaterialTheme.typography.titleMedium)
                                Text(proposal.reason)
                                Text("المقترح: ${proposal.action}")
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

        OutlinedTextField(
            request,
            { request = it },
            Modifier.fillMaxWidth().heightIn(min = 120.dp),
            label = { Text("أمر Amar AI Agent") },
            placeholder = { Text("مثال: حلل السوق، اجمع الأدلة، اختبر الفكرة، ثم أعطني تقريرًا موثقًا مع ما هو مؤكد وما يزال مجهولًا.") }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = request.isNotBlank() && !busy,
            onClick = {
                busy = true
                error = null
                scope.launch {
                    runCatching { agent.ask(request.trim()) }
                        .onSuccess { result ->
                            answer = result.answer
                            actions = result.proposedActions
                            evidence = result.toolEvidence
                        }
                        .onFailure { error = it.message ?: "حدث خطأ" }
                    busy = false
                }
            }
        ) { Text(if (busy) "الوكيل يعمل…" else "تشغيل AMAR AI Agent") }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نتيجة الوكيل", style = MaterialTheme.typography.titleLarge)
                Text(answer)
                if (actions.isNotEmpty()) {
                    Text("الأدوات/المسارات التي حددها الوكيل", style = MaterialTheme.typography.titleMedium)
                    actions.forEach { Text("• $it") }
                }
                if (evidence.isNotEmpty()) {
                    Text("سجل الدورة", style = MaterialTheme.typography.titleMedium)
                    evidence.takeLast(30).forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                }
                Text("حالة التنفيذ: FAIL-CLOSED — لا توجد صلاحية تنفيذ تداول في هذه المرحلة.", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
