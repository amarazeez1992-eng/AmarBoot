package com.personal.gridbot.amaros.bots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.broker.AmarBot1UiRuntime
import kotlinx.coroutines.launch

/**
 * Live BOT1 lifecycle controls. Every result comes from the verified runtime;
 * the panel never talks to the bridge or MT5 directly.
 */
@Composable
fun AmarBot1LiveControlPanel() {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("Runtime: غير متصل") }
    val runtimeInstalled = AmarBot1UiRuntime.isInstalled()
    val targetSymbol = AmarTradingSymbolContext.selected.brokerSymbol

    fun run(label: String, action: suspend () -> com.personal.gridbot.amaros.broker.AmarBrokerResult) {
        scope.launch {
            status = "$label — جارٍ التحقق..."
            val result = action()
            status = if (result.executed) {
                "✓ $label — تم التنفيذ والتحقق"
            } else {
                "✕ $label — ${result.message}"
            }
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1230))) {
        Column(
            Modifier.padding(9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("BOT1 • التحكم التنفيذي الموثق", color = Color(0xFF24E8FF), fontWeight = FontWeight.Black, fontSize = 12.sp)
            Text(
                if (runtimeInstalled) "Runtime متصل • الرمز: ${targetSymbol.ifBlank { "غير محدد" }}"
                else "Runtime غير مثبت — لن يتم إرسال أي أمر إلى MT5",
                color = if (runtimeInstalled) Color(0xFF18F2A4) else Color(0xFFFF6AD5),
                fontSize = 9.sp
            )
            Text(status, color = Color(0xFFF3FAFF), fontSize = 9.sp, fontWeight = FontWeight.Bold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                LiveButton("تشغيل", Color(0xFF18F2A4), Modifier.weight(1f)) {
                    run("تشغيل") { AmarBot1UiRuntime.start(targetSymbol.ifBlank { null }) }
                }
                LiveButton("إطفاء", Color(0xFFFF4F78), Modifier.weight(1f)) {
                    run("إطفاء") { AmarBot1UiRuntime.stop() }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                LiveButton("إعادة البناء", Color(0xFF24E8FF), Modifier.weight(1f)) {
                    run("إعادة البناء") { AmarBot1UiRuntime.rebuild(targetSymbol.ifBlank { null }) }
                }
                LiveButton("إغلاق الكل", Color(0xFFFF4F78), Modifier.weight(1f)) {
                    run("إغلاق الكل") { AmarBot1UiRuntime.closeAll() }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                LiveButton("إغلاق BUY", Color(0xFFFF6AD5), Modifier.weight(1f)) {
                    run("إغلاق BUY") { AmarBot1UiRuntime.closeBuy() }
                }
                LiveButton("إغلاق SELL", Color(0xFFFF6AD5), Modifier.weight(1f)) {
                    run("إغلاق SELL") { AmarBot1UiRuntime.closeSell() }
                }
            }
        }
    }
}

@Composable
private fun LiveButton(label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = if (color == Color(0xFF24E8FF) || color == Color(0xFF18F2A4)) Color.Black else Color.White
        )
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}
