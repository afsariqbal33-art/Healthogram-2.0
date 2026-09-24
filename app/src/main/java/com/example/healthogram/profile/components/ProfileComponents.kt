package com.example.healthogram.profile.components

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.model.ProfessionalServiceItem
import com.example.healthogram.profile.permissions.FeatureKey
import com.example.healthogram.profile.permissions.FeaturePermissionEngine
import com.example.healthogram.profile.permissions.PermissionResult

/**
 * Visual Account Type Badge.
 * Distinct from VerifiedBadge to avoid any user confusion.
 */
@Composable
fun AccountTypeBadge(
    accountType: AccountType,
    modifier: Modifier = Modifier,
    isProfessionalCreator: Boolean = false,
    testTag: String = "account_type_badge"
) {
    val (label, icon, containerColor, contentColor) = when {
        accountType == AccountType.INDIVIDUAL && isProfessionalCreator -> {
            Quadruple(
                "Creator",
                Icons.Default.AutoAwesome,
                HealthogramTheme.colors.primaryContainer,
                HealthogramTheme.colors.primary
            )
        }
        accountType == AccountType.INDIVIDUAL -> {
            Quadruple(
                "Individual",
                Icons.Default.Person,
                HealthogramTheme.colors.surfaceVariant,
                HealthogramTheme.colors.textSecondary
            )
        }
        accountType == AccountType.DOCTOR -> {
            Quadruple(
                "Doctor",
                Icons.Default.MedicalServices,
                HealthogramTheme.colors.primaryContainer,
                HealthogramTheme.colors.primary
            )
        }
        accountType == AccountType.CLINIC -> {
            Quadruple(
                "Clinic",
                Icons.Default.LocalHospital,
                HealthogramTheme.colors.infoContainer,
                HealthogramTheme.colors.info
            )
        }
        accountType == AccountType.HOSPITAL -> {
            Quadruple(
                "Hospital",
                Icons.Default.Apartment,
                HealthogramTheme.colors.warningContainer,
                HealthogramTheme.colors.warning
            )
        }
        accountType == AccountType.LABORATORY -> {
            Quadruple(
                "Laboratory",
                Icons.Default.Biotech,
                HealthogramTheme.colors.successContainer,
                HealthogramTheme.colors.success
            )
        }
        else -> {
            Quadruple(
                "Individual",
                Icons.Default.Person,
                HealthogramTheme.colors.surfaceVariant,
                HealthogramTheme.colors.textSecondary
            )
        }
    }

    Surface(
        modifier = modifier.testTag(testTag),
        shape = HealthogramTheme.shapes.pill,
        color = containerColor,
        border = BorderStroke(0.5.dp, contentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = contentColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Modern Profile Avatar with dynamic gradient ring.
 */
@Composable
fun ProfileAvatar(
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    hasActiveStory: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val initial = (displayName.firstOrNull() ?: 'H').uppercaseChar().toString()
    val ringPadding = if (hasActiveStory) 3.dp else 0.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (hasActiveStory) {
                    Modifier
                        .background(HealthogramTheme.colors.storyGradient)
                        .padding(ringPadding)
                } else Modifier
            )
            .clip(CircleShape)
            .background(HealthogramTheme.colors.surface)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HealthogramTheme.colors.primary,
                            HealthogramTheme.colors.secondary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = HealthogramTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.42).sp
                ),
                color = Color.White
            )
        }
    }
}

/**
 * Scannable Profile Stats Counters (Posts, Followers, Following).
 */
@Composable
fun ProfileStats(
    postsCount: Long,
    followersCount: Long,
    followingCount: Long,
    modifier: Modifier = Modifier,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(count = formatStatCount(postsCount), label = "Posts")
        StatItem(count = formatStatCount(followersCount), label = "Followers", onClick = onFollowersClick)
        StatItem(count = formatStatCount(followingCount), label = "Following", onClick = onFollowingClick)
    }
}

@Composable
private fun StatItem(
    count: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary
        )
        Text(
            text = label,
            style = HealthogramTheme.typography.caption,
            color = HealthogramTheme.colors.textMuted
        )
    }
}

private fun formatStatCount(count: Long): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

/**
 * Dynamic Follow / Unfollow Button with smooth state transition.
 */
