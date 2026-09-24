package com.example.healthogram.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.notification.NotificationPreferences
import com.example.healthogram.notification.NotificationRepository

/**
 * Notification Settings Page.
 * Manages channels, categories, quiet hours shortcut, devices shortcut, and privacy settings.
 */
@Composable
fun NotificationSettingsPage(
    onBack: () -> Unit,
    onNavigateQuietHours: () -> Unit = {},
    onNavigateDevices: () -> Unit = {},
    onNavigatePrivacy: () -> Unit = {},
    onNavigateCategorySettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { NotificationRepository.getInstance() }
    val preferences by repository.preferences.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_settings_page"),
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
                        text = "Notification Settings",
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(HealthogramTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Navigation Shortcuts Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Column {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Bedtime,
                        title = "Quiet Hours & Schedule",
                        subtitle = if (preferences.quietHoursEnabled) "Active (${preferences.quietHoursStart} - ${preferences.quietHoursEnd})" else "Disabled",
                        onClick = onNavigateQuietHours
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Devices,
                        title = "Registered Devices (Multi-Device)",
                        subtitle = "Manage active push tokens across phone, tablet, and desktop",
                        onClick = onNavigateDevices
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Security,
                        title = "Notification Privacy & Disclaimers",
                        subtitle = "Health Passport isolation & lock-screen privacy controls",
                        onClick = onNavigatePrivacy
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Tune,
                        title = "Granular Category Controls",
                        subtitle = "Fine-tune specific sub-events and activity alerts",
                        onClick = onNavigateCategorySettings
                    )
                }
            }

            // Master Channels Section
            Text(
                text = "DELIVERY CHANNELS",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsToggleRow(
                        title = "Push Notifications",
                        subtitle = "Receive notifications when the app is in the background",
                        checked = preferences.pushEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(pushEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsToggleRow(
                        title = "In-App Banners",
                        subtitle = "Show heads-up banners while actively using Healthogram",
                        checked = preferences.inAppEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(inAppEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsToggleRow(
                        title = "Sound",
                        subtitle = "Play sound for incoming notifications",
                        checked = preferences.soundEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(soundEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsToggleRow(
                        title = "Vibration",
                        subtitle = "Vibrate device for high-priority alerts",
                        checked = preferences.vibrationEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(vibrationEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)
                    SettingsToggleRow(
                        title = "App Icon Badges",
                        subtitle = "Display unread counts on home screen and app header",
                        checked = preferences.badgeEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(badgeEnabled = it)) }
                    )
                }
            }

            // Subsystem Categories Section
            Text(
                text = "NOTIFICATION CATEGORIES",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textMuted
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Essential Healthcare & Security (Non-negotiable lock)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Health & Security Alerts", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            }
                            Text("Health Passport access requests & login security. Always active.", style = HealthogramTheme.typography.caption, color = Color(0xFF10B981))
                        }
                        Switch(checked = true, onCheckedChange = null, enabled = false)
                    }
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Audio & Video Calls",
                        subtitle = "Incoming calls and missed telehealth consultations",
                        checked = preferences.callsEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(callsEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Messages & Requests",
                        subtitle = "Direct chats, voice notes, and group updates",
                        checked = preferences.messagesEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(messagesEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Appointments & Reminders",
                        subtitle = "Doctor consultations, clinic slots, and schedule alerts",
                        checked = preferences.appointmentsEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(appointmentsEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Social Interactions",
                        subtitle = "Follows, likes, comments, and creator livestreams",
                        checked = preferences.socialEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(socialEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Marketplace Orders",
                        subtitle = "Order confirmation, shipping, delivery, and refunds",
                        checked = preferences.marketplaceEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(marketplaceEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Seller Center Alerts",
                        subtitle = "New customer orders, inventory alerts, and payouts",
                        checked = preferences.sellerEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(sellerEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Professional Verification",
                        subtitle = "License approvals and compliance review statuses",
                        checked = preferences.verificationEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(verificationEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Translation System",
                        subtitle = "Document translation completion and live call status",
                        checked = preferences.translationEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(translationEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "AI Studio Tools",
                        subtitle = "Job completions, media enhancements, and usage caps",
                        checked = preferences.aiEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(aiEnabled = it)) }
                    )
                    Divider(color = HealthogramTheme.colors.borderLight, thickness = 0.5.dp)

                    SettingsToggleRow(
                        title = "Promotions & Deals",
                        subtitle = "Flash sales, wellness discounts, and marketing campaigns",
                        checked = preferences.promotionsEnabled,
                        onCheckedChange = { repository.updatePreferences(preferences.copy(promotionsEnabled = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = HealthogramTheme.colors.primary
            )
        )
    }
}
