package com.personal.gridbot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.gridbot.data.Bot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotListScreen(
    bots: List<Bot>,
    onAddBotClick: () -> Unit,
    onToggleActive: (Bot) -> Unit,
    onDeleteBot: (Bot) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Grid Trade Bot") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBotClick) {
                Icon(Icons.Default.Add, contentDescription = "إضافة بوت")
            }
        }
    ) { padding ->
        if (bots.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("لا يوجد بوتات بعد - اضغط + لإضافة بوت جديد")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(bots) { bot ->
                    BotCard(bot, onToggleActive, onDeleteBot)
                }
            }
        }
    }
}

@Composable
fun BotCard(bot: Bot, onToggleActive: (Bot) -> Unit, onDeleteBot: (Bot) -> Unit) {
    Card(Modifier.fillMaxWidth().padding(12.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(bot.name, style = MaterialTheme.typography.titleMedium)
                Switch(checked = bot.isActive, onCheckedChange = { onToggleActive(bot) })
            }
            Spacer(Modifier.height(8.dp))
            Text("الزوج: ${bot.pair}")
            Text("الاستثمار: ${bot.investment} | مستويات الشبكة: ${bot.gridCount}")
            Text("جني الأرباح: ${bot.takeProfitPct}% | وقف الخسارة: ${bot.stopLossPct}%")
            Text("الربح الحالي: ${bot.currentProfit} (${bot.profitPercent}%)")
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onDeleteBot(bot) }) { Text("حذف البوت") }
        }
    }
}
