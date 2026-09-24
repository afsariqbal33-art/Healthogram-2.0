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
 * Specialized Hospital Profile Page (Section 65).
 * Showcases clinical departments, 24/7 emergency readiness, resident surgeons, and facility info.
 */
@Composable
fun HospitalProfilePage(
    hospitalId: String,
    currentViewerUid: String?,
    onBack: () -> Unit,
    onBookAppointment: () -> Unit = {},
    onManageHospital: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Departments") }
    val tabs = listOf("Departments", "Services", "Emergency Info", "Doctors")

    LaunchedEffect(hospitalId) {
        orgProfile = repository.getOrganizationProfile(hospitalId)
        if (currentViewerUid != null) {
            isFollowing = repository.isFollowing(currentViewerUid, hospitalId)
        }
    }

    val isOwner = currentViewerUid == orgProfile?.ownerUid

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("hospital_profile_page")
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
                    text = orgProfile?.name ?: "Hospital Profile",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Hospital", tint = HealthogramTheme.colors.textPrimary)
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
            // Hospital Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(displayName = orgProfile?.name ?: "Hospital", size = 80.dp)
                    ProfileStats(
                        postsCount = 42L,
                        followersCount = orgProfile?.followersCount ?: 0L,
                        followingCount = 68L,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Name, Verification, Account Badge
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = orgProfile?.name ?: "Metropolitan Central Hospital",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HealthogramVerifiedBadge(
                            status = orgProfile?.verificationStatus ?: VerificationStatus.NOT_STARTED,
                            size = BadgeSize.MEDIUM
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AccountTypeBadge(accountType = AccountType.HOSPITAL)
                        Text(
                            text = "• Level 1 Trauma Care",
                            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.warning
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
                            text = "Hospital Management Console",
                            onClick = onManageHospital,
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
                                            repository.unfollowUser(currentViewerUid, hospitalId)
                                            isFollowing = false
                                        } else {
                                            repository.followUser(currentViewerUid, hospitalId)
                                            isFollowing = true
                                        }
                                        orgProfile = repository.getOrganizationProfile(hospitalId)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BookAppointmentButton(onClick = onBookAppointment, label = "Outpatient Booking", modifier = Modifier.weight(1.4f))
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
                "Departments" -> {
                    val depts = orgProfile?.departments ?: emptyList()
                    items(depts) { dept ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dept.name, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Head: ${dept.headDoctorName} • ${dept.activeDoctorCount} Doctors", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                }
                                HealthogramOutlineButton(text = "View Wing", onClick = onBookAppointment, size = ButtonSize.SMALL)
                            }
                        }
                    }
                }

                "Services" -> {
                    items(orgProfile?.services ?: emptyList()) { srv ->
                        ServiceCard(service = srv, onBookService = onBookAppointment)
                    }
                }

                "Emergency Info" -> {
                    orgProfile?.let { org ->
                        item {
                            Surface(
                                shape = HealthogramTheme.shapes.medium,
                                color = HealthogramTheme.colors.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Emergency, contentDescription = null, tint = HealthogramTheme.colors.error)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("24/7 Emergency & Acute Trauma", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Immediate triage hotline: ${org.contactPhone}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.error)
                                }
                            }
                        }
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
                        EmptyProfileState(
                            icon = Icons.Default.MedicalServices,
                            title = "Physicians & Specialists",
                            message = "Browse credentialed hospital physicians by department."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
