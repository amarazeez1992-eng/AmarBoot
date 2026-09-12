package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.bots.AmarMarketStateStore
import kotlin.math.cos
import kotlin.math.sin

private val StageBg = Color(0xFF02050A)
private val StageDeep = Color(0xFF081521)
private val StageCyan = Color(0xFF28E7FF)
private val StageBlue = Color(0xFF397BFF)
private val StageGold = Color(0xFFFFC84A)
private val StageMagenta = Color(0xFFFF4FD8)
private val StageWhite = Color(0xFFF2FBFF)

@Composable
fun AmarAi3DOrbitalStage(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "amar_ai_roaming")
    val route by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(12000, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "roaming_route"
    )
    val pulse by transition.animateFloat(
        .55f,
        1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    val bob by transition.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val spin by transition.animateFloat(
        -2f,
        2f,
        infiniteRepeatable(tween(2300, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "spin"
    )

    val market = AmarMarketStateStore.snapshot
    val direction = when (market.direction.name) {
        "BUY" -> "شراء"
        "SELL" -> "بيع"
        "NEUTRAL" -> "محايد"
        else -> "—"
    }
    val marketText = if (market.symbol.isNotBlank()) "السوق ${market.symbol} • $direction" else "السوق • $direction"

    // One normalized route is shared by the robot, AI identity, news and market state.
    val phase = route * 4f
    val segment = phase.toInt().coerceAtMost(3)
    val local = phase - segment
    val eased = local * local * (3f - 2f * local)
    val nx = when (segment) {
        0 -> -1f
        1 -> -1f + 2f * eased
        2 -> 1f
        else -> 1f - 2f * eased
    }
    val ny = when (segment) {
        0 -> 1f - 2f * eased
        1 -> -1f
        2 -> -1f + 2f * eased
        else -> 1f
    }

    Box(modifier.fillMaxWidth().height(235.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val rx = size.width * .32f
            val ry = size.height * .28f
            val convoyX = nx * rx
            val convoyY = ny * ry

            drawRect(Brush.verticalGradient(listOf(StageBg, StageDeep, StageBg)))
            drawCircle(StageCyan.copy(alpha = .05f), size.minDimension * .25f, androidx.compose.ui.geometry.Offset(cx, cy))
            drawCircle(StageBlue.copy(alpha = .035f), size.minDimension * .42f, androidx.compose.ui.geometry.Offset(cx, cy))

            for (i in 0..3) {
                val scale = 1f + i * .12f
                drawOval(
                    brush = Brush.sweepGradient(listOf(StageCyan.copy(alpha = .10f / scale), StageBlue.copy(alpha = .04f / scale), StageGold.copy(alpha = .065f / scale), StageCyan.copy(alpha = .10f / scale))),
                    topLeft = androidx.compose.ui.geometry.Offset(cx - rx * scale, cy - ry * scale),
                    size = androidx.compose.ui.geometry.Size(rx * 2f * scale, ry * 2f * scale),
                    style = Stroke(width = 1f + i * .35f)
                )
            }

            val p1 = androidx.compose.ui.geometry.Offset(cx - rx, cy + ry)
            val p2 = androidx.compose.ui.geometry.Offset(cx - rx, cy - ry)
            val p3 = androidx.compose.ui.geometry.Offset(cx + rx, cy - ry)
            val p4 = androidx.compose.ui.geometry.Offset(cx + rx, cy + ry)
            listOf(p1 to p2, p2 to p3, p3 to p4, p4 to p1).forEach { (a, b) ->
                drawLine(StageCyan.copy(alpha = .11f), a, b, 1.4f, cap = StrokeCap.Round)
            }

            withTransform({
                translate(convoyX, convoyY)
                rotate(spin, pivot = androidx.compose.ui.geometry.Offset(cx, cy))
            }) {
                val lift = bob * 3.5f
                val headW = size.width * .14f
                val headH = size.height * .15f
                val headCx = cx
                val headCy = cy - size.height * .15f + lift
                val headTop = headCy - headH / 2f

                drawCircle(StageCyan.copy(alpha = .10f), headW * .72f, androidx.compose.ui.geometry.Offset(headCx, headCy))
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF214D62), Color(0xFF07121B), Color(0xFF163042))),
                    topLeft = androidx.compose.ui.geometry.Offset(headCx - headW / 2f, headTop),
                    size = androidx.compose.ui.geometry.Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headW * .18f, headH * .18f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan, StageBlue, StageGold)),
                    topLeft = androidx.compose.ui.geometry.Offset(headCx - headW / 2f, headTop),
                    size = androidx.compose.ui.geometry.Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headW * .18f, headH * .18f),
                    style = Stroke(2f)
                )
                drawCircle(StageWhite.copy(alpha = pulse), headW * .045f, androidx.compose.ui.geometry.Offset(headCx - headW * .18f, headCy))
                drawCircle(StageWhite.copy(alpha = pulse), headW * .045f, androidx.compose.ui.geometry.Offset(headCx + headW * .18f, headCy))
                drawLine(StageGold.copy(alpha = .9f), androidx.compose.ui.geometry.Offset(headCx, headTop), androidx.compose.ui.geometry.Offset(headCx, headTop - headH * .32f), 2f, cap = StrokeCap.Round)
                drawCircle(StageMagenta.copy(alpha = pulse), 4f, androidx.compose.ui.geometry.Offset(headCx, headTop - headH * .36f))

                val bodyW = size.width * .19f
                val bodyH = size.height * .21f
                val bodyTop = cy + size.height * .01f + lift
                val bodyLeft = headCx - bodyW / 2f
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF1C4050), Color(0xFF050B11), Color(0xFF182A47))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop),
                    size = androidx.compose.ui.geometry.Size(bodyW, bodyH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f, 22f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan.copy(alpha = .65f), StageBlue, StageGold.copy(alpha = .65f))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop),
                    size = androidx.compose.ui.geometry.Size(bodyW, bodyH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(22f, 22f),
                    style = Stroke(1.8f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan.copy(alpha = .85f), StageGold.copy(alpha = .7f))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * .28f, bodyTop + bodyH * .28f),
                    size = androidx.compose.ui.geometry.Size(bodyW * .44f, bodyH * .17f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
                )

                val armY = bodyTop + bodyH * .40f
                drawLine(StageCyan.copy(alpha = .85f), androidx.compose.ui.geometry.Offset(bodyLeft, armY), androidx.compose.ui.geometry.Offset(bodyLeft - bodyW * .27f, armY + 13f), 5f, cap = StrokeCap.Round)
                drawLine(StageBlue.copy(alpha = .9f), androidx.compose.ui.geometry.Offset(bodyLeft + bodyW, armY), androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * 1.27f, armY + 13f), 5f, cap = StrokeCap.Round)
                drawCircle(StageGold, 4.5f, androidx.compose.ui.geometry.Offset(bodyLeft - bodyW * .27f, armY + 13f))
                drawCircle(StageMagenta, 4.5f, androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * 1.27f, armY + 13f))

                val legY = bodyTop + bodyH
                drawLine(StageBlue.copy(alpha = .85f), androidx.compose.ui.geometry.Offset(headCx - bodyW * .20f, legY), androidx.compose.ui.geometry.Offset(headCx - bodyW * .20f, legY + 18f), 6f, cap = StrokeCap.Round)
                drawLine(StageCyan.copy(alpha = .85f), androidx.compose.ui.geometry.Offset(headCx + bodyW * .20f, legY), androidx.compose.ui.geometry.Offset(headCx + bodyW * .20f, legY + 18f), 6f, cap = StrokeCap.Round)
            }

            val satellite = androidx.compose.ui.geometry.Offset(
                cx + cos(route * Math.PI * 2).toFloat() * rx * 1.16f,
                cy + sin(route * Math.PI * 2).toFloat() * ry * 1.16f
            )
            drawCircle(StageGold.copy(alpha = .75f), 3.5f, satellite)
        }

        // The information convoy uses the exact same normalized route as the robot.
        Box(
            Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = nx * 120f
                    translationY = ny * 66f
                }
        ) {
            Box {
                Text("◈ AMAR AI  •  SUPERVISOR", color = StageCyan, fontSize = 8.sp, fontWeight = FontWeight.Black)
                Text("NEWS  •  $marketText", color = StageGold, fontSize = 7.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(
            "AI • NEWS • MARKET  /  LIVE ROAMING",
            color = StageCyan.copy(alpha = .72f),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
