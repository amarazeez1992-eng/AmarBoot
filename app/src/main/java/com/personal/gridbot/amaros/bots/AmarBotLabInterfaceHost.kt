package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AmarBotInterface(val label: String) {
    A("واجهة 1"),
    B("واجهة 2")
}

private const val PREFS = "amar_bot_interfaces"

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("bot_$botNumber", AmarBotInterface.A.name)
        return runCatching { AmarBotInterface.valueOf(value ?: AmarBotInterface.A.name) }
            .getOrDefault(AmarBotInterface.A)
    }

    fun save(context: Context, botNumber: Int, interfaceId: AmarBotInterface) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString("bot_$botNumber", interfaceId.name).apply()
    }
}

/** Thin shell: internal interfaces keep their original scrolling and layout. */
@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedBot by remember { mutableStateOf(1) }
    var selectedInterface by remember {
        mutableStateOf(AmarBotInterfaceRegistry.load(context, 1))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        InterfaceSelector(selectedInterface) { next ->
            selectedInterface = next
            AmarBotInterfaceRegistry.save(context, selectedBot, next)
        }

        AnimatedContent(
            targetState = selectedInterface,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "amar-bot-interface"
        ) { active ->
            when (active) {
                AmarBotInterface.A -> AmarBotLabProfessionalScreen(onBackHome = onBackHome)
                AmarBotInterface.B -> AmarBotLabInterfaceBScreenV2(
                    onBackHome = onBackHome,
                    selectedBot = selectedBot,
                    onBotSelected = { bot ->
                        selectedBot = bot
                        selectedInterface = AmarBotInterfaceRegistry.load(context, bot)
                    }
                )
            }
        }
    }
}

@Composable
private fun InterfaceSelector(selected: AmarBotInterface, onSelect: (AmarBotInterface) -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF07111A))
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("الواجهات", color = Color(0xFFB9F8FF), fontSize = 11.sp, fontWeight = FontWeight.Black)
        AmarBotInterface.entries.forEach { item ->
            val active = item == selected
            Box(
                Modifier.weight(1f).height(40.dp)
                    .background(if (active) Color(0xFF19E6FF) else Color(0xFF102533), RoundedCornerShape(10.dp))
                    .border(1.dp, if (active) Color(0xFF6CFAFF) else Color(0xFF1C4657), RoundedCornerShape(10.dp))
                    .clickable { onSelect(item) },
                contentAlignment = Alignment.Center
            ) {
                Text(item.label, color = if (active) Color.Black else Color(0xFFEAFBFF), fontWeight = FontWeight.Black, fontSize = 12.sp)
            }
        }
    }
}
