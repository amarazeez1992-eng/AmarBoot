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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val StageBg = Color(0xFF03070B)
private val StageCyan = Color(0xFF1DE5FF)
private val StageGold = Color(0xFFFFC84A)
private val StageWhite = Color(0xFFEFFFFF)

@Composable
fun AmarAi3DOrbitalStage(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "amar_ai_orbital")
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robot_bob"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "robot_pulse"
    )

    Box(
        modifier = modifier.fillMaxWidth().height(250.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(
            modifier = Modifier.matchParentSize().padding(horizontal = 8.dp)
        ) {
            val cx = size.width / 2f
            val cy = size.height * 0.48f + bob * 5f
            val rx = size.width * 0.34f
            val ry = size.height * 0.20f
            val angle = orbit * (2f * Math.PI.toFloat())

            drawRect(
                brush = Brush.verticalGradient(
                    listOf(StageBg, Color(0xFF07131A), StageBg)
                )
            )
            drawCircle(StageCyan.copy(alpha = 0.06f), size.minDimension * 0.30f, Offset(cx, cy))
            drawCircle(StageGold.copy(alpha = 0.035f), size.minDimension * 0.40f, Offset(cx, cy))

            for (i in 0..2) {
                val scale = 1f + i * 0.14f
                val oval = Rect(
                    cx - rx * scale,
                    cy - ry * scale,
                    cx + rx * scale,
                    cy + ry * scale
                )
                drawOval(
                    brush = Brush.linearGradient(
                        listOf(
                            StageCyan.copy(alpha = 0.10f / scale),
                            StageGold.copy(alpha = 0.04f / scale)
                        )
                    ),
                    topLeft = oval.topLeft,
                    size = oval.size,
                    style = Stroke(width = 1.2f + i * 0.45f)
                )
            }

            val points = listOf(
                Triple(angle, 1f, StageCyan),
                Triple(angle + 2.094f, 0.88f, StageGold),
                Triple(angle + 4.188f, 1.08f, StageCyan)
            )
            points.forEach { (pointAngle, scale, color) ->
                val point = Offset(
                    cx + rx * scale * cos(pointAngle),
                    cy + ry * scale * sin(pointAngle)
                )
                drawCircle(color, 4f + pulse * 2f, point)
                drawCircle(StageWhite.copy(alpha = 0.55f), 1.5f, point)
            }

            val headW = size.width * 0.16f
            val headH = size.height * 0.18f
            val head = Rect(
                cx - headW / 2f,
                cy - size.height * 0.28f,
                cx + headW / 2f,
                cy - size.height * 0.10f
            )
            val headRadius = CornerRadius(headW * 0.18f, headH * 0.18f)
            drawRoundRect(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF183746), Color(0xFF071017))
                ),
                topLeft = head.topLeft,
                size = head.size,
                cornerRadius = headRadius
            )
            drawRoundRect(
                color = StageCyan.copy(alpha = 0.70f),
                topLeft = head.topLeft,
                size = head.size,
                cornerRadius = headRadius,
                style = Stroke(width = 2.2f)
            )
            drawCircle(StageCyan.copy(alpha = 0.28f), headW * 0.48f, head.center)
            drawCircle(StageWhite.copy(alpha = pulse), headW * 0.045f, Offset(head.center.x - headW * 0.18f, head.center.y))
            drawCircle(StageWhite.copy(alpha = pulse), headW * 0.045f, Offset(head.center.x + headW * 0.18f, head.center.y))
            drawLine(
                color = StageGold,
                start = Offset(cx, head.top - headH * 0.16f),
                end = Offset(cx, head.top - headH * 0.42f),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawCircle(StageGold.copy(alpha = pulse), 4f, Offset(cx, head.top - headH * 0.46f))

            val bodyW = size.width * 0.21f
            val bodyH = size.height * 0.22f
            val body = Rect(
                cx - bodyW / 2f,
                cy - size.height * 0.06f,
                cx + bodyW / 2f,
                cy + size.height * 0.16f
            )
            val bodyRadius = CornerRadius(24f, 24f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF173A47), Color(0xFF061016))
                ),
                topLeft = body.topLeft,
                size = body.size,
                cornerRadius = bodyRadius
            )
            drawRoundRect(
                color = StageGold.copy(alpha = 0.58f),
                topLeft = body.topLeft,
                size = body.size,
                cornerRadius = bodyRadius,
                style = Stroke(width = 2f)
            )

            val core = Rect(
                body.left + bodyW * 0.12f,
                body.top + bodyH * 0.16f,
                body.right - bodyW * 0.12f,
                body.bottom - bodyH * 0.16f
            )
            drawRoundRect(
                color = StageCyan.copy(alpha = 0.22f),
                topLeft = core.topLeft,
                size = core.size,
                cornerRadius = CornerRadius(14f, 14f),
                style = Stroke(width = 1.2f)
            )

            val armY = body.center.y - 2f
            drawLine(StageCyan.copy(alpha = 0.8f), Offset(body.left, armY), Offset(body.left - bodyW * 0.28f, armY + 18f), 6f, StrokeCap.Round)
            drawLine(StageCyan.copy(alpha = 0.8f), Offset(body.right, armY), Offset(body.right + bodyW * 0.28f, armY + 18f), 6f, StrokeCap.Round)
            drawCircle(StageGold, 5f, Offset(body.left - bodyW * 0.28f, armY + 18f))
            drawCircle(StageGold, 5f, Offset(body.right + bodyW * 0.28f, armY + 18f))

            val legY = body.bottom
            drawLine(StageCyan.copy(alpha = 0.65f), Offset(cx - bodyW * 0.23f, legY), Offset(cx - bodyW * 0.23f, legY + 22f), 7f, StrokeCap.Round)
            drawLine(StageCyan.copy(alpha = 0.65f), Offset(cx + bodyW * 0.23f, legY), Offset(cx + bodyW * 0.23f, legY + 22f), 7f, StrokeCap.Round)
        }

        Text(
            text = "AMAR AI  •  المشرف الذكي",
            color = StageCyan.copy(alpha = 0.82f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
        )
    }
}
