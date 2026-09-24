package com.example.healthogram.ui.verification.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.verification.*

// =============================================================================
// 1. REUSABLE VERIFIED BADGE (STEP 07 SECTION 19)
// =============================================================================

enum class VerifiedBadgeSize(val iconSize: Dp, val checkSize: Dp) {
    SMALL(16.dp, 10.dp),
    MEDIUM(20.dp, 12.dp),
    LARGE(26.dp, 16.dp)
}

/**
 * Healthogram Official Verified Badge.
 * Features Healthogram's distinct aesthetic with tap-to-learn verification disclosure.
 */
@Composable
fun VerifiedBadge(
    status: VerificationStatus,
    badgeType: VerificationBadgeType = VerificationBadgeType.CYAN_CHECK,
    size: VerifiedBadgeSize = VerifiedBadgeSize.MEDIUM,
    showLabel: Boolean = false,
    enableTapModal: Boolean = true,
    modifier: Modifier = Modifier,
    testTag: String = "verified_badge"
) {
    var showInfoModal by remember { mutableStateOf(false) }

    // Rule: Do NOT show a badge if verification_status != verified
    if (!status.isVerifiedState && status != VerificationStatus.UNDER_REVIEW && status != VerificationStatus.EXPIRED && status != VerificationStatus.SUSPENDED) {
        return
    }

    val badgeColor = when {
        status.isVerifiedState -> when (badgeType) {
            VerificationBadgeType.GOLD_SHIELD -> Color(0xFFD97706)
            VerificationBadgeType.GREEN_CROSS -> Color(0xFF059669)
            else -> HealthogramTheme.colors.verifiedBadgeColor // Cyan / Brand Teal
        }
        status == VerificationStatus.UNDER_REVIEW || status == VerificationStatus.SUBMITTED -> HealthogramTheme.colors.warning
        status == VerificationStatus.EXPIRED || status == VerificationStatus.SUSPENDED -> HealthogramTheme.colors.error
        else -> Color.Gray
    }

    val iconVector: ImageVector = when {
        status.isVerifiedState -> when (badgeType) {
            VerificationBadgeType.GOLD_SHIELD -> Icons.Default.Shield
            VerificationBadgeType.GREEN_CROSS -> Icons.Default.Add
            else -> Icons.Default.Check
        }
        status == VerificationStatus.UNDER_REVIEW || status == VerificationStatus.SUBMITTED -> Icons.Default.HourglassTop
        else -> Icons.Default.Warning
    }

    Row(
        modifier = modifier
            .testTag(testTag)
            .clickable(enabled = enableTapModal) { showInfoModal = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size.iconSize)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = "Verified by Healthogram",
                tint = Color.White,
                modifier = Modifier.size(size.checkSize)
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (status.isVerifiedState) "Verified" else VerificationFunctions.getVerificationStatusLabel(status),
                style = HealthogramTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = badgeColor
            )
        }
    }

    if (showInfoModal) {
        VerificationBadgeInfoModal(
            status = status,
            onDismiss = { showInfoModal = false }
        )
    }
}

// =============================================================================
// 2. VERIFICATION BADGE INFO MODAL (STEP 07 SECTION 2)
// =============================================================================

@Composable
fun VerificationBadgeInfoModal(
    status: VerificationStatus,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Verified by Healthogram",
                    style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "This account has passed Healthogram's platform verification process based on the information and documents submitted to the platform.",
                    style = HealthogramTheme.typography.bodyMedium,
                    color = HealthogramTheme.colors.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = HealthogramTheme.colors.divider)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "What this badge does NOT mean:",
                    style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("• Government endorsement or licensing replacement", style = HealthogramTheme.typography.bodySmall)
                    Text("• Medical guarantee or diagnostic accuracy promise", style = HealthogramTheme.typography.bodySmall)
                    Text("• Professional competence or legal certification", style = HealthogramTheme.typography.bodySmall)
                    Text("• Medical advice approval or drug safety guarantee", style = HealthogramTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = HealthogramTheme.colors.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Healthogram verification does not replace government licensing or regulatory approval.",
                        style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = HealthogramTheme.colors.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_badge_modal_button")
            ) {
                Text("Understood", fontWeight = FontWeight.Bold)
            }
        }
    )
}

// =============================================================================
// 3. VERIFICATION STATUS CARD
// =============================================================================

