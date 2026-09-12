package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF07121B)
private val Panel = Color(0xFF0D202B)
private val Panel2 = Color(0xFF102A37)
private val Ink = Color(0xFFE9FBFF)
private val Muted = Color(0xFF8CA9B5)
private val Cyan = Color(0xFF19E6FF)
private val Teal = Color(0xFF00F0A8)
private val Gold = Color(0xFFFFC84D)
private val Red = Color(0xFFFF5364)
private val Line = Color(0xFF214452)

@Composable
fun Bot1ProfessionalScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repository.load()) }
    var selectedBot by remember { mutableIntStateOf(bots.firstOrNull()?.botNumber ?: 1) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var editing by remember { mutableStateOf(false) }
    var editingBotName by remember { mutableStateOf(false) }
    var botNameDraft by remember { mutableStateOf("") }
    var draftName by remember { mutableStateOf("") }
    var draftRisk by remember { mutableStateOf("قياسي") }
    var draftRebuild by remember { mutableStateOf("يدوي") }
    var draftEntry by remember { mutableStateOf("أساسي") }
    var draftMetadata by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf(AmarBot1RuntimeConfig()) }

    fun persist(updated: List<AmarSavedBot>) { bots = updated; repository.save(updated) }
    val current = bots.firstOrNull { it.botNumber == selectedBot }
    val strategies = current?.strategies.orEmpty()

    fun openEditor(number: Int, saved: AmarSavedStrategy?) {
        selectedStrategy = number
        draft = saved?.profile ?: AmarBot1RuntimeConfig()
        draftName = saved?.name ?: "استراتيجية رقم $number"
        draftRisk = saved?.riskProfile ?: "قياسي"
        draftRebuild = saved?.rebuildRule ?: "يدوي"
        draftEntry = saved?.entryRule ?: "أساسي"
        draftMetadata = saved?.metadata ?: ""
        editing = true
    }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("خزنة البوتات", color = Cyan, fontSize = 27.sp, fontWeight = FontWeight.Black)
                        Text("BOT 1 / ملفات البوت والاستراتيجيات المحفوظة", color = Muted, fontSize = 10.sp)
                    }
                    Button(onClick = {
                        val next = ((bots.maxOfOrNull { it.botNumber } ?: 0) + 1)
                        val created = AmarSavedBot(next, "بوت $next")
                        persist(bots + created); selectedBot = next; selectedStrategy = 1
                    }, colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black)) { Text("+ بوت") }
                }
            }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    bots.forEach { bot ->
                        val active = bot.botNumber == selectedBot
                        Column(Modifier.width(110.dp).background(if (active) Panel2 else Panel, RoundedCornerShape(18.dp)).border(1.dp, if (active) Cyan else Line, RoundedCornerShape(18.dp)).clickable { selectedBot = bot.botNumber; selectedStrategy = 1 }.padding(10.dp)) {
                            Text(bot.name, color = if (active) Cyan else Ink, fontWeight = FontWeight.Black)
                            Text("${bot.strategies.size}/10 استراتيجية", color = Muted, fontSize = 9.sp)
                        }
                    }
                }
            }
            if (current == null) {
                item { EmptyState { val next = ((bots.maxOfOrNull { it.botNumber } ?: 0) + 1); persist(bots + AmarSavedBot(next, "بوت $next")); selectedBot = next } }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton("تعديل اسم البوت", Cyan) { botNameDraft = current.name; editingBotName = true }
                        ActionButton("إعادة تهيئة", Gold) {
                            persist(bots.map { if (it.botNumber == selectedBot) it.copy(name = "بوت $selectedBot", strategies = emptyList()) else it }); selectedStrategy = 1; editing = false
                        }
                        ActionButton("حذف البوت", Red) {
                            val remaining = bots.filterNot { it.botNumber == selectedBot }
                            persist(remaining); selectedBot = remaining.firstOrNull()?.botNumber ?: 1; selectedStrategy = 1; editing = false
                        }
                    }
                }
                if (editingBotName) {
                    item {
                        Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                OutlinedTextField(botNameDraft, { botNameDraft = it }, Modifier.fillMaxWidth(), label = { Text("اسم البوت") })
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ActionButton("حفظ", Teal) { persist(bots.map { if (it.botNumber == selectedBot) it.copy(name = botNameDraft.ifBlank { "بوت $selectedBot" }) else it }); editingBotName = false }
                                    ActionButton("إلغاء", Muted) { editingBotName = false }
                                }
                            }
                        }
                    }
                }
                item { Text("استراتيجيات ${current.name}", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black) }
                item {
                    ActionButton(if (strategies.size < 10) "+ استراتيجية" else "الخانات مكتملة 10/10", Teal) {
                        if (strategies.size < 10) {
                            val next = (1..10).first { n -> strategies.none { it.number == n } }
                            val created = AmarSavedStrategy(next, "استراتيجية رقم $next", AmarBot1RuntimeConfig())
                            persist(bots.map { if (it.botNumber == selectedBot) it.copy(strategies = (it.strategies + created).sortedBy { s -> s.number }) else it })
                            openEditor(next, created)
                        }
                    }
                }
                item { Text("الخانات 01 — 10", color = Gold, fontWeight = FontWeight.Bold) }
                (1..10).forEach { number ->
                    item {
                        val saved = strategies.firstOrNull { it.number == number }
                        StrategyRow(number, saved != null, number == selectedStrategy, saved?.name ?: "خانة فارغة") { openEditor(number, saved) }
                    }
                }
                if (editing) {
                    item { StrategyEditor(draftName, draft, draftRisk, draftRebuild, draftEntry, draftMetadata, { draftName = it }, { draft = it }, { draftRisk = it }, { draftRebuild = it }, { draftEntry = it }, { draftMetadata = it }) }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("حفظ الاستراتيجية ✓", Teal) {
                                val n = selectedStrategy.coerceIn(1, 10)
                                val item = AmarSavedStrategy(n, draftName.ifBlank { "استراتيجية رقم $n" }, draft, draftRisk, draftRebuild, draftEntry, draftMetadata)
                                persist(bots.map { bot -> if (bot.botNumber == selectedBot) bot.copy(strategies = (bot.strategies.filterNot { it.number == n } + item).sortedBy { it.number }) else bot })
                                editing = false
                            }
                            ActionButton("حذف", Red) {
                                persist(bots.map { bot -> if (bot.botNumber == selectedBot) bot.copy(strategies = bot.strategies.filterNot { it.number == selectedStrategy }) else bot }); editing = false
                            }
                            ActionButton("إلغاء", Muted) { editing = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun EmptyState(onAdd: () -> Unit) { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("الخزنة فارغة", color = Gold, fontWeight = FontWeight.Black); Spacer(Modifier.height(10.dp)); ActionButton("إضافة بوت", Cyan, onAdd) } }

@Composable private fun StrategyRow(number: Int, saved: Boolean, selected: Boolean, name: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().background(if (selected) Panel2 else Panel, RoundedCornerShape(15.dp)).border(1.dp, if (selected) Cyan else Line, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (saved) "✓" else "○", color = if (saved) Teal else Muted, fontSize = 19.sp, fontWeight = FontWeight.Black); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(name, color = Ink, fontWeight = FontWeight.Bold); Text(if (saved) "محفوظة — اضغط للتعديل" else "خانة فارغة — اضغط للإنشاء", color = if (saved) Teal else Muted, fontSize = 9.sp) }; Text("$number", color = Gold, fontWeight = FontWeight.Black) } }

