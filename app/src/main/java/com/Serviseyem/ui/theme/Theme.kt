package com.Serviseyem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700), // Yemeni Amber Gold
    onPrimary = Color.Black,
    secondary = Color(0xFFFFC107),
    onSecondary = Color.Black,
    tertiary = Color(0xFF38BDF8), // Electric Safe Sky-Blue
    background = Color(0xFF0F0F12), // Premium Pitch Obsidian
    surface = Color(0xFF161619), // Elevated obsidian card
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFFD700),
    onPrimary = Color.Black,
    secondary = Color(0xFFFFC107),
    onSecondary = Color.Black,
    tertiary = Color(0xFF38BDF8),
    background = Color(0xFFF9F9FB),
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun ServiseyemTheme(
    darkTheme: Boolean = true, // Force Dark-Mode for premium Yemeni local look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
