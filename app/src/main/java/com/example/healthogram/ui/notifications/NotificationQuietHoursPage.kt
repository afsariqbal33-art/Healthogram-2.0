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
import androidx.compose.material.icons.outlined.Bedtime
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
import com.example.healthogram.notification.NotificationPreferences
import com.example.healthogram.notification.NotificationRepository

/**
 * Quiet Hours & Do Not Disturb Configuration Page.
 */
@Composable
fun NotificationQuietHoursPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { NotificationRepository.getInstance() }
    val preferences by repository.preferences.collectAsState()

    var quietHoursEnabled by remember(preferences) { mutableStateOf(preferences.quietHoursEnabled) }
    var startTime by remember(preferences) { mutableStateOf(preferences.quietHoursStart) }
    var endTime by remember(preferences) { mutableStateOf(preferences.quietHoursEnd) }
    var allowCriticalBypass by remember(preferences) { mutableStateOf(preferences.allowCriticalBypass) }

    fun saveChanges() {
        repository.updatePreferences(
            preferences.copy(
                quietHoursEnabled = quietHoursEnabled,
                quietHoursStart = startTime,
                quietHoursEnd = endTime,
                allowCriticalBypass = allowCriticalBypass
            )
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("notification_quiet_hours_page"),
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
                        text = "Quiet Hours & Sleep Schedule",
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
            // Quiet Hours Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (quietHoursEnabled) Color(0xFF1E293B) else HealthogramTheme.colors.surface
                ),
                border = BorderStroke(1.dp, if (quietHoursEnabled) Color(0xFF334155) else HealthogramTheme.colors.borderLight)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Bedtime, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Enable Quiet Hours",
                                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (quietHoursEnabled) Color.White else HealthogramTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (quietHoursEnabled) "Currently scheduled ($startTime - $endTime)" else "Notifications alert normally",
                                    style = HealthogramTheme.typography.caption,
                                    color = if (quietHoursEnabled) Color(0xFF94A3B8) else HealthogramTheme.colors.textMuted
                                )
                            }
                        }

                        Switch(
                            checked = quietHoursEnabled,
                            onCheckedChange = {
                                quietHoursEnabled = it
                                saveChanges()
                            }
                        )
                    }
                }
            }

            // Schedule Presets
            if (quietHoursEnabled) {
                Text(
                    text = "SCHEDULE INTERVAL",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textMuted
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        TimeSelectionRow(
                            label = "Starts At (Bedtime)",
                            time = startTime,
                            onSelect = {
                                startTime = it
                                saveChanges()
                            }
                        )
                        Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 12.dp))
                        TimeSelectionRow(
                            label = "Ends At (Morning)",
                            time = endTime,
                            onSelect = {
                                endTime = it
                                saveChanges()
                            }
                        )
                    }
                }

                Text(
                    text = "CRITICAL EXEMPTIONS & EMERGENCIES",
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textMuted
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Emergency Health Passport & Security Alerts", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                }
                                Text("Crucial security notices always bypass Quiet Hours.", style = HealthogramTheme.typography.caption, color = Color(0xFF10B981))
                            }
                            Switch(checked = true, onCheckedChange = null, enabled = false)
                        }

                        Divider(color = HealthogramTheme.colors.borderLight, modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow Incoming Calls & Consultations", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                Text("Ring during quiet hours for doctor and specialist calls.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = allowCriticalBypass,
                                onCheckedChange = {
                                    allowCriticalBypass = it
                                    saveChanges()
                                }
                            )
                        }
                    }
                }

                // Policy Explanation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "During quiet hours, promotional and social notifications are queued and delivered silently after $endTime without disturbing your rest.",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSelectionRow(
    label: String,
    time: String,
    onSelect: (String) -> Unit
) {
    val presets = listOf("21:00", "22:00", "23:00", "06:00", "07:00", "08:00")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HealthogramTheme.colors.primary.copy(alpha = 0.12f)
            ) {
                Text(
                    text = time,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            presets.take(3).forEach { preset ->
                AssistChip(
                    onClick = { onSelect(preset) },
                    label = { Text(preset, fontSize = 12.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (time == preset) HealthogramTheme.colors.primary.copy(alpha = 0.15f) else HealthogramTheme.colors.surface
                    )
                )
            }
        }
    }
}
