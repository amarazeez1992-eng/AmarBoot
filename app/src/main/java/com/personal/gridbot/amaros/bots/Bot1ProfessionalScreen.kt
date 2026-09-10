package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
private val Purple = Color(0xFFB14DFF)
private val Line = Color(0xFF214452)

@Composable
fun Bot1ProfessionalScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { AmarBotVaultRepository(context) }
    var bots by remember { mutableStateOf(repository.load()) }
    var selectedBot by remember { mutableIntStateOf(bots.firstOrNull()?.botNumber ?: 1) }
    var selectedStrategy by remember { mutableIntStateOf(1) }
    var editing by remember { mutableStateOf(false) }
    var draftName by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf(AmarBot1RuntimeConfig()) }

    fun persist(updated: List<AmarSavedBot>) { bots = updated; repository.save(updated) }
    val current = bots.firstOrNull { it.botNumber == selectedBot }
    val strategies = current?.strategies.orEmpty()
    val selected = strategies.firstOrNull { it.number == selectedStrategy }

    Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text("خزنة البوتات", color = Cyan, fontSize = 27.sp, fontWeight = FontWeight.Black); Text("إدارة وحفظ وتعديل الاستراتيجيات", color = Muted, fontSize = 10.sp) }
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
                        val selectedBotCard = bot.botNumber == selectedBot
                        Column(Modifier.width(105.dp).background(if (selectedBotCard) Panel2 else Panel, RoundedCornerShape(18.dp)).border(1.dp, if (selectedBotCard) Cyan else Line, RoundedCornerShape(18.dp)).clickable { selectedBot = bot.botNumber; selectedStrategy = 1 }.padding(10.dp)) {
                            Text(bot.name, color = if (selectedBotCard) Cyan else Ink, fontWeight = FontWeight.Black)
                            Text("${bot.strategies.size} استراتيجية محفوظة", color = Muted, fontSize = 9.sp)
                        }
                    }
                }
            }
            if (current == null) {
                item { EmptyState { val next = ((bots.maxOfOrNull { it.botNumber } ?: 0) + 1); persist(bots + AmarSavedBot(next, "بوت $next")); selectedBot = next } }
            } else {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionButton("تعديل اسم البوت", Cyan) { draftName = current.name; editing = true }
                        ActionButton("إعادة تهيئة البوت", Gold) {
                            persist(bots.map { if (it.botNumber == selectedBot) it.copy(strategies = emptyList()) else it })
                            selectedStrategy = 1
                        }
                        ActionButton("حذف البوت", Red) {
                            val remaining = bots.filterNot { it.botNumber == selectedBot }
                            persist(remaining); selectedBot = remaining.firstOrNull()?.botNumber ?: 1; selectedStrategy = 1
                        }
                    }
                }
                item { Text("استراتيجيات ${current.name}", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        ActionButton("+ استراتيجية", Teal) {
                            val next = ((strategies.maxOfOrNull { it.number } ?: 0) + 1)
                            val created = AmarSavedStrategy(next, "استراتيجية رقم $next", AmarBot1RuntimeConfig())
                            persist(bots.map { if (it.botNumber == selectedBot) it.copy(strategies = it.strategies + created) else it })
                            selectedStrategy = next; draft = created.profile; draftName = created.name; editing = true
                        }
                        ActionButton("حذف المحددة", Red) {
                            persist(bots.map { if (it.botNumber == selectedBot) it.copy(strategies = it.strategies.filterNot { s -> s.number == selectedStrategy }) else it })
                            selectedStrategy = 1
                        }
                    }
                }
                items((1..10).toList()) { number ->
                    val saved = strategies.firstOrNull { it.number == number }
                    StrategyRow(number, saved != null, number == selectedStrategy, saved?.name ?: "خانة فارغة") {
                        selectedStrategy = number
                        if (saved != null) { draft = saved.profile; draftName = saved.name; editing = true }
                        else { draft = AmarBot1RuntimeConfig(); draftName = "استراتيجية رقم $number"; editing = true }
                    }
                }
                if (editing) {
                    item { StrategyEditor(draftName, draft, { draftName = it }, { draft = it }) }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActionButton("حفظ الاستراتيجية ✓", Teal) {
                                val normalized = if (selectedStrategy in 1..10) selectedStrategy else 1
                                val item = AmarSavedStrategy(normalized, draftName.ifBlank { "استراتيجية رقم $normalized" }, draft)
                                persist(bots.map { bot -> if (bot.botNumber == selectedBot) bot.copy(strategies = (bot.strategies.filterNot { it.number == normalized } + item).sortedBy { it.number }) else bot })
                                editing = false
                            }
                            ActionButton("إلغاء", Muted) { editing = false }
                        }
                    }
                }
            }
        }
    }
}

