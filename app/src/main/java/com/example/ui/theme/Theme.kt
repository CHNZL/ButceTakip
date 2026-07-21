package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PremiumGold,
    onPrimary = PremiumDarkBackground,
    primaryContainer = PremiumDarkSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = PremiumDarkGold,
    onSecondary = PremiumDarkBackground,
    background = PremiumDarkBackground,
    onBackground = Color.White,
    surface = PremiumDarkSurface,
    onSurface = Color.White,
    surfaceVariant = PremiumDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = PremiumDeepBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = PremiumDeepBlue,
    secondary = PremiumGold,
    onSecondary = Color.White,
    background = PremiumLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = PremiumLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to enforce premium theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
