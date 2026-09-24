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
import com.example.healthogram.profile.model.PrivacyAudience
import com.example.healthogram.profile.model.ProfilePrivacySettings
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Privacy Settings Page (Section 65).
 * Configures public/private mode, messaging audience, and calling audience.
 */
@Composable
fun PrivacySettingsPage(
    user: User?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var privacySettings by remember { mutableStateOf(ProfilePrivacySettings()) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        val priv = repository.getPrivateProfile(uid)
        if (priv != null) {
            privacySettings = priv.privacySettings
        }
    }

    fun updateSettings(newSettings: ProfilePrivacySettings) {
        privacySettings = newSettings
        val uid = user?.uid ?: return
        scope.launch {
            isSaving = true
            val existing = repository.getPrivateProfile(uid)
            if (existing != null) {
                repository.savePrivateProfile(existing.copy(privacySettings = newSettings))
            }
            isSaving = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("privacy_settings_page")
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
                    text = "Privacy & Safety",
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
            // Private Account Toggle
            item {
                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Private Account", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "When your account is private, only approved followers can see your medical milestones and feed posts.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = privacySettings.isPrivateAccount,
                            onCheckedChange = {
                                updateSettings(privacySettings.copy(isPrivateAccount = it))
                            }
                        )
                    }
                }
            }

            item {
                Text("Interaction Controls", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            // Who can message
            item {
                AudienceSelector(
                    title = "Who Can Send Direct Messages",
                    subtitle = "Control who can initiate private health conversations",
                    current = privacySettings.whoCanMessage,
                    onSelected = { updateSettings(privacySettings.copy(whoCanMessage = it)) }
                )
            }

            // Who can call
            item {
                AudienceSelector(
                    title = "Who Can Audio Call",
                    subtitle = "Filter incoming voice consultations and calls",
                    current = privacySettings.whoCanCall,
                    onSelected = { updateSettings(privacySettings.copy(whoCanCall = it)) }
                )
            }

            // Who can video call
            item {
                AudienceSelector(
                    title = "Who Can Video Call",
                    subtitle = "Restrict camera and video consultation connections",
                    current = privacySettings.whoCanVideoCall,
                    onSelected = { updateSettings(privacySettings.copy(whoCanVideoCall = it)) }
                )
            }

            // Who can follow
            item {
                AudienceSelector(
                    title = "Who Can Follow Your Updates",
                    subtitle = "Require approval or limit subscription requests",
                    current = privacySettings.whoCanFollow,
                    onSelected = { updateSettings(privacySettings.copy(whoCanFollow = it)) }
                )
            }

            item {
                Text("Activity & Mentions", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                                Text("Show Activity Status", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Allow accounts you follow to see when you were last active.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = privacySettings.showActivityStatus,
                                onCheckedChange = { updateSettings(privacySettings.copy(showActivityStatus = it)) }
                            )
                        }

                        Divider(color = HealthogramTheme.colors.borderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow Mentions & Tags", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Allow other verified doctors or friends to tag your profile.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Switch(
                                checked = privacySettings.allowTagging,
                                onCheckedChange = { updateSettings(privacySettings.copy(allowTagging = it)) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun AudienceSelector(
    title: String,
    subtitle: String,
    current: PrivacyAudience,
    onSelected: (PrivacyAudience) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    HealthogramBasicCard {
        Column {
            Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = HealthogramTheme.shapes.small,
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(current.displayName, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    PrivacyAudience.values().forEach { aud ->
                        DropdownMenuItem(
                            text = { Text(aud.displayName) },
                            onClick = {
                                onSelected(aud)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
