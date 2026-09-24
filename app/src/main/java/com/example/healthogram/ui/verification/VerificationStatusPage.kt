package com.example.healthogram.ui.verification

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
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.profile.model.VerificationProfile
import com.example.healthogram.profile.repository.ProfileRepository

/**
 * Platform Verification Status & Requirements Page (Section 65).
 * Highlights:
 * - Verification status lifecycle (Draft -> Submitted -> Under Review -> Approved / Rejected)
 * - Country requirements
 * - Secure document metadata (No sensitive public URLs or leaked national IDs)
 * - Health Passport Scanner capability activation preview.
 */
@Composable
fun VerificationStatusPage(
    user: User?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { ProfileRepository.getInstance() }
    var verificationProfile by remember { mutableStateOf<VerificationProfile?>(null) }

    LaunchedEffect(user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        verificationProfile = repository.getVerificationProfile(uid)
    }

    val status = if (user?.isVerified == true) {
        VerificationStatus.APPROVED
    } else {
        verificationProfile?.status ?: VerificationStatus.NOT_STARTED
    }

    val accountType = user?.accountType ?: AccountType.INDIVIDUAL

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .testTag("verification_status_page")
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
                    text = "Professional Verification",
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
            // Status Banner
            item {
                Surface(
                    shape = HealthogramTheme.shapes.medium,
                    color = when (status) {
                        VerificationStatus.APPROVED -> HealthogramTheme.colors.successContainer
                        VerificationStatus.UNDER_REVIEW,
                        VerificationStatus.SUBMITTED -> HealthogramTheme.colors.warningContainer
                        VerificationStatus.REJECTED -> HealthogramTheme.colors.errorContainer
                        else -> HealthogramTheme.colors.surfaceVariant
                    },
                    border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HealthogramVerifiedBadge(status = status, size = BadgeSize.LARGE)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Status: ${status.name.replace('_', ' ')}",
                                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when (status) {
                                VerificationStatus.APPROVED -> "Your account is officially verified on Healthogram Trust Network. You carry the verified badge across posts, profiles, and consultations."
                                VerificationStatus.UNDER_REVIEW -> "Your documentation has been submitted and is currently being audited by the Medical Credentials Board."
                                VerificationStatus.REJECTED -> "Verification was rejected. Please review board notes and submit revised government credentials."
                                else -> "Verification unlocks trust badges, patient referrals, and clinical capabilities on Healthogram."
                            },
                            style = HealthogramTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Scanner Capability Rule (Section 49 & 65)
            item {
                Text("Health Passport Scanner Privilege", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                val scannerActive = status == VerificationStatus.APPROVED && accountType in setOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY)

                HealthogramBasicCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = if (scannerActive) Icons.Default.QrCodeScanner else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (scannerActive) HealthogramTheme.colors.primary else HealthogramTheme.colors.warning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (scannerActive) "Scanner Active" else "Scanner Restricted",
                                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (scannerActive) HealthogramTheme.colors.primary else HealthogramTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (scannerActive) {
                                    "Your verified status allows scanning patient QR codes upon physical presentation to access consented medical records."
                                } else {
                                    "Unverified accounts strictly CANNOT scan Health Passports. Professional verification and board accreditation are required."
                                },
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textMuted
                            )
                        }
                    }
                }
            }

            // Country Verification Requirements
            item {
                Text("Requirements for ${accountType.name.lowercase().replaceFirstChar { it.uppercase() }}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }

            item {
                HealthogramBasicCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (accountType) {
                            AccountType.DOCTOR -> {
                                RequirementBullet("Active State Medical Board License")
                                RequirementBullet("Board Certification or Specialty Diploma")
                                RequirementBullet("Government-Issued Photo Identification")
                                RequirementBullet("National Provider Identifier (NPI)")
                            }
                            AccountType.CLINIC -> {
                                RequirementBullet("State Healthcare Facility License")
                                RequirementBullet("Medical Director Appointment Letter")
                                RequirementBullet("Clinical Liability Insurance Proof")
                            }
                            AccountType.HOSPITAL -> {
                                RequirementBullet("State Department of Health Hospital Operating License")
                                RequirementBullet("Joint Commission / CMS Accreditation Certificate")
                                RequirementBullet("Authorized Hospital Administrator Affidavit")
                            }
                            AccountType.LABORATORY -> {
                                RequirementBullet("CLIA Certificate of Accreditation / Compliance")
                                RequirementBullet("CAP (College of American Pathologists) Certificate")
                                RequirementBullet("Laboratory Director Credential Verification")
                            }
                            else -> {
                                RequirementBullet("Government Issued Identity Card or Passport")
                                RequirementBullet("Verified Phone Number & Two-Factor Authentication")
                            }
                        }
                    }
                }
            }

            if (status == VerificationStatus.NOT_STARTED || status == VerificationStatus.DRAFT) {
                item {
                    HealthogramPrimaryButton(
                        text = "Submit Verification Documents",
                        onClick = { /* document submission flow */ },
                        icon = Icons.Default.UploadFile
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun RequirementBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = HealthogramTheme.typography.bodySmall)
    }
}
