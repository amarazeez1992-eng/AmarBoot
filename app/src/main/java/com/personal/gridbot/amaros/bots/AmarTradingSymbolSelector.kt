package com.personal.gridbot.amaros.bots

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AmarTradingSymbolSelector(context: Context) {
    var selected by remember { mutableStateOf(AmarTradingSymbolContext.selected) }
    var customBrokerSymbol by remember { mutableStateOf(AmarTradingSymbolContext.selected.brokerSymbol) }

    LaunchedEffect(Unit) {
        AmarTradingSymbolContext.load(context)
        selected = AmarTradingSymbolContext.selected
        customBrokerSymbol = selected.brokerSymbol
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFF07131C))
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("أداة التداول", color = Color(0xFFB9F8FF), fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text(
                    "يحدد السوق الذي سيُرسل إليه التنفيذ بعد تحقق MT5/Bridge",
                    color = Color(0xFF7896A5), fontSize = 8.sp
                )
            }
            Text(selected.brokerSymbol.ifBlank { "غير محدد" }, color = Color(0xFF18F2A4), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AmarSymbolChip("🥇 الذهب", selected.id == "XAUUSD", Modifier.weight(1f)) {
                val next = AmarTradingSymbolCatalog.XAUUSD
                AmarTradingSymbolContext.select(context, next)
                selected = next
                customBrokerSymbol = next.brokerSymbol
            }
            AmarSymbolChip("₿ البيتكوين", selected.id == "BTCUSD", Modifier.weight(1f)) {
                val next = AmarTradingSymbolCatalog.BTCUSD
                AmarTradingSymbolContext.select(context, next)
                selected = next
                customBrokerSymbol = next.brokerSymbol
            }
            AmarSymbolChip("مخصص", selected.id == "CUSTOM", Modifier.weight(1f)) {
                val next = AmarTradingSymbolCatalog.CUSTOM.copy(brokerSymbol = customBrokerSymbol)
                AmarTradingSymbolContext.select(context, next)
                selected = next
            }
        }

        if (selected.id == "CUSTOM") {
            OutlinedTextField(
                value = customBrokerSymbol,
                onValueChange = {
                    customBrokerSymbol = it
                    val next = AmarTradingSymbolCatalog.CUSTOM.copy(brokerSymbol = it.trim())
                    AmarTradingSymbolContext.select(context, next)
                    selected = next
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("اسم الرمز كما يظهر في MT5") },
                placeholder = { Text("مثال: XAUUSDm أو BTCUSD.a") }
            )
        }

        Spacer(Modifier.height(1.dp))
        Text(
            "السوق المختار لا يعني تنفيذًا حيًا بحد ذاته؛ التنفيذ لا يُعتبر ناجحًا حتى يؤكده Runtime/MT5.",
            color = Color(0xFFFFC84D), fontSize = 8.sp
        )
    }
}

@Composable
private fun AmarSymbolChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    BoxLikeSymbolChip(text, selected, modifier, onClick)
}

@Composable
private fun BoxLikeSymbolChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier
            .height(40.dp)
            .background(if (selected) Color(0xFF24E8FF) else Color(0xFF102533), RoundedCornerShape(11.dp))
            .border(1.dp, if (selected) Color(0xFF8DFAFF) else Color(0xFF1C4657), RoundedCornerShape(11.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (selected) Color.Black else Color(0xFFEAFBFF), fontSize = 10.sp, fontWeight = FontWeight.Black)
    }
}
