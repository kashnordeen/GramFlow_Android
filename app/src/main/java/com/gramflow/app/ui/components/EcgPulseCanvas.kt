package com.gramflow.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gramflow.app.ui.theme.AccentLime

@Composable
fun EcgPulseCanvas(
    modifier: Modifier = Modifier
        .width(80.dp)
        .height(28.dp),
    color: Color = AccentLime
) {
    val transition = rememberInfiniteTransition(label = "ecg")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecgPhase"
    )

    val cachedPath = remember { Path() }
    val dashIntervals = remember { floatArrayOf(200f, 200f) }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val midY = h / 2

        cachedPath.reset()
        cachedPath.moveTo(0f, midY)
        cachedPath.lineTo(w * 0.25f, midY)
        cachedPath.lineTo(w * 0.32f, h * 0.15f)
        cachedPath.lineTo(w * 0.42f, h * 0.85f)
        cachedPath.lineTo(w * 0.52f, h * 0.30f)
        cachedPath.lineTo(w * 0.60f, h * 0.65f)
        cachedPath.lineTo(w * 0.68f, midY)
        cachedPath.lineTo(w, midY)

        drawPath(
            path = cachedPath,
            color = color,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(dashIntervals, phase)
            )
        )
    }
}
