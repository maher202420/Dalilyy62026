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

    // Define the 3 dynamic palettes
    var primaryColor = Color(0xFFD4AF37)
    var secondaryColor = Color(0xFF064E3B)
    var bgColor = Color(0xFF042F2E)
    var surfaceColor = Color(0xFF0B3F37)
    var surfaceVariantColor = Color(0xFF0D4F46)

    when (settings.themeName) {
        "cosmic" -> {
            primaryColor = Color(0xFF94A3B8) // Cosmic Silver
            secondaryColor = Color(0xFF475569) // Dark Slate
            bgColor = Color(0xFF0F172A) // Slate Deep Dark
            surfaceColor = Color(0xFF1E293B) // Slate Card Background
            surfaceVariantColor = Color(0xFF334155) // Slate accent info
        }
        "charcoal_gold" -> {
            primaryColor = Color(0xFFD4AF37) // Luxury Gold
            secondaryColor = Color(0xFF262626) // Deep Charcoal Accent
            bgColor = Color(0xFF111111) // Absolute Dark Charcoal
            surfaceColor = Color(0xFF262626) // Card back
            surfaceVariantColor = Color(0xFF1A1A1A) // Variant
        }
        "royal_emerald" -> {
            primaryColor = Color(0xFF10B981) // Emerald Mint
            secondaryColor = Color(0xFF064E3B) // Royal Deep Sage
            bgColor = Color(0xFF022C22) // Meadow Deep Dark
            surfaceColor = Color(0xFF065F46) // Emerald Card Background
            surfaceVariantColor = Color(0xFF047857) // Accent
        }
        else -> {
            // Default Emerald Gold
            primaryColor = parseHexColor(settings.primaryColor, Color(0xFFD4AF37))
            secondaryColor = parseHexColor(settings.secondaryColor, Color(0xFF064E3B))
            bgColor = parseHexColor(settings.baseCanvasColor, Color(0xFF042F2E))
            surfaceColor = Color(0xFF0D4F46)
            surfaceVariantColor = Color(0xFF0B3F37)
        }
    }

    // Rich luxury dark color scheme
    val colorScheme = darkColorScheme(
        primary = primaryColor,
        secondary = secondaryColor,
        background = bgColor,
        surface = surfaceColor,
        onPrimary = Color.Black,
        onSecondary = Color.White,
        onBackground = getTextColor(settings.textColorOption),
        onSurface = getTextColor(settings.textColorOption),
        primaryContainer = secondaryColor,
        onPrimaryContainer = Color.White,
        surfaceVariant = surfaceVariantColor,
        onSurfaceVariant = Color.White
    )

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

fun getTextColor(textColorOption: String): Color {
    return when (textColorOption) {
        "bright_white" -> Color(0xFFFFFFFF)
        "light_gold" -> Color(0xFFFDE047)
        "vibrant_silver" -> Color(0xFFCBD5E1)
        else -> Color(0xFFFFFFFF)
    }
}
