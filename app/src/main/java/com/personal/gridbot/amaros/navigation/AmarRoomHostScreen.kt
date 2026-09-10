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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.accounts.AmarAccountsScreen
import com.personal.gridbot.amaros.bots.Bot1PremiumScreen
import com.personal.gridbot.amaros.broker.AmarMt5RuntimeRegistry
import com.personal.gridbot.amaros.chart.AmarLiveTradingChartHost
import com.personal.gridbot.amaros.chart.AmarTradingChartScreen
import com.personal.gridbot.amaros.rooms.commandcenter.CommandCenterScreen
import com.personal.gridbot.amaros.settings.AmarDeveloperOptionsScreen

@Composable
fun AmarRoomHostScreen(room: AmarRoom, onBackHome: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("عمار", style = MaterialTheme.typography.headlineSmall)
                    Text("${room.emoji} ${room.titleAr}", style = MaterialTheme.typography.titleMedium)
                }
                Button(onClick = onBackHome) { Text("الرئيسية") }
            }
            when (room) {
                AmarRoom.COMMAND_CENTER -> CommandCenterScreen()
                AmarRoom.CHART -> {
                    val runtime = AmarMt5RuntimeRegistry.current()
                    if (runtime == null) AmarTradingChartScreen(symbol = "الذهب")
                    else AmarLiveTradingChartHost(symbol = "XAUUSD", provider = runtime.marketData)
                }
                AmarRoom.ACCOUNTS -> AmarAccountsScreen()
                AmarRoom.SETTINGS -> AmarDeveloperOptionsScreen()
                AmarRoom.BOT_LAB -> Bot1PremiumScreen()
                else -> AmarRoomWorkspace(room)
            }
        }
    }
}
