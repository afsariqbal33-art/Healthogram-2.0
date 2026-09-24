package com.example.healthogram.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme

enum class HealthogramLogoSize(val iconSize: Dp, val cornerRadius: Dp, val symbolSize: Dp, val fontSize: Float) {
    SMALL(28.dp, 8.dp, 16.dp, 14f),
    MEDIUM(38.dp, 12.dp, 22.dp, 18f),
    LARGE(52.dp, 16.dp, 32.dp, 24f),
    SPLASH(80.dp, 24.dp, 48.dp, 32f)
}

/**
 * Modern Healthogram Brand Logo.
 * Rounded-square visual container with modern Royal Blue to Light Green gradient.
 * Contains recognized health & technology emblem + optional wordmark.
 */
@Composable
fun HealthogramLogo(
    modifier: Modifier = Modifier,
    size: HealthogramLogoSize = HealthogramLogoSize.MEDIUM,
    showWordmark: Boolean = true,
    wordmarkColor: Color = HealthogramTheme.colors.textPrimary
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size.iconSize)
                .clip(RoundedCornerShape(size.cornerRadius))
                .background(brush = HealthogramTheme.colors.brandGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = "Healthogram Symbol",
                tint = Color.White,
                modifier = Modifier.size(size.symbolSize)
            )
        }

        if (showWordmark) {
            Spacer(modifier = Modifier.width(HealthogramTheme.spacing.sm))
            Column {
                Text(
                    text = "HEALTHOGRAM",
                    style = HealthogramTheme.typography.titleLarge.copy(
                        fontSize = size.fontSize.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    ),
                    color = wordmarkColor
                )
            }
        }
    }
}
