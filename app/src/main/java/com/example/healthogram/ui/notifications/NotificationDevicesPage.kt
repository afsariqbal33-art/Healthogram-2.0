package com.example.healthogram.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.TabletMac
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Multi-Device Push & FCM Token Management Page.
 * Displays up to 4 simultaneous active user sessions with per-device push status and revocation.
 */
@Composable
fun NotificationDevicesPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { NotificationRepository.getInstance() }
    val service = remember { NotificationService.getInstance() }
    val devices by repository.devices.collectAsState()
    var testFeedbackMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_devices_page"),
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Registered Devices & FCM",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Devices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Active Sessions (Max 4)", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Healthogram syncs in-app and push notifications across up to 4 concurrent authorized devices. Device tokens are refreshed automatically and invalidated on logout or revocation.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                }
            }

            testFeedbackMessage?.let { feedback ->
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(feedback, style = HealthogramTheme.typography.caption, color = Color(0xFF15803D))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "AUTHORIZED DEVICES (${devices.count { it.isActive }}/4 ACTIVE)",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textMuted
                )
            }

            items(devices, key = { it.deviceId }) { device ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (device.isActive) HealthogramTheme.colors.surface else HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, if (device.isActive) HealthogramTheme.colors.borderLight else Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val platformIcon = when {
                                    device.platform.contains("Android", ignoreCase = true) -> Icons.Outlined.PhoneAndroid
                                    device.platform.contains("Tablet", ignoreCase = true) -> Icons.Outlined.TabletMac
                                    else -> Icons.Outlined.Laptop
                                }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(platformIcon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(device.deviceName, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("${device.platform} • ${device.osVersion}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (device.isActive) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFF6B7280).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (device.isActive) "ACTIVE" else "REVOKED",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = HealthogramTheme.typography.caption.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (device.isActive) Color(0xFF059669) else Color(0xFF4B5563)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val df = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        Text("Token: ${device.fcmTokenOrInstallationId.take(24)}...", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        Text("Last Active: ${df.format(Date(device.lastActiveAt))}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)

                        if (device.isActive) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        service.dispatchEvent(
                                            recipientUid = "current_user",
                                            type = NotificationType.SECURITY_ALERT,
                                            variables = mapOf("device_name" to device.deviceName)
                                        )
                                        testFeedbackMessage = "Test push successfully sent to ${device.deviceName}"
                                    }
                                ) {
                                    Text("Test Push", style = HealthogramTheme.typography.labelSmall)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = {
                                        repository.revokeDevice(device.deviceId)
                                        testFeedbackMessage = "Revoked push permissions for ${device.deviceName}"
                                    }
                                ) {
                                    Text("Revoke", color = HealthogramTheme.colors.error, style = HealthogramTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
