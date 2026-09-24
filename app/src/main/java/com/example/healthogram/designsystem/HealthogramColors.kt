package com.example.healthogram.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Healthogram Master Semantic Color Palette.
 * Designed according to Step 02 Brand Direction:
 * Primary: Healthogram Royal Blue
 * Secondary: Healthogram Light Green
 * Full Light and Dark token matrices.
 */
@Immutable
data class HealthogramColorTokens(
    val primary: Color,
    val primaryLight: Color,
    val primaryDark: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    val secondary: Color,
    val secondaryLight: Color,
    val secondaryDark: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnPrimary: Color,

    val border: Color,
    val borderLight: Color,
    val divider: Color,

    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val info: Color,
    val infoContainer: Color,

    val brandGradient: Brush,
    val storyGradient: Brush,
    val verifiedBadgeColor: Color,
    val isDark: Boolean
) {
    val onSurface: Color get() = textPrimary
    val onSurfaceVariant: Color get() = textSecondary
}

// Brand Raw Color Constants
val RoyalBlueDark = Color(0xFF1E3A8A)       // 900
val RoyalBluePrimary = Color(0xFF2563EB)    // 600 - Main Brand Primary
val RoyalBlueLight = Color(0xFF3B82F6)      // 500
val RoyalBlueContainerLight = Color(0xFFDBEAFE) // 100
val RoyalBlueOnContainerLight = Color(0xFF1E40AF)

val LightGreenPrimary = Color(0xFF10B981)   // 500 - Main Brand Secondary
val LightGreenLight = Color(0xFF34D399)     // 400
val LightGreenDark = Color(0xFF047857)      // 700
val LightGreenContainerLight = Color(0xFFD1FAE5) // 100
val LightGreenOnContainerLight = Color(0xFF065F46)

// Soft Neutrals & Charcoals
val NeutralCharcoalDark = Color(0xFF0F172A)  // Slate 900
val NeutralCharcoalMedium = Color(0xFF1E293B)// Slate 800
val NeutralCharcoalCard = Color(0xFF182234)
val NeutralCardBorderDark = Color(0xFF334155)

val NeutralPureWhite = Color(0xFFFFFFFF)
val NeutralOffWhite = Color(0xFFF8FAFC)     // Slate 50
val NeutralLightGraySurface = Color(0xFFF1F5F9) // Slate 100
val NeutralLightGrayBorder = Color(0xFFE2E8F0) // Slate 200
val NeutralLightGrayDivider = Color(0xFFCBD5E1) // Slate 300
val NeutralTextMutedLight = Color(0xFF64748B) // Slate 500

val SemanticSuccess = Color(0xFF16A34A)
val SemanticSuccessContainer = Color(0xFFDCFCE7)
val SemanticWarning = Color(0xFFF59E0B)
val SemanticWarningContainer = Color(0xFFFEF3C7)
val SemanticError = Color(0xFFEF4444)
val SemanticErrorContainer = Color(0xFFFEE2E2)
val SemanticInfo = Color(0xFF0EA5E9)
val SemanticInfoContainer = Color(0xFFE0F2FE)

val HealthogramLightColors = HealthogramColorTokens(
    primary = RoyalBluePrimary,
    primaryLight = RoyalBlueLight,
    primaryDark = RoyalBlueDark,
    primaryContainer = RoyalBlueContainerLight,
    onPrimaryContainer = RoyalBlueOnContainerLight,

    secondary = LightGreenPrimary,
    secondaryLight = LightGreenLight,
    secondaryDark = LightGreenDark,
    secondaryContainer = LightGreenContainerLight,
    onSecondaryContainer = LightGreenOnContainerLight,

    background = NeutralOffWhite,
    surface = NeutralPureWhite,
    surfaceElevated = NeutralPureWhite,
    surfaceVariant = NeutralLightGraySurface,

    textPrimary = NeutralCharcoalDark,
    textSecondary = Color(0xFF334155),
    textMuted = NeutralTextMutedLight,
    textOnPrimary = NeutralPureWhite,

    border = NeutralLightGrayBorder,
    borderLight = Color(0xFFF1F5F9),
    divider = NeutralLightGrayDivider,

    success = SemanticSuccess,
    successContainer = SemanticSuccessContainer,
    warning = SemanticWarning,
    warningContainer = SemanticWarningContainer,
    error = SemanticError,
    errorContainer = SemanticErrorContainer,
    info = SemanticInfo,
    infoContainer = SemanticInfoContainer,

    brandGradient = Brush.linearGradient(
        colors = listOf(RoyalBluePrimary, LightGreenPrimary)
    ),
    storyGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2563EB), Color(0xFF10B981), Color(0xFF06B6D4))
    ),
    verifiedBadgeColor = RoyalBluePrimary,
    isDark = false
)

val HealthogramDarkColors = HealthogramColorTokens(
    primary = RoyalBlueLight,
    primaryLight = Color(0xFF60A5FA),
    primaryDark = RoyalBluePrimary,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),

    secondary = LightGreenLight,
    secondaryLight = Color(0xFF6EE7B7),
    secondaryDark = LightGreenPrimary,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),

    background = Color(0xFF0B0F19),
    surface = NeutralCharcoalDark,
    surfaceElevated = NeutralCharcoalMedium,
    surfaceVariant = Color(0xFF161F30),

    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFFCBD5E1),
    textMuted = Color(0xFF94A3B8),
    textOnPrimary = Color(0xFF0F172A),

    border = NeutralCardBorderDark,
    borderLight = Color(0xFF1E293B),
    divider = Color(0xFF334155),

    success = Color(0xFF22C55E),
    successContainer = Color(0xFF052E16),
    warning = Color(0xFFFBBF24),
    warningContainer = Color(0xFF451A03),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF450A0A),
    info = Color(0xFF38BDF8),
    infoContainer = Color(0xFF082F49),

    brandGradient = Brush.linearGradient(
        colors = listOf(RoyalBlueLight, LightGreenLight)
    ),
    storyGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3B82F6), Color(0xFF34D399), Color(0xFF22D3EE))
    ),
    verifiedBadgeColor = Color(0xFF38BDF8),
    isDark = true
)

val LocalHealthogramColors = staticCompositionLocalOf { HealthogramLightColors }

val HealthogramColorTokens.onSurface: Color get() = this.textPrimary
val HealthogramColorTokens.onSurfaceVariant: Color get() = this.textSecondary

object HealthogramColors {
    val primary: Color = RoyalBluePrimary
    val primaryLight: Color = RoyalBlueLight
    val primaryDark: Color = RoyalBlueDark
    val secondary: Color = LightGreenPrimary
    val background: Color = NeutralOffWhite
    val surface: Color = NeutralPureWhite
    val surfaceVariant: Color = NeutralLightGraySurface
    val onSurface: Color = NeutralCharcoalDark
    val onSurfaceVariant: Color = Color(0xFF64748B)
    val outlineVariant: Color = NeutralLightGrayBorder
    val error: Color = SemanticError
    val warning: Color = SemanticWarning
    val success: Color = SemanticSuccess
    val info: Color = SemanticInfo
    val amber500: Color = Color(0xFFF59E0B)
    val emerald500: Color = Color(0xFF10B981)
    val red500: Color = Color(0xFFEF4444)
    val blue500: Color = Color(0xFF3B82F6)
}
