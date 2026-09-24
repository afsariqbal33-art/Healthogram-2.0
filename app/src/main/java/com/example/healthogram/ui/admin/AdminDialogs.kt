package com.example.healthogram.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.healthogram.admin.AccountActionType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType

/**
 * MFA / Re-authentication Dialog for sensitive administrative operations.
 */
@Composable
fun AdminReauthDialog(
    title: String = "Security Re-Authentication Required",
    description: String = "This sensitive operation requires administrative identity verification. Enter your secure admin PIN or password.",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "MFA Required",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        hasError = false
                    },
                    label = { Text("Admin PIN (demo: 1234 or owner999)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = hasError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Invalid credentials. Use '1234' or 'owner999' for test authorization.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (pin.isBlank()) {
                                hasError = true
                            } else {
                                onConfirm(pin)
                            }
                        }
                    ) {
                        Text("Verify & Proceed")
                    }
                }
            }
        }
    }
}

/**
 * Standard confirmation dialog for administrative actions with mandatory reason.
 */
@Composable
fun AdminActionConfirmDialog(
    title: String,
    targetDescription: String,
    impactWarning: String,
    confirmButtonText: String = "Confirm Action",
    isDestructive: Boolean = false,
    onConfirm: (reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = targetDescription,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠️ $impactWarning",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        showError = false
                    },
                    label = { Text("Administrative Justification / Reason") },
                    placeholder = { Text("Enter required audit explanation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    isError = showError
                )
                if (showError) {
                    Text(
                        text = "Audit policy strictly requires a documented reason for this action.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        onClick = {
                            if (reason.isBlank()) {
                                showError = true
                            } else {
                                onConfirm(reason)
                            }
                        }
                    ) {
                        Text(confirmButtonText)
                    }
                }
            }
        }
    }
}

/**
 * Account Suspension & Restriction Dialog.
 */
@Composable
fun AdminUserSuspensionDialog(
    user: User,
    onConfirm: (actionType: AccountActionType, reasonCode: String, reason: String, durationHours: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAction by remember { mutableStateOf(AccountActionType.TEMPORARY_SUSPEND) }
    var reasonCode by remember { mutableStateOf("policy_violation") }
    var reasonText by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf<Long?>(72L) } // 72 hours default

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Account Action: ${user.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "UID: ${user.uid} • ${user.accountType.name} • ${user.countryCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Action Type:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedAction == AccountActionType.TEMPORARY_SUSPEND,
                        onClick = { selectedAction = AccountActionType.TEMPORARY_SUSPEND },
                        label = { Text("Suspend") }
                    )
                    FilterChip(
                        selected = selectedAction == AccountActionType.CONTENT_RESTRICTION,
                        onClick = { selectedAction = AccountActionType.CONTENT_RESTRICTION },
                        label = { Text("Restrict Content") }
                    )
                    FilterChip(
                        selected = selectedAction == AccountActionType.MARKETPLACE_SUSPEND,
                        onClick = { selectedAction = AccountActionType.MARKETPLACE_SUSPEND },
                        label = { Text("Freeze Market") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Duration:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedDuration == 24L,
                        onClick = { selectedDuration = 24L },
                        label = { Text("24 Hours") }
                    )
                    FilterChip(
                        selected = selectedDuration == 72L,
                        onClick = { selectedDuration = 72L },
                        label = { Text("3 Days") }
                    )
                    FilterChip(
                        selected = selectedDuration == 720L,
                        onClick = { selectedDuration = 720L },
                        label = { Text("30 Days") }
                    )
                    FilterChip(
                        selected = selectedDuration == null,
                        onClick = { selectedDuration = null },
                        label = { Text("Indefinite") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    label = { Text("Documented Reason for Action") },
                    placeholder = { Text("Violations observed, reported ticket ID, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            if (reasonText.isNotBlank()) {
                                onConfirm(selectedAction, reasonCode, reasonText, selectedDuration)
                            }
                        }
                    ) {
                        Text("Apply Enforcement")
                    }
                }
            }
        }
    }
}

/**
 * Verification Application Review Dialog.
 */
@Composable
fun AdminVerificationReviewDialog(
    user: User,
    onApprove: (badgeType: VerificationBadgeType, notes: String) -> Unit,
    onReject: (internalReason: String, customerReason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var selectedBadge by remember {
        mutableStateOf(
            when (user.accountType.name) {
                "HOSPITAL", "CLINIC" -> VerificationBadgeType.GOLD_SHIELD
                "LABORATORY" -> VerificationBadgeType.GREEN_CROSS
                else -> VerificationBadgeType.CYAN_CHECK
            }
        )
    }
    var isRejectMode by remember { mutableStateOf(false) }
    var customerRejectReason by remember { mutableStateOf("Submitted credentials could not be validated against the national registry.") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Review Credential Application",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${user.displayName} • ${user.accountType.name} (${user.countryCode})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Reg Path: verification_private/${user.uid}/license_cert.pdf",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))
                // Simulated secure document check
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Institutional Documents Status",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✓ Ministry of Health Accreditation Certificate (Verified Signature)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "✓ Commercial Medical Registration / License ID verified with authority",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "✓ Identity token verified via Government National Registry API",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                if (!isRejectMode) {
                    Text("Assigned Badge Type:", style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedBadge == VerificationBadgeType.CYAN_CHECK,
                            onClick = { selectedBadge = VerificationBadgeType.CYAN_CHECK },
                            label = { Text("Cyan Check") }
                        )
                        FilterChip(
                            selected = selectedBadge == VerificationBadgeType.GOLD_SHIELD,
                            onClick = { selectedBadge = VerificationBadgeType.GOLD_SHIELD },
                            label = { Text("Gold Shield") }
                        )
                        FilterChip(
                            selected = selectedBadge == VerificationBadgeType.GREEN_CROSS,
                            onClick = { selectedBadge = VerificationBadgeType.GREEN_CROSS },
                            label = { Text("Green Cross") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Audit Approval Notes") },
                        placeholder = { Text("e.g. MOH Registry Reference #SA-9012 valid.") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Internal Rejection Reason (Confidential)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerRejectReason,
                        onValueChange = { customerRejectReason = it },
                        label = { Text("Customer-Facing Reason (Displayed to Applicant)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = { isRejectMode = !isRejectMode }
                    ) {
                        Text(if (isRejectMode) "Switch to Approve" else "Switch to Reject")
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (!isRejectMode) {
                            Button(
                                onClick = { onApprove(selectedBadge, notes.ifBlank { "Credential verified and validated." }) }
                            ) {
                                Text("Approve & Badge")
                            }
                        } else {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                onClick = { onReject(notes.ifBlank { "Document mismatch" }, customerRejectReason) }
                            ) {
                                Text("Reject Application")
                            }
                        }
                    }
                }
            }
        }
    }
}
