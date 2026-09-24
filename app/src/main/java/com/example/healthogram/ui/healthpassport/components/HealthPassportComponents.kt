package com.example.healthogram.ui.healthpassport.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.HealthogramBasicCard
import com.example.healthogram.designsystem.components.HealthogramPrimaryButton
import com.example.healthogram.designsystem.components.HealthogramSecondaryButton
import com.example.healthogram.healthpassport.*

/**
 * 1. HealthPassportHeader: Clean medical header displaying Health ID, Privacy status, and last updated.
 */
@Composable
fun HealthPassportHeader(
    healthId: String,
    privacyStatusText: String = "PRIVATE BY DEFAULT",
    lastUpdatedText: String = "Updated Today",
    onQrClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("health_passport_header"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = "Health Passport",
                            tint = HealthogramTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Health Passport",
                            style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.textPrimary
                        )
                        Text(
                            text = healthId,
                            style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = HealthogramTheme.colors.primary
                        )
                    }
                }

                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(HealthogramTheme.colors.surfaceVariant)
                        .testTag("header_qr_button")
                ) {
                    Icon(
                        Icons.Default.QrCode2,
                        contentDescription = "Show QR Code",
                        tint = HealthogramTheme.colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.successContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, HealthogramTheme.colors.success.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onPrivacyClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(HealthogramTheme.colors.success)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = privacyStatusText,
                            style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                            color = HealthogramTheme.colors.success
                        )
                    }
                }

                Text(
                    text = lastUpdatedText,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
        }
    }
}

/**
 * 2. HealthPassportCard: Clickable dashboard module card.
 */
@Composable
fun HealthPassportCard(
    title: String,
    count: Int,
    subtitle: String,
    icon: ImageVector,
    color: Color = HealthogramTheme.colors.primary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("health_card_${title.lowercase().replace(" ", "_")}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = HealthogramTheme.shapes.pill,
                color = HealthogramTheme.colors.surfaceVariant
            ) {
                Text(
                    text = "$count",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.textPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(18.dp))
        }
    }
}

/**
 * 3. MedicalRecordCard: Generic wrapper for medical records.
 */
@Composable
fun MedicalRecordCard(
    title: String,
    category: String,
    date: String,
    subtitle: String? = null,
    statusText: String? = null,
    statusColor: Color = HealthogramTheme.colors.primary,
    actions: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    HealthogramBasicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.uppercase(),
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.primary
            )
            Text(
                text = date,
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (statusText != null) {
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }
        }
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = HealthogramTheme.typography.bodySmall,
                color = HealthogramTheme.colors.textMuted
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
        if (actions != null) {
            Spacer(modifier = Modifier.height(8.dp))
            actions()
        }
    }
}

/**
 * 4. MedicationCard: Formatted medicine dosing card.
 */
@Composable
fun MedicationCard(medication: HealthMedication) {
    MedicalRecordCard(
        title = medication.medicineName,
        category = "Medication",
        date = "Started ${medication.startDate}",
        subtitle = medication.genericName.ifBlank { "Prescribed clinical therapy" },
        statusText = medication.status,
        statusColor = if (medication.status == "ACTIVE") HealthogramTheme.colors.success else HealthogramTheme.colors.textMuted
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dosage: ${medication.dosage}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                Text("Route: ${medication.route}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textMuted)
            }
            Text("Frequency: ${medication.frequency}", style = HealthogramTheme.typography.bodySmall)
            if (medication.instructions.isNotBlank()) {
                Text(
                    text = "Instructions: ${medication.instructions}",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.primary
                )
            }
        }
    }
}

/**
 * 5. AllergyCard: Allergy warning card with severity indicator.
 */
