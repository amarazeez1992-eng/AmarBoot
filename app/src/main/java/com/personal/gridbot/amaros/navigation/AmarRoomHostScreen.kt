package com.personal.gridbot.amaros.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.accounts.AmarAccountsScreen
import com.personal.gridbot.amaros.bots.AmarBotLabProfessionalScreen
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarLiveTradingChartHost
import com.personal.gridbot.amaros.chart.AmarTradingChartScreen
import com.personal.gridbot.amaros.design.AmarAppearanceScreen
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen
import com.personal.gridbot.amaros.settings.AmarDeveloperOptionsScreen
import com.personal.gridbot.ui.theme.AmarThemeMode

@Composable
fun AmarRoomHostScreen(room: AmarRoom, onBackHome: () -> Unit, themeMode: AmarThemeMode, onThemeModeChange: (AmarThemeMode) -> Unit) {
    if (room == AmarRoom.BOT_LAB) {
        AmarBotLabProfessionalScreen(onBackHome = onBackHome)
        return
    }
    var settingsMode by remember { mutableIntStateOf(0) }
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) { Text("عمار", style = MaterialTheme.typography.headlineSmall); Text("${room.emoji} ${room.titleAr}", style = MaterialTheme.typography.titleMedium) }
                Button(onClick = onBackHome) { Text("الرئيسية") }
            }
            when (room) {
                AmarRoom.COMMAND_CENTER -> CommandCenterScreen()
                AmarRoom.CHART -> { val runtime = AmarMt5RuntimeRegistry.current(); if (runtime == null) AmarTradingChartScreen(symbol = "الذهب") else AmarLiveTradingChartHost(symbol = "XAUUSD", provider = runtime.marketData) }
                AmarRoom.ACCOUNTS -> AmarAccountsScreen()
                AmarRoom.SETTINGS -> Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { settingsMode = 0 }, modifier = Modifier.weight(1f)) { Text("المظهر") }; Button(onClick = { settingsMode = 1 }, modifier = Modifier.weight(1f)) { Text("الإعدادات المتقدمة") } }
                    if (settingsMode == 0) AmarAppearanceScreen(themeMode, onThemeModeChange) else AmarDeveloperOptionsScreen()
                }
                else -> AmarRoomWorkspace(room)
            }
        }
    }
}
