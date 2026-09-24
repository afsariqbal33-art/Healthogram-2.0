package com.example.healthogram.ui.notifications

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.notification.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Backward compatibility model if referenced by existing tests or components.
 */
data class NotificationModel(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean,
    val category: String,
    val icon: ImageVector
)

/**
 * Healthogram Central Notification Center.
 * Supports All, Social, Messages, Calls, Marketplace, Seller, Health & Security, Appointments, System.
 * Enables: Mark Read, Mark All Read, Delete, Clear Category, Settings, and Subsystem Event Testing.
 */
@Composable
fun NotificationsPage(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenDetails: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val repository = remember { NotificationRepository.getInstance() }
    val service = remember { NotificationService.getInstance() }

    val notifications by repository.notifications.collectAsState()
    val unreadCounts by repository.unreadCount.collectAsState()
    val inAppBanner by service.inAppBanner.collectAsState()

    var selectedCategory by remember { mutableStateOf<NotificationCategory?>(null) } // null = All
    var showClearCategoryDialog by remember { mutableStateOf(false) }
    var showSimulateMenu by remember { mutableStateOf(false) }

    val categoryTabs = listOf(
        null to "All",
        NotificationCategory.HEALTH_SECURITY to "Health & Security",
        NotificationCategory.MESSAGES to "Messages",
        NotificationCategory.CALLS to "Calls",
        NotificationCategory.APPOINTMENTS to "Appointments",
        NotificationCategory.MARKETPLACE to "Marketplace",
        NotificationCategory.SELLER to "Seller",
        NotificationCategory.SOCIAL to "Social",
        NotificationCategory.SYSTEM to "System"
    )

    val filteredNotifications = remember(notifications, selectedCategory) {
        if (selectedCategory == null) notifications
        else notifications.filter { it.category == selectedCategory }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("notifications_page")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = HealthogramTheme.colors.surface,
                border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Notifications",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (unreadCounts.total > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = HealthogramTheme.colors.primary
                            ) {
                                Text(
                                    text = unreadCounts.total.toString(),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    style = HealthogramTheme.typography.caption.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showSimulateMenu = true },
                            modifier = Modifier.testTag("simulate_notif_button")
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = "Simulate Event", tint = HealthogramTheme.colors.primary)
                        }

                        IconButton(
                            onClick = { repository.markAllRead(selectedCategory) },
                            modifier = Modifier.testTag("mark_all_read_button")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark All Read", tint = HealthogramTheme.colors.textSecondary)
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.testTag("notification_settings_button")
                        ) {
                            Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = HealthogramTheme.colors.textSecondary)
                        }
                    }
                }
            }

            // Category Filter Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryTabs) { (cat, label) ->
                    val isSelected = selectedCategory == cat
                    val count = when (cat) {
                        null -> unreadCounts.total
                        NotificationCategory.HEALTH_SECURITY -> unreadCounts.healthSecurity
                        NotificationCategory.MESSAGES -> unreadCounts.messages
                        NotificationCategory.CALLS -> unreadCounts.calls
                        NotificationCategory.APPOINTMENTS -> unreadCounts.appointments
                        NotificationCategory.MARKETPLACE -> unreadCounts.marketplace
                        NotificationCategory.SELLER -> unreadCounts.seller
                        NotificationCategory.SOCIAL -> unreadCounts.social
                        NotificationCategory.SYSTEM -> unreadCounts.system
                        else -> 0
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.surface,
                        border = BorderStroke(1.dp, if (isSelected) HealthogramTheme.colors.primary else HealthogramTheme.colors.borderLight),
                        modifier = Modifier
                            .clickable { selectedCategory = cat }
                            .testTag("tab_${label.replace(" ", "_").lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = HealthogramTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else HealthogramTheme.colors.textPrimary
                            )
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else HealthogramTheme.colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (count > 99) "99+" else count.toString(),
                                        style = HealthogramTheme.typography.caption.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) HealthogramTheme.colors.primary else Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Category Actions (Clear, Filter summary)
            if (selectedCategory != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedCategory?.displayName} (${filteredNotifications.size})",
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                        color = HealthogramTheme.colors.textMuted
                    )

                    if (filteredNotifications.isNotEmpty()) {
                        TextButton(
                            onClick = { showClearCategoryDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Clear ${selectedCategory?.displayName}",
                                style = HealthogramTheme.typography.caption.copy(color = HealthogramTheme.colors.error)
                            )
                        }
                    }
                }
            }

            // Notifications List
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.NotificationsOff, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No notifications yet",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You're all caught up! New updates, messages, and security notices will appear here.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showSimulateMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.AddAlert, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Test Notification", color = HealthogramTheme.colors.primary, style = HealthogramTheme.typography.labelSmall)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.notificationId }) { item ->
                        NotificationItemCard(
                            item = item,
                            onClick = {
                                repository.markNotificationRead(item.notificationId)
                                onOpenDetails(item.notificationId)
                            },
                            onMarkRead = { repository.markNotificationRead(item.notificationId) },
                            onDelete = { repository.deleteNotification(item.notificationId) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }

        // Heads-up In-App Banner Overlay
        NotificationInAppBanner(
            notification = inAppBanner,
            onDismiss = { service.dismissInAppBanner() },
            onClick = { item ->
                service.dismissInAppBanner()
                repository.markNotificationRead(item.notificationId)
                onOpenDetails(item.notificationId)
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }

    // Clear Category Confirmation Dialog
    if (showClearCategoryDialog && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showClearCategoryDialog = false },
            title = { Text("Clear ${selectedCategory?.displayName}?") },
            text = { Text("Are you sure you want to dismiss all notifications in this category?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCategory?.let { repository.clearCategory(it) }
                        showClearCategoryDialog = false
                    }
                ) {
                    Text("Clear All", color = HealthogramTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Subsystem Event Simulator Sheet
    if (showSimulateMenu) {
        AlertDialog(
            onDismissRequest = { showSimulateMenu = false },
            title = { Text("Trigger System Notification Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Test real-time dispatch, FCM payloads, and in-app banners across core subsystems:", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)

                    SimulateTriggerRow("Health Passport Access Request", Icons.Default.HealthAndSafety, Color(0xFF10B981)) {
                        NotificationCustomActions.sendHealthSecurityNotification(
                            recipientUid = "current_user",
                            orgName = "Apex Medical Labs",
                            requestId = "req_${System.currentTimeMillis()}"
                        )
                        showSimulateMenu = false
                    }

                    SimulateTriggerRow("Incoming Telehealth Call", Icons.Default.Call, Color(0xFF3B82F6)) {
                        NotificationCustomActions.sendCallNotification(
                            recipientUid = "current_user",
                            callerUid = "doc_marcus",
                            callerName = "Dr. Marcus Vance",
                            callId = "call_${System.currentTimeMillis()}",
                            isVideo = true
                        )
                        showSimulateMenu = false
                    }

                    SimulateTriggerRow("New Direct Chat Message", Icons.Default.Chat, Color(0xFF6366F1)) {
                        NotificationCustomActions.sendMessageNotification(
                            recipientUid = "current_user",
                            senderUid = "user_elena",
                            senderName = "Elena Rostova",
                            conversationId = "conv_elena_12"
                        )
                        showSimulateMenu = false
                    }

                    SimulateTriggerRow("Order Shipped with Tracking", Icons.Default.LocalShipping, Color(0xFFF59E0B)) {
                        NotificationCustomActions.sendMarketplaceNotification(
                            recipientUid = "current_user",
                            orderId = "ord_7712",
                            orderNumber = "HLTH-8821",
                            productName = "Pulse Oximeter Pro",
                            type = NotificationType.ORDER_SHIPPED
                        )
                        showSimulateMenu = false
                    }

                    SimulateTriggerRow("Practitioner Verification Approved", Icons.Default.Verified, Color(0xFF059669)) {
                        NotificationCustomActions.sendVerificationNotification(
                            recipientUid = "current_user",
                            requestId = "ver_license_442",
                            type = NotificationType.VERIFICATION_APPROVED
                        )
                        showSimulateMenu = false
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimulateMenu = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun SimulateTriggerRow(
    title: String,
    icon: ImageVector,
    color: Color,
    onTrigger: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTrigger() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun NotificationItemCard(
    item: NotificationItem,
    onClick: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val iconVector = when (item.category) {
        NotificationCategory.HEALTH_SECURITY -> Icons.Default.HealthAndSafety
        NotificationCategory.CALLS -> Icons.Default.Call
        NotificationCategory.MESSAGES -> Icons.Default.Chat
        NotificationCategory.APPOINTMENTS -> Icons.Default.CalendarToday
        NotificationCategory.MARKETPLACE -> Icons.Default.ShoppingBag
        NotificationCategory.SELLER -> Icons.Default.Storefront
        NotificationCategory.VERIFICATION -> Icons.Default.Verified
        NotificationCategory.SOCIAL -> Icons.Default.Favorite
        NotificationCategory.AI_STUDIO -> Icons.Default.Psychology
        else -> Icons.Default.Notifications
    }

    val iconColor = when (item.category) {
        NotificationCategory.HEALTH_SECURITY -> Color(0xFF10B981)
        NotificationCategory.CALLS -> Color(0xFF3B82F6)
        NotificationCategory.MESSAGES -> Color(0xFF6366F1)
        NotificationCategory.MARKETPLACE, NotificationCategory.SELLER -> Color(0xFFF59E0B)
        NotificationCategory.VERIFICATION -> Color(0xFF059669)
        else -> HealthogramTheme.colors.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${item.notificationId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) HealthogramTheme.colors.primary.copy(alpha = 0.05f) else HealthogramTheme.colors.surface
        ),
        border = BorderStroke(
            1.dp,
            if (!item.isRead) HealthogramTheme.colors.primary.copy(alpha = 0.25f) else HealthogramTheme.colors.borderLight
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
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
                        style = HealthogramTheme.typography.bodyMedium.copy(
                            fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = HealthogramTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = formatRelativeTime(item.createdAt),
                        style = HealthogramTheme.typography.caption.copy(fontSize = 11.sp),
                        color = HealthogramTheme.colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.body,
                    style = HealthogramTheme.typography.bodySmall,
                    color = if (!item.isRead) HealthogramTheme.colors.textPrimary.copy(alpha = 0.85f) else HealthogramTheme.colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = item.category.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = HealthogramTheme.typography.caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                            color = HealthogramTheme.colors.textMuted
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { expandedMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false }
                        ) {
                            if (!item.isRead) {
                                DropdownMenuItem(
                                    text = { Text("Mark as read") },
                                    leadingIcon = { Icon(Icons.Default.Done, contentDescription = null) },
                                    onClick = {
                                        onMarkRead()
                                        expandedMenu = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
                                onClick = {
                                    onDelete()
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                }
            }

            if (!item.isRead) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary)
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (60 * 1000)
    val hours = diff / (3600 * 1000)
    val days = diff / (24 * 3600 * 1000)
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "${mins}m ago"
        hours < 24 -> "${hours}h ago"
        days == 1L -> "Yesterday"
        else -> "${days}d ago"
    }
}