@Composable
fun AllergyCard(allergy: HealthAllergy) {
    val severityColor = when (allergy.severity.uppercase()) {
        "LIFE_THREATENING" -> HealthogramTheme.colors.error
        "HIGH" -> Color(0xFFE65100)
        else -> HealthogramTheme.colors.warning
    }
    MedicalRecordCard(
        title = allergy.allergen,
        category = "Allergy Advisory",
        date = "Recorded",
        statusText = allergy.severity,
        statusColor = severityColor
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Reaction: ${allergy.reaction}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            if (allergy.notes.isNotBlank()) {
                Text("Clinical Note: ${allergy.notes}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }
    }
}

/**
 * 6. ConditionCard: Chronic or acute health condition.
 */
@Composable
fun ConditionCard(condition: HealthCondition) {
    MedicalRecordCard(
        title = condition.conditionName,
        category = "Condition",
        date = "Diagnosed ${condition.diagnosedDate}",
        subtitle = "Code: ${condition.conditionCode}",
        statusText = condition.status,
        statusColor = if (condition.status == "ACTIVE") HealthogramTheme.colors.primary else HealthogramTheme.colors.success
    ) {
        if (condition.description.isNotBlank()) {
            Text(condition.description, style = HealthogramTheme.typography.bodySmall)
        }
    }
}

/**
 * 7. VisitCard: Doctor encounter summary.
 */
@Composable
fun VisitCard(visit: HealthVisit) {
    MedicalRecordCard(
        title = visit.visitType,
        category = "Doctor Consultation",
        date = visit.visitDate,
        subtitle = visit.organizationId.ifBlank { "Clinic Visit" },
        statusText = "Completed",
        statusColor = HealthogramTheme.colors.success
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Reason: ${visit.reason}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
            if (visit.clinicalSummary.isNotBlank()) {
                Text("Summary: ${visit.clinicalSummary}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
            }
            if (visit.followUpDate != null) {
                Text("Follow-up: ${visit.followUpDate}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)
            }
        }
    }
}

/**
 * 8. LabReportCard: Diagnostic laboratory results card.
 */
@Composable
fun LabReportCard(
    report: HealthLabReport,
    onDownloadClick: () -> Unit = {}
) {
    MedicalRecordCard(
        title = "Diagnostic Lab Report",
        category = "Laboratory",
        date = report.reportDate,
        subtitle = report.organizationId.ifBlank { "Clinical Lab" },
        statusText = report.reportStatus,
        statusColor = HealthogramTheme.colors.primary,
        actions = {
            HealthogramSecondaryButton(
                text = "Download Secure Report",
                onClick = onDownloadClick,
                icon = Icons.Default.Download,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        if (report.summary.isNotBlank()) {
            Text(report.summary, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
        }
    }
}

/**
 * 9. PrescriptionCard: Digital prescription details card.
 */
@Composable
fun PrescriptionCard(prescription: HealthPrescription) {
    MedicalRecordCard(
        title = "Digital Medical Prescription",
        category = "Prescription",
        date = prescription.prescriptionDate,
        subtitle = prescription.organizationId.ifBlank { "Attending Physician" },
        statusText = "Signed",
        statusColor = HealthogramTheme.colors.success
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            prescription.medications.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.medicineName, style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text("${item.dosage} • ${item.frequency}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                    }
                    Text(item.duration, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold), color = HealthogramTheme.colors.primary)
                }
            }
            if (prescription.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Directions: ${prescription.instructions}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }
    }
}

/**
 * 10. DocumentCard: Medical document entry.
 */
@Composable
fun DocumentCard(
    document: HealthDocument,
    onDownloadClick: () -> Unit = {}
) {
    MedicalRecordCard(
        title = document.title,
        category = document.documentType.replace("_", " ").uppercase(),
        date = document.documentDate,
        subtitle = "${document.mimeType} • ${(document.fileSize / 1024)} KB",
        actions = {
            HealthogramSecondaryButton(
                text = "Secure Download",
                onClick = onDownloadClick,
                icon = Icons.Default.FileDownload,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        if (document.description.isNotBlank()) {
            Text(document.description, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)
        }
    }
}

/**
 * 11. HealthTimelineItem: Chronological timeline event entry.
 */
@Composable
fun HealthTimelineItem(
    title: String,
    category: String,
    date: String,
    provider: String,
    summary: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth().testTag("timeline_item_$category"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(HealthogramTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(18.dp))
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(64.dp)
                    .background(HealthogramTheme.colors.border)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.uppercase(),
                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = HealthogramTheme.colors.primary
                )
                Text(
                    text = date,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
            Text(
                text = title,
                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
            if (provider.isNotBlank()) {
                Text(
                    text = provider,
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.textMuted
                )
            }
            if (summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * 12. AccessRequestCard: Pending consent request received from verified healthcare provider.
 */
@Composable
fun AccessRequestCard(
    request: HealthAccessRequest,
    onApprove: (selectedScopes: List<String>, durationHours: Int) -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("access_request_card_${request.requestId}"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.warning.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(HealthogramTheme.colors.warningContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = HealthogramTheme.colors.warning, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(request.requesterName, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text(
                            text = "${request.requesterRole} • ${request.requesterOrganizationId}",
                            style = HealthogramTheme.typography.caption,
                            color = HealthogramTheme.colors.textMuted
                        )
                    }
                }
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.warningContainer
                ) {
                    Text(
                        text = "PENDING",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.warning
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Purpose: ${request.requestReason}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Requested Scopes: ${request.requestedScopes.joinToString(", ")}", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium), color = HealthogramTheme.colors.primary)

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HealthogramSecondaryButton(
                    text = "Deny Access",
                    onClick = onReject,
                    modifier = Modifier.weight(1f)
                )
                HealthogramPrimaryButton(
                    text = "Authorize Access",
                    onClick = { onApprove(request.requestedScopes, 24) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 13. PermissionCard: Active authorized permission grant.
 */
@Composable
fun PermissionCard(
    grant: HealthAccessGrant,
    onRevoke: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("permission_card_${grant.grantId}"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(grant.organizationId.ifBlank { "Authorized Healthcare Entity" }, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = "Role: ${grant.requesterRole}",
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
                Surface(
                    shape = HealthogramTheme.shapes.pill,
                    color = HealthogramTheme.colors.successContainer
                ) {
                    Text(
                        text = HealthPassportFunctions.getPermissionExpiryLabel(grant.expiresAt),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.success
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Purpose: ${grant.purpose}", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.textSecondary)

            Spacer(modifier = Modifier.height(6.dp))
            Text("Permitted Scopes: ${grant.grantedScopes.joinToString(", ")}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.primary)

            Spacer(modifier = Modifier.height(12.dp))
            HealthogramSecondaryButton(
                text = "Revoke Access Immediately",
                onClick = onRevoke,
                icon = Icons.Default.Block,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 14. AccessHistoryItem: Immutable access audit log row.
 */
@Composable
fun AccessHistoryItem(log: HealthAccessLog) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.logId}"),
        shape = HealthogramTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (log.result == "SUCCESS") HealthogramTheme.colors.successContainer
                        else HealthogramTheme.colors.errorContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (log.result == "SUCCESS") Icons.Default.Security else Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (log.result == "SUCCESS") HealthogramTheme.colors.success else HealthogramTheme.colors.error,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.action.replace("_", " ").uppercase(),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )
                    Text(
                        text = HealthPassportFunctions.formatDate(log.timestamp),
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.textMuted
                    )
                }
                Text(
                    text = "Requester: ${log.requesterRole} (${log.organizationId})",
                    style = HealthogramTheme.typography.bodySmall,
                    color = HealthogramTheme.colors.textSecondary
                )
                Text(
                    text = "Scope: ${log.scope} • Target: ${log.resourceType}",
                    style = HealthogramTheme.typography.caption,
                    color = HealthogramTheme.colors.primary
                )
            }
        }
    }
}

/**
 * 15. HealthPrivacyBanner: Prominent medical privacy disclosure.
 */
@Composable
fun HealthPrivacyBanner(
    title: String = "Zero-Knowledge Medical Vault",
    description: String = "Your health data is private, encrypted, and isolated from social feeds and marketplace."
) {
    Surface(
        shape = HealthogramTheme.shapes.medium,
        color = HealthogramTheme.colors.primaryContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                Text(description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
            }
        }
    }
}

/**
 * 16. HealthQRCard: 15-Minute Rotating Token QR Display.
 */
@Composable
fun HealthQRCard(
    session: HealthQRSession,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("qr_card_session"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Dynamic Access Ticket", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                "QR contains opaque token only • Zero medical records in QR",
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(HealthogramTheme.shapes.large)
                    .background(Color.White)
                    .border(1.dp, HealthogramTheme.colors.border, HealthogramTheme.shapes.large)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.QrCode2,
                    contentDescription = "Session QR Token",
                    tint = HealthogramTheme.colors.textPrimary,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Session ID: ${session.sessionId.take(16)}...",
                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                color = HealthogramTheme.colors.primary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HealthogramSecondaryButton(
                text = "Generate New Token",
                onClick = onRefresh,
                icon = Icons.Default.Refresh,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 17. HealthcareScannerCard: Scanner interface for authorized clinical accounts.
 */
@Composable
fun HealthcareScannerCard(
    isVerifiedClinician: Boolean,
    onScanTicket: (token: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("scanner_card"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
        border = BorderStroke(1.dp, HealthogramTheme.colors.border)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Clinical QR Scanner", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(
                "Restricted to verified Doctors, Clinics, Hospitals & Laboratories",
                style = HealthogramTheme.typography.caption,
                color = HealthogramTheme.colors.textMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (isVerifiedClinician) {
                HealthogramPrimaryButton(
                    text = "Launch Camera Scanner",
                    onClick = { onScanTicket("qrs_mock_ticket_demo") },
                    icon = Icons.Default.CameraAlt,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Surface(
                    shape = HealthogramTheme.shapes.medium,
                    color = HealthogramTheme.colors.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Access Denied: Unverified accounts cannot access healthcare scanner.",
                        modifier = Modifier.padding(12.dp),
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.error
                    )
                }
            }
        }
    }
}

/**
 * 18. SecureUploadCard: Document upload dropzone card with privacy warning.
 */
@Composable
fun SecureUploadCard(
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUploadClick() }
            .testTag("secure_upload_card"),
        shape = HealthogramTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Tap to Select Document or Image", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text("Supported: PDF, JPG, PNG, HEIC (Max 20MB)", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

/**
 * 19. HealthEmptyState: Clean empty state placeholder.
 */
@Composable
fun HealthEmptyState(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = HealthogramTheme.colors.textMuted, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(description, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
    }
}

/**
 * 20. HealthLoadingState
 */
@Composable
fun HealthLoadingState(message: String = "Loading encrypted health records...") {
    Box(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = HealthogramTheme.colors.primary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
        }
    }
}

/**
 * 21. HealthErrorState
 */
@Composable
fun HealthErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.errorContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = HealthogramTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.error)
            Spacer(modifier = Modifier.height(10.dp))
            HealthogramSecondaryButton(text = "Retry", onClick = onRetry)
        }
    }
}
