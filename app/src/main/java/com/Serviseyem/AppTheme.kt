package com.Serviseyem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// ============================================================
// 🎨 الألوان الجديدة المضافة
// ============================================================
object AppTheme {
    var darkBg by mutableStateOf(Color(0xFF0D1B1E))
    var primaryRed by mutableStateOf(Color(0xFFCE1126))
    var accentGold by mutableStateOf(Color(0xFFFFD700))
    var surfaceDark by mutableStateOf(Color(0xFF162A2D))
    val textLight = Color(0xFFF5F5F5)
    val grayText = Color(0xFFA0B2B5)
    val lightGreen = Color(0xFF4CAF50)
    
    // الألوان الجديدة المضافة
    val cosmicSilver = Color(0xFF9E9E9E)
    val luxuryGold = Color(0xFFD4AF37)
    val emeraldGreen = Color(0xFF004B49)
    val smokyBlack = Color(0xFF121212)
    val softPink = Color(0xFFFFB6C1)
    val goldenWhite = Color(0xFFFAF6EB)
}
