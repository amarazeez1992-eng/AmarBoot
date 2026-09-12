package com.personal.gridbot.ui.amarapproved

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val HomeBg = Color(0xFF05080D)
private val HomePanel = Color(0xE6101821)
private val HomeGold = Color(0xFFD9A52E)
private val HomeGoldLight = Color(0xFFFFD66B)
private val HomeBlack = Color(0xFF030507)
private val HomeWhite = Color(0xFFF4F8FA)
private val HomeCyan = Color(0xFF20E6FF)
private val HomeGreen = Color(0xFF00D99A)
private val HomeRed = Color(0xFFFF435E)
private val HomeLine = Color(0xFF29404D)
private val HomeMuted = Color(0xFF91A5AF)

@Composable
fun AmarApprovedHomeScreen() {
    var selected by remember { mutableStateOf("الرئيسية") }
    var clock by remember { mutableStateOf(formatArabicClock()) }
    val motion = rememberInfiniteTransition(label = "official-home-motion")
    val pulse by motion.animateFloat(.96f, 1.04f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "amar-pulse")
    val sweep by motion.animateFloat(0f, 360f, infiniteRepeatable(tween(9000)), label = "amar-orbit")

    LaunchedEffect(Unit) {
        while (true) {
            clock = formatArabicClock()
            delay(1000)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(HomeBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        val compact = maxWidth < 360.dp
        val side = if (compact) 9.dp else 14.dp

        Column(
            Modifier.fillMaxSize().padding(horizontal = side),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(7.dp))
            OfficialTopBar(compact, clock)
            Spacer(Modifier.height(if (compact) 7.dp else 10.dp))

            Box(
                Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                OfficialSpatialLayout(
                    compact = compact,
                    pulse = pulse,
                    sweep = sweep,
                    selected = selected,
                    onSelect = { selected = it }
                )
            }

            Spacer(Modifier.height(5.dp))
            Text("منصة التداول الذكية", color = HomeMuted, fontSize = if (compact) 10.sp else 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            OfficialBottomBar(selected) { selected = it }
            Spacer(Modifier.height(5.dp))
        }
    }
}

private fun formatArabicClock(): String {
    val raw = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
    val suffix = if (raw.endsWith("AM")) "ص" else "م"
    val digits = raw.removeSuffix(" AM").removeSuffix(" PM")
        .map { when (it) { '0' -> '٠'; '1' -> '١'; '2' -> '٢'; '3' -> '٣'; '4' -> '٤'; '5' -> '٥'; '6' -> '٦'; '7' -> '٧'; '8' -> '٨'; '9' -> '٩'; else -> it } }
        .joinToString("")
    return "$digits $suffix"
}

@Composable
private fun OfficialTopBar(compact: Boolean, clock: String) {
    Row(
        Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp))
            .background(HomePanel).padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 7.dp else 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatusPill()
        ClockCard(clock, compact)
        Box(
            Modifier.size(if (compact) 29.dp else 34.dp).clip(CircleShape)
                .background(Brush.radialGradient(listOf(HomeGoldLight, HomeGold, HomeBlack))),
            contentAlignment = Alignment.Center
        ) { TinyLineIcon(IconKind.SETTINGS, HomeBlack, Modifier.size(if (compact) 18.dp else 21.dp)) }
    }
}

