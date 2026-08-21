package com.gramflow.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gramflow.app.ui.theme.AccentLime

@Composable
fun IsometricCubeCanvas(
    modifier: Modifier = Modifier
        .width(68.dp)
        .height(52.dp)
) {
    Canvas(modifier = modifier) {
        // Bottom Base Cube
        val baseTop = Path().apply {
            moveTo(34f, 34f)
            lineTo(56f, 22f)
            lineTo(34f, 10f)
            lineTo(12f, 22f)
            close()
        }
        drawPath(baseTop, color = Color(0xFF1A1D1C))
        drawPath(baseTop, color = AccentLime.copy(alpha = 0.3f), style = Stroke(1.2f))

        val baseLeft = Path().apply {
            moveTo(12f, 22f)
            lineTo(34f, 34f)
            lineTo(34f, 48f)
            lineTo(12f, 36f)
            close()
        }
        drawPath(baseLeft, color = Color(0xFF141716))
        drawPath(baseLeft, color = AccentLime.copy(alpha = 0.3f), style = Stroke(1.2f))

        val baseRight = Path().apply {
            moveTo(34f, 34f)
            lineTo(56f, 22f)
            lineTo(56f, 36f)
            lineTo(34f, 48f)
            close()
        }
        drawPath(baseRight, color = Color(0xFF181B1A))
        drawPath(baseRight, color = AccentLime.copy(alpha = 0.3f), style = Stroke(1.2f))

        // Top Glowing Cube
        val topTop = Path().apply {
            moveTo(34f, 22f)
            lineTo(50f, 13f)
            lineTo(34f, 4f)
            lineTo(18f, 13f)
            close()
        }
        drawPath(topTop, color = AccentLime.copy(alpha = 0.15f))
        drawPath(topTop, color = AccentLime, style = Stroke(1.5f))

        val topLeft = Path().apply {
            moveTo(18f, 13f)
            lineTo(34f, 22f)
            lineTo(34f, 32f)
            lineTo(18f, 23f)
            close()
        }
        drawPath(topLeft, color = AccentLime.copy(alpha = 0.25f))
        drawPath(topLeft, color = AccentLime, style = Stroke(1.5f))

        val topRight = Path().apply {
            moveTo(34f, 22f)
            lineTo(50f, 13f)
            lineTo(50f, 23f)
            lineTo(34f, 32f)
            close()
        }
        drawPath(topRight, color = AccentLime.copy(alpha = 0.35f))
        drawPath(topRight, color = AccentLime, style = Stroke(1.5f))
    }
}
