package com.Serviseyem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Composable
fun ServisEmTheme(
    primaryColor: Color = Color(0xFF1B5E20),
    secondaryColor: Color = Color(0xFFFFC700),
    backgroundColor: Color = Color(0xFFFFFFFF),
    textColor: Color = Color(0xFF000000),
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = lightColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        background = backgroundColor,
        surface = if (backgroundColor == Color.White || backgroundColor == Color(0xFFFFFFFF)) Color(0xFFF5F5F7) else backgroundColor,
        onPrimary = if (primaryColor == Color(0xFF1B5E20) || primaryColor == Color(0xFF000000)) Color.White else Color.Black,
        onSecondary = Color.Black,
        onBackground = textColor,
        onSurface = textColor,
        surfaceVariant = if (backgroundColor == Color.White || backgroundColor == Color(0xFFFFFFFF)) Color(0xFFEFEFEF) else backgroundColor,
        onSurfaceVariant = textColor
    )
    MaterialTheme(
        colorScheme = dynamicColorScheme,
        content = content
    )
}
