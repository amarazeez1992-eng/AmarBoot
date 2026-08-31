package com.personal.gridbot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.personal.gridbot.data.Bot

@Composable
fun AddBotDialog(onDismiss: () -> Unit, onConfirm: (Bot) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pair by remember { mutableStateOf("EURUSD") }
    var investment by remember { mutableStateOf("100") }
    var gridCount by remember { mutableStateOf("5") }
    var takeProfit by remember { mutableStateOf("1.0") }
    var stopLoss by remember { mutableStateOf("5.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة بوت جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("اسم البوت") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pair, { pair = it }, label = { Text("زوج العملة (مثال EURUSD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(investment, { investment = it }, label = { Text("مبلغ الاستثمار") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(gridCount, { gridCount = it }, label = { Text("عدد مستويات الشبكة") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(takeProfit, { takeProfit = it }, label = { Text("جني الأرباح %") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(stopLoss, { stopLoss = it }, label = { Text("وقف الخسارة %") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(
                    Bot(
                        name = name,
                        pair = pair,
                        investment = investment.toDoubleOrNull() ?: 0.0,
                        gridCount = gridCount.toIntOrNull() ?: 5,
                        takeProfitPct = takeProfit.toDoubleOrNull() ?: 1.0,
                        stopLossPct = stopLoss.toDoubleOrNull() ?: 5.0
                    )
                )
            }) { Text("إنشاء") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
