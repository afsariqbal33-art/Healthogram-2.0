package com.example.healthogram.ui.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.notification.NotificationCategory
import com.example.healthogram.notification.NotificationItem
import com.example.healthogram.notification.NotificationPriority

/**
 * Real-time In-App Heads-Up Notification Banner.
 * Appears at the top of the screen when a high/critical notification arrives.
 */
@Composable
fun NotificationInAppBanner(
    notification: NotificationItem?,
    onDismiss: () -> Unit,
    onClick: (NotificationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        notification?.let { item ->
            val isCritical = item.priority == NotificationPriority.CRITICAL
            val containerColor = if (isCritical) Color(0xFF1E293B) else HealthogramTheme.colors.surface
            val iconTint = when (item.category) {
                NotificationCategory.HEALTH_SECURITY -> Color(0xFF10B981)
                NotificationCategory.CALLS -> Color(0xFF3B82F6)
                NotificationCategory.MESSAGES -> Color(0xFF6366F1)
                NotificationCategory.MARKETPLACE, NotificationCategory.SELLER -> Color(0xFFF59E0B)
                else -> HealthogramTheme.colors.primary
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClick(item) }
                    .testTag("in_app_notification_banner"),
                color = containerColor,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCritical) Color(0xFFEF4444).copy(alpha = 0.5f) else HealthogramTheme.colors.borderLight
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconVector = when (item.category) {
                            NotificationCategory.HEALTH_SECURITY -> Icons.Default.HealthAndSafety
                            NotificationCategory.CALLS -> Icons.Default.Call
                            NotificationCategory.MESSAGES -> Icons.Default.Chat
                            NotificationCategory.APPOINTMENTS -> Icons.Default.CalendarToday
                            NotificationCategory.MARKETPLACE -> Icons.Default.ShoppingBag
                            NotificationCategory.SELLER -> Icons.Default.Storefront
                            NotificationCategory.VERIFICATION -> Icons.Default.Verified
                            else -> Icons.Default.Notifications
                        }
                        Icon(iconVector, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = HealthogramTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = if (isCritical) Color.White else HealthogramTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Now",
                                style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                                color = if (isCritical) Color(0xFF94A3B8) else HealthogramTheme.colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.body,
                            style = HealthogramTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = if (isCritical) Color(0xFFCBD5E1) else HealthogramTheme.colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = if (isCritical) Color(0xFF94A3B8) else HealthogramTheme.colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
