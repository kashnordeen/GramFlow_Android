package com.gramflow.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gramflow.app.ui.theme.AccentLime
import com.gramflow.app.ui.theme.BgDark

@Composable
fun MetricLineChartCanvas(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokePath = Path().apply {
            moveTo(0f, h * 0.85f)
            quadraticBezierTo(w * 0.2f, h * 0.70f, w * 0.4f, h * 0.55f)
            quadraticBezierTo(w * 0.7f, h * 0.35f, w * 0.95f, h * 0.15f)
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(w * 0.95f, h)
            lineTo(0f, h)
            close()
        }

        // Gradient Area Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AccentLime.copy(alpha = 0.35f),
                    AccentLime.copy(alpha = 0.0f)
                )
            )
        )

        // Line Stroke
        drawPath(
            path = strokePath,
            color = AccentLime,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // End Point Indicator
        drawCircle(
            color = BgDark,
            radius = 5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.15f)
        )
        drawCircle(
            color = AccentLime,
            radius = 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.15f)
        )
    }
}
