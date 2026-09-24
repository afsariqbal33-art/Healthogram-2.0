package com.example.healthogram.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.core.User
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.model.CommunicationSettings
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Communication Settings Page (Section 65).
 * Configures calling, video consultation preferences, network quality, and emergency overrides.
 */
@Composable
fun CommunicationSettingsPage(
    user: User?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var commSettings by remember { mutableStateOf(CommunicationSettings()) }

    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        val priv = repository.getPrivateProfile(uid)
        if (priv != null) {
            commSettings = priv.communicationSettings
        }
    }

    fun updateComm(newSettings: CommunicationSettings) {
        commSettings = newSettings
        val uid = user?.uid ?: return
        scope.launch {
            val existing = repository.getPrivateProfile(uid)
            if (existing != null) {
                repository.savePrivateProfile(existing.copy(communicationSettings = newSettings))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("communication_settings_page")
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
                    text = "Communication Settings",
                    style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text("Channels & Protocols", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("In-App Audio Calling", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Enable high-fidelity voice consultations and calls", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = commSettings.audioCallsEnabled,
                                onCheckedChange = { updateComm(commSettings.copy(audioCallsEnabled = it)) }
                            )
                        }

                        Divider(color = HealthogramTheme.colors.borderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("In-App Video Calling", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Enable encrypted telehealth video consultations", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = commSettings.videoCallsEnabled,
                                onCheckedChange = { updateComm(commSettings.copy(videoCallsEnabled = it)) }
                            )
                        }

                        Divider(color = HealthogramTheme.colors.borderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Direct Messaging", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Enable text chats and prescription sharing", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = commSettings.messagesEnabled,
                                onCheckedChange = { updateComm(commSettings.copy(messagesEnabled = it)) }
                            )
                        }
                    }
                }
            }

            item {
                Text("Emergency Overrides", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Emergency Contact Bypass", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "Allow designated emergency contacts to bypass Do-Not-Disturb and silence settings.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = commSettings.allowEmergencyBypass,
                            onCheckedChange = { updateComm(commSettings.copy(allowEmergencyBypass = it)) }
                        )
                    }
                }
            }

            item {
                Text("Call Quality & Data Optimization", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Column {
                        Text("Bandwidth Profile", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = commSettings.callQualityMode == "HD",
                                onClick = { updateComm(commSettings.copy(callQualityMode = "HD")) },
                                label = { Text("HD Quality (Best)") },
                                leadingIcon = if (commSettings.callQualityMode == "HD") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = commSettings.callQualityMode == "SAVER",
                                onClick = { updateComm(commSettings.copy(callQualityMode = "SAVER")) },
                                label = { Text("Data Saver") },
                                leadingIcon = if (commSettings.callQualityMode == "SAVER") {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
