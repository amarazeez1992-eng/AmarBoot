package com.personal.gridbot.amaros.rooms.commandcenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun CommandCenterScreen(
    state: CommandCenterState = PreviewCommandCenterState,
    onOpenBotLab: () -> Unit = {}
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Text("مركز القيادة", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(4.dp))
                Text("لوحة قيادة قابلة للتوسعة — البيانات الحالية تجريبية فقط.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (state.sections.showAccount) {
            item {
                CommandSectionCard("الحساب") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CommandMetricCard("الرصيد", money(state.account.balance), "القيمة الحالية", Modifier.weight(1f))
                        CommandMetricCard("حقوق الملكية", money(state.account.equity), "القيمة الحالية", Modifier.weight(1f))
                    }
                    CommandKeyValue("الهامش المستخدم", money(state.account.margin))
                    CommandKeyValue("الهامش المتاح", money(state.account.freeMargin))
                    CommandKeyValue("الربح العائم", money(state.account.floatingProfit))
                    CommandKeyValue("ربح اليوم", money(state.account.dailyProfit))
                    CommandKeyValue("إجمالي الربح", money(state.account.totalProfit))
                }
            }
        }
        if (state.sections.showMarket) {
            item {
                CommandSectionCard("السوق") {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(state.market.symbol, style = MaterialTheme.typography.titleLarge)
                            Text(state.market.status, style = MaterialTheme.typography.bodySmall)
                        }
                        CommandStatusPill(state.market.direction)
                    }
                    CommandKeyValue("القوة", "${state.market.strength}%")
                    CommandKeyValue("الجلسة", state.market.session)
                    if (state.market.price > 0.0) CommandKeyValue("السعر", format(state.market.price))
                    else CommandKeyValue("السعر", "بانتظار مصدر السوق")
                }
            }
        }
        if (state.sections.showTrading) {
            item {
                CommandSectionCard("حالة التداول") {
                    CommandKeyValue("البوت الحالي", state.trading.botName)
                    CommandKeyValue("الحالة", state.trading.botStatus)
                    CommandKeyValue("الصفقات المفتوحة", state.trading.positions.toString())
                    CommandKeyValue("الأوامر المعلّقة", state.trading.pendingOrders.toString())
                    CommandKeyValue("الرابحة / الخاسرة", "${state.trading.winningTrades} / ${state.trading.losingTrades}")
                    CommandStatusPill("فتح مختبر البوتات")
                }
            }
        }
        if (state.sections.showRisk) {
            item {
                CommandSectionCard("المخاطر والحماية") {
                    CommandKeyValue("المخاطرة", "${format(state.risk.riskPercent)}%")
                    CommandKeyValue("السحب الحالي", "${format(state.risk.drawdownPercent)}%")
                    CommandKeyValue("الحماية", state.risk.protection)
                    CommandStatusPill(if (state.risk.emergencyStop) "توقف طوارئ" else "النظام آمن", positive = !state.risk.emergencyStop)
                }
            }
        }
        if (state.sections.showAlerts) {
            item {
                CommandSectionCard("آخر التنبيهات") {
                    if (state.alerts.isEmpty()) Text("لا توجد تنبيهات حالياً")
                    else state.alerts.take(5).forEach { alert ->
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(alert.title, style = MaterialTheme.typography.bodyLarge)
                            Text(alert.detail, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

private fun money(value: Double): String = String.format(Locale.US, "%.2f $", value)
private fun format(value: Double): String = String.format(Locale.US, "%.2f", value)
