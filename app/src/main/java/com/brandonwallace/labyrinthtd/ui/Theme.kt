package com.brandonwallace.labyrinthtd.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LabyrinthColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),      // Imperial Gold
    secondary = Color(0xFF5A9E4B),    // Meadow Green
    tertiary = Color(0xFFC75B39),     // Terracotta / Siege Red
    background = Color(0xFF1E1610),   // Dark Oak Timber
    surface = Color(0xFF2B2017),      // Seasoned Wood Panel
    onPrimary = Color(0xFF1A120B),
    onSecondary = Color(0xFFF5EBD6),
    onBackground = Color(0xFFF5EBD6), // Warm Parchment
    onSurface = Color(0xFFF5EBD6)
)

@Composable
fun LabyrinthTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LabyrinthColorScheme,
        content = content
    )
}