@Composable private fun EmptyState(onAdd: () -> Unit) { Column(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(20.dp)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text("الخزنة فارغة", color = Gold, fontWeight = FontWeight.Black); Text("أضف بوتًا جديدًا أو أنشئه من الخانة الحالية.", color = Muted, fontSize = 11.sp); Spacer(Modifier.height(10.dp)); ActionButton("إضافة بوت", Cyan, onAdd) } }

@Composable private fun StrategyRow(number: Int, saved: Boolean, selected: Boolean, name: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().background(if (selected) Panel2 else Panel, RoundedCornerShape(15.dp)).border(1.dp, if (selected) Cyan else Line, RoundedCornerShape(15.dp)).clickable(onClick = onClick).padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(if (saved) "✓" else "○", color = if (saved) Teal else Muted, fontSize = 19.sp, fontWeight = FontWeight.Black); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(name, color = Ink, fontWeight = FontWeight.Bold); Text(if (saved) "محفوظة — اضغط للتعديل" else "خانة فارغة — اضغط للإنشاء", color = if (saved) Teal else Muted, fontSize = 9.sp) }; Text("$number", color = Gold, fontWeight = FontWeight.Black) } }

@Composable private fun StrategyEditor(name: String, config: AmarBot1RuntimeConfig, setName: (String) -> Unit, setConfig: (AmarBot1RuntimeConfig) -> Unit) { Card(colors = CardDefaults.cardColors(Panel), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text("محرر الاستراتيجية", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Black); OutlinedTextField(name, setName, Modifier.fillMaxWidth(), label = { Text("اسم الاستراتيجية") }); ConfigSlider("اللوت", config.lot.toFloat(), .01f, 1f) { setConfig(config.copy(lot = it.toDouble())) }; ConfigSlider("مسافة الشبكة", config.gridStep.toFloat(), 1f, 300f) { setConfig(config.copy(gridStep = it.toDouble())) }; ConfigSlider("الحد الأقصى", config.maxOrders.toFloat(), 1f, 100f) { setConfig(config.copy(maxOrders = it.toInt())) }; ConfigSlider("المضاعف", config.multiplier.toFloat(), 1f, 5f) { setConfig(config.copy(multiplier = it.toDouble())) }; ConfigSlider("هدف السلة", config.basketTp.toFloat(), 0f, 500f) { setConfig(config.copy(basketTp = it.toDouble())) }; ConfigSlider("خسارة السلة", config.basketSl.toFloat(), -500f, 0f) { setConfig(config.copy(basketSl = it.toDouble())) }; ConfigSlider("التتبع", config.trailing.toFloat(), 0f, 300f) { setConfig(config.copy(trailing = it.toDouble())) } } } }

@Composable private fun ConfigSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) { Text("$label  ${"%.2f".format(value)}", color = Ink, fontSize = 10.sp); Slider(value, onChange, valueRange = min..max, colors = SliderDefaults.colors(thumbColor = Cyan, activeTrackColor = Cyan, inactiveTrackColor = Line)) }

@Composable private fun ActionButton(text: String, color: Color, onClick: () -> Unit) { Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = if (color == Gold) Color.Black else Color.White), modifier = Modifier.heightIn(min = 46.dp)) { Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
