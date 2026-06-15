package com.Serviseyem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun ServisEmTheme(
    primaryColor: Color = Color(0xFFFFD700),
    secondaryColor: Color = Color(0xFF03DAC6),
    backgroundColor: Color = Color(0xFF0A0A0C),
    textColor: Color = Color(0xFFFFFFFF),
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = darkColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        background = backgroundColor,
        surface = Color(0xFF131316),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = textColor,
        onSurface = textColor,
        surfaceVariant = Color(0xFF1B1B1F),
        onSurfaceVariant = Color.LightGray
    )
    MaterialTheme(
        colorScheme = dynamicColorScheme,
        content = content
    )
}
