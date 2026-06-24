package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = CyberBlack,
    primaryContainer = CyberLightSurface,
    onPrimaryContainer = NeonGreen,
    secondary = CyberCyan,
    onSecondary = CyberBlack,
    secondaryContainer = CyberMediumSurface,
    onSecondaryContainer = CyberCyan,
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = CyberBlack,
    onBackground = TextLight,
    surface = CyberDarkSurface,
    onSurface = TextLight,
    surfaceVariant = CyberMediumSurface,
    onSurfaceVariant = TextMuted,
    outline = CyberLightSurface,
    error = NeonPink,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for cybersecurity vibe
    dynamicColor: Boolean = false, // Disable dynamic colors to keep neon cybersecurity styling
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
