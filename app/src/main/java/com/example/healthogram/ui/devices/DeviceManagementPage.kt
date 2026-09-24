package com.example.healthogram.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*

data class ActiveDeviceModel(
    val id: String,
    val name: String,
    val type: String,
    val location: String,
    val lastActive: String,
    val isCurrentDevice: Boolean,
    val icon: ImageVector
)

@Composable
fun DeviceManagementPage(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var devices by remember {
        mutableStateOf(
            listOf(
                ActiveDeviceModel("d1", "Google Pixel 9 Pro", "Android Phone", "Dubai, UAE", "Active now", true, Icons.Default.PhoneAndroid),
                ActiveDeviceModel("d2", "Apple iPad Pro M4", "Tablet", "Dubai, UAE", "2 hours ago", false, Icons.Default.TabletMac),
                ActiveDeviceModel("d3", "MacBook Pro 16\"", "Desktop Browser", "San Francisco, USA", "Yesterday", false, Icons.Default.Laptop)
            )
        )
    }

    var socialPermission by remember { mutableStateOf(true) }
    var marketplacePermission by remember { mutableStateOf(true) }
    var messagingPermission by remember { mutableStateOf(true) }
    var callingPermission by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("device_management_page")
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
                Text(
                    text = "Devices & Permissions",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text("Logged-In Devices", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            items(devices, key = { it.id }) { dev ->
                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(HealthogramTheme.colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(dev.icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(dev.name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    if (dev.isCurrentDevice) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = HealthogramTheme.shapes.pill,
                                            color = HealthogramTheme.colors.successContainer
                                        ) {
                                            Text(
                                                text = "This Device",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = HealthogramTheme.typography.overline,
                                                color = HealthogramTheme.colors.success
                                            )
                                        }
                                    }
                                }
                                Text("${dev.type} • ${dev.location}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                Text(dev.lastActive, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = if (dev.isCurrentDevice) HealthogramTheme.colors.success else HealthogramTheme.colors.textMuted)
                            }
                        }

                        if (!dev.isCurrentDevice) {
                            HealthogramDangerButton(
                                text = "Revoke",
                                onClick = { devices = devices.filter { it.id != dev.id } },
                                icon = null
                            )
                        }
                    }
                }
            }

            // Organization Device Permission Matrix (Section 23)
            item {
                Text("Device Access Matrix", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Social Platform Publishing", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Allow publishing posts, reels & stories from this device", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = socialPermission, onCheckedChange = { socialPermission = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HealthogramTheme.colors.borderLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Marketplace Store Operations", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Allow managing products, fulfilling orders & payouts", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = marketplacePermission, onCheckedChange = { marketplacePermission = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HealthogramTheme.colors.borderLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Clinical Messaging & Consultation", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Allow chatting with patients and verified doctors", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = messagingPermission, onCheckedChange = { messagingPermission = it })
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = HealthogramTheme.colors.borderLight)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Encrypted Audio / Video Calling", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text("Allow initiating and receiving encrypted telehealth calls", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = callingPermission, onCheckedChange = { callingPermission = it })
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
