package com.gramflow.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.gramflow.app.ui.theme.TextPrimary

@Composable
fun PrivacyMaskText(
    text: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(fontSize = 18.sp, color = TextPrimary),
    color: Color = TextPrimary
) {
    if (isVisible) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color
        )
    } else {
        Text(
            text = "••••",
            modifier = modifier,
            style = style,
            color = color
        )
    }
}
