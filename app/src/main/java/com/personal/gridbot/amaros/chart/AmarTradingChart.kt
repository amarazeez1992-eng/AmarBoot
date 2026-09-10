package com.personal.gridbot.amaros.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** مصدر بيانات السوق الحقيقي الذي سيُوصل لاحقاً بالوسيط أو منصة التداول. */
interface AmarMarketDataProvider {
    fun candles(symbol: String, timeframe: AmarTimeframe): List<AmarCandle>
}

data class AmarCandle(
    val timeMs: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
)

enum class AmarTimeframe(val arabicLabel: String) {
    M1("1 دقيقة"), M2("2 دقيقة"), M3("3 دقائق"), M4("4 دقائق"), M5("5 دقائق"), M6("6 دقائق"), M10("10 دقائق"), M12("12 دقيقة"), M15("15 دقيقة"), M20("20 دقيقة"), M30("30 دقيقة"), H1("1 ساعة"), H2("2 ساعة"), H3("3 ساعات"), H4("4 ساعات"), H6("6 ساعات"), H8("8 ساعات"), H12("12 ساعة"), D1("يومي"), W1("أسبوعي"), MN1("شهري")
}

@Composable
fun AmarTradingChartScreen(
    symbol: String = "الذهب",
    provider: AmarMarketDataProvider? = null,
) {
    var timeframe by remember { mutableStateOf(AmarTimeframe.M1) }
    val candles = remember(symbol, timeframe, provider) {
        provider?.candles(symbol, timeframe).orEmpty()
    }

    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("الرسم البياني: $symbol")
            Text("• ${timeframe.arabicLabel}")
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AmarTimeframe.entries.forEach { frame ->
                FilterChip(
                    selected = timeframe == frame,
                    onClick = { timeframe = frame },
                    label = { Text(frame.arabicLabel) },
                )
            }
        }
        if (candles.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                Text("بانتظار مصدر بيانات السوق الحقيقي")
                Text("لا يتم إنشاء شموع وهمية أو محاكاة داخل شاشة التداول")
            }
        } else {
            AmarCandleCanvas(candles, Modifier.fillMaxWidth().weight(1f))
        }
    }
}

@Composable
private fun AmarCandleCanvas(candles: List<AmarCandle>, modifier: Modifier) {
    Canvas(modifier) {
        val visible = candles.takeLast(120)
        val min = visible.minOf { it.low }
        val max = visible.maxOf { it.high }
        val range = (max - min).coerceAtLeast(0.0000001)
        val step = size.width / visible.size.coerceAtLeast(1)
        val bodyWidth = (step * 0.62f).coerceAtLeast(2f)

        visible.forEachIndexed { index, candle ->
            fun y(price: Double): Float = size.height - (((price - min) / range) * size.height).toFloat()
            val x = step * index + step / 2f
            val up = candle.close >= candle.open
            val bodyTop = y(maxOf(candle.open, candle.close))
            val bodyBottom = y(minOf(candle.open, candle.close))
            val wickColor = if (up) Color(0xFFE0E0E0) else Color(0xFFFFC857)
            drawLine(whickColor(wickColor), Offset(x, y(candle.high)), Offset(x, y(candle.low)), strokeWidth = 1.5f)
            drawRect(
                color = if (up) Color(0xFFE0E0E0) else Color(0xFFFFC857),
                topLeft = Offset(x - bodyWidth / 2f, bodyTop),
                size = androidx.compose.ui.geometry.Size(bodyWidth, (bodyBottom - bodyTop).coerceAtLeast(2f)),
            )
        }
    }
}

private fun whickColor(color: Color): Color = color
