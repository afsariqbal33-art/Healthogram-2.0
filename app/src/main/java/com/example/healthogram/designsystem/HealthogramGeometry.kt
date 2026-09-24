package com.example.healthogram.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Spacing Scale for Healthogram.
 * Recommended base scale: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64 dp.
 */
@Immutable
data class HealthogramSpacingTokens(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val base: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 40.dp,
    val huge: Dp = 48.dp,
    val giant: Dp = 64.dp,

    // Semantic Spacers
    val screenPaddingHorizontal: Dp = 16.dp,
    val screenPaddingVertical: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val itemSpacing: Dp = 12.dp,
    val sectionSpacing: Dp = 24.dp
)

val LocalHealthogramSpacing = staticCompositionLocalOf { HealthogramSpacingTokens() }

/**
 * Healthogram Border Radius System.
 * Recommended: Small = 8, Medium = 12, Large = 16, XL = 20, XXL = 28, Pill = 999.
 */
@Immutable
data class HealthogramShapeTokens(
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val xl: Shape = RoundedCornerShape(20.dp),
    val xxl: Shape = RoundedCornerShape(28.dp),
    val pill: Shape = RoundedCornerShape(999.dp)
)

val LocalHealthogramShapes = staticCompositionLocalOf { HealthogramShapeTokens() }

/**
 * Healthogram Elevation & Shadow System.
 * None = 0.dp, Small = 2.dp, Medium = 6.dp, Large = 12.dp.
 */
@Immutable
data class HealthogramElevationTokens(
    val none: Dp = 0.dp,
    val small: Dp = 2.dp,
    val medium: Dp = 6.dp,
    val large: Dp = 12.dp
)

val LocalHealthogramElevation = staticCompositionLocalOf { HealthogramElevationTokens() }
