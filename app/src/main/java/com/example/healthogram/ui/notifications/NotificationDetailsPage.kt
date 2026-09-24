package com.example.healthogram.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.notification.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Detailed view of a single notification with metadata, deep link target, and security audit confirmation.
 */
@Composable
fun NotificationDetailsPage(
    notificationId: String,
    onBack: () -> Unit,
    onNavigateDeepLink: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { NotificationRepository.getInstance() }
    val notifications by repository.notifications.collectAsState()
    val notification = notifications.find { it.notificationId == notificationId }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(notificationId) {
        repository.markNotificationRead(notificationId)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_details_page"),
        topBar = {
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
                            text = "Notification Details",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = HealthogramTheme.colors.error)
                    }
                }
            }
        }
    ) { innerPadding ->
        if (notification == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("This notification is no longer available.", style = HealthogramTheme.typography.bodyMedium, color = HealthogramTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) { Text("Back to Notifications") }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(HealthogramTheme.colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Chip
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HealthogramTheme.colors.primary.copy(alpha = 0.1f),
                                border = BorderStroke(0.5.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = notification.category.displayName,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary
                                )
                            }

                            // Priority Badge
                            val (badgeColor, priorityText) = when (notification.priority) {
                                NotificationPriority.CRITICAL -> Color(0xFFEF4444) to "CRITICAL"
                                NotificationPriority.HIGH -> Color(0xFFF59E0B) to "HIGH"
                                NotificationPriority.NORMAL -> Color(0xFF3B82F6) to "NORMAL"
                                NotificationPriority.LOW -> Color(0xFF6B7280) to "LOW"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = priorityText,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = badgeColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = notification.title,
                            style = HealthogramTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = notification.body,
                            style = HealthogramTheme.typography.bodyLarge,
                            color = HealthogramTheme.colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val df = SimpleDateFormat("EEEE, MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        Text(
                            text = "Received: ${df.format(Date(notification.createdAt))}",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }

                // Privacy Invariant Guarantee Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Healthogram Zero-Clinical-Leak Policy",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF15803D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "This notification complies with Healthogram security standards. No diagnoses, medications, or sensitive records are transmitted in cleartext push payloads.",
                                style = HealthogramTheme.typography.bodySmall,
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }

                // Technical & Deep Link Metadata
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Reference & Routing",
                            style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailMetadataRow("Type", notification.notificationType.name)
                        DetailMetadataRow("Target Entity", "${notification.targetType.name} : ${notification.targetId ?: "N/A"}")
                        DetailMetadataRow("Deep Link", notification.deepLink)
                        DetailMetadataRow("Notification ID", notification.notificationId.take(18) + "...")
                    }
                }

                // Primary Target CTA Action
                if (notification.targetType != NotificationTargetType.NONE) {
                    Button(
                        onClick = { onNavigateDeepLink(notification.deepLink) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("notification_cta_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HealthogramTheme.colors.primary)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        val ctaLabel = when (notification.targetType) {
                            NotificationTargetType.HEALTH_ACCESS -> "Review Health Passport Access"
                            NotificationTargetType.CONVERSATION -> "Open Secure Message"
                            NotificationTargetType.CALL -> "View Call Log"
                            NotificationTargetType.ORDER -> "View Order Details"
                            NotificationTargetType.SELLER_DASHBOARD -> "Open Seller Dashboard"
                            NotificationTargetType.APPOINTMENT -> "View Appointment Details"
                            NotificationTargetType.VERIFICATION -> "View Verification Status"
                            else -> "Open Content"
                        }
                        Text(ctaLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Notification?") },
            text = { Text("This will remove the notification from your Notification Center. Audit logs remain recorded.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteNotification(notificationId)
                        showDeleteConfirm = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = HealthogramTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailMetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        Text(value, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium), color = HealthogramTheme.colors.textPrimary)
    }
}
