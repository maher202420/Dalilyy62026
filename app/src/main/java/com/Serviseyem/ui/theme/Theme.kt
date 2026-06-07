package com.Serviseyem.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkSlateBackground = Color(0xFF0C0D0E)
val CardBackgroundLow = Color(0xFF15171B)
val GoldAccentColor = Color(0xFFFFD700)

@Composable
fun ServisEmTheme(
    primaryColor: Color = GoldAccentColor,
    content: @Composable () -> Unit
) {
    val dynamicColorScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        background = DarkSlateBackground,
        onBackground = Color.White,
        surface = CardBackgroundLow,
        onSurface = Color.White,
        onSecondary = Color.LightGray
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        content = content
    )
}
