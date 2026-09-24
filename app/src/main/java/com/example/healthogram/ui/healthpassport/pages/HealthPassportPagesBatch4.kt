package com.example.healthogram.ui.healthpassport.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.healthpassport.*
import com.example.healthogram.ui.healthpassport.components.*

/**
 * PAGE 22: HealthSharingPage
 */
@Composable
fun HealthSharingPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    var providerName by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("Clinical Consultation") }
    var shareConditions by remember { mutableStateOf(true) }
    var shareAllergies by remember { mutableStateOf(true) }
    var shareMedications by remember { mutableStateOf(true) }
    var sharePrescriptions by remember { mutableStateOf(false) }
    var shareLabReports by remember { mutableStateOf(false) }
    var durationHours by remember { mutableStateOf(24) }
    var isShared by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_sharing_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Share Health Passport", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(
                title = "Explicit Granular Authorization",
                description = "Choose exact categories and duration. You can revoke access at any second."
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HealthogramTextField(
                        value = providerName,
                        onValueChange = { providerName = it },
                        label = "Provider / Clinic ID or Name",
                        leadingIcon = Icons.Default.MedicalServices
                    )
                    HealthogramTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = "Consultation Purpose"
                    )

                    Text("Select Permitted Scopes:", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shareConditions, onCheckedChange = { shareConditions = it })
                        Text("Conditions & Diagnoses", style = HealthogramTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shareAllergies, onCheckedChange = { shareAllergies = it })
                        Text("Allergies & Warnings", style = HealthogramTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shareMedications, onCheckedChange = { shareMedications = it })
                        Text("Active Medications", style = HealthogramTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = sharePrescriptions, onCheckedChange = { sharePrescriptions = it })
                        Text("Prescriptions", style = HealthogramTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = shareLabReports, onCheckedChange = { shareLabReports = it })
                        Text("Lab Reports", style = HealthogramTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Duration: ${HealthPassportFunctions.getPermissionDurationLabel(durationHours)}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HealthogramSecondaryButton(text = "1h", onClick = { durationHours = 1 }, size = ButtonSize.SMALL)
                        HealthogramSecondaryButton(text = "24h", onClick = { durationHours = 24 }, size = ButtonSize.SMALL)
                        HealthogramSecondaryButton(text = "7d", onClick = { durationHours = 168 }, size = ButtonSize.SMALL)
                    }

                    if (isShared) {
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.successContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Access granted to $providerName for ${HealthPassportFunctions.getPermissionDurationLabel(durationHours)}.",
                                modifier = Modifier.padding(12.dp),
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.success
                            )
                        }
                    }

                    HealthogramPrimaryButton(
                        text = "Issue Authorization Grant",
                        onClick = {
                            val scopes = mutableListOf<String>()
                            if (shareConditions) scopes.add("conditions")
                            if (shareAllergies) scopes.add("allergies")
                            if (shareMedications) scopes.add("medications")
                            if (sharePrescriptions) scopes.add("prescriptions")
                            if (shareLabReports) scopes.add("lab_reports")

                            val fakeReq = HealthAccessRequest(
                                patientUid = patientUid,
                                requesterUid = "provider_${providerName.lowercase().replace(" ", "_")}",
                                requesterRole = "DOCTOR",
                                requesterOrganizationId = providerName.ifBlank { "General Clinic" },
                                requesterName = providerName.ifBlank { "Dr. Smith" },
                                requestReason = purpose,
                                requestedScopes = scopes
                            )
                            engine.requestHealthAccess(
                                requester = User(
                                    uid = fakeReq.requesterUid,
                                    email = "doc@clinic.com",
                                    phoneNumber = "+12345678",
                                    displayName = fakeReq.requesterName,
                                    username = "doc",
                                    accountType = AccountType.DOCTOR,
                                    isVerified = true
                                ),
                                patientUid = patientUid,
                                requestedScopes = scopes,
                                reason = purpose,
                                durationHours = durationHours
                            )
                            engine.approveHealthAccess(patientUid, fakeReq.requestId, scopes, durationHours)
                            isShared = true
                        },
                        icon = Icons.Default.VpnKey,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 23: HealthIDPage
 */
@Composable
fun HealthIDPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val profile = engine.getOrCreateProfile(patientUid)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_id_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Health ID Card", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large,
                border = androidx.compose.foundation.BorderStroke(1.dp, HealthogramTheme.colors.primary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = HealthogramTheme.colors.primary, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("GLOBAL HEALTH PASSPORT", style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.textMuted)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(profile.healthId, style = HealthogramTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Country: ${profile.countryCode} • Blood Group: ${profile.bloodGroup}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                    Text("Emergency Contact: ${profile.emergencyContactName} (${profile.emergencyContactPhone})", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textSecondary)
                }
            }
        }
        item {
            HealthPrivacyBanner(
                title = "Opaque Identifier Principle",
                description = "Health IDs never encode phone numbers, emails, government IDs, or diagnoses. Safe to display in clinic reception."
            )
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 24: HealthQRPage
 */
@Composable
fun HealthQRPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val profile = engine.getOrCreateProfile(patientUid)
    var session by remember { mutableStateOf(engine.generateSecureHealthQRCode(patientUid, profile.healthId)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_qr_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Health Passport QR", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthQRCard(
                session = session,
                onRefresh = {
                    session = engine.generateSecureHealthQRCode(patientUid, profile.healthId)
                }
            )
        }

        item {
            HealthPrivacyBanner(
                title = "Anti-Eavesdropping Security",
                description = "This QR code rotates every 15 minutes and carries zero clinical data. Scanning providers must authenticate and request explicit consent."
            )
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 25: HealthPassportSettingsPage
 */
@Composable
fun HealthPassportSettingsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {},
    onDeleteData: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val currentSettings = engine.getSettings(patientUid)
    var offlineEnabled by remember { mutableStateOf(currentSettings.offlinePassportEnabled) }
    var biometricRequired by remember { mutableStateOf(currentSettings.biometricUnlockRequired) }
    var hideNotificationPreviews by remember { mutableStateOf(currentSettings.hidePreviewsInNotifications) }
    var autoLockMinutes by remember { mutableStateOf(currentSettings.autoLockTimeoutMinutes) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_passport_settings_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Passport Security Settings", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Local Vault Storage", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Offline Medical Caching", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text("Disabled by default for maximum privacy. Encrypted local store.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = offlineEnabled, onCheckedChange = {
                            offlineEnabled = it
                            engine.updateSettings(patientUid, currentSettings.copy(offlinePassportEnabled = it))
                        })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric Authentication", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text("Require fingerprint or face recognition when opening vault.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = biometricRequired, onCheckedChange = { biometricRequired = it })
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hide Notification Previews", style = HealthogramTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            Text("Do not reveal diagnosis or medicine names on lockscreen.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
                        }
                        Switch(checked = hideNotificationPreviews, onCheckedChange = { hideNotificationPreviews = it })
                    }

                    Divider()

                    HealthogramSecondaryButton(
                        text = "Purge Local Sensitive Cache",
                        onClick = { engine.clearSensitiveLocalCache(patientUid) },
                        icon = Icons.Default.CleaningServices,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HealthogramSecondaryButton(
                        text = "Request Health Data Deletion",
                        onClick = onDeleteData,
                        icon = Icons.Default.DeleteForever,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 26: SecureHealthExportPage
 */
@Composable
fun SecureHealthExportPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    var authConfirmed by remember { mutableStateOf(false) }
    var exportUrl by remember { mutableStateOf<String?>(null) }
    var exportError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("secure_health_export_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Secure Passport Export", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(
                title = "Authentication Challenge Required",
                description = "Exporting your full medical history requires explicit re-authentication to prevent unauthorized device downloads."
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Export Verification", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = authConfirmed, onCheckedChange = { authConfirmed = it })
                        Text("Confirm identity challenge passed (Biometrics / Password)", style = HealthogramTheme.typography.bodySmall)
                    }

                    if (exportUrl != null) {
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.successContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Temporary Export Link (Expires in 15 mins):\n$exportUrl",
                                modifier = Modifier.padding(12.dp),
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.success
                            )
                        }
                    }

                    if (exportError != null) {
                        Text(exportError!!, color = HealthogramTheme.colors.error, style = HealthogramTheme.typography.caption)
                    }

                    HealthogramPrimaryButton(
                        text = "Generate Encrypted Archive (.ZIP)",
                        onClick = {
                            val res = engine.secureHealthExport(
                                patientUid = patientUid,
                                categories = listOf("conditions", "allergies", "medications", "lab_reports", "prescriptions"),
                                dateRange = "ALL",
                                authChallengePassed = authConfirmed
                            )
                            if (res.isSuccess) {
                                exportUrl = res.getOrNull()
                                exportError = null
                            } else {
                                exportError = res.exceptionOrNull()?.message
                            }
                        },
                        icon = Icons.Default.LockReset,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 27: HealthDeleteRequestPage
 */
@Composable
fun HealthDeleteRequestPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    var passwordConfirmed by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_delete_request_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Delete Health Passport Data", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.errorContainer),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Permanent Health Data Deletion", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
                    Text("This operation will schedule the complete deletion of your private health passport, prescriptions, lab reports, and doctor consultations. You will enter a 30-day recovery window.", style = HealthogramTheme.typography.bodySmall, color = HealthogramTheme.colors.error)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = passwordConfirmed, onCheckedChange = { passwordConfirmed = it })
                        Text("I understand that all health records will be permanently purged.", style = HealthogramTheme.typography.caption)
                    }

                    if (message != null) {
                        Text(message!!, style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.error)
                    }

                    HealthogramPrimaryButton(
                        text = "Schedule Data Deletion",
                        onClick = {
                            val res = engine.requestDataDeletion(patientUid, passwordConfirmed)
                            message = if (res.isSuccess) res.getOrNull() else res.exceptionOrNull()?.message
                        },
                        icon = Icons.Default.Delete,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 28: HealthcareQRScannerPage
 * STRICT ACCESS RESTRICTION: Permitted ONLY for verified DOCTOR, CLINIC, HOSPITAL, and LABORATORY.
 * REJECTS Individual, unverified, customer, and marketplace seller accounts.
 */
@Composable
fun HealthcareQRScannerPage(
    currentUser: User = User(
        uid = "doc_test_1",
        email = "dr.smith@mgh.org",
        phoneNumber = "+1234567890",
        displayName = "Dr. Robert Smith, MD",
        username = "drsmith",
        accountType = AccountType.DOCTOR,
        isVerified = true
    ),
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val eligibility = engine.validateScannerEligibility(currentUser)
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }
    var scannedSession by remember { mutableStateOf<HealthQRSession?>(null) }
    var requestSubmitted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("healthcare_qr_scanner_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Healthcare Provider Scanner", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthcareScannerCard(
                isVerifiedClinician = eligibility.isSuccess,
                onScanTicket = { token ->
                    // Mock scanning patient's QR token
                    val patientUid = "user_patient_demo"
                    val session = engine.generateSecureHealthQRCode(patientUid, "HG-749204829103")
                    val res = engine.scanHealthQRCode(currentUser, session.sessionId)
                    if (res.isSuccess) {
                        scannedSession = res.getOrNull()
                        scanStatusMessage = "Ticket verified! Session established with patient."
                    } else {
                        scanStatusMessage = res.exceptionOrNull()?.message
                    }
                }
            )
        }

        if (scannedSession != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                    shape = HealthogramTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Patient Session Connected", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        Text("Patient ID: ${scannedSession!!.patientUid}", style = HealthogramTheme.typography.caption)

                        if (!requestSubmitted) {
                            HealthogramPrimaryButton(
                                text = "Request Medical Consultation Access",
                                onClick = {
                                    val scopes = if (currentUser.accountType == AccountType.LABORATORY) {
                                        listOf("tests", "lab_reports")
                                    } else {
                                        listOf("conditions", "allergies", "medications", "prescriptions")
                                    }
                                    engine.requestHealthAccess(
                                        requester = currentUser,
                                        patientUid = scannedSession!!.patientUid,
                                        requestedScopes = scopes,
                                        reason = "Clinical consultation encounter",
                                        durationHours = 24,
                                        qrSessionId = scannedSession!!.sessionId
                                    )
                                    requestSubmitted = true
                                    scanStatusMessage = "Access request sent to patient's device for explicit approval."
                                },
                                icon = Icons.Default.Send,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Surface(
                                shape = HealthogramTheme.shapes.medium,
                                color = HealthogramTheme.colors.successContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Access request pending patient approval. Patient must tap 'Authorize Access' on their dashboard.",
                                    modifier = Modifier.padding(12.dp),
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.success
                                )
                            }
                        }
                    }
                }
            }
        }

        if (scanStatusMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surfaceVariant)
                ) {
                    Text(
                        text = scanStatusMessage!!,
                        modifier = Modifier.padding(12.dp),
                        style = HealthogramTheme.typography.caption
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}
