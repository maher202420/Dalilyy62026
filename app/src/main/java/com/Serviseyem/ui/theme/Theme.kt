package com.Serviseyem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.Serviseyem.services.FirebaseService

val GoldLuxury = Color(0xFFD4AF37)
val EmeraldDark = Color(0xFF042F2E)
val EmeraldMedium = Color(0xFF064E3B)
val EmeraldLight = Color(0xFF0D4F46)
val CosmicSilver = Color(0xFFE5E5EA)

fun parseHexColor(hexString: String, defaultColor: Color): Color {
    return try {
        val cleanHex = hexString.replace("#", "").trim()
        if (cleanHex.length == 6) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else if (cleanHex.length == 8) {
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } else {
            defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun WAMServicesTheme(
    content: @Composable () -> Unit
) {
    val settingsState = FirebaseService.settings.collectAsState()
    val settings = settingsState.value

    val primaryDynamic = parseHexColor(settings.primaryColor, GoldLuxury)
    val secondaryDynamic = parseHexColor(settings.secondaryColor, EmeraldMedium)
    val bgDynamic = parseHexColor(settings.baseCanvasColor, EmeraldDark)

    // Rich luxury dark color scheme
    val colorScheme = darkColorScheme(
        primary = primaryDynamic,
        secondary = secondaryDynamic,
        tertiary = CosmicSilver,
        background = bgDynamic,
        surface = EmeraldLight,
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onTertiary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        primaryContainer = secondaryDynamic,
        onPrimaryContainer = Color.White,
        surfaceVariant = Color(0xFF0B3F37),
        onSurfaceVariant = Color(0xFFE5E5EA)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
