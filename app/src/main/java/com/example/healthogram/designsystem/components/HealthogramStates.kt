package com.example.healthogram.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * Shimmering Skeleton Loader Box
 */
@Composable
fun HealthogramShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = HealthogramTheme.shapes.medium
) {
    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(HealthogramTheme.colors.border.copy(alpha = alpha))
    )
}

/**
 * Skeleton Loader for Cards & Feed items
 */
@Composable
fun HealthogramCardLoading(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HealthogramShimmerBox(modifier = Modifier.size(44.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    HealthogramShimmerBox(modifier = Modifier.size(120.dp, 16.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    HealthogramShimmerBox(modifier = Modifier.size(80.dp, 12.dp))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            HealthogramShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), shape = HealthogramTheme.shapes.medium)
        }
    }
}

/**
 * Master Empty State Component
 */
@Composable
fun HealthogramEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Inbox,
    actionButtonText: String? = null,
    onActionClick: () -> Unit = {},
    testTag: String = "empty_state"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(HealthogramTheme.spacing.xl)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HealthogramTheme.colors.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.md))

        Text(
            text = title,
            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xs))

        Text(
            text = description,
            style = HealthogramTheme.typography.bodyMedium,
            color = HealthogramTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )

        if (actionButtonText != null) {
            Spacer(modifier = Modifier.height(HealthogramTheme.spacing.base))
            HealthogramPrimaryButton(
                text = actionButtonText,
                onClick = onActionClick,
                size = ButtonSize.MEDIUM
            )
        }
    }
}

/**
 * Master Error State Component
 */
@Composable
fun HealthogramErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something Went Wrong",
    onRetry: (() -> Unit)? = null,
    testTag: String = "error_state"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(HealthogramTheme.spacing.xl)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(HealthogramTheme.colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = HealthogramTheme.colors.error,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.md))

        Text(
            text = title,
            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(HealthogramTheme.spacing.xs))

        Text(
            text = message,
            style = HealthogramTheme.typography.bodyMedium,
            color = HealthogramTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(HealthogramTheme.spacing.base))
            HealthogramOutlineButton(
                text = "Retry",
                onClick = onRetry,
                icon = Icons.Default.Refresh
            )
        }
    }
}

/**
 * Offline Network State Component
 */
@Composable
fun HealthogramOfflineState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    HealthogramEmptyState(
        title = "No Internet Connection",
        description = "Please check your network settings. Healthogram offline mode is caching your active passport.",
        icon = Icons.Default.WifiOff,
        actionButtonText = "Reconnect",
        onActionClick = onRetry,
        modifier = modifier
    )
}
