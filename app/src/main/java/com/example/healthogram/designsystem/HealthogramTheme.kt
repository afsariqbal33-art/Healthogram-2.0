package com.example.healthogram.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Healthogram Master Theme Provider.
 * Allows global configuration of colors, typography, spacing, shapes, and elevations.
 */
object HealthogramTheme {
    val colors: HealthogramColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHealthogramColors.current

    val typography: HealthogramTypographyTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHealthogramTypography.current

    val spacing: HealthogramSpacingTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHealthogramSpacing.current

    val shapes: HealthogramShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHealthogramShapes.current

    val elevation: HealthogramElevationTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalHealthogramElevation.current
}

@Composable
fun HealthogramDesignSystemTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorTokens = if (darkTheme) HealthogramDarkColors else HealthogramLightColors
    val typographyTokens = HealthogramTypographyTokens()
    val spacingTokens = HealthogramSpacingTokens()
    val shapeTokens = HealthogramShapeTokens()
    val elevationTokens = HealthogramElevationTokens()

    // Map to MaterialTheme M3 ColorScheme so standard M3 components adhere seamlessly
    val materialColorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorTokens.primary,
            onPrimary = colorTokens.textOnPrimary,
            primaryContainer = colorTokens.primaryContainer,
            onPrimaryContainer = colorTokens.onPrimaryContainer,
            secondary = colorTokens.secondary,
            onSecondary = colorTokens.textOnPrimary,
            secondaryContainer = colorTokens.secondaryContainer,
            onSecondaryContainer = colorTokens.onSecondaryContainer,
            background = colorTokens.background,
            onBackground = colorTokens.textPrimary,
            surface = colorTokens.surface,
            onSurface = colorTokens.textPrimary,
            surfaceVariant = colorTokens.surfaceVariant,
            onSurfaceVariant = colorTokens.textSecondary,
            error = colorTokens.error,
            onError = colorTokens.textOnPrimary,
            outline = colorTokens.border
        )
    } else {
        lightColorScheme(
            primary = colorTokens.primary,
            onPrimary = colorTokens.textOnPrimary,
            primaryContainer = colorTokens.primaryContainer,
            onPrimaryContainer = colorTokens.onPrimaryContainer,
            secondary = colorTokens.secondary,
            onSecondary = colorTokens.textOnPrimary,
            secondaryContainer = colorTokens.secondaryContainer,
            onSecondaryContainer = colorTokens.onSecondaryContainer,
            background = colorTokens.background,
            onBackground = colorTokens.textPrimary,
            surface = colorTokens.surface,
            onSurface = colorTokens.textPrimary,
            surfaceVariant = colorTokens.surfaceVariant,
            onSurfaceVariant = colorTokens.textSecondary,
            error = colorTokens.error,
            onError = colorTokens.textOnPrimary,
            outline = colorTokens.border
        )
    }

    CompositionLocalProvider(
        LocalHealthogramColors provides colorTokens,
        LocalHealthogramTypography provides typographyTokens,
        LocalHealthogramSpacing provides spacingTokens,
        LocalHealthogramShapes provides shapeTokens,
        LocalHealthogramElevation provides elevationTokens
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content
        )
    }
}
