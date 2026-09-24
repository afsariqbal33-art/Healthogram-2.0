package com.example.healthogram.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.healthogram.communication.*
import com.example.healthogram.designsystem.HealthogramTheme

/**
 * HEALTHOGRAM STEP 11: COMMUNICATION PRIVACY & SETTINGS PAGE
 *
 * Direct control over all messaging and call privacy settings:
 * - Direct messaging permissions
 * - Audio & video call permissions
 * - Online status, last seen, read receipts, typing indicators
 * - Multi-device management (up to 4 active sessions)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageSettingsPage(
    currentUid: String,
    repository: CommunicationRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settings by remember { mutableStateOf(repository.getSettings(currentUid)) }
    val userDevices by repository.userDevices.collectAsState()
    val myDevices = remember(userDevices, currentUid) {
        userDevices[currentUid] ?: listOf(
            UserDevice(deviceId = "dev_1", uid = currentUid, deviceName = "Pixel 8 Pro (Current Device)", isActive = true)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communication Privacy", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("communication_settings_page")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthogramTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Messaging Permissions
            item {
                Text("MESSAGING PERMISSIONS", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SettingSwitchRow(
                            title = "Allow Direct Text Messages",
                            subtitle = "Allow contacts to send you one-to-one messages",
                            checked = settings.allowTextMessages,
                            onCheckedChange = {
                                val updated = settings.copy(allowTextMessages = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Allow Voice Messages",
                            subtitle = "Receive voice audio clips in conversation",
                            checked = settings.allowVoiceMessages,
                            onCheckedChange = {
                                val updated = settings.copy(allowVoiceMessages = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Allow Media Messages",
                            subtitle = "Receive images, videos, and PDF documents",
                            checked = settings.allowMediaMessages,
                            onCheckedChange = {
                                val updated = settings.copy(allowMediaMessages = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Allow Message Requests",
                            subtitle = "Receive requests from users outside your network",
                            checked = settings.allowMessageRequests,
                            onCheckedChange = {
                                val updated = settings.copy(allowMessageRequests = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                    }
                }
            }

            // Section 2: Audio & Video Calling Permissions
            item {
                Text("CALLING PERMISSIONS", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SettingSwitchRow(
                            title = "Allow Audio Calls",
                            subtitle = "Permit incoming HD audio calls",
                            checked = settings.allowAudioCalls,
                            onCheckedChange = {
                                val updated = settings.copy(allowAudioCalls = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Allow Video Calls",
                            subtitle = "Permit incoming HD video consultations",
                            checked = settings.allowVideoCalls,
                            onCheckedChange = {
                                val updated = settings.copy(allowVideoCalls = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Calls from Verified Accounts Only",
                            subtitle = "Block calls from non-verified general users",
                            checked = settings.allowCallsFromVerifiedAccounts,
                            onCheckedChange = {
                                val updated = settings.copy(allowCallsFromVerifiedAccounts = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                    }
                }
            }

            // Section 3: Privacy & Presence Controls
            item {
                Text("PRESENCE & READ RECEIPTS", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SettingSwitchRow(
                            title = "Show Online Status",
                            subtitle = "Show green indicator when active on Healthogram",
                            checked = settings.showOnlineStatus,
                            onCheckedChange = {
                                val updated = settings.copy(showOnlineStatus = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Show Last Seen",
                            subtitle = "Display timestamp of your recent activity",
                            checked = settings.showLastSeen,
                            onCheckedChange = {
                                val updated = settings.copy(showLastSeen = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Read Receipts (Blue Double-Tick)",
                            subtitle = "Let participants see when you've read their messages",
                            checked = settings.showReadReceipts,
                            onCheckedChange = {
                                val updated = settings.copy(showReadReceipts = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SettingSwitchRow(
                            title = "Typing Indicator",
                            subtitle = "Show 'typing...' when composing a message",
                            checked = settings.showTypingIndicator,
                            onCheckedChange = {
                                val updated = settings.copy(showTypingIndicator = it)
                                settings = updated
                                repository.updateSettings(updated, currentUid)
                            }
                        )
                    }
                }
            }

            // Section 4: Multi-Device Management (Max 4 sessions)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CONNECTED DEVICES (${myDevices.count { it.isActive }}/4)", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    Text("Max 4 active", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                }
                Spacer(modifier = Modifier.height(8.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(containerColor = HealthogramTheme.colors.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        myDevices.forEachIndexed { index, device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = HealthogramTheme.colors.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(device.deviceName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                        Text(if (device.isActive) "Active session" else "Revoked", style = HealthogramTheme.typography.caption, color = if (device.isActive) Color(0xFF10B981) else Color.Gray)
                                    }
                                }
                                if (device.isActive) {
                                    TextButton(onClick = { repository.revokeDevice(currentUid, device.deviceId, currentUid) }) {
                                        Text("Revoke", color = HealthogramTheme.colors.error)
                                    }
                                }
                            }
                            if (index < myDevices.size - 1) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