@Composable
private fun StatusPill() {
    Row(
        Modifier.clip(RoundedCornerShape(40.dp)).background(HomePanel).padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(HomeGreen).shadow(5.dp, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text("متصل الآن", color = HomeWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ClockCard(clock: String, compact: Boolean) {
    Row(
        Modifier.clip(RoundedCornerShape(18.dp)).background(HomeBlack).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(clock, color = HomeGoldLight, fontSize = if (compact) 9.sp else 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OfficialSpatialLayout(
    compact: Boolean,
    pulse: Float,
    sweep: Float,
    selected: String,
    onSelect: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val circle = if (compact) 62.dp else 72.dp
        val center = if (compact) 112.dp else 132.dp
        val label = if (compact) 70.dp else 82.dp

        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = minOf(size.width, size.height) * .28f
            drawCircle(HomeGold.copy(alpha = .08f), r, c, style = Stroke(1.4.dp.toPx()))
            drawCircle(HomeCyan.copy(alpha = .06f), r * .76f, c, style = Stroke(1.2.dp.toPx()))
            drawArc(HomeGold.copy(alpha = .55f), sweep, 90f, false, Offset(c.x-r,c.y-r), Size(r*2,r*2), style=Stroke(2.dp.toPx()))
            drawArc(HomeCyan.copy(alpha = .45f), sweep+180f, 75f, false, Offset(c.x-r*.86f,c.y-r*.86f), Size(r*1.72f,r*1.72f), style=Stroke(1.5.dp.toPx()))
        }

        // Left side: three fixed, non-overlapping circles.
        OfficialNode("الأخبار", IconKind.NEWS, HomeGold, circle, label, selected == "الأخبار", Modifier.align(Alignment.CenterStart)) { onSelect("الأخبار") }
        OfficialNode("حالة السوق", IconKind.MARKET, HomeGreen, circle, label, selected == "حالة السوق", Modifier.align(Alignment.TopStart)) { onSelect("حالة السوق") }
        OfficialNode("مختبر البوتات", IconKind.BOT, HomeCyan, circle, label, selected == "مختبر البوتات", Modifier.align(Alignment.BottomStart)) { onSelect("مختبر البوتات") }

        // Right side: three fixed, non-overlapping circles.
        OfficialNode("AMAR AI", IconKind.AI, HomeCyan, circle, label, selected == "AMAR AI", Modifier.align(Alignment.CenterEnd)) { onSelect("AMAR AI") }
        OfficialNode("الرسم البياني", IconKind.CHART, HomeGold, circle, label, selected == "الرسم البياني", Modifier.align(Alignment.TopEnd)) { onSelect("الرسم البياني") }
        OfficialNode("التحليل الذكي", IconKind.ANALYSIS, HomeGoldLight, circle, label, selected == "التحليل الذكي", Modifier.align(Alignment.BottomEnd)) { onSelect("التحليل الذكي") }

        AmarCenterCore(
            modifier = Modifier.align(Alignment.Center).size(center * pulse),
            sweep = sweep
        )

        // Red state indicator is intentionally kept hidden inside the center layer.
        Box(Modifier.align(Alignment.Center).size(10.dp).clip(CircleShape).background(HomeRed.copy(alpha = 0.0f)))
    }
}

@Composable
private fun AmarCenterCore(modifier: Modifier, sweep: Float) {
    Box(
        modifier.shadow(22.dp, CircleShape).clip(CircleShape)
            .background(Brush.radialGradient(listOf(HomeWhite, HomeGoldLight, HomeGold, HomeBlack))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            drawCircle(HomeBlack.copy(alpha = .55f), style = Stroke(2.dp.toPx()))
            drawOval(topLeft = Offset(size.width*.10f,size.height*.23f), size = Size(size.width*.80f,size.height*.54f), color = HomeGold.copy(alpha=.60f), style = Stroke(1.7.dp.toPx()))
            drawArc(color=HomeWhite.copy(alpha=.8f), startAngle=sweep, sweepAngle=70f, useCenter=false, topLeft=Offset(size.width*.06f,size.height*.06f), size=Size(size.width*.88f,size.height*.88f), style=Stroke(2.dp.toPx()))
            drawCircle(HomeGold, radius=4.dp.toPx(), center=Offset(size.width*.72f,size.height*.29f))
        }
        Text("AMAR", color = HomeBlack, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
    }
}

@Composable
private fun OfficialNode(
    title: String,
    kind: IconKind,
    accent: Color,
    circle: Dp,
    labelWidth: Dp,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier.clickable(onClick=onClick), horizontalAlignment=Alignment.CenterHorizontally) {
        Box(
            Modifier.size(circle).shadow(if(selected) 18.dp else 11.dp, CircleShape).clip(CircleShape)
                .background(Brush.radialGradient(listOf(HomeWhite, accent.copy(alpha=.78f), HomeBlack))),
            contentAlignment=Alignment.Center
        ) { TinyLineIcon(kind, if(selected) HomeBlack else HomeWhite, Modifier.size(circle*.47f)) }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.width(labelWidth).clip(RoundedCornerShape(40.dp))
                .background(Brush.horizontalGradient(listOf(HomeBlack, accent.copy(alpha=.72f))))
                .padding(horizontal=5.dp, vertical=4.dp),
            contentAlignment=Alignment.Center
        ) { Text(title, color=HomeWhite, fontSize=8.sp, fontWeight=FontWeight.Bold, textAlign=TextAlign.Center, maxLines=1) }
    }
}

private enum class IconKind { NEWS, MARKET, BOT, AI, CHART, ANALYSIS, SETTINGS, HOME, ALERT, PERFORMANCE, MORE }

@Composable
private fun TinyLineIcon(kind: IconKind, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val s = 2.1.dp.toPx(); val w=size.width; val h=size.height
        when(kind) {
            IconKind.NEWS -> {
                drawRoundRect(color, Offset(w*.17f,h*.18f), Size(w*.66f,h*.64f), CornerRadius(5f,5f), style=Stroke(s))
                drawLine(color,Offset(w*.29f,h*.34f),Offset(w*.71f,h*.34f),strokeWidth=s)
                drawLine(color,Offset(w*.29f,h*.49f),Offset(w*.71f,h*.49f),strokeWidth=s)
                drawLine(color,Offset(w*.29f,h*.64f),Offset(w*.58f,h*.64f),strokeWidth=s)
            }
            IconKind.MARKET -> {
                drawLine(color,Offset(w*.18f,h*.82f),Offset(w*.18f,h*.18f),strokeWidth=s);drawLine(color,Offset(w*.18f,h*.82f),Offset(w*.88f,h*.82f),strokeWidth=s)
                drawRect(color,Offset(w*.29f,h*.56f),Size(w*.11f,h*.26f));drawRect(color,Offset(w*.48f,h*.39f),Size(w*.11f,h*.43f));drawRect(color,Offset(w*.67f,h*.24f),Size(w*.11f,h*.58f))
            }
            IconKind.BOT -> {
                drawRoundRect(color,Offset(w*.18f,h*.25f),Size(w*.64f,h*.50f),CornerRadius(7f,7f),style=Stroke(s));drawCircle(color,2.3.dp.toPx(),Offset(w*.40f,h*.49f));drawCircle(color,2.3.dp.toPx(),Offset(w*.60f,h*.49f));drawLine(color,Offset(w*.5f,h*.25f),Offset(w*.5f,h*.10f),strokeWidth=s);drawCircle(color,2.dp.toPx(),Offset(w*.5f,h*.09f))
            }
            IconKind.AI -> {
                drawCircle(color,w*.30f,Offset(w*.5f,h*.5f),style=Stroke(s));drawArc(color,0f,260f,false,Offset(w*.18f,h*.18f),Size(w*.64f,h*.64f),style=Stroke(s));drawCircle(color,2.4.dp.toPx(),Offset(w*.73f,h*.28f));drawLine(color,Offset(w*.5f,h*.18f),Offset(w*.5f,h*.08f),strokeWidth=s)
            }
            IconKind.CHART -> {
                drawLine(color,Offset(w*.12f,h*.82f),Offset(w*.12f,h*.18f),strokeWidth=s);drawLine(color,Offset(w*.12f,h*.82f),Offset(w*.90f,h*.82f),strokeWidth=s);drawLine(color,Offset(w*.23f,h*.66f),Offset(w*.43f,h*.50f),strokeWidth=s);drawLine(color,Offset(w*.43f,h*.50f),Offset(w*.58f,h*.61f),strokeWidth=s);drawLine(color,Offset(w*.58f,h*.61f),Offset(w*.82f,h*.30f),strokeWidth=s)
            }
            IconKind.ANALYSIS -> { drawCircle(color,w*.28f,Offset(w*.46f,h*.45f),style=Stroke(s));drawLine(color,Offset(w*.65f,h*.64f),Offset(w*.85f,h*.84f),strokeWidth=s);drawLine(color,Offset(w*.58f,h*.34f),Offset(w*.78f,h*.14f),strokeWidth=s) }
            IconKind.SETTINGS -> { drawCircle(color,w*.30f,Offset(w/2f,h/2f),style=Stroke(s));drawCircle(color,w*.08f,Offset(w/2f,h/2f));for(i in 0 until 8){val a=Math.toRadians((i*45).toDouble());drawLine(color,Offset(w/2f+kotlin.math.cos(a).toFloat()*w*.38f,h/2f+kotlin.math.sin(a).toFloat()*h*.38f),Offset(w/2f+kotlin.math.cos(a).toFloat()*w*.48f,h/2f+kotlin.math.sin(a).toFloat()*h*.48f),strokeWidth=s)}}
            IconKind.HOME -> { val p=Path().apply{moveTo(w*.15f,h*.46f);lineTo(w*.5f,h*.16f);lineTo(w*.85f,h*.46f);lineTo(w*.78f,h*.46f);lineTo(w*.78f,h*.84f);lineTo(w*.22f,h*.84f);lineTo(w*.22f,h*.46f);close()};drawPath(p,color,style=Stroke(s)) }
            IconKind.ALERT -> { drawCircle(color,w*.34f,Offset(w/2f,h*.48f),style=Stroke(s));drawLine(color,Offset(w*.5f,h*.28f),Offset(w*.5f,h*.56f),strokeWidth=s);drawCircle(color,1.8.dp.toPx(),Offset(w*.5f,h*.68f)) }
            IconKind.PERFORMANCE -> { drawLine(color,Offset(w*.18f,h*.82f),Offset(w*.18f,h*.20f),strokeWidth=s);drawLine(color,Offset(w*.18f,h*.82f),Offset(w*.88f,h*.82f),strokeWidth=s);drawRect(color,Offset(w*.30f,h*.55f),Size(w*.12f,h*.27f));drawRect(color,Offset(w*.49f,h*.39f),Size(w*.12f,h*.43f));drawRect(color,Offset(w*.68f,h*.25f),Size(w*.12f,h*.57f)) }
            IconKind.MORE -> { drawCircle(color,2.5.dp.toPx(),Offset(w*.25f,h*.5f));drawCircle(color,2.5.dp.toPx(),Offset(w*.5f,h*.5f));drawCircle(color,2.5.dp.toPx(),Offset(w*.75f,h*.5f)) }
        }
    }
}

@Composable
private fun OfficialBottomBar(selected:String,onSelect:(String)->Unit) {
    val items=listOf("الإعدادات" to IconKind.SETTINGS,"التنبيهات" to IconKind.ALERT,"الرئيسية" to IconKind.HOME,"الأداء" to IconKind.PERFORMANCE,"المزيد" to IconKind.MORE)
    Row(Modifier.fillMaxWidth().shadow(12.dp,RoundedCornerShape(28.dp)).clip(RoundedCornerShape(28.dp)).background(HomePanel).padding(horizontal=3.dp,vertical=5.dp),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){items.forEach{(label,kind)->val active=selected==label;Column(Modifier.clickable{onSelect(label)}.padding(horizontal=5.dp,vertical=2.dp),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(if(active)39.dp else 30.dp).clip(CircleShape).background(if(active)Brush.radialGradient(listOf(HomeGoldLight,HomeGold,HomeBlack)) else Brush.radialGradient(listOf(Color(0xFF24313A),HomeBlack))),contentAlignment=Alignment.Center){TinyLineIcon(kind,if(active)HomeBlack else HomeMuted,Modifier.size(if(active)21.dp else 18.dp))};Text(label,color=if(active)HomeGoldLight else HomeMuted,fontSize=8.sp,fontWeight=if(active)FontWeight.ExtraBold else FontWeight.SemiBold)}}}}
