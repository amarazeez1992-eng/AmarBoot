package com.personal.gridbot.amaros.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AmarAiPremiumCoreVisual(modifier: Modifier = Modifier) {
    val motion = rememberInfiniteTransition(label = "amar_ai_premium_motion")
    val rotation by motion.animateFloat(0f, 360f, infiniteRepeatable(tween(11000, easing = LinearEasing)), label = "rotation")
    val reverseRotation by motion.animateFloat(360f, 0f, infiniteRepeatable(tween(8000, easing = LinearEasing)), label = "reverse")
    val pulse by motion.animateFloat(0.94f, 1.06f, infiniteRepeatable(tween(1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")
    val shimmer by motion.animateFloat(0f, 1f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "shimmer")

    val black = Color(0xFF07060D)
    val indigo = Color(0xFF3030B8)
    val purple = Color(0xFF7C35FF)
    val pink = Color(0xFFFF2FAE)
    val gold = Color(0xFFFFC857)
    val cyan = Color(0xFF31E7FF)

    Box(
        modifier
            .height(286.dp)
            .background(
                Brush.linearGradient(listOf(black, Color(0xFF17102B), Color(0xFF27103A), black)),
                RoundedCornerShape(36.dp)
            )
            .border(
                1.dp,
                Brush.sweepGradient(listOf(gold, pink, purple, cyan, indigo, gold)),
                RoundedCornerShape(36.dp)
            )
    ) {
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Color.White.copy(alpha = .10f), purple.copy(alpha = .13f), Color.Transparent),
                    radius = 520f
                ), RoundedCornerShape(36.dp)
            )
        )

        Box(
            Modifier.align(Alignment.Center).size(210.dp)
                .graphicsLayer { rotationZ = rotation }
                .border(1.dp, Brush.sweepGradient(listOf(Color.Transparent, gold, pink, Color.Transparent, cyan, Color.Transparent)), CircleShape)
        )
        Box(
            Modifier.align(Alignment.Center).size(178.dp)
                .graphicsLayer { rotationZ = reverseRotation }
                .border(2.dp, Brush.sweepGradient(listOf(Color.Transparent, purple, pink, Color.Transparent, gold)), CircleShape)
        )

        val orbitRadius = 104f
        listOf(
            Triple(cyan, rotation, 0),
            Triple(pink, rotation + 120f, 1),
            Triple(gold, rotation + 240f, 2)
        ).forEach { (color, angle, _) ->
            val radians = Math.toRadians(angle.toDouble())
            Box(
                Modifier.align(Alignment.Center)
                    .offset(x = (cos(radians) * orbitRadius).toFloat().dp, y = (sin(radians) * orbitRadius).toFloat().dp)
                    .size(9.dp)
                    .shadow(12.dp, CircleShape)
                    .background(color, CircleShape)
            )
        }

        Box(
            Modifier.align(Alignment.Center).size(128.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
                .shadow(38.dp, CircleShape)
                .background(
                    Brush.radialGradient(listOf(Color.White, cyan, indigo, purple, pink, black)),
                    CircleShape
                )
                .border(2.dp, gold.copy(alpha = .82f), CircleShape)
        )
        Box(
            Modifier.align(Alignment.Center).size(112.dp)
                .background(Brush.radialGradient(listOf(Color.White.copy(.96f), cyan.copy(.75f), purple.copy(.38f), Color.Transparent)), CircleShape)
        )
        Text(
            "AI",
            Modifier.align(Alignment.Center),
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )

        Box(
            Modifier.fillMaxWidth().height(80.dp).align(Alignment.TopCenter)
                .graphicsLayer { translationX = (shimmer * 520f) - 260f; alpha = .18f }
                .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.White, Color.Transparent)))
        )

        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AMAR AI CORE", color = gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("INTELLIGENT • ADAPTIVE • ADVISORY", color = Color.White.copy(.72f), fontSize = 9.sp)
        }
    }
}
