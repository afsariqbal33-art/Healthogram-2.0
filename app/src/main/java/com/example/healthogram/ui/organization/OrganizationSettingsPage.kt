package com.example.healthogram.ui.organization

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
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.model.OrganizationProfile
import com.example.healthogram.profile.repository.ProfileRepository

/**
 * Organization Facility & Subscription Settings Page (Section 65).
 */
@Composable
fun OrganizationSettingsPage(
    orgId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }

    LaunchedEffect(orgId) {
        orgProfile = repository.getOrganizationProfile(orgId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("organization_settings_page")
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
                    text = "Organization Settings",
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
                Text("Subscription & Capacity Tier", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(orgProfile?.subscriptionPlan ?: "Standard 4-Device Plan", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("Maximum Concurrent Active Devices: ${orgProfile?.maxDevices ?: 4}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                            }
                            Surface(shape = HealthogramTheme.shapes.pill, color = HealthogramTheme.colors.primaryContainer) {
                                Text("ACTIVE", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                        }
                        Text(
                            "Upgrade to Enterprise Tier to support up to 8 hardware terminals across emergency and outpatient units.",
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }
            }

            item {
                Text("Facility Registry & Compliance", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Facility Verification: ${orgProfile?.verificationStatus ?: "Approved"}", style = HealthogramTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Address: ${orgProfile?.address ?: "Chicago, IL"}", style = HealthogramTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emergency Contact: ${orgProfile?.contactPhone ?: "+1 312 555 0100"}", style = HealthogramTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
