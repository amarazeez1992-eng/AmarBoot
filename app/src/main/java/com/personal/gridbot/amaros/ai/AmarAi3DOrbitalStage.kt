package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val RobotCyan = Color(0xFF1DE5FF)
private val RobotGold = Color(0xFFFFC84A)
private val RobotDark = Color(0xFF071019)
private val RobotSteel = Color(0xFF5C7482)

@Composable
fun AmarAi3DOrbitalStage(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "amar_ai_orbit")
    val t by infinite.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "orbit")
    val floatY by infinite.animateFloat(-4f, 5f, infiniteRepeatable(tween(2300, easing = FastOutSlowInEasing), RepeatMode.Reverse)), label = "robot_float")
    val pulse by infinite.animateFloat(0.55f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse)), label = "robot_pulse")

    Box(modifier.fillMaxWidth().height(178.dp)) {
        Canvas(Modifier.fillMaxSize().alpha(0.9f)) {
            val center = Offset(size.width * 0.5f, size.height * 0.55f)
            val radiusX = size.width * 0.37f
            val radiusY = size.height * 0.31f
            drawOval(
                brush = Brush.radialGradient(listOf(RobotCyan.copy(alpha = 0.13f), Color.Transparent), radius = size.width * 0.45f),
                topLeft = Offset(center.x - size.width * 0.45f, center.y - size.height * 0.45f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.9f, size.height * 0.9f)
            )
            for (ring in 0..2) {
                drawOval(
                    color = if (ring == 1) RobotGold.copy(alpha = 0.28f) else RobotCyan.copy(alpha = 0.18f),
                    topLeft = Offset(center.x - radiusX * (1f + ring * 0.08f), center.y - radiusY * (1f - ring * 0.08f)),
                    size = androidx.compose.ui.geometry.Size(radiusX * 2f * (1f + ring * 0.08f), radiusY * 2f * (1f - ring * 0.08f)),
                    style = Stroke(width = 1.2f)
                )
            }
            val phase = t * Math.PI.toFloat() * 2f
            val news = listOf("ذهب", "أخبار", "سيولة", "فوركس", "أسواق")
            news.forEachIndexed { i, _ ->
                val a = phase + i * (Math.PI.toFloat() * 2f / news.size)
                val p = Offset(center.x + cos(a) * radiusX, center.y + sin(a) * radiusY)
                drawCircle(RobotGold.copy(alpha = 0.7f), radius = 3.5f, center = p)
            }
            val robotCenter = Offset(center.x, center.y + floatY)
            drawCircle(RobotCyan.copy(alpha = 0.13f * pulse), radius = 47f, center = robotCenter)
            drawOval(RobotDark, Offset(robotCenter.x - 28f, robotCenter.y - 30f), androidx.compose.ui.geometry.Size(56f, 65f))
            drawOval(Brush.linearGradient(listOf(Color(0xFF9FB6C2), RobotSteel, Color(0xFF243640))), Offset(robotCenter.x - 25f, robotCenter.y - 28f), androidx.compose.ui.geometry.Size(50f, 57f))
            drawRoundRect(RobotDark, Offset(robotCenter.x - 21f, robotCenter.y - 16f), androidx.compose.ui.geometry.Size(42f, 27f), 12f, 12f)
            drawCircle(RobotCyan.copy(alpha = pulse), 3.2f, Offset(robotCenter.x - 9f, robotCenter.y - 3f))
            drawCircle(RobotCyan.copy(alpha = pulse), 3.2f, Offset(robotCenter.x + 9f, robotCenter.y - 3f))
            drawLine(RobotGold.copy(alpha = pulse), Offset(robotCenter.x - 8f, robotCenter.y + 6f), Offset(robotCenter.x + 8f, robotCenter.y + 6f), 2f, StrokeCap.Round)
            drawLine(RobotGold, Offset(robotCenter.x, robotCenter.y - 28f), Offset(robotCenter.x, robotCenter.y - 40f), 2f)
            drawCircle(RobotCyan.copy(alpha = pulse), 4f, Offset(robotCenter.x, robotCenter.y - 42f))
            drawLine(RobotSteel, Offset(robotCenter.x - 25f, robotCenter.y + 18f), Offset(robotCenter.x - 39f, robotCenter.y + 31f), 5f, StrokeCap.Round)
            drawLine(RobotSteel, Offset(robotCenter.x + 25f, robotCenter.y + 18f), Offset(robotCenter.x + 39f, robotCenter.y + 31f), 5f, StrokeCap.Round)
            val trail = Path().apply {
                moveTo(robotCenter.x - 45f, robotCenter.y + 43f)
                cubicTo(robotCenter.x - 15f, robotCenter.y + 52f, robotCenter.x + 15f, robotCenter.y + 52f, robotCenter.x + 45f, robotCenter.y + 43f)
            }
            drawPath(trail, RobotCyan.copy(alpha = 0.35f), style = Stroke(2f))
        }
        Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf("الذهب", "الأخبار", "السيولة", "الفوركس").forEachIndexed { i, label ->
                val a = ((t * 360f + i * 90f) % 360f) / 360f
                Text(label, color = if (i % 2 == 0) RobotGold else RobotCyan, fontSize = 8.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(0.45f + a * 0.5f).clip(RoundedCornerShape(20.dp)).background(RobotDark.copy(alpha = 0.7f)).padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        Text("AMAR • يتحرك ويحلل ويراقب", color = RobotCyan, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp))
    }
}
