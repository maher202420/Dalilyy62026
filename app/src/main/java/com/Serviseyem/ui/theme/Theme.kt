package com.Serviseyem.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),      // Gold Accent
    secondary = Color(0xFF064E3B),    // Luxury Green
    tertiary = Color(0xFF10B981),
    background = Color(0xFF0F172A),  // Navy slate
    surface = Color(0xFF1E293B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF334155)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFC5A028),
    secondary = Color(0xFF064E3B),
    tertiary = Color(0xFF059669),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE2E8F0)
)

@Composable
fun ServiseyemTheme(
    darkTheme: Boolean = true, // We default to dark luxury theme
    primaryColorHex: String = "#D4AF37",
    canvasColorHex: String = "#0F172A",
    content: @Composable () -> Unit
) {
    val primaryColor = try {
        Color(android.graphics.Color.parseColor(primaryColorHex))
    } catch (e: Exception) {
        Color(0xFFD4AF37)
    }

    val canvasColor = try {
        Color(android.graphics.Color.parseColor(canvasColorHex))
    } catch (e: Exception) {
        Color(0xFF0F172A)
    }

    val customColors = DarkColorScheme.copy(
        primary = primaryColor,
        background = canvasColor,
        surface = try {
            Color(android.graphics.Color.parseColor(canvasColorHex)).copy(alpha = 0.9f)
        } catch(e: Exception) {
            Color(0xFF1E293B)
        }
    )

    MaterialTheme(
        colorScheme = customColors,
        content = content
    )
}