@Composable
fun FollowButton(
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AnimatedContent(
        targetState = isFollowing,
        transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
        label = "follow_transition"
    ) { following ->
        if (following) {
            OutlinedButton(
                onClick = onToggleFollow,
                enabled = enabled,
                modifier = modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("unfollow_button"),
                shape = HealthogramTheme.shapes.pill,
                border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = HealthogramTheme.colors.textPrimary
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Following", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        } else {
            Button(
                onClick = onToggleFollow,
                enabled = enabled,
                modifier = modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("follow_button"),
                shape = HealthogramTheme.shapes.pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthogramTheme.colors.primary,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Follow", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

/**
 * Direct Message Button.
 */
@Composable
fun MessageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .testTag("message_button"),
        shape = HealthogramTheme.shapes.pill,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HealthogramTheme.colors.textPrimary
        )
    ) {
        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Message", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Message", style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

/**
 * Audio Call Button.
 */
@Composable
fun AudioCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(HealthogramTheme.colors.surfaceVariant)
            .testTag("audio_call_button")
    ) {
        Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Audio Call",
            tint = HealthogramTheme.colors.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Video Call Button.
 */
@Composable
fun VideoCallButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(HealthogramTheme.colors.surfaceVariant)
            .testTag("video_call_button")
    ) {
        Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = "Video Call",
            tint = HealthogramTheme.colors.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Book Appointment Button for Doctors, Clinics, Hospitals.
 */
@Composable
fun BookAppointmentButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Book Appointment"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .testTag("book_appointment_button"),
        shape = HealthogramTheme.shapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthogramTheme.colors.primary,
            contentColor = Color.White
        )
    ) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

/**
 * Service Card displaying individual medical or clinical service offerings.
 */
@Composable
fun ServiceCard(
    service: ProfessionalServiceItem,
    onBookService: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}"),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.title,
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    if (service.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = service.description,
                            style = HealthogramTheme.typography.bodySmall,
                            color = HealthogramTheme.colors.textMuted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${service.currency} ${String.format("%.2f", service.fee)}",
                        style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.primary
                    )
                    Text(
                        text = "${service.durationMinutes} mins",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (service.isTelehealthAvailable) {
                    Surface(
                        shape = HealthogramTheme.shapes.pill,
                        color = HealthogramTheme.colors.infoContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = HealthogramTheme.colors.info, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Online Available", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.info)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }

                TextButton(
                    onClick = onBookService,
                    modifier = Modifier.defaultMinSize(minHeight = 40.dp)
                ) {
                    Text("Select & Schedule", style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

/**
 * Professional Credentials Card (Specialty, Education, Licenses, Business Hours).
 */
@Composable
fun ProfessionalInfoCard(
    specializations: List<String>,
    education: List<String>,
    experienceYears: Int,
    languages: List<String>,
    businessHours: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Credentials & Practice Info", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

            if (specializations.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Specializations", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                        Text(specializations.joinToString(" • "), style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            if (education.isNotEmpty()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.School, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Education & Training", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold))
                        Text(education.joinToString("\n"), style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkHistory, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$experienceYears years clinical experience", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Translate, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Languages: ${languages.joinToString(", ")}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hours: $businessHours", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            }
        }
    }
}

/**
 * Organization Info Card (Address, Departments, Contact).
 */
@Composable
fun OrganizationInfoCard(
    address: String,
    phone: String,
    email: String,
    workingHours: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.surface,
        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Facility & Contact Details", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

            if (address.isNotBlank()) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(address, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                }
            }

            if (phone.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(phone, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                }
            }

            if (email.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(email, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(workingHours, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            }
        }
    }
}

/**
 * Reusable Loading Skeleton for Profile pages.
 */
@Composable
fun ProfileSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.surfaceVariant)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(width = 40.dp, height = 18.dp).clip(HealthogramTheme.shapes.small).background(HealthogramTheme.colors.surfaceVariant))
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.size(width = 50.dp, height = 12.dp).clip(HealthogramTheme.shapes.small).background(HealthogramTheme.colors.surfaceVariant))
                    }
                }
            }
        }

        Box(modifier = Modifier.size(width = 160.dp, height = 20.dp).clip(HealthogramTheme.shapes.small).background(HealthogramTheme.colors.surfaceVariant))
        Box(modifier = Modifier.size(width = 90.dp, height = 16.dp).clip(HealthogramTheme.shapes.small).background(HealthogramTheme.colors.surfaceVariant))
        Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(HealthogramTheme.shapes.small).background(HealthogramTheme.colors.surfaceVariant))
        Box(modifier = Modifier.fillMaxWidth().height(44.dp).clip(HealthogramTheme.shapes.pill).background(HealthogramTheme.colors.surfaceVariant))
    }
}

/**
 * Reusable Empty State Component with icon and helpful message.
 */
@Composable
fun EmptyProfileState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = HealthogramTheme.colors.surfaceVariant,
            modifier = Modifier.size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.textMuted,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = title,
            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = HealthogramTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = HealthogramTheme.typography.bodySmall,
            color = HealthogramTheme.colors.textMuted,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HealthogramPrimaryButton(
                text = actionLabel,
                onClick = onAction,
                size = ButtonSize.SMALL
            )
        }
    }
}

/**
 * Permission Guard Composable: conditionally renders content or fallback when access is denied.
 */
@Composable
fun PermissionGuard(
    feature: FeatureKey,
    user: User?,
    isVerified: Boolean = user?.isVerified ?: false,
    scannerPermission: Boolean = false,
    content: @Composable () -> Unit,
    fallback: @Composable (String) -> Unit = { reason ->
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = HealthogramTheme.shapes.medium,
            color = HealthogramTheme.colors.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Access Restricted", tint = HealthogramTheme.colors.warning)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = reason,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textPrimary
                )
            }
        }
    }
) {
    val result = remember(feature, user, isVerified, scannerPermission) {
        FeaturePermissionEngine.checkPermission(
            feature = feature,
            user = user,
            isVerified = isVerified,
            scannerPermission = scannerPermission
        )
    }

    if (result.isGranted) {
        content()
    } else {
        fallback(result.reason ?: "Access denied")
    }
}
