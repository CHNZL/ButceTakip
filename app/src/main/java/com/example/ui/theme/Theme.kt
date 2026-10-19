package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// --- 1. ROYAL EMERALD COLOR SCHEMES (Flagship New Theme) ---
private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldMintPrimary,
    onPrimary = Color(0xFF021B13),
    primaryContainer = Color(0xFF0F3E31),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = EmeraldChampagneGold,
    onSecondary = Color(0xFF451A03),
    secondaryContainer = Color(0xFF13362B),
    onSecondaryContainer = Color(0xFFFDE68A),
    background = EmeraldDarkBackground,
    onBackground = Color(0xFFECFDF5),
    surface = EmeraldDarkSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = EmeraldDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldNoblePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = EmeraldChampagneGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = EmeraldLightBackground,
    onBackground = Color(0xFF064E3B),
    surface = EmeraldLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = EmeraldLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed
)

// --- 2. CLASSIC SAPPHIRE & GOLD (Original Theme) ---
private val SapphireDarkColorScheme = darkColorScheme(
    primary = PremiumGold,
    onPrimary = PremiumDarkBackground,
    primaryContainer = PremiumDarkSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = PremiumDarkGold,
    onSecondary = PremiumDarkBackground,
    secondaryContainer = PremiumDarkSurfaceVariant,
    onSecondaryContainer = PremiumGold,
    background = PremiumDarkBackground,
    onBackground = Color.White,
    surface = PremiumDarkSurface,
    onSurface = Color.White,
    surfaceVariant = PremiumDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

private val SapphireLightColorScheme = lightColorScheme(
    primary = PremiumDeepBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0),
    onPrimaryContainer = PremiumDeepBlue,
    secondary = PremiumGold,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF78350F),
    background = PremiumLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = PremiumLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed
)

// --- 3. OBSIDIAN & ROSE GOLD ---
private val ObsidianDarkColorScheme = darkColorScheme(
    primary = RoseGoldPrimary,
    onPrimary = Color(0xFF4C0519),
    primaryContainer = Color(0xFF3B1522),
    onPrimaryContainer = Color(0xFFFECDD3),
    secondary = RoseGoldSecondary,
    onSecondary = Color(0xFF500724),
    secondaryContainer = Color(0xFF242031),
    onSecondaryContainer = Color(0xFFFDA4AF),
    background = ObsidianDarkBackground,
    onBackground = Color(0xFFFFF1F2),
    surface = ObsidianDarkSurface,
    onSurface = Color(0xFFFFF1F2),
    surfaceVariant = ObsidianDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = ErrorRed
)

private val ObsidianLightColorScheme = lightColorScheme(
    primary = RoseNoblePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF881337),
    secondary = RoseGoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF1F2),
    onSecondaryContainer = Color(0xFF9F1239),
    background = RoseLightBackground,
    onBackground = Color(0xFF4C0519),
    surface = RoseLightSurface,
    onSurface = Color(0xFF18181B),
    surfaceVariant = RoseLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF52525B),
    error = ErrorRed
)

// --- 4. TITANIUM & ARCTIC CYAN ---
private val TitaniumDarkColorScheme = darkColorScheme(
    primary = ArcticCyanPrimary,
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF164E63),
    onPrimaryContainer = Color(0xFFCFFAFE),
    secondary = ArcticSkySecondary,
    onSecondary = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF1B283E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    background = TitaniumDarkBackground,
    onBackground = Color(0xFFF0F9FF),
    surface = TitaniumDarkSurface,
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = TitaniumDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    error = ErrorRed
)

private val TitaniumLightColorScheme = lightColorScheme(
    primary = ArcticDeepPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBAE6FD),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = ArcticCyanPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF075985),
    background = ArcticLightBackground,
    onBackground = Color(0xFF0C4A6E),
    surface = ArcticLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = ArcticLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed
)

