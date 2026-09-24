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
import com.example.healthogram.profile.model.ProfessionalProfile
import com.example.healthogram.profile.model.PublicProfile
import com.example.healthogram.profile.repository.ProfileRepository
import kotlinx.coroutines.launch

/**
 * Specialized Doctor Profile Page (Section 65).
 * Showcases verified medical credentials, board certifications, consultation fee,
 * direct booking, telehealth capability, and clinical service offerings.
 */
@Composable
fun DoctorProfilePage(
    doctorUid: String,
    currentViewerUid: String?,
    onBack: () -> Unit,
    onBookAppointment: () -> Unit = {},
    onMessage: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    val scope = rememberCoroutineScope()

    var publicProfile by remember { mutableStateOf<PublicProfile?>(null) }
    var professionalProfile by remember { mutableStateOf<ProfessionalProfile?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Services") }
    val tabs = listOf("Services", "Credentials", "Articles", "Reviews")

    LaunchedEffect(doctorUid) {
        publicProfile = repository.getPublicProfile(doctorUid)
        professionalProfile = repository.getProfessionalProfile(doctorUid)
        if (currentViewerUid != null) {
            isFollowing = repository.isFollowing(currentViewerUid, doctorUid)
        }
    }

    val isOwner = currentViewerUid == doctorUid

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("doctor_profile_page")
    ) {
        // Top App Bar
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
                    text = publicProfile?.username ?: "Doctor Profile",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { /* share */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share Doctor Profile", tint = HealthogramTheme.colors.textPrimary)
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
            // Profile Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileAvatar(displayName = publicProfile?.displayName ?: "Doctor", size = 80.dp)
                    ProfileStats(
                        postsCount = publicProfile?.postsCount ?: 0L,
                        followersCount = publicProfile?.followersCount ?: 0L,
                        followingCount = publicProfile?.followingCount ?: 0L,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Names & Badges
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = publicProfile?.displayName ?: "Dr. Specialist",
                            style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        HealthogramVerifiedBadge(
                            status = if (publicProfile?.isVerified == true) VerificationStatus.APPROVED else VerificationStatus.NOT_STARTED,
                            size = BadgeSize.MEDIUM
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AccountTypeBadge(accountType = AccountType.DOCTOR)
                        professionalProfile?.let {
                            Text(
                                text = "• ${it.professionalTitle}",
                                style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }

                    if (!publicProfile?.bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = publicProfile!!.bio, style = HealthogramTheme.typography.bodyMedium)
                    }
                }
            }

            // Consultation Fee Banner
            professionalProfile?.let { prof ->
                item {
                    Surface(
                        shape = HealthogramTheme.shapes.medium,
                        color = HealthogramTheme.colors.surfaceVariant,
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Standard Consultation", style = HealthogramTheme.typography.labelSmall, color = HealthogramTheme.colors.textMuted)
                                Text("${prof.currency} ${String.format("%.2f", prof.defaultConsultationFee)}", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                            }
                            if (prof.onlineConsultationEnabled) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, tint = HealthogramTheme.colors.info, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Telehealth Active", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.info)
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isOwner) {
                        FollowButton(
                            isFollowing = isFollowing,
                            onToggleFollow = {
                                scope.launch {
                                    if (currentViewerUid != null) {
                                        if (isFollowing) {
                                            repository.unfollowUser(currentViewerUid, doctorUid)
                                            isFollowing = false
                                        } else {
                                            repository.followUser(currentViewerUid, doctorUid)
                                            isFollowing = true
                                        }
                                        publicProfile = repository.getPublicProfile(doctorUid)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BookAppointmentButton(onClick = onBookAppointment, modifier = Modifier.weight(1.4f))
                        MessageButton(onClick = onMessage)
                    } else {
                        HealthogramOutlineButton(
                            text = "Edit Doctor Profile",
                            onClick = { /* edit */ },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Content Tabs
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
                    val services = professionalProfile?.services ?: emptyList()
                    if (services.isEmpty()) {
                        item {
                            EmptyProfileState(
                                icon = Icons.Default.MedicalServices,
                                title = "No Services Listed",
                                message = "Dr. Elena Rostova provides tailored clinical consults upon scheduling."
                            )
                        }
                    } else {
                        items(services) { srv ->
                            ServiceCard(service = srv, onBookService = onBookAppointment)
                        }
                    }
                }

                "Credentials" -> {
                    professionalProfile?.let { prof ->
                        item {
                            ProfessionalInfoCard(
                                specializations = prof.specializations,
                                education = prof.education,
                                experienceYears = prof.experienceYears,
                                languages = prof.languages,
                                businessHours = prof.businessHours
                            )
                        }
                    }
                }

                else -> {
                    item {
                        EmptyProfileState(
                            icon = Icons.Default.Article,
                            title = "Medical Publications",
                            message = "Peer-reviewed publications and clinical guidance will display here."
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
