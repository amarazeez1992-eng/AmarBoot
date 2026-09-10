package com.personal.gridbot.amaros.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/** مصدر بيانات السوق الحقيقي الذي سيُوصل بالوسيط أو MT5. لا ينشئ بيانات وهمية. */
interface AmarMarketDataProvider {
    fun candles(symbol: String, timeframe: AmarTimeframe): List<AmarCandle>
}

data class AmarCandle(val timeMs: Long, val open: Double, val high: Double, val low: Double, val close: Double, val volume: Double = 0.0)

enum class AmarTimeframe(val arabicLabel: String, val shortLabel: String) {
    M1("1 دقيقة", "M1"), M2("2 دقيقة", "M2"), M3("3 دقائق", "M3"), M4("4 دقائق", "M4"), M5("5 دقائق", "M5"), M6("6 دقائق", "M6"), M10("10 دقائق", "M10"), M12("12 دقيقة", "M12"), M15("15 دقيقة", "M15"), M20("20 دقيقة", "M20"), M30("30 دقيقة", "M30"), H1("1 ساعة", "H1"), H2("2 ساعة", "H2"), H3("3 ساعات", "H3"), H4("4 ساعات", "H4"), H6("6 ساعات", "H6"), H8("8 ساعات", "H8"), H12("12 ساعة", "H12"), D1("يومي", "D1"), W1("أسبوعي", "W1"), MN1("شهري", "MN1")
}

data class AmarChartPosition(val ticket: String, val side: Side, val volume: Double, val price: Double, val stopLoss: Double? = null, val takeProfit: Double? = null)
data class AmarChartPendingOrder(val ticket: String, val side: Side, val volume: Double, val price: Double)
data class AmarChartHistoryDeal(val ticket: String, val side: Side, val price: Double, val profit: Double)
enum class Side { BUY, SELL }
data class AmarChartQuote(val bid: Double? = null, val ask: Double? = null, val spread: Double? = null)
interface AmarChartExecutionController { fun buy(volume: Double): Boolean; fun sell(volume: Double): Boolean }

@Composable
fun AmarTradingChartScreen(symbol: String = "الذهب", provider: AmarMarketDataProvider? = null, quote: AmarChartQuote = AmarChartQuote(), positions: List<AmarChartPosition> = emptyList(), pendingOrders: List<AmarChartPendingOrder> = emptyList(), history: List<AmarChartHistoryDeal> = emptyList(), executionController: AmarChartExecutionController? = null, liveExecutionAuthorized: Boolean = false) {
    var timeframe by remember { mutableStateOf(AmarTimeframe.M1) }
    var oneClick by remember { mutableStateOf(false) }
    var showOrders by remember { mutableStateOf(true) }
    var showLevels by remember { mutableStateOf(true) }
    var showHistory by remember { mutableStateOf(true) }
    var showCrosshair by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf("0.01") }
    val candles = remember(symbol, timeframe, provider) { provider?.candles(symbol, timeframe).orEmpty() }
    Column(Modifier.fillMaxSize().background(Color(0xFF0B0F14)).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ChartHeader(symbol, timeframe, quote)
        TimeframeBar(timeframe) { timeframe = it }
        if (oneClick) OneClickBar(volume, liveExecutionAuthorized && executionController != null, { volume = it }, { executionController?.buy(volume.toDoubleOrNull() ?: 0.0) }, { executionController?.sell(volume.toDoubleOrNull() ?: 0.0) })
        ChartOptionsBar(oneClick, showOrders, showLevels, showHistory, showCrosshair, { oneClick = it }, { showOrders = it }, { showLevels = it }, { showHistory = it }, { showCrosshair = it })
        if (candles.isEmpty()) EmptyLiveChartMessage(liveExecutionAuthorized) else AmarModernCandleChart(candles, if (showLevels) positions else emptyList(), if (showOrders) pendingOrders else emptyList(), if (showHistory) history else emptyList(), showCrosshair, Modifier.weight(1f).fillMaxWidth())
        UnifiedTradeStrip(positions, pendingOrders, history)
    }
}

