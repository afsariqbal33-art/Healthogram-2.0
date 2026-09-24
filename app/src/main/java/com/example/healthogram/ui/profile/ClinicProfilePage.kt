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
 * Specialized Clinic Profile Page (Section 65).
 * Highlights outpatient multi-specialty services, facility hours, resident doctors, and direct appointment requests.
 */
@Composable
fun ClinicProfilePage(
    clinicId: String,
    currentViewerUid: String?,
    onBack: () -> Unit,
    onBookAppointment: () -> Unit = {},
    onManageClinic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var orgProfile by remember { mutableStateOf<OrganizationProfile?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Services") }
    val tabs = listOf("Services", "Doctors", "Facility Info", "Posts")

    LaunchedEffect(clinicId) {
        orgProfile = repository.getOrganizationProfile(clinicId)
        if (currentViewerUid != null) {
            isFollowing = repository.isFollowing(currentViewerUid, clinicId)
        }
    }

    val isOwner = currentViewerUid == orgProfile?.ownerUid

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("clinic_profile_page")
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
                    text = orgProfile?.name ?: "Clinic Profile",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Clinic", tint = HealthogramTheme.colors.textPrimary)
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
            // Clinic Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(displayName = orgProfile?.name ?: "Clinic", size = 80.dp)
                    ProfileStats(
                        postsCount = 18L,
                        followersCount = orgProfile?.followersCount ?: 0L,
                        followingCount = 42L,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Clinic Name & Badges
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = orgProfile?.name ?: "Healthcare Clinic",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HealthogramVerifiedBadge(
                            status = orgProfile?.verificationStatus ?: VerificationStatus.NOT_STARTED,
                            size = BadgeSize.MEDIUM
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AccountTypeBadge(accountType = AccountType.CLINIC)
                        Text(
                            text = "• ${orgProfile?.city}, ${orgProfile?.countryCode}",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
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
                            text = "Clinic Management Console",
                            onClick = onManageClinic,
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
                                            repository.unfollowUser(currentViewerUid, clinicId)
                                            isFollowing = false
                                        } else {
                                            repository.followUser(currentViewerUid, clinicId)
                                            isFollowing = true
                                        }
                                        orgProfile = repository.getOrganizationProfile(clinicId)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BookAppointmentButton(onClick = onBookAppointment, modifier = Modifier.weight(1.4f))
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
                "Services" -> {
                    val services = orgProfile?.services ?: emptyList()
                    if (services.isEmpty()) {
                        item {
                            EmptyProfileState(
                                icon = Icons.Default.LocalHospital,
                                title = "Services Updating",
                                message = "Clinical outpatient services will be displayed here."
                            )
                        }
                    } else {
                        items(services) { srv ->
                            ServiceCard(service = srv, onBookService = onBookAppointment)
                        }
                    }
                }

                "Doctors" -> {
                    item {
                        Text("Resident Medical Staff", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    }
                    items(listOf(
                        Triple("Dr. Elena Rostova", "Cardiology Specialist", "Stanford Medicine Alumni"),
                        Triple("Dr. Marcus Vance", "Internal Medicine Chief", "Harvard Medical School"),
                        Triple("Dr. Sophia Morales", "Pediatric Medicine", "Johns Hopkins Medicine")
                    )) { (docName, spec, edu) ->
                        HealthogramBasicCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(docName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text(spec, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    Text(edu, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)
                                }
                                HealthogramPrimaryButton(text = "Book", onClick = onBookAppointment, size = ButtonSize.SMALL)
                            }
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
                        EmptyProfileState(
                            icon = Icons.Default.Feed,
                            title = "Clinic Updates",
                            message = "Health announcements and seasonal wellness updates."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
