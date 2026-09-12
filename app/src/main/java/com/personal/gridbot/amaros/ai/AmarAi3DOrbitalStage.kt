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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personal.gridbot.amaros.bots.AmarMarketStateStore

private val StageBg = Color(0xFF03070B)
private val StageCyan = Color(0xFF1DE5FF)
private val StageGold = Color(0xFFFFC84A)
private val StageWhite = Color(0xFFEFFFFF)

@Composable
fun AmarAi3DOrbitalStage(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "amar_ai_roaming")
    val route by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "roaming_route"
    )
    val pulse by transition.animateFloat(
        initialValue = .72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "robot_pulse"
    )
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "robot_bob"
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

    Box(modifier.fillMaxWidth().height(290.dp), contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.matchParentSize().padding(horizontal = 8.dp)) {
            val cx = size.width / 2f
            val cy = size.height * .50f
            val rx = size.width * .38f
            val ry = size.height * .29f

            drawRect(Brush.verticalGradient(listOf(StageBg, Color(0xFF07131A), StageBg)))
            drawCircle(StageCyan.copy(alpha = .055f), size.minDimension * .32f, Offset(cx, cy))
            drawCircle(StageGold.copy(alpha = .028f), size.minDimension * .44f, Offset(cx, cy))

            for (i in 0..2) {
                val scale = 1f + i * .14f
                val ovalRect = Rect(
                    cx - rx * scale,
                    cy - ry * scale,
                    cx + rx * scale,
                    cy + ry * scale
                )
                drawOval(
                    brush = Brush.linearGradient(
                        listOf(
                            StageCyan.copy(alpha = .10f / scale),
                            StageGold.copy(alpha = .04f / scale)
                        )
                    ),
                    topLeft = ovalRect.topLeft,
                    size = ovalRect.size,
                    style = Stroke(width = 1.2f + i * .45f)
                )
            }

            val routePoints = listOf(
                Offset(cx - rx * .88f, cy + ry * .80f),
                Offset(cx - rx * .88f, cy - ry * .80f),
                Offset(cx + rx * .88f, cy - ry * .80f),
                Offset(cx + rx * .88f, cy + ry * .80f)
            )
            for (i in 0 until routePoints.lastIndex) {
                drawLine(
                    StageCyan.copy(alpha = .09f),
                    routePoints[i],
                    routePoints[i + 1],
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }

            val localBob = bob * 5f
            val headW = size.width * .16f
            val headH = size.height * .18f
            val headCx = cx
            val headCy = cy - size.height * .19f + localBob
            val head = Rect(
                headCx - headW / 2,
                headCy - headH / 2,
                headCx + headW / 2,
                headCy + headH / 2
            )
            drawRoundRect(
                brush = Brush.radialGradient(listOf(Color(0xFF183746), Color(0xFF071017))),
                topLeft = head.topLeft,
                size = head.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(.18f * headW, .18f * headH)
            )
            drawRoundRect(
                color = StageCyan.copy(alpha = .78f),
                topLeft = head.topLeft,
                size = head.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(.18f * headW, .18f * headH),
                style = Stroke(2.2f)
            )
            drawCircle(StageCyan.copy(alpha = .28f), headW * .48f, head.center)
            drawCircle(StageWhite.copy(alpha = pulse), headW * .045f, Offset(headCx - headW * .18f, head.center.y))
            drawCircle(StageWhite.copy(alpha = pulse), headW * .045f, Offset(headCx + headW * .18f, head.center.y))
            drawLine(StageGold, Offset(headCx, head.top), Offset(headCx, head.top - headH * .34f), 2f, cap = StrokeCap.Round)
            drawCircle(StageGold.copy(alpha = pulse), 4f, Offset(headCx, head.top - headH * .38f))

            val bodyW = size.width * .21f
            val bodyH = size.height * .22f
            val bodyTop = cy - size.height * .02f + localBob
            val body = Rect(headCx - bodyW / 2, bodyTop, headCx + bodyW / 2, bodyTop + bodyH)
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF173A47), Color(0xFF061016))),
                topLeft = body.topLeft,
                size = body.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f)
            )
            drawRoundRect(
                color = StageGold.copy(alpha = .58f),
                topLeft = body.topLeft,
                size = body.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f),
                style = Stroke(2f)
            )
            val innerBody = Rect(
                body.left + bodyW * .12f,
                body.top + bodyH * .16f,
                body.right - bodyW * .12f,
                body.bottom - bodyH * .16f
            )
            drawRoundRect(
                color = StageCyan.copy(alpha = .22f),
                topLeft = innerBody.topLeft,
                size = innerBody.size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                style = Stroke(1.2f)
            )

            val armY = body.center.y - 2f
            drawLine(StageCyan.copy(alpha = .8f), Offset(body.left, armY), Offset(body.left - bodyW * .28f, armY + 18f), 6f, cap = StrokeCap.Round)
            drawLine(StageCyan.copy(alpha = .8f), Offset(body.right, armY), Offset(body.right + bodyW * .28f, armY + 18f), 6f, cap = StrokeCap.Round)
            drawCircle(StageGold, 5f, Offset(body.left - bodyW * .28f, armY + 18f))
            drawCircle(StageGold, 5f, Offset(body.right + bodyW * .28f, armY + 18f))

            val legY = body.bottom
            drawLine(StageCyan.copy(alpha = .65f), Offset(headCx - bodyW * .23f, legY), Offset(headCx - bodyW * .23f, legY + 22f), 7f, cap = StrokeCap.Round)
            drawLine(StageCyan.copy(alpha = .65f), Offset(headCx + bodyW * .23f, legY), Offset(headCx + bodyW * .23f, legY + 22f), 7f, cap = StrokeCap.Round)
        }

        val phase = route * 4f
        val segment = phase.toInt().coerceAtMost(3)
        val local = phase - segment
        val eased = local * local * (3f - 2f * local)
        val widthPx = 330f
        val heightPx = 245f
        val x = when (segment) {
            0 -> -widthPx / 2f
            1 -> -widthPx / 2f + widthPx * eased
            2 -> widthPx / 2f
            else -> widthPx / 2f - widthPx * eased
        }
        val y = when (segment) {
            0 -> heightPx / 2f - heightPx * eased
            1 -> -heightPx / 2f
            2 -> -heightPx / 2f + heightPx * eased
            else -> heightPx / 2f
        }

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
        ) {
            Text("🤖  AMAR AI", color = StageCyan.copy(alpha = .95f), fontSize = 9.sp, fontWeight = FontWeight.Black)
            Text(
                "• أخبار • $marketText",
                color = StageGold.copy(alpha = .95f),
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Text(
            "AMAR AI  •  المشرف الذكي • يتجول مع الأخبار وحالة السوق",
            color = StageCyan.copy(alpha = .82f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp)
        )
    }
}