@Composable private fun ChartHeader(symbol: String, timeframe: AmarTimeframe, quote: AmarChartQuote) { Row(Modifier.fillMaxWidth().background(Color(0xFF121821)).padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(symbol, color = Color.White, style = MaterialTheme.typography.titleMedium); Text("${timeframe.shortLabel} • سوق مباشر عند توفر المصدر", color = Color(0xFF9AA5B1), style = MaterialTheme.typography.labelSmall) }; Column(horizontalAlignment = Alignment.End) { Text(quote.bid?.let { formatPrice(it) } ?: "—", color = Color(0xFF4DD0E1), style = MaterialTheme.typography.titleSmall); Text("شراء ${quote.ask?.let { formatPrice(it) } ?: "—"}", color = Color(0xFFFFC857), style = MaterialTheme.typography.labelSmall) } } }
@Composable private fun TimeframeBar(selected: AmarTimeframe, onSelected: (AmarTimeframe) -> Unit) { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) { AmarTimeframe.entries.forEach { frame -> FilterChip(selected == frame, { onSelected(frame) }, label = { Text(frame.shortLabel) }) } } }
@Composable private fun ChartOptionsBar(oneClick: Boolean, showOrders: Boolean, showLevels: Boolean, showHistory: Boolean, crosshair: Boolean, onOneClick: (Boolean) -> Unit, onOrders: (Boolean) -> Unit, onLevels: (Boolean) -> Unit, onHistory: (Boolean) -> Unit, onCrosshair: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) { CompactToggle("تداول سريع", oneClick, onOneClick); CompactToggle("أوامر", showOrders, onOrders); CompactToggle("وقف/ربح", showLevels, onLevels); CompactToggle("السجل", showHistory, onHistory); CompactToggle("مؤشر", crosshair, onCrosshair) } }
@Composable private fun CompactToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 6.dp)) { Switch(checked, onChecked); Spacer(Modifier.width(3.dp)); Text(label, color = Color(0xFFD7DEE7), style = MaterialTheme.typography.labelSmall) } }
@Composable private fun OneClickBar(volume: String, enabled: Boolean, onVolumeChange: (String) -> Unit, onBuy: () -> Unit, onSell: () -> Unit) { Row(Modifier.fillMaxWidth().background(Color(0xFF151C25)).padding(6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { OutlinedButton(enabled, onSell) { Text("بيع") }; OutlinedButton(enabled, onBuy) { Text("شراء") }; Text("الحجم", color = Color(0xFFB7C1CC)); androidx.compose.material3.OutlinedTextField(volume, onVolumeChange, singleLine = true, modifier = Modifier.width(82.dp)); Text(if (enabled) "تنفيذ مصرح" else "التنفيذ الحقيقي مقفول", color = if (enabled) Color(0xFF62D39A) else Color(0xFFFF7B72), style = MaterialTheme.typography.labelSmall) } }
@Composable private fun EmptyLiveChartMessage(liveExecutionAuthorized: Boolean) { Box(Modifier.fillMaxWidth().weight(1f).border(1.dp, Color(0xFF27313D)).background(Color(0xFF090D12)), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("الرسم البياني بانتظار بيانات السوق الحقيقية", color = Color.White, style = MaterialTheme.typography.titleMedium); Text("لن يتم إنشاء شموع وهمية أو بيانات محاكاة داخل شاشة التداول.", color = Color(0xFF9AA5B1)); Text(if (liveExecutionAuthorized) "التنفيذ يحتاج أيضاً إلى موصل وسيط فعلي." else "التنفيذ الحقيقي غير مصرح في الوضع الحالي.", color = Color(0xFFFFC857)) } } }

@Composable private fun AmarModernCandleChart(candles: List<AmarCandle>, positions: List<AmarChartPosition>, pendingOrders: List<AmarChartPendingOrder>, history: List<AmarChartHistoryDeal>, crosshair: Boolean, modifier: Modifier) {
    var pointerX by remember { mutableStateOf<Float?>(null) }; var pointerY by remember { mutableStateOf<Float?>(null) }
    val visible = candles.takeLast(160); val minPrice = visible.minOf { it.low }; val maxPrice = visible.maxOf { it.high }; val range = (maxPrice - minPrice).coerceAtLeast(0.0000001)
    Box(modifier.background(Color(0xFF080C11)).pointerInput(crosshair) { if (crosshair) detectDragGestures(onDragStart = { pointerX = it.x; pointerY = it.y }, onDrag = { change, _ -> pointerX = change.position.x; pointerY = change.position.y }, onDragEnd = { pointerX = null; pointerY = null }, onDragCancel = { pointerX = null; pointerY = null }) }) {
        Canvas(Modifier.fillMaxSize()) {
            val chartRight = size.width - 58f; val chartHeight = size.height - 26f; val step = chartRight / visible.size.coerceAtLeast(1); val bodyWidth = (step * 0.58f).coerceAtLeast(2f); fun y(price: Double): Float = chartHeight - (((price - minPrice) / range) * chartHeight).toFloat()
            for (i in 1..6) { val gy = chartHeight * i / 7f; drawLine(Color(0xFF17202B), Offset(0f, gy), Offset(chartRight, gy), 1f) }
            for (i in 1..10) { val gx = chartRight * i / 11f; drawLine(Color(0xFF111923), Offset(gx, 0f), Offset(gx, chartHeight), 1f) }
            visible.forEachIndexed { index, candle -> val x = step * index + step / 2f; val openY = y(candle.open); val closeY = y(candle.close); val highY = y(candle.high); val lowY = y(candle.low); val bullish = candle.close >= candle.open; val candleColor = if (bullish) Color(0xFF35C98B) else Color(0xFFFF5C68); drawLine(candleColor, Offset(x, highY), Offset(x, lowY), 1.5f); drawRect(candleColor, Offset(x - bodyWidth / 2f, minOf(openY, closeY)), androidx.compose.ui.geometry.Size(bodyWidth, abs(closeY - openY).coerceAtLeast(2f))) }
            positions.forEach { position -> val color = if (position.side == Side.BUY) Color(0xFF35C98B) else Color(0xFFFF5C68); drawLine(color, Offset(0f, y(position.price)), Offset(chartRight, y(position.price)), 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))); position.stopLoss?.let { drawLine(Color(0xFFFF7B72), Offset(0f, y(it)), Offset(chartRight, y(it)), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))) }; position.takeProfit?.let { drawLine(Color(0xFF62D39A), Offset(0f, y(it)), Offset(chartRight, y(it)), 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))) } }
            pendingOrders.forEach { order -> val color = if (order.side == Side.BUY) Color(0xFF4DD0E1) else Color(0xFFFFC857); drawLine(color, Offset(0f, y(order.price)), Offset(chartRight, y(order.price)), 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))) }
            history.takeLast(30).forEach { deal -> val color = if (deal.profit >= 0.0) Color(0xFF62D39A) else Color(0xFFFF7B72); val x = chartRight - (history.indexOf(deal) + 1) * 8f; drawCircle(color, 3.5f, Offset(x.coerceAtLeast(4f), y(deal.price))) }
            if (crosshair && pointerX != null && pointerY != null) { drawLine(Color(0xFFB7C1CC), Offset(pointerX!!, 0f), Offset(pointerX!!, chartHeight), 1f); drawLine(Color(0xFFB7C1CC), Offset(0f, pointerY!!), Offset(chartRight, pointerY!!), 1f) }
            for (i in 0..6) { val price = maxPrice - (range * i / 6.0); androidx.compose.ui.graphics.drawscope.DrawScope.Default.drawContext.canvas.nativeCanvas.drawText(formatPrice(price), chartRight + 5f, chartHeight * i / 6f + 4f, android.graphics.Paint().apply { textSize = 24f; color = android.graphics.Color.LTGRAY }) }
        }
    }
}

@Composable private fun UnifiedTradeStrip(positions: List<AmarChartPosition>, pending: List<AmarChartPendingOrder>, history: List<AmarChartHistoryDeal>) { Row(Modifier.fillMaxWidth().background(Color(0xFF121821)).padding(7.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { Text("صفقات ${positions.size}", color = Color(0xFF62D39A)); Text("معلقة ${pending.size}", color = Color(0xFFFFC857)); Text("سجل ${history.size}", color = Color(0xFFB7C1CC)); Text("ربح السجل ${formatPrice(history.sumOf { it.profit })}", color = Color.White) } }

private fun formatPrice(value: Double): String = if (value == 0.0) "0" else "%.5f".format(java.util.Locale.US, value)
