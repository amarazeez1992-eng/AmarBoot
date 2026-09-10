package com.personal.gridbot.amaros.bots

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.broker.AmarBridgeConfig
import com.personal.gridbot.amaros.broker.AmarMarketDirection
import com.personal.gridbot.amaros.broker.AmarMt5BridgeClient
import com.personal.gridbot.amaros.broker.AmarMt5MarketAnalysis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MarketInk = Color(0xFF172033)
private val MarketMuted = Color(0xFF718096)
private val MarketGreen = Color(0xFF12B76A)
private val MarketRed = Color(0xFFE5484D)
private val MarketYellow = Color(0xFFF4B400)
private val MarketBlue = Color(0xFF246BFE)
private val MarketBorder = Color(0xFFE1E7F0)
private val MarketOrange = Color(0xFFFF8A34)

private data class LiveFrame(val label: String, val key: String)

/** B30: real MT5 market state. No synthetic percentages and no strategy mutation. */
@Composable
fun Bot1MarketStatus() {
    var bridgeUrl by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("XAUUSD") }
    var connected by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("أدخل بيانات الجسر لبدء القراءة") }
    var accountText by remember { mutableStateOf("—") }
    var botText by remember { mutableStateOf("—") }
    var marketText by remember { mutableStateOf("—") }
    val directions = remember { mutableStateMapOf<String, AmarMarketDirection>() }
    val scope = rememberCoroutineScope()
    val frames = remember {
        listOf(
            LiveFrame("4 ساعات", "H4"),
            LiveFrame("ساعة", "H1"),
            LiveFrame("30 دقيقة", "M30"),
            LiveFrame("15 دقيقة", "M15"),
            LiveFrame("5 دقائق", "M5"),
            LiveFrame("1 دقيقة", "M1"),
        )
    }

    suspend fun refresh() {
        if (bridgeUrl.isBlank() || token.isBlank() || symbol.isBlank()) return
        loading = true
        try {
            val client = AmarMt5BridgeClient(AmarBridgeConfig(bridgeUrl.trim(), token))
            val health = client.health()
            if (!health.connected) {
                connected = false
                message = "الجسر يعمل لكن MT5 غير متصل"
                return
            }
            val account = client.account()
            val market = client.market(symbol.trim())
            val bot = client.botStatus(symbol = symbol.trim(), magic = 20260908L)
            accountText = "الرصيد ${account.balance} ${account.currency} • حقوق الملكية ${account.equity}"
            botText = "${bot.positions} صفقة • ${bot.pendingOrders} أمر معلق • الربح العائم ${bot.floatingProfit}"
            marketText = "شراء ${market.ask} • بيع ${market.bid} • السبريد ${market.spreadPoints}"
            frames.forEach { frame ->
                val candles = client.candles(symbol.trim(), frame.key, 80).items
                directions[frame.key] = AmarMt5MarketAnalysis.direction(candles)
            }
            connected = true
            message = "بيانات حقيقية • آخر تحديث ${market.timestampMs}"
        } catch (error: Exception) {
            connected = false
            message = error.message ?: "تعذر الاتصال بالجسر"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(bridgeUrl, token, symbol) {
        if (bridgeUrl.isBlank() || token.isBlank()) return@LaunchedEffect
        while (true) {
            refresh()
            delay(3_000L)
        }
    }

    val summary = AmarMt5MarketAnalysis.summary(directions.values.toList())
    val pulse by rememberInfiniteTransition(label = "marketPulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "marketPulseValue"
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("حالة السوق", color = MarketInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("قراءة حقيقية متعددة الفريمات لبوت 1", color = MarketBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.size(11.dp).alpha(pulse).background(if (connected) MarketGreen else MarketYellow, androidx.compose.foundation.shape.CircleShape))
            }
            Spacer(Modifier.height(12.dp))
            ConnectionSettings(
                bridgeUrl = bridgeUrl,
                token = token,
                symbol = symbol,
                loading = loading,
                onUrl = { bridgeUrl = it },
                onToken = { token = it },
                onSymbol = { symbol = it },
                onConnect = { scope.launch { refresh() } },
            )
            Spacer(Modifier.height(10.dp))
            StatusBanner(connected, message)
            Spacer(Modifier.height(10.dp))
            LiveAccountStrip(accountText, botText, marketText)
            Spacer(Modifier.height(11.dp))
            Text("اتجاه الفريمات", color = MarketInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(7.dp))
            frames.forEach { frame ->
                MarketTimeframeRow(frame.label, directions[frame.key])
                Spacer(Modifier.height(6.dp))
            }
            Spacer(Modifier.height(6.dp))
            MarketSummary(summary.first, summary.second, summary.third)
            Spacer(Modifier.height(8.dp))
            Text("هذه القراءة وصفية فقط ولا تغيّر إعدادات BOT 1 أو تتخذ قرار تنفيذ.", color = MarketMuted, fontSize = 9.sp)
        }
    }
}

