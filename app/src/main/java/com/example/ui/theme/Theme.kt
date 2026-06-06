package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.example.services.FirebaseService

// Premium color hex definitions
val GoldLuxury = Color(0xFFFFD700)
val GoldDark = Color(0xFFC5A059)
val EmeraldGreen = Color(0xFF1B4D3E)
val LightEmerald = Color(0xFF1F6B58)
val CosmicSilver = Color(0xFFBCC6CC)
val SpaceGrey = Color(0xFF1C1C1E)
val LuxuryGoldAccent = Color(0xFFFFDF00)

private val FallbackDarkColorScheme = darkColorScheme(
    primary = GoldLuxury,
    secondary = EmeraldGreen,
    tertiary = CosmicSilver,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFFF5F5F5), // crisp white/grey text
    onSurface = Color(0xFFECECEC),
    primaryContainer = EmeraldGreen,
    onPrimaryContainer = Color.White
)

private val FallbackLightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    secondary = GoldDark,
    tertiary = SpaceGrey,
    background = Color(0xFFFCFCFC),
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF121212), // deep dark text
    onSurface = Color(0xFF1A1A1A),
    primaryContainer = GoldLuxury,
    onPrimaryContainer = Color.Black
)

fun parseHexColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val settingsState = FirebaseService.settings.collectAsState()
    val settings = settingsState.value

    val primaryDynamic = parseHexColor(settings.primaryColor, Color(0xFFD4AF37))
    val secondaryDynamic = parseHexColor(settings.secondaryColor, Color(0xFF064E3B))
    val bgDynamic = parseHexColor(settings.baseCanvasColor, Color(0xFF042F2E))

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryDynamic,
            secondary = secondaryDynamic,
            tertiary = CosmicSilver,
            background = bgDynamic,
            surface = Color(0xFF0B3F37),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onTertiary = Color.Black,
            onBackground = Color(0xFFFFFFFF), // Ensure extremely high contrast solid white on dark
            onSurface = Color(0xFFF2F2F7), // Light secondary text on surface
            primaryContainer = secondaryDynamic,
            onPrimaryContainer = Color.White,
            surfaceVariant = Color(0xFF0D4F46),
            onSurfaceVariant = Color(0xFFE5E5EA)
        )
    } else {
        lightColorScheme(
            primary = primaryDynamic,
            secondary = secondaryDynamic,
            tertiary = SpaceGrey,
            background = bgDynamic,
            surface = Color(0xFFFFFFFF),
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onTertiary = Color.White,
            onBackground = Color(0xFF000000), // Ensure high-contrast pure black text in light mode
            onSurface = Color(0xFF1C1C1E),
            primaryContainer = primaryDynamic,
            onPrimaryContainer = Color.Black,
            surfaceVariant = Color(0xFFF2F2F7),
            onSurfaceVariant = Color(0xFF2C2C2E)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
