package com.personal.gridbot.amaros.bots

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context

enum class AmarBotInterface(val label: String) { A("A"), B("B"), C("C"), D("D") }
private const val PREFS = "amar_bot_interfaces"

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("bot_$botNumber", AmarBotInterface.A.name)
        return runCatching { AmarBotInterface.valueOf(value ?: AmarBotInterface.A.name) }.getOrDefault(AmarBotInterface.A)
    }
    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("bot_$botNumber", interfaceId.name).apply()
    }
}

@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedBot by remember { mutableStateOf(1) }
    var selectedInterface by remember { mutableStateOf(AmarBotInterfaceRegistry.load(context, 1)) }
    var remaining by remember { mutableStateOf(AmarTradingTimeframeContext.selected.remainingMillis()) }
    LaunchedEffect(AmarTradingTimeframeContext.selected) {
        while (true) {
            remaining = AmarTradingTimeframeContext.selected.remainingMillis()
            kotlinx.coroutines.delay(1000)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        GlobalTimeframeBar(remaining) { next -> AmarTradingTimeframeContext.selected = next }
        InterfaceSelector(selectedInterface) { next ->
            selectedInterface = next
            AmarBotInterfaceRegistry.save(context, selectedBot, next)
        }
        AnimatedContent(targetState = selectedInterface, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "amar-bot-interface") { active ->
            when (active) {
                AmarBotInterface.A -> AmarBotLabProfessionalScreen(onBackHome = onBackHome)
                AmarBotInterface.B -> AmarBotLabInterfaceBScreen(onBackHome = onBackHome, selectedBot = selectedBot, onBotSelected = { bot ->
                    selectedBot = bot
                    selectedInterface = AmarBotInterfaceRegistry.load(context, bot)
                })
                AmarBotInterface.C, AmarBotInterface.D -> AmarBotFutureInterfacePlaceholder(active)
            }
        }
    }
}

@Composable
private fun GlobalTimeframeBar(remaining: Long, onSelect: (com.personal.gridbot.amaros.chart.AmarTimeframe) -> Unit) {
    val selected = AmarTradingTimeframeContext.selected
    Column(Modifier.fillMaxWidth().background(Color(0xFF06121B)).padding(horizontal = 8.dp, vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("فريم الدخول", color = Color(0xFFB9F8FF), fontSize = 10.sp)
            Spacer(Modifier.weight(1f))
            Text("${selected.arabicLabel}  •  متبقي ${formatTimeframeRemaining(remaining)}", color = Color(0xFF18F2A4), fontSize = 10.sp)
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            com.personal.gridbot.amaros.chart.AmarTimeframe.entries.forEach { tf ->
                val active = tf == selected
                Box(Modifier.height(34.dp).background(if (active) Color(0xFF24E8FF) else Color(0xFF102533), RoundedCornerShape(9.dp)).border(1.dp, if (active) Color(0xFF8DFAFF) else Color(0xFF1C4657), RoundedCornerShape(9.dp)).padding(horizontal = 11.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.clickable(onClick = { onSelect(tf) }).let { }
                    Text(tf.shortLabel, color = if (active) Color.Black else Color(0xFFEAFBFF), fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun InterfaceSelector(selected: AmarBotInterface, onSelect: (AmarBotInterface) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF07111A)).padding(horizontal = 9.dp, vertical = 7.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("واجهات", color = Color(0xFFB9F8FF), fontSize = 11.sp)
        AmarBotInterface.entries.forEach { item ->
            val active = item == selected
            Box(Modifier.weight(1f).height(36.dp).background(if (active) Color(0xFF19E6FF) else Color(0xFF102533), RoundedCornerShape(10.dp)).border(1.dp, if (active) Color(0xFF6CFAFF) else Color(0xFF1C4657), RoundedCornerShape(10.dp)).clickable { onSelect(item) }, contentAlignment = Alignment.Center) {
                Text(item.label, color = if (active) Color.Black else Color(0xFFEAFBFF), fontWeight = androidx.compose.ui.text.font.FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AmarBotFutureInterfacePlaceholder(interfaceId: AmarBotInterface) {
    Box(Modifier.fillMaxWidth().height(170.dp).padding(12.dp).background(Color(0xFF0B1923), RoundedCornerShape(18.dp)).border(1.dp, Color(0xFF1C4657), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
        Text("واجهة ${interfaceId.label}\nمساحة تصميم مستقلة — لا يوجد تغيير في استراتيجية التداول", color = Color(0xFFB9F8FF), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
    }
}