@Composable
private fun ConnectionSettings(
    bridgeUrl: String,
    token: String,
    symbol: String,
    loading: Boolean,
    onUrl: (String) -> Unit,
    onToken: (String) -> Unit,
    onSymbol: (String) -> Unit,
    onConnect: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text("اتصال MT5 — قراءة فقط", color = MarketInk, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        OutlinedTextField(value = bridgeUrl, onValueChange = onUrl, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("عنوان الجسر الآمن") }, placeholder = { Text("https://عنوان-اللابتوب:8765") })
        OutlinedTextField(value = token, onValueChange = onToken, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("رمز الجسر") })
        OutlinedTextField(value = symbol, onValueChange = onSymbol, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("رمز السوق") })
        Button(
            onClick = onConnect,
            enabled = !loading && bridgeUrl.isNotBlank() && token.isNotBlank() && symbol.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(43.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MarketBlue),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        ) { Text(if (loading) "جاري الاتصال…" else "اختبار الاتصال والقراءة") }
    }
}

@Composable
private fun StatusBanner(connected: Boolean, message: String) {
    val color = if (connected) MarketGreen else MarketYellow
    Row(
        modifier = Modifier.fillMaxWidth().background(color.copy(alpha = 0.08f), androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
            .border(1.dp, color.copy(alpha = 0.28f), androidx.compose.foundation.shape.RoundedCornerShape(13.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.size(7.dp))
        Text(message, color = if (connected) MarketGreen else Color(0xFF805B12), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LiveAccountStrip(account: String, bot: String, market: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(account, color = MarketInk, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        Text(bot, color = MarketBlue, fontSize = 10.sp)
        Text(market, color = MarketOrange, fontSize = 10.sp)
    }
}

@Composable
private fun MarketTimeframeRow(label: String, direction: AmarMarketDirection?) {
    val color = when (direction?.labelAr) {
        "صاعد" -> MarketGreen
        "هابط" -> MarketRed
        "محايد" -> MarketYellow
        else -> MarketMuted
    }
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
            .border(1.dp, MarketBorder, androidx.compose.foundation.shape.RoundedCornerShape(13.dp)).padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), color = MarketInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Box(Modifier.size(8.dp).background(color, androidx.compose.foundation.shape.CircleShape))
        Spacer(Modifier.size(6.dp))
        Text(direction?.labelAr ?: "بانتظار البيانات", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(8.dp))
        Text(direction?.percentage?.let { "$it%" } ?: "—", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MarketSummary(buy: Int, neutral: Int, sell: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        MarketScoreCard("الشراء", if (buy == 0) "—" else "$buy%", MarketGreen, Modifier.weight(1f))
        MarketScoreCard("الحياد", if (neutral == 0) "—" else "$neutral%", MarketYellow, Modifier.weight(1f))
        MarketScoreCard("البيع", if (sell == 0) "—" else "$sell%", MarketRed, Modifier.weight(1f))
    }
}

@Composable
private fun MarketScoreCard(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier.background(color.copy(alpha = 0.07f), androidx.compose.foundation.shape.RoundedCornerShape(13.dp))
            .border(1.dp, color.copy(alpha = 0.25f), androidx.compose.foundation.shape.RoundedCornerShape(13.dp)).padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = MarketMuted, fontSize = 9.sp)
    }
}
