package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PREFS = "amar_bot_interfaces"
enum class AmarBotInterface(val label: String) { A("واجهة 1"), B("واجهة 2") }

object AmarBotInterfaceRegistry {
    fun load(context: Context, botNumber: Int): AmarBotInterface = runCatching {
        AmarBotInterface.valueOf(
            context.getSharedPreferences(PREFS, 0)
                .getString("bot_$botNumber", AmarBotInterface.A.name)!!
        )
    }.getOrDefault(AmarBotInterface.A)

    fun save(context: Context, botNumber: Int, id: AmarBotInterface) {
        context.getSharedPreferences(PREFS, 0).edit()
            .putString("bot_$botNumber", id.name)
            .apply()
    }
}

@Composable
fun AmarBotLabInterfaceHost(onBackHome: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selected by remember {
        mutableStateOf(
            AmarBotInterfaceRegistry.load(
                context,
                AmarBotLabSelectionContext.selectedBot
            )
        )
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Button(
                onClick = {
                    selected = AmarBotInterface.A
                    AmarBotInterfaceRegistry.save(context, AmarBotLabSelectionContext.selectedBot, selected)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == AmarBotInterface.A) Color(0xFF1DE5FF) else Color(0xFF0E2230),
                    contentColor = if (selected == AmarBotInterface.A) Color.Black else Color.White
                )
            ) { Text("واجهة 1", fontWeight = FontWeight.Black, fontSize = 10.sp) }

            Button(
                onClick = {
                    selected = AmarBotInterface.B
                    AmarBotInterfaceRegistry.save(context, AmarBotLabSelectionContext.selectedBot, selected)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == AmarBotInterface.B) Color(0xFF1DE5FF) else Color(0xFF0E2230),
                    contentColor = if (selected == AmarBotInterface.B) Color.Black else Color.White
                )
            ) { Text("واجهة 2", fontWeight = FontWeight.Black, fontSize = 10.sp) }
        }

        Box(Modifier.fillMaxSize()) {
            if (selected == AmarBotInterface.A) {
                // واجهة 1 الأصلية المختصرة + نظام السحب للأرقام، بدون حذف أو استبدال مختبر البوت.
                AmarBotLabInterface1Screen(onBackHome)
            } else {
                // واجهة 2 تبقى كما هي.
                AmarBotLabInterface2Screen(onBackHome)
            }
        }
    }
}
