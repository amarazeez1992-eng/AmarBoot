package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

private val StageBg = Color(0xFF02050A)
private val StageDeep = Color(0xFF071522)
private val StageCyan = Color(0xFF28E7FF)
private val StageBlue = Color(0xFF397BFF)
private val StageGold = Color(0xFFFFC84A)
private val StageMagenta = Color(0xFFFF4FD8)
private val StageWhite = Color(0xFFF2FBFF)

@Composable
fun AmarAi3DOrbitalStage(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "amar_ai_professional_roaming")
    val route by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = tween(14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "convoy_route"
    )
    val pulse by transition.animateFloat(
        .72f,
        1f,
        infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "ai_pulse"
    )
    val bob by transition.animateFloat(
        -1f,
        1f,
        infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ai_bob"
    )
    val spin by transition.animateFloat(
        -2.5f,
        2.5f,
        infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ai_spin"
    )

    val market = AmarMarketStateStore.snapshot
    val direction = when (market.direction.name) {
        "BUY" -> "شراء"
        "SELL" -> "بيع"
        "NEUTRAL" -> "محايد"
        else -> "—"
    }
    val marketText = if (market.symbol.isNotBlank()) {
        "السوق • ${market.symbol} • $direction"
    } else {
        "حالة السوق • $direction"
    }

    val phase = route * 4f
    val segment = phase.toInt().coerceAtMost(3)
    val local = phase - segment
    val eased = local * local * (3f - 2f * local)

    Box(modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxWidth().height(300.dp).padding(horizontal = 8.dp)) {
            val cx = size.width / 2f
            val cy = size.height * .50f
            val rx = size.width * .36f
            val ry = size.height * .29f
            val convoyX = when (segment) {
                0 -> -rx
                1 -> -rx + rx * 2f * eased
                2 -> rx
                else -> rx - rx * 2f * eased
            }
            val convoyY = when (segment) {
                0 -> ry - ry * 2f * eased
                1 -> -ry
                2 -> -ry + ry * 2f * eased
                else -> ry
            }

            drawRect(Brush.verticalGradient(listOf(StageBg, StageDeep, StageBg)))
            drawCircle(StageCyan.copy(alpha = .055f), size.minDimension * .32f, androidx.compose.ui.geometry.Offset(cx, cy))
            drawCircle(StageBlue.copy(alpha = .035f), size.minDimension * .48f, androidx.compose.ui.geometry.Offset(cx, cy))
            drawCircle(StageMagenta.copy(alpha = .022f), size.minDimension * .60f, androidx.compose.ui.geometry.Offset(cx, cy))

            for (i in 0..3) {
                val scale = 1f + i * .13f
                drawOval(
                    brush = Brush.sweepGradient(listOf(StageCyan.copy(alpha = .10f / scale), StageBlue.copy(alpha = .045f / scale), StageGold.copy(alpha = .07f / scale), StageCyan.copy(alpha = .10f / scale))),
                    topLeft = androidx.compose.ui.geometry.Offset(cx - rx * scale, cy - ry * scale),
                    size = androidx.compose.ui.geometry.Size(rx * 2f * scale, ry * 2f * scale),
                    style = Stroke(width = 1.1f + i * .4f)
                )
            }

            val routePoints = listOf(
                androidx.compose.ui.geometry.Offset(cx - rx, cy + ry),
                androidx.compose.ui.geometry.Offset(cx - rx, cy - ry),
                androidx.compose.ui.geometry.Offset(cx + rx, cy - ry),
                androidx.compose.ui.geometry.Offset(cx + rx, cy + ry),
                androidx.compose.ui.geometry.Offset(cx - rx, cy + ry)
            )
            for (i in 0 until routePoints.lastIndex) {
                drawLine(StageCyan.copy(alpha = .12f), routePoints[i], routePoints[i + 1], 1.5f, cap = StrokeCap.Round)
            }

            withTransform({
                translate(convoyX, convoyY)
                rotate(spin, pivot = androidx.compose.ui.geometry.Offset(cx, cy))
            }) {
                val localBob = bob * 4f
                val headW = size.width * .16f
                val headH = size.height * .17f
                val headCx = cx
                val headCy = cy - size.height * .18f + localBob
                val headTop = headCy - headH / 2f

                drawRoundRect(
                    brush = Brush.linearGradient(listOf(StageBlue.copy(alpha = .20f), StageMagenta.copy(alpha = .08f))),
                    topLeft = androidx.compose.ui.geometry.Offset(headCx - headW * .62f, headCy - headH * .62f),
                    size = androidx.compose.ui.geometry.Size(headW * 1.24f, headH * 1.24f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headW * .25f, headH * .25f)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF214D62), Color(0xFF07121B), Color(0xFF163042))),
                    topLeft = androidx.compose.ui.geometry.Offset(headCx - headW / 2f, headTop),
                    size = androidx.compose.ui.geometry.Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headW * .18f, headH * .18f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan.copy(alpha = .9f), StageBlue.copy(alpha = .65f), StageGold.copy(alpha = .75f))),
                    topLeft = androidx.compose.ui.geometry.Offset(headCx - headW / 2f, headTop),
                    size = androidx.compose.ui.geometry.Size(headW, headH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(headW * .18f, headH * .18f),
                    style = Stroke(2.4f)
                )
                drawCircle(StageCyan.copy(alpha = .18f), headW * .50f, androidx.compose.ui.geometry.Offset(headCx, headCy))
                drawCircle(StageWhite.copy(alpha = pulse), headW * .052f, androidx.compose.ui.geometry.Offset(headCx - headW * .19f, headCy))
                drawCircle(StageWhite.copy(alpha = pulse), headW * .052f, androidx.compose.ui.geometry.Offset(headCx + headW * .19f, headCy))
                drawLine(StageGold.copy(alpha = .9f), androidx.compose.ui.geometry.Offset(headCx, headTop), androidx.compose.ui.geometry.Offset(headCx, headTop - headH * .35f), 2.2f, cap = StrokeCap.Round)
                drawCircle(StageMagenta.copy(alpha = pulse), 4.5f, androidx.compose.ui.geometry.Offset(headCx, headTop - headH * .39f))

                val bodyW = size.width * .22f
                val bodyH = size.height * .23f
                val bodyTop = cy - size.height * .015f + localBob
                val bodyLeft = headCx - bodyW / 2f
                drawRoundRect(
                    brush = Brush.linearGradient(listOf(Color(0xFF1C4050), Color(0xFF050B11), Color(0xFF182A47))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop),
                    size = androidx.compose.ui.geometry.Size(bodyW, bodyH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26f, 26f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan.copy(alpha = .55f), StageBlue.copy(alpha = .70f), StageGold.copy(alpha = .55f))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft, bodyTop),
                    size = androidx.compose.ui.geometry.Size(bodyW, bodyH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(26f, 26f),
                    style = Stroke(2f)
                )
                drawRoundRect(
                    brush = Brush.radialGradient(listOf(StageCyan.copy(alpha = .35f), StageBlue.copy(alpha = .05f))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * .15f, bodyTop + bodyH * .16f),
                    size = androidx.compose.ui.geometry.Size(bodyW * .70f, bodyH * .68f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(StageCyan.copy(alpha = .9f), StageGold.copy(alpha = .7f))),
                    topLeft = androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * .27f, bodyTop + bodyH * .29f),
                    size = androidx.compose.ui.geometry.Size(bodyW * .46f, bodyH * .18f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )

                val armY = bodyTop + bodyH * .42f
                drawLine(StageCyan.copy(alpha = .82f), androidx.compose.ui.geometry.Offset(bodyLeft, armY), androidx.compose.ui.geometry.Offset(bodyLeft - bodyW * .30f, armY + 17f), 6f, cap = StrokeCap.Round)
                drawLine(StageBlue.copy(alpha = .86f), androidx.compose.ui.geometry.Offset(bodyLeft + bodyW, armY), androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * 1.30f, armY + 17f), 6f, cap = StrokeCap.Round)
                drawCircle(StageGold.copy(alpha = .95f), 5.5f, androidx.compose.ui.geometry.Offset(bodyLeft - bodyW * .30f, armY + 17f))
                drawCircle(StageMagenta.copy(alpha = .90f), 5.5f, androidx.compose.ui.geometry.Offset(bodyLeft + bodyW * 1.30f, armY + 17f))

                val legY = bodyTop + bodyH
                drawLine(StageBlue.copy(alpha = .8f), androidx.compose.ui.geometry.Offset(headCx - bodyW * .23f, legY), androidx.compose.ui.geometry.Offset(headCx - bodyW * .23f, legY + 24f), 7f, cap = StrokeCap.Round)
                drawLine(StageCyan.copy(alpha = .8f), androidx.compose.ui.geometry.Offset(headCx + bodyW * .23f, legY), androidx.compose.ui.geometry.Offset(headCx + bodyW * .23f, legY + 24f), 7f, cap = StrokeCap.Round)
                drawLine(StageGold.copy(alpha = .75f), androidx.compose.ui.geometry.Offset(headCx - bodyW * .23f, legY + 24f), androidx.compose.ui.geometry.Offset(headCx - bodyW * .34f, legY + 24f), 4f, cap = StrokeCap.Round)
                drawLine(StageGold.copy(alpha = .75f), androidx.compose.ui.geometry.Offset(headCx + bodyW * .23f, legY + 24f), androidx.compose.ui.geometry.Offset(headCx + bodyW * .34f, legY + 24f), 4f, cap = StrokeCap.Round)
            }

            drawCircle(StageGold.copy(alpha = .65f), 4f, androidx.compose.ui.geometry.Offset(cx + convoyX, cy + convoyY))
        }

        // AI + NEWS + MARKET are a single moving convoy, not independent static labels.
        val stageWidth = 330f
        val stageHeight = 245f
        val convoyX = when (segment) {
            0 -> -stageWidth / 2f
            1 -> -stageWidth / 2f + stageWidth * eased
            2 -> stageWidth / 2f
            else -> stageWidth / 2f - stageWidth * eased
        }
        val convoyY = when (segment) {
            0 -> stageHeight / 2f - stageHeight * eased
            1 -> -stageHeight / 2f
            2 -> -stageHeight / 2f + stageHeight * eased
            else -> stageHeight / 2f
        }

        Box(
            Modifier
                .align(Alignment.Center)
                .graphicsLayer {
                    translationX = convoyX
                    translationY = convoyY
                }
        ) {
            Box(Modifier.graphicsLayer { scaleX = 1.03f; scaleY = 1.03f }) {
                Text(
                    "◈  AMAR AI  •  SUPERVISOR",
                    color = StageCyan.copy(alpha = .98f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    "NEWS ROOM  •  $marketText",
                    color = StageGold.copy(alpha = .98f),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }

        Text(
            "AMAR AI  •  المشرف الذكي  •  AI + NEWS + MARKET",
            color = StageCyan.copy(alpha = .88f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp)
        )
    }
}
