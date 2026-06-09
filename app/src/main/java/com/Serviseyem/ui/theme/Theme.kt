package com.Serviseyem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF161616),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun ServisEmTheme(
    primaryColor: Color = Color(0xFFFFD700),
    fontFamily: FontFamily = FontFamily.Default,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = DarkColorScheme.copy(
        primary = primaryColor
    )
    MaterialTheme(
        colorScheme = dynamicColorScheme,
        content = content
    )
}