// --- 5. ROYAL AMETHYST & VELVET LILAC ---
private val AmethystDarkColorScheme = darkColorScheme(
    primary = AmethystLilacPrimary,
    onPrimary = Color(0xFF1E0A3C),
    primaryContainer = Color(0xFF3B0764),
    onPrimaryContainer = Color(0xFFF3E8FF),
    secondary = AmethystVioletSecondary,
    onSecondary = Color(0xFF1E0A3C),
    secondaryContainer = Color(0xFF261D45),
    onSecondaryContainer = Color(0xFFE9D5FF),
    background = AmethystDarkBackground,
    onBackground = Color(0xFFFAF5FF),
    surface = AmethystDarkSurface,
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = AmethystDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = ErrorRed
)

private val AmethystLightColorScheme = lightColorScheme(
    primary = AmethystNoblePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF),
    onPrimaryContainer = Color(0xFF581C87),
    secondary = AmethystLilacPrimary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFAF5FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    background = AmethystLightBackground,
    onBackground = Color(0xFF3B0764),
    surface = AmethystLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = AmethystLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    preset: AppThemePreset = AppThemePreset.EMERALD,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (preset) {
            AppThemePreset.EMERALD -> if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
            AppThemePreset.SAPPHIRE -> if (darkTheme) SapphireDarkColorScheme else SapphireLightColorScheme
            AppThemePreset.OBSIDIAN_ROSE -> if (darkTheme) ObsidianDarkColorScheme else ObsidianLightColorScheme
            AppThemePreset.TITANIUM_CYAN -> if (darkTheme) TitaniumDarkColorScheme else TitaniumLightColorScheme
            AppThemePreset.AMETHYST -> if (darkTheme) AmethystDarkColorScheme else AmethystLightColorScheme
        }
    }

    val appThemeColors = when (preset) {
        AppThemePreset.EMERALD -> if (darkTheme) {
            AppThemeColors(
                preset = preset,
                isDark = true,
                heroGradient = listOf(Color(0xFF032219), Color(0xFF064E3B), Color(0xFF0D5E47)),
                navBarContainer = Color(0xFF031610),
                navBarIndicator = Color(0xFF10B981),
                navBarSelectedText = Color(0xFF021B13),
                navBarUnselected = Color(0xFF64748B),
                accentColor = Color(0xFF10B981),
                cardBorder = Color(0xFF13362B).copy(alpha = 0.8f),
                surfaceHighlight = Color(0xFF0F3E31)
            )
        } else {
            AppThemeColors(
                preset = preset,
                isDark = false,
                heroGradient = listOf(Color(0xFF064E3B), Color(0xFF059669)),
                navBarContainer = Color(0xFFFFFFFF),
                navBarIndicator = Color(0xFFD1FAE5),
                navBarSelectedText = Color(0xFF065F46),
                navBarUnselected = Color(0xFF64748B),
                accentColor = Color(0xFF059669),
                cardBorder = Color(0xFFE2E8F0),
                surfaceHighlight = Color(0xFFD1FAE5)
            )
        }
        AppThemePreset.SAPPHIRE -> if (darkTheme) {
            AppThemeColors(
                preset = preset,
                isDark = true,
                heroGradient = listOf(Color(0xFF061226), Color(0xFF0F2B5B)),
                navBarContainer = Color(0xFF061226),
                navBarIndicator = Color(0xFFD4AF37),
                navBarSelectedText = Color(0xFF061226),
                navBarUnselected = Color(0xFF94A3B8),
                accentColor = Color(0xFFD4AF37),
                cardBorder = Color(0xFF1E293B).copy(alpha = 0.8f),
                surfaceHighlight = Color(0xFF1E293B)
            )
        } else {
            AppThemeColors(
                preset = preset,
                isDark = false,
                heroGradient = listOf(Color(0xFF0B2349), Color(0xFF1E3A8A)),
                navBarContainer = Color(0xFF0B2349),
                navBarIndicator = Color(0xFFD4AF37),
                navBarSelectedText = Color(0xFF0B2349),
                navBarUnselected = Color(0xFF94A3B8),
                accentColor = Color(0xFFD4AF37),
                cardBorder = Color(0xFFE2E8F0),
                surfaceHighlight = Color(0xFFE2E8F0)
            )
        }
        AppThemePreset.OBSIDIAN_ROSE -> if (darkTheme) {
            AppThemeColors(
                preset = preset,
                isDark = true,
                heroGradient = listOf(Color(0xFF15101F), Color(0xFF381525), Color(0xFF4C0519)),
                navBarContainer = Color(0xFF0C0B11),
                navBarIndicator = Color(0xFFFB7185),
                navBarSelectedText = Color(0xFF4C0519),
                navBarUnselected = Color(0xFF71717A),
                accentColor = Color(0xFFFB7185),
                cardBorder = Color(0xFF242031).copy(alpha = 0.8f),
                surfaceHighlight = Color(0xFF3B1522)
            )
        } else {
            AppThemeColors(
                preset = preset,
                isDark = false,
                heroGradient = listOf(Color(0xFF881337), Color(0xFFBE123C)),
                navBarContainer = Color(0xFFFFFFFF),
                navBarIndicator = Color(0xFFFFE4E6),
                navBarSelectedText = Color(0xFF881337),
                navBarUnselected = Color(0xFF71717A),
                accentColor = Color(0xFFBE123C),
                cardBorder = Color(0xFFFCE7E9),
                surfaceHighlight = Color(0xFFFFE4E6)
            )
        }
        AppThemePreset.TITANIUM_CYAN -> if (darkTheme) {
            AppThemeColors(
                preset = preset,
                isDark = true,
                heroGradient = listOf(Color(0xFF0A1526), Color(0xFF113254), Color(0xFF083344)),
                navBarContainer = Color(0xFF070B12),
                navBarIndicator = Color(0xFF06B6D4),
                navBarSelectedText = Color(0xFF082F49),
                navBarUnselected = Color(0xFF64748B),
                accentColor = Color(0xFF06B6D4),
                cardBorder = Color(0xFF1B283E).copy(alpha = 0.8f),
                surfaceHighlight = Color(0xFF164E63)
            )
        } else {
            AppThemeColors(
                preset = preset,
                isDark = false,
                heroGradient = listOf(Color(0xFF0369A1), Color(0xFF0284C7)),
                navBarContainer = Color(0xFFFFFFFF),
                navBarIndicator = Color(0xFFBAE6FD),
                navBarSelectedText = Color(0xFF0369A1),
                navBarUnselected = Color(0xFF64748B),
                accentColor = Color(0xFF0284C7),
                cardBorder = Color(0xFFE0F2FE),
                surfaceHighlight = Color(0xFFBAE6FD)
            )
        }
        AppThemePreset.AMETHYST -> if (darkTheme) {
            AppThemeColors(
                preset = preset,
                isDark = true,
                heroGradient = listOf(Color(0xFF1A0B2E), Color(0xFF4C1D95), Color(0xFF6B21A8)),
                navBarContainer = Color(0xFF0D0818),
                navBarIndicator = Color(0xFFA855F7),
                navBarSelectedText = Color(0xFF2E1065),
                navBarUnselected = Color(0xFF94A3B8),
                accentColor = Color(0xFFA855F7),
                cardBorder = Color(0xFF261D45).copy(alpha = 0.8f),
                surfaceHighlight = Color(0xFF3B0764)
            )
        } else {
            AppThemeColors(
                preset = preset,
                isDark = false,
                heroGradient = listOf(Color(0xFF581C87), Color(0xFF7E22CE)),
                navBarContainer = Color(0xFFFFFFFF),
                navBarIndicator = Color(0xFFF3E8FF),
                navBarSelectedText = Color(0xFF581C87),
                navBarUnselected = Color(0xFF6B7280),
                accentColor = Color(0xFF7E22CE),
                cardBorder = Color(0xFFF3E8FF),
                surfaceHighlight = Color(0xFFF3E8FF)
            )
        }
    }

    CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
