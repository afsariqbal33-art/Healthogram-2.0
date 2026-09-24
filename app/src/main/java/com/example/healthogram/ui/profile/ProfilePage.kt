package com.example.healthogram.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.components.AccountTypeBadge
import com.example.healthogram.profile.components.ProfileAvatar
import com.example.healthogram.profile.components.ProfileStats
import com.example.healthogram.profile.model.PublicProfile
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Healthogram Authoritative Role-Aware Profile Page (Step 04).
 *
 * Supports all 5 permitted account types:
 * 1. Individual
 * 2. Doctor
 * 3. Clinic
 * 4. Hospital
 * 5. Laboratory
 *
 * Enforces:
 * - Strict separation of public profile data from private records.
 * - Role-specific navigation cards and management links.
 * - Distinct Verified Badge vs Account Type Badge.
 * - Interactive Role-Switcher for instantaneous architectural inspection.
 */
@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    user: User? = null,
    accountType: AccountType = user?.accountType ?: AccountType.INDIVIDUAL,
    onSettingsClick: () -> Unit = {},
    onProfessionalDashboardClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onCommunicationClick: () -> Unit = {},
    onVerificationClick: () -> Unit = {},
    onClinicDashboardClick: () -> Unit = {},
    onHospitalDashboardClick: () -> Unit = {},
    onLaboratoryDashboardClick: () -> Unit = {},
    onDoctorViewClick: (String) -> Unit = {},
    onClinicViewClick: (String) -> Unit = {},
    onHospitalViewClick: (String) -> Unit = {},
    onLaboratoryViewClick: (String) -> Unit = {},
    onManageMembersClick: (String) -> Unit = {},
    onManageDevicesClick: (String) -> Unit = {},
    onAppointmentsClick: () -> Unit = {}
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    // State for dynamic inspection of all 5 allowed account types
    var activePreviewType by remember(accountType) { mutableStateOf(accountType) }
    var isProfessionalCreator by remember { mutableStateOf(user?.isProfessional ?: false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var publicProfile by remember { mutableStateOf<PublicProfile?>(null) }

    LaunchedEffect(user?.uid, activePreviewType) {
        val uid = when (activePreviewType) {
            AccountType.DOCTOR -> "user_doctor_demo"
            AccountType.CLINIC -> "org_clinic_demo"
            AccountType.HOSPITAL -> "org_hospital_demo"
            AccountType.LABORATORY -> "org_lab_demo"
            AccountType.INDIVIDUAL -> user?.uid ?: "user_individual_demo"
        }
        val p = repository.getPublicProfile(uid)
        publicProfile = p ?: PublicProfile(
            uid = uid,
            username = user?.username ?: "alex_mercer",
            displayName = user?.displayName ?: "Alex Mercer",
            bio = user?.bio ?: "Marathon runner & health enthusiast.",
            accountType = activePreviewType,
            isVerified = user?.isVerified ?: true,
            followersCount = 2430L,
            followingCount = 318L,
            postsCount = 48L
        )
    }

    val displayUsername = publicProfile?.username ?: user?.username ?: "alex_mercer"
    val displayName = publicProfile?.displayName ?: user?.displayName ?: "Alex Mercer"
    val displayBio = publicProfile?.bio ?: "Healthogram certified account."
    val isVerified = publicProfile?.isVerified ?: (user?.isVerified ?: true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("profile_page")
    ) {
        // Top Action Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HealthogramTheme.colors.surface,
            border = BorderStroke(0.5.dp, HealthogramTheme.colors.borderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayUsername,
                        style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    HealthogramVerifiedBadge(
                        status = if (isVerified) VerificationStatus.APPROVED else VerificationStatus.NOT_STARTED,
                        size = BadgeSize.SMALL
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrivacyClick) {
                        Icon(Icons.Outlined.Shield, contentDescription = "Privacy & Safety", tint = HealthogramTheme.colors.textPrimary)
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = HealthogramTheme.colors.textPrimary)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Role Switcher Pill Bar (Section 65 Inspection Aid)
            item {
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            AccountType.INDIVIDUAL,
                            AccountType.DOCTOR,
                            AccountType.CLINIC,
                            AccountType.HOSPITAL,
                            AccountType.LABORATORY
                        ).forEach { type ->
                            val isSelected = activePreviewType == type
                            Surface(
                                shape = HealthogramTheme.shapes.pill,
                                color = if (isSelected) HealthogramTheme.colors.primary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { activePreviewType = type }
                                    .padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = type.displayName.take(4),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else HealthogramTheme.colors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            // Profile Header Area
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileAvatar(
                        displayName = displayName,
                        size = 76.dp,
                        hasActiveStory = true
                    )
                    ProfileStats(
                        postsCount = publicProfile?.postsCount ?: 48L,
                        followersCount = publicProfile?.followersCount ?: 2430L,
                        followingCount = publicProfile?.followingCount ?: 318L,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Names, Account Badge & Bio
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = displayName,
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AccountTypeBadge(
                            accountType = activePreviewType,
                            isProfessionalCreator = isProfessionalCreator
                        )
                        if (publicProfile?.city?.isNotBlank() == true) {
                            Text(
                                text = "• ${publicProfile?.city}, ${publicProfile?.countryCode}",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayBio,
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.textPrimary
                    )

                    if (!publicProfile?.website.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = publicProfile!!.website,
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                color = HealthogramTheme.colors.primary
                            )
                        }
                    }
                }
            }

            // Quick Profile Actions (Edit Profile / Share Profile / View Specialized Profile)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthogramOutlineButton(
                        text = "Edit Profile",
                        onClick = onEditProfileClick,
                        modifier = Modifier.weight(1f),
                        size = ButtonSize.SMALL
                    )
                    when (activePreviewType) {
                        AccountType.DOCTOR -> {
                            HealthogramPrimaryButton(
                                text = "Doctor View",
                                onClick = { onDoctorViewClick("user_doctor_demo") },
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                        AccountType.CLINIC -> {
                            HealthogramPrimaryButton(
                                text = "Clinic View",
                                onClick = { onClinicViewClick("org_clinic_demo") },
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                        AccountType.HOSPITAL -> {
                            HealthogramPrimaryButton(
                                text = "Hospital View",
                                onClick = { onHospitalViewClick("org_hospital_demo") },
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                        AccountType.LABORATORY -> {
                            HealthogramPrimaryButton(
                                text = "Lab View",
                                onClick = { onLaboratoryViewClick("org_lab_demo") },
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                        AccountType.INDIVIDUAL -> {
                            HealthogramOutlineButton(
                                text = "Share Profile",
                                onClick = { /* share profile */ },
                                modifier = Modifier.weight(1f),
                                size = ButtonSize.SMALL
                            )
                        }
                    }
                }
            }

            // Role-Specific Management Entry Banners (Sections 11, 16, 18, 20)
            when (activePreviewType) {
                AccountType.INDIVIDUAL -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppointmentsClick() }
                                .testTag("profile_appointments_banner"),
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.surface,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("My Clinical Appointments", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("View upcoming visits, check in & join telehealth", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProfessionalDashboardClick() },
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.surfaceVariant,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Creator & Professional Dashboard", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                        Text("Audience insights, broadcast tools & AI Studio", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted)
                            }
                        }
                    }
                }

                AccountType.DOCTOR -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProfessionalDashboardClick() },
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.primaryContainer,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Doctor Practice Dashboard", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.onPrimaryContainer)
                                        Text("Consultation rates, patient appointments & scanner", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.onPrimaryContainer.copy(alpha = 0.8f))
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.primary)
                            }
                        }
                    }
                }

                AccountType.CLINIC -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClinicDashboardClick() },
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.infoContainer,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.info.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocalHospital, contentDescription = null, tint = HealthogramTheme.colors.info, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Clinic Management Console", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.info)
                                        Text("Manage 16 resident specialists, terminals & triage", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.info)
                            }
                        }
                    }
                }

                AccountType.HOSPITAL -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onHospitalDashboardClick() },
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.warningContainer,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.warning.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = HealthogramTheme.colors.warning, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Hospital Center Console", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.warning)
                                        Text("4 clinical wings, Level 1 trauma & 8 terminal stations", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.warning)
                            }
                        }
                    }
                }

                AccountType.LABORATORY -> {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLaboratoryDashboardClick() },
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.successContainer,
                            border = BorderStroke(1.dp, HealthogramTheme.colors.success.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Biotech, contentDescription = null, tint = HealthogramTheme.colors.success, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Diagnostic Laboratory Console", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.success)
                                        Text("Diagnostic test catalog, sample turnaround & lab staff", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.success)
                            }
                        }
                    }
                }
            }

            // Quick Settings Jump Links
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickLinkButton(
                        label = "Privacy",
                        icon = Icons.Outlined.Shield,
                        onClick = onPrivacyClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickLinkButton(
                        label = "Calls & Comms",
                        icon = Icons.Outlined.Call,
                        onClick = onCommunicationClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickLinkButton(
                        label = "Verification",
                        icon = Icons.Outlined.Verified,
                        onClick = onVerificationClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Media & Tab Navigation
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = HealthogramTheme.colors.surface,
                    contentColor = HealthogramTheme.colors.primary
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, icon = { Icon(Icons.Default.Movie, contentDescription = "Reels") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved") })
                }
            }

            // Media Grid Items
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(3) { col ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(HealthogramTheme.colors.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == 1) Icons.Default.Movie else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = HealthogramTheme.colors.textMuted.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun QuickLinkButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 44.dp),
        shape = HealthogramTheme.shapes.small,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