@Composable
fun VerificationStatusCard(
    profile: VerificationProfile?,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = profile?.verificationStatus ?: VerificationStatus.NOT_STARTED
    val containerColor = when (status) {
        VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> HealthogramTheme.colors.successContainer
        VerificationStatus.SUBMITTED, VerificationStatus.UNDER_REVIEW -> HealthogramTheme.colors.warningContainer
        VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> HealthogramTheme.colors.warningContainer
        VerificationStatus.REJECTED, VerificationStatus.EXPIRED, VerificationStatus.SUSPENDED, VerificationStatus.REVOKED -> HealthogramTheme.colors.errorContainer
        else -> HealthogramTheme.colors.surfaceVariant
    }

    val iconVector = when (status) {
        VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> Icons.Default.Verified
        VerificationStatus.SUBMITTED, VerificationStatus.UNDER_REVIEW -> Icons.Default.HourglassTop
        VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> Icons.Default.HelpCenter
        VerificationStatus.REJECTED -> Icons.Default.Cancel
        VerificationStatus.EXPIRED -> Icons.Default.Alarm
        VerificationStatus.SUSPENDED, VerificationStatus.REVOKED -> Icons.Default.Block
        else -> Icons.Default.Shield
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("verification_status_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = HealthogramTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = VerificationFunctions.getVerificationStatusLabel(status),
                        style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                }

                if (profile?.verificationExpiresAt != null && profile.verificationExpiresAt > 0) {
                    Surface(
                        color = HealthogramTheme.colors.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = VerificationFunctions.getVerificationExpiryMessage(profile.verificationExpiresAt),
                            style = HealthogramTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = HealthogramTheme.colors.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = VerificationFunctions.getVerificationStatusMessage(status),
                style = HealthogramTheme.typography.bodyMedium,
                color = HealthogramTheme.colors.onSurface.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("verification_card_action_button")
            ) {
                val actionText = when (status) {
                    VerificationStatus.NOT_STARTED -> "Start Verification"
                    VerificationStatus.DRAFT -> "Continue Draft"
                    VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> "Provide Missing Information"
                    VerificationStatus.EXPIRED -> "Renew Verification"
                    VerificationStatus.REJECTED -> "View Rejection Details & Reapply"
                    VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> "View Verification Details"
                    else -> "View Application Status"
                }
                Text(actionText, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// =============================================================================
// 4. VERIFICATION PROGRESS CARD (STEP 07 SECTION 10)
// =============================================================================

@Composable
fun VerificationProgressCard(
    currentStep: Int,
    totalSteps: Int = 5,
    modifier: Modifier = Modifier
) {
    val stepTitles = listOf(
        "Account Info",
        "Identity / Org",
        "Documents",
        "Review",
        "Decision"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("verification_progress_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Application Progress",
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = HealthogramTheme.typography.labelSmall,
                    color = HealthogramTheme.colors.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = HealthogramTheme.colors.primary,
                trackColor = HealthogramTheme.colors.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stepTitles.forEachIndexed { index, title ->
                    val stepNum = index + 1
                    val isDone = stepNum < currentStep
                    val isCurrent = stepNum == currentStep
                    val color = when {
                        isCurrent -> HealthogramTheme.colors.primary
                        isDone -> HealthogramTheme.colors.success
                        else -> HealthogramTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(if (isDone || isCurrent) color else HealthogramTheme.colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            } else {
                                Text(
                                    text = "$stepNum",
                                    style = HealthogramTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = if (isCurrent) Color.White else HealthogramTheme.colors.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = title,
                            style = HealthogramTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = color,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 5. REQUIREMENT CARD
// =============================================================================

@Composable
fun VerificationRequirementCard(
    requirement: CountryVerificationRequirement,
    isUploaded: Boolean,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        modifier = modifier
            .fillMaxWidth()
            .testTag("requirement_card_${requirement.requirementId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = requirement.displayName,
                        style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                    if (requirement.required) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = HealthogramTheme.colors.errorContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Required",
                                style = HealthogramTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = HealthogramTheme.colors.error,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = requirement.description,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isUploaded) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Uploaded",
                        tint = HealthogramTheme.colors.success,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onUploadClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("upload_req_button_${requirement.requirementId}")
                ) {
                    Text("Upload", style = HealthogramTheme.typography.labelSmall)
                }
            }
        }
    }
}

// =============================================================================
// 6. DOCUMENT CARD
// =============================================================================

@Composable
fun VerificationDocumentCard(
    document: VerificationDocument,
    canDelete: Boolean,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("doc_card_${document.documentId}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = VerificationFunctions.getVerificationDocumentLabel(document.documentType),
                        style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.onSurface
                    )
                    Text(
                        text = "${document.fileName} • ${document.fileSize / 1024} KB",
                        style = HealthogramTheme.typography.bodySmall,
                        color = HealthogramTheme.colors.onSurfaceVariant
                    )
                    if (document.documentNumberLast4.isNotEmpty()) {
                        Text(
                            text = "Identifier: •••• ${document.documentNumberLast4}",
                            style = HealthogramTheme.typography.labelSmall,
                            color = HealthogramTheme.colors.primary
                        )
                    }
                }
            }

            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_doc_button_${document.documentId}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Document", tint = HealthogramTheme.colors.error)
                }
            }
        }
    }
}

// =============================================================================
// 7. EXPIRY BANNER (STEP 07 SECTION 21)
// =============================================================================

@Composable
fun VerificationExpiryBanner(
    expiresAt: Long?,
    onRenewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (expiresAt == null) return
    val diffDays = (expiresAt - System.currentTimeMillis()) / (24 * 3600 * 1000L)
    if (diffDays > 30) return // Only display when expiration is approaching or expired

    val isExpired = diffDays < 0
    val containerColor = if (isExpired) HealthogramTheme.colors.errorContainer else HealthogramTheme.colors.warningContainer

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("verification_expiry_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isExpired) Icons.Default.Warning else Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = if (isExpired) HealthogramTheme.colors.error else HealthogramTheme.colors.warning
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isExpired) "Verification Expired" else "Verification Renewal Approaching",
                        style = HealthogramTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = VerificationFunctions.getVerificationExpiryMessage(expiresAt),
                        style = HealthogramTheme.typography.bodySmall
                    )
                }
            }

            Button(
                onClick = onRenewClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("renew_verification_button")
            ) {
                Text("Renew", style = HealthogramTheme.typography.labelSmall)
            }
        }
    }
}

// =============================================================================
// 8. HEALTHCARE SCANNER ELIGIBILITY CARD (STEP 07 SECTION 34)
// =============================================================================

@Composable
fun HealthcareScannerEligibilityCard(
    user: User,
    onGetVerifiedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("scanner_eligibility_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = HealthogramTheme.colors.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Healthcare Verification Required",
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Healthcare verification is required to use the Health Passport scanner. Verified Doctors, Clinics, Hospitals, and Diagnostic Laboratories can access authorized clinical records.",
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGetVerifiedClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scanner_get_verified_button")
            ) {
                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Verified", fontWeight = FontWeight.Bold)
            }
        }
    }
}
