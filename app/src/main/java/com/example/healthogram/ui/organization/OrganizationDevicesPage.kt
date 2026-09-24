package com.example.healthogram.ui.organization

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.model.OrganizationDevice
import com.example.healthogram.profile.model.OrganizationDevicePermissions
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Organization Hardware Device Management Page (Section 65).
 * Enforces controlled multi-device policies:
 * - Default 4 active devices (expandable up to 8 with subscription package)
 * - Fine-grained feature permissions per hardware terminal.
 */
@Composable
fun OrganizationDevicesPage(
    orgId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var devices by remember { mutableStateOf<List<OrganizationDevice>>(emptyList()) }
    var selectedDeviceForPerms by remember { mutableStateOf<OrganizationDevice?>(null) }

    LaunchedEffect(orgId) {
        devices = repository.getOrganizationDevices(orgId)
    }

    val maxAllowedDevices = 4 // Base tier (up to 8 in enterprise tier)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("organization_devices_page")
    ) {
        // App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Organization Devices",
                        style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Active Hardware Terminals (${devices.size} / $maxAllowedDevices)",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Quota card
            item {
                Surface(
                    shape = HealthogramTheme.shapes.medium,
                    color = HealthogramTheme.colors.primaryContainer,
                    border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Devices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Multi-Device Policy Enforced",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.onPrimaryContainer
                            )
                            Text(
                                "Standard plan supports 4 active devices simultaneously. Enterprise license supports up to 8 dedicated clinical stations.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            items(devices) { dev ->
                HealthogramBasicCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dev.deviceName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("${dev.osVersion} • IP: ${dev.ipAddress}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Surface(
                                shape = HealthogramTheme.shapes.pill,
                                color = HealthogramTheme.colors.successContainer
                            ) {
                                Text(
                                    text = dev.status.uppercase(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.success
                                )
                            }
                        }

                        Divider(color = HealthogramTheme.colors.borderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Clinical Privileges Configured",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                            TextButton(onClick = { selectedDeviceForPerms = dev }) {
                                Text("Configure Privileges", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // Configure Permissions Dialog
    selectedDeviceForPerms?.let { dev ->
        var perms by remember(dev) { mutableStateOf(dev.devicePermissions) }

        AlertDialog(
            onDismissRequest = { selectedDeviceForPerms = null },
            title = {
                Text(
                    "Device Feature Permissions: ${dev.deviceName}",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        DevicePermSwitch("Appointments & Triage", perms.appointments) {
                            perms = perms.copy(appointments = it)
                        }
                    }
                    item {
                        DevicePermSwitch("Telehealth Consultations", perms.consultations) {
                            perms = perms.copy(consultations = it)
                        }
                    }
                    item {
                        DevicePermSwitch("Direct Messaging & Chats", perms.textMessaging) {
                            perms = perms.copy(textMessaging = it)
                        }
                    }
                    item {
                        DevicePermSwitch("In-App Audio Calling", perms.audioCalling) {
                            perms = perms.copy(audioCalling = it)
                        }
                    }
                    item {
                        DevicePermSwitch("In-App Video Calling", perms.videoCalling) {
                            perms = perms.copy(videoCalling = it)
                        }
                    }
                    item {
                        DevicePermSwitch("Administration / Management", perms.management) {
                            perms = perms.copy(management = it)
                        }
                    }
                    item {
                        DevicePermSwitch("Gemini AI Studio Tools", perms.aiStudio) {
                            perms = perms.copy(aiStudio = it)
                        }
                    }
                    item {
                        DevicePermSwitch("Feed & Social Content", perms.socialMedia) {
                            perms = perms.copy(socialMedia = it)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.updateOrganizationDevicePermissions(orgId, dev.deviceId, perms)
                            devices = repository.getOrganizationDevices(orgId)
                            selectedDeviceForPerms = null
                        }
                    }
                ) {
                    Text("Save Permissions", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedDeviceForPerms = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DevicePermSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = HealthogramTheme.typography.bodySmall)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
