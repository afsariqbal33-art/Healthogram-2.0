package com.example.healthogram.ui.profile

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
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.*
import com.example.healthogram.profile.model.OrganizationProfile
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Specialized Diagnostic Laboratory Profile Page (Section 65).
 * Showcases accredited test catalog, turnaround times, pricing, and specimen instructions.
 *
 * Strict Rule: Laboratory accounts manage diagnostic test catalogs and report delivery.
 * They DO NOT possess a personal health passport.
 */
@Composable
fun LaboratoryProfilePage(
    laboratoryId: String,
    currentViewerUid: String?,
    onBack: () -> Unit,
    onRequestTest: () -> Unit = {},
    onManageLab: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Test Catalog") }
    val tabs = listOf("Test Catalog", "Specimen Guidelines", "Facility Info", "Accreditations")

    LaunchedEffect(laboratoryId) {
        orgProfile = repository.getOrganizationProfile(laboratoryId)
        if (currentViewerUid != null) {
            isFollowing = repository.isFollowing(currentViewerUid, laboratoryId)
        }
    }

    val isOwner = currentViewerUid == orgProfile?.ownerUid

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("laboratory_profile_page")
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = HealthogramTheme.colors.textPrimary)
                }
                Text(
                    text = orgProfile?.name ?: "Laboratory Profile",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Laboratory", tint = HealthogramTheme.colors.textPrimary)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Lab Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(displayName = orgProfile?.name ?: "Lab", size = 80.dp)
                    ProfileStats(
                        postsCount = 12L,
                        followersCount = orgProfile?.followersCount ?: 0L,
                        followingCount = 19L,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name & Badges
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = orgProfile?.name ?: "Apex Precision Diagnostics",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HealthogramVerifiedBadge(
                            status = orgProfile?.verificationStatus ?: VerificationStatus.NOT_STARTED,
                            size = BadgeSize.MEDIUM
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AccountTypeBadge(accountType = AccountType.LABORATORY)
                        Text(
                            text = "• CAP Accredited Molecular Lab",
                            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.success
                        )
                    }

                    if (!orgProfile?.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = orgProfile!!.description, style = HealthogramTheme.typography.bodyMedium)
                    }
                }
            }

            // Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isOwner) {
                        HealthogramPrimaryButton(
                            text = "Laboratory Operations Console",
                            onClick = onManageLab,
                            icon = Icons.Default.Dashboard,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        FollowButton(
                            isFollowing = isFollowing,
                            onToggleFollow = {
                                scope.launch {
                                    if (currentViewerUid != null) {
                                        if (isFollowing) {
                                            repository.unfollowUser(currentViewerUid, laboratoryId)
                                            isFollowing = false
                                        } else {
                                            repository.followUser(currentViewerUid, laboratoryId)
                                            isFollowing = true
                                        }
                                        orgProfile = repository.getOrganizationProfile(laboratoryId)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onRequestTest,
                            modifier = Modifier
                                .weight(1.4f)
                                .defaultMinSize(minHeight = 44.dp),
                            shape = HealthogramTheme.shapes.pill,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HealthogramTheme.colors.primary,
                                contentColor = androidx.compose.ui.graphics.Color.White
                            )
                        ) {
                            Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Diagnostic Test", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                    containerColor = HealthogramTheme.colors.surface,
                    contentColor = HealthogramTheme.colors.primary
                ) {
                    tabs.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab, style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)) }
                        )
                    }
                }
            }

            when (selectedTab) {
                "Test Catalog" -> {
                    val tests = orgProfile?.testCatalog ?: emptyList()
                    items(tests) { test ->
                        HealthogramBasicCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(test.testName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Code: ${test.testCode} • Sample: ${test.sampleType}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text("Turnaround: ${test.standardTurnaroundHours}h • Fasting: ${if (test.requiresFasting) "Required" else "No"}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${test.currency} ${String.format("%.2f", test.price)}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HealthogramOutlineButton(text = "Order", onClick = onRequestTest, size = ButtonSize.SMALL)
                                }
                            }
                        }
                    }
                }

                "Specimen Guidelines" -> {
                    item {
                        HealthogramBasicCard {
                            Text("Specimen Handling & Collection Rules", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Whole blood specimens must be collected in K2-EDTA tubes and delivered within 24 hours.", style = HealthogramTheme.typography.bodySmall)
                            Text("• Serum separator tubes require centrifugation within 2 hours of phlebotomy.", style = HealthogramTheme.typography.bodySmall)
                            Text("• Molecular swab samples require viral transport medium (VTM) cold chain storage (2-8°C).", style = HealthogramTheme.typography.bodySmall)
                        }
                    }
                }

                "Facility Info" -> {
                    orgProfile?.let { org ->
                        item {
                            OrganizationInfoCard(
                                address = org.address,
                                phone = org.contactPhone,
                                email = org.contactEmail,
                                workingHours = org.workingHours
                            )
                        }
                    }
                }

                else -> {
                    item {
                        HealthogramBasicCard {
                            Text("Accreditation & Quality Standards", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("College of American Pathologists (CAP) #890214\nClinical Laboratory Improvement Amendments (CLIA) #22D9941029", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
