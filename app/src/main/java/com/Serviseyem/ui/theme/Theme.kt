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
    fontFamily: androidx.compose.ui.text.font.FontFamily = androidx.compose.ui.text.font.FontFamily.Default,
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

    val customTypography = androidx.compose.material3.Typography(
        bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        bodyMedium = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        bodySmall = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        titleLarge = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        titleMedium = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        titleSmall = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        labelLarge = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        labelMedium = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily),
        labelSmall = androidx.compose.ui.text.TextStyle(fontFamily = fontFamily)
    )

    MaterialTheme(
        colorScheme = dynamicColorScheme,
        typography = customTypography,
        content = content
    )
}
