package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class LayoutStyle(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val badgeText: String
) {
    MODERN(
        id = "MODERN",
        displayName = "Modern Akıcı Tasarım (Neo-Fintech)",
        subtitle = "Yeni nesil cüzdan kartı, akıllı nakit akışı çubuğu, hızlı işlem menüsü ve sekmeli piyasa merkezi",
        badgeText = "YENİ TASARIM"
    ),
    CLASSIC(
        id = "CLASSIC",
        displayName = "Klasik Kurumsal Tasarım",
        subtitle = "Geleneksel dikey akış ve orijinal liste görünümü",
        badgeText = "ORİJİNAL"
    );

    companion object {
        fun fromId(id: String?): LayoutStyle {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: MODERN
        }
    }
}

enum class AppThemePreset(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val badgeText: String?,
    val iconColor: Color,
    val previewColors: List<Color>,
    val previewGradient: List<Color>
) {
    EMERALD(
        id = "EMERALD",
        displayName = "Kraliyet Zümrütü",
        subtitle = "Zengin zümrüt yeşili & şampanya altını zarafeti",
        badgeText = "YENİ TEMA",
        iconColor = Color(0xFF10B981),
        previewColors = listOf(Color(0xFF041611), Color(0xFF0A231C), Color(0xFF10B981), Color(0xFFF59E0B)),
        previewGradient = listOf(Color(0xFF032219), Color(0xFF064E3B), Color(0xFF10B981))
    ),
    SAPPHIRE(
        id = "SAPPHIRE",
        displayName = "Klasik Lacivert & Altın",
        subtitle = "Orijinal klasik lacivert ve asil altın tonları",
        badgeText = "ORİJİNAL",
        iconColor = Color(0xFFD4AF37),
        previewColors = listOf(Color(0xFF061226), Color(0xFF0F172A), Color(0xFF0B2349), Color(0xFFD4AF37)),
        previewGradient = listOf(Color(0xFF061226), Color(0xFF0F2B5B), Color(0xFFD4AF37))
    ),
    OBSIDIAN_ROSE(
        id = "OBSIDIAN_ROSE",
        displayName = "Obsidyen & Rose Gold",
        subtitle = "Gece siyahı kadife ve parıltılı rose gold dokunuşu",
        badgeText = "LÜKS",
        iconColor = Color(0xFFFB7185),
        previewColors = listOf(Color(0xFF0E0D13), Color(0xFF171520), Color(0xFFBE123C), Color(0xFFFB7185)),
        previewGradient = listOf(Color(0xFF15101F), Color(0xFF381525), Color(0xFFFB7185))
    ),
    TITANIUM_CYAN(
        id = "TITANIUM_CYAN",
        displayName = "Modern Titanyum & Arktik",
        subtitle = "Yüksek teknoloji titanyum ve elektrik camgöbeği",
        badgeText = "FİNTECH",
        iconColor = Color(0xFF06B6D4),
        previewColors = listOf(Color(0xFF080C14), Color(0xFF0F1726), Color(0xFF0284C7), Color(0xFF06B6D4)),
        previewGradient = listOf(Color(0xFF0A1526), Color(0xFF113254), Color(0xFF06B6D4))
    ),
    AMETHYST(
        id = "AMETHYST",
        displayName = "Kraliyet Ametisti & Kadife Mor",
        subtitle = "Derin lüks mor ve büyüleyici lila ışıltısı",
        badgeText = "PRESTİJ",
        iconColor = Color(0xFFA855F7),
        previewColors = listOf(Color(0xFF0F0B1E), Color(0xFF17122C), Color(0xFF7E22CE), Color(0xFFA855F7)),
        previewGradient = listOf(Color(0xFF1A0B2E), Color(0xFF4C1D95), Color(0xFFA855F7))
    );

    companion object {
        fun fromId(id: String?): AppThemePreset {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: EMERALD
        }
    }
}

data class AppThemeColors(
    val preset: AppThemePreset,
    val isDark: Boolean,
    val heroGradient: List<Color>,
    val navBarContainer: Color,
    val navBarIndicator: Color,
    val navBarSelectedText: Color,
    val navBarUnselected: Color,
    val accentColor: Color,
    val cardBorder: Color,
    val surfaceHighlight: Color
)

val LocalAppThemeColors = staticCompositionLocalOf {
    AppThemeColors(
        preset = AppThemePreset.EMERALD,
        isDark = false,
        heroGradient = listOf(Color(0xFF064E3B), Color(0xFF059669)),
        navBarContainer = Color(0xFF031610),
        navBarIndicator = Color(0xFF10B981),
        navBarSelectedText = Color(0xFF021B13),
        navBarUnselected = Color(0xFF94A3B8),
        accentColor = Color(0xFF10B981),
        cardBorder = Color(0xFFE2E8F0),
        surfaceHighlight = Color(0xFFD1FAE5)
    )
}
