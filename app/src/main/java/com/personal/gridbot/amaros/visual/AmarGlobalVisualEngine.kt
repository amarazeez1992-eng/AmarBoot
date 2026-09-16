package com.personal.gridbot.amaros.visual

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.personal.gridbot.amaros.broker.AmarBot1CommandRuntimeRegistry
import kotlinx.coroutines.delay

@Composable
fun AmarGlobalVisualStateCollector(context: Context) {
    LaunchedEffect(Unit) {
        AmarGlobalVisualStateStore.setEnabled(AmarVisualEffectsPreference.load(context))
        var previous: AmarTradingVisualState? = null
        while (true) {
            val runtime = AmarBot1CommandRuntimeRegistry.current()
            val remote = runCatching { runtime?.client?.bot1State() }.getOrNull()
            val pnl = remote?.floatingProfitLoss
            val fresh = remote?.available == true && remote.fresh && remote.heartbeatMs != null
            val next = when {
                !fresh || pnl == null || !pnl.isFinite() -> AmarTradingVisualState.NORMAL
                pnl > 0.0 -> AmarTradingVisualState.PROFIT
                pnl < 0.0 -> AmarTradingVisualState.LOSS
                else -> AmarTradingVisualState.NORMAL
            }
            val current = AmarGlobalVisualStateStore.current()
            val nonce = if (previous != null && previous != next) System.nanoTime() else current.eventNonce
            AmarGlobalVisualStateStore.publish(
                current.copy(
                    tradingState = next,
                    floatingProfitLoss = pnl,
                    sourceFresh = fresh,
                    eventNonce = nonce,
                )
            )
            previous = next
            delay(500L)
        }
    }
}

@Composable
fun AmarGlobalVisualAtmosphere(modifier: Modifier = Modifier) {
    val state = AmarGlobalVisualStateStore.current()
    if (!state.enabled) return
    val transition = rememberInfiniteTransition(label = "amar-global-atmosphere")
    val travel by transition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(6500, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "border-travel",
    )
    val pulse by transition.animateFloat(
        0.34f,
        0.88f,
        infiniteRepeatable(tween(1350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "border-pulse",
    )
    val secondaryPulse by transition.animateFloat(
        0.18f,
        0.52f,
        infiniteRepeatable(tween(2100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ambient-pulse",
    )
    val rainbow = Brush.sweepGradient(
        listOf(
            Color(0xFF00E5FF), Color(0xFF1687FF), Color(0xFF6B35FF),
            Color(0xFFB83DFF), Color(0xFF00E5FF),
        )
    )
    val accent = when (state.tradingState) {
        AmarTradingVisualState.PROFIT -> Color(0xFF00FF9C)
        AmarTradingVisualState.LOSS -> Color(0xFFFF365B)
        AmarTradingVisualState.NORMAL -> Color.Transparent
    }
    val accentAlpha = if (state.tradingState == AmarTradingVisualState.NORMAL) 0f else pulse
    Canvas(modifier.fillMaxSize().clip(RoundedCornerShape(18.dp))) {
        val inset = 3.dp.toPx()
        val radius = 18.dp.toPx()
        val rectW = size.width - inset * 2
        val rectH = size.height - inset * 2
        rotate(travel, pivot = center) {
            drawRoundRect(
                brush = rainbow,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(rectW, rectH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round),
                alpha = 0.72f,
            )
        }
        rotate(-travel * 0.42f, pivot = center) {
            drawRoundRect(
                brush = rainbow,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(rectW, rectH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                style = Stroke(width = 0.8.dp.toPx()),
                alpha = secondaryPulse,
            )
        }
        if (accentAlpha > 0f) {
            drawRoundRect(
                color = accent,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(rectW, rectH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                style = Stroke(width = 5.dp.toPx()),
                alpha = accentAlpha * 0.22f,
            )
            drawRoundRect(
                color = accent,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(rectW, rectH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                style = Stroke(width = 1.5.dp.toPx()),
                alpha = accentAlpha,
            )
        }
    }
}

@Composable
fun AmarVisualEffectsToggle(context: Context, modifier: Modifier = Modifier) {
    val enabled = AmarGlobalVisualStateStore.current().enabled
    val color = if (enabled) Color(0xFF00E6A0) else Color(0xFFFF4058)
    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(50))
            .clickable {
                val next = !AmarGlobalVisualStateStore.current().enabled
                AmarVisualEffectsPreference.save(context, next)
                AmarGlobalVisualStateStore.setEnabled(next)
            }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = color, radius = size.minDimension * 0.30f)
            drawCircle(
                color = color,
                radius = size.minDimension * 0.46f,
                alpha = 0.18f,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