@Composable private fun StrategyEditor(name: String, config: AmarBot1RuntimeConfig, risk: String, rebuild: String, entry: String, metadata: String, setName: (String) -> Unit, setConfig: (AmarBot1RuntimeConfig) -> Unit, setRisk: (String) -> Unit, setRebuild: (String) -> Unit, setEntry: (String) -> Unit, setMetadata: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("محرر الاستراتيجية", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Black)
        OutlinedTextField(name, setName, Modifier.fillMaxWidth(), label = { Text("اسم الاستراتيجية") })
        OutlinedTextField(risk, setRisk, Modifier.fillMaxWidth(), label = { Text("ملف المخاطر") })
        OutlinedTextField(rebuild, setRebuild, Modifier.fillMaxWidth(), label = { Text("قاعدة إعادة البناء") })
        OutlinedTextField(entry, setEntry, Modifier.fillMaxWidth(), label = { Text("قاعدة الدخول") })
        OutlinedTextField(metadata, setMetadata, Modifier.fillMaxWidth(), label = { Text("ملاحظات / Metadata") })
        ConfigSlider("اللوت", config.lot.toFloat(), .01f, 1f) { setConfig(config.copy(lot = it.toDouble())) }
        ConfigSlider("مسافة الشبكة", config.gridStep.toFloat(), 1f, 300f) { setConfig(config.copy(gridStep = it.toDouble())) }
        ConfigSlider("الحد الأقصى", config.maxOrders.toFloat(), 1f, 100f) { setConfig(config.copy(maxOrders = it.toInt())) }
        ConfigSlider("المضاعف", config.multiplier.toFloat(), 1f, 5f) { setConfig(config.copy(multiplier = it.toDouble())) }
        ConfigSlider("هدف السلة", config.basketTp.toFloat(), 0f, 500f) { setConfig(config.copy(basketTp = it.toDouble())) }
        ConfigSlider("خسارة السلة", config.basketSl.toFloat(), -500f, 0f) { setConfig(config.copy(basketSl = it.toDouble())) }
        ConfigSlider("التتبع", config.trailing.toFloat(), 0f, 300f) { setConfig(config.copy(trailing = it.toDouble())) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = config.buyEnabled, onClick = { setConfig(config.copy(buyEnabled = !config.buyEnabled)) }, label = { Text("BUY") })
            FilterChip(selected = config.sellEnabled, onClick = { setConfig(config.copy(sellEnabled = !config.sellEnabled)) }, label = { Text("SELL") })
        }
    } }
}

@Composable
private fun ConfigSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    AmarDragValueControl(
        label = label,
        value = value.toDouble(),
        min = min.toDouble(),
        max = max.toDouble(),
        step = when (label) {
            "اللوت" -> 0.01
            "المضاعف" -> 0.01
            "هدف السلة", "خسارة السلة", "التتبع" -> 0.5
            "مسافة الشبكة" -> 1.0
            else -> 1.0
        },
        accent = when (label) {
            "اللوت" -> Teal
            "مسافة الشبكة" -> Cyan
            "الحد الأقصى" -> Gold
            "المضاعف" -> Color(0xFFB14DFF)
            "هدف السلة" -> Teal
            "خسارة السلة" -> Red
            else -> Color(0xFFFF4FA3)
        },
        format = if (label == "الحد الأقصى" || label == "مسافة الشبكة") "%.0f" else "%.2f",
        onValueChange = { onChange(it.toFloat()) }
    )
}

@Composable private fun ActionButton(text: String, color: Color, onClick: () -> Unit) { Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == Gold) Color.Black else Color.White), modifier = Modifier.heightIn(min = 46.dp)) { Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
