package com.personal.gridbot.amaros.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** مستوى الحركة العام، قابل للتبديل دون تغيير منطق التطبيق. */
enum class AmarMotionLevel(val durationMs: Int) {
    OFF(0),
    LOW(180),
    MEDIUM(280),
    HIGH(420),
    CINEMATIC(650)
}

@Composable
fun Modifier.amarEntrance(
    visible: Boolean,
    level: AmarMotionLevel = AmarMotionLevel.MEDIUM
): Modifier {
    val target = if (visible) 1f else 0f
    val alpha = animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = level.durationMs.coerceAtLeast(1),
            easing = FastOutSlowInEasing
        ),
        label = "amarEntrance"
    )
    return graphicsLayer { this.alpha = if (level == AmarMotionLevel.OFF) target else alpha.value }
}
