package com.example.healthogram.ui.healthpassport.pages

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.healthpassport.*
import com.example.healthogram.ui.healthpassport.components.*

/**
 * PAGE 1: HealthPassportHomePage
 * Main Dashboard with top Health ID, QR launcher, privacy indicators, and category cards.
 */
@Composable
fun HealthPassportHomePage(
    patientUid: String = "user_patient_demo",
    onNavigate: (String) -> Unit = {},
    onScanQR: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val profile = engine.getOrCreateProfile(patientUid)
    val conditions by engine.conditions.collectAsState()
    val allergies by engine.allergies.collectAsState()
    val medications by engine.medications.collectAsState()
    val visits by engine.visits.collectAsState()
    val tests by engine.tests.collectAsState()
    val labReports by engine.labReports.collectAsState()
    val prescriptions by engine.prescriptions.collectAsState()
    val documents by engine.documents.collectAsState()
    val bills by engine.bills.collectAsState()
    val requests by engine.accessRequests.collectAsState()
    val pendingCount = requests.count { it.patientUid == patientUid && it.status == "pending" }
    val activeGrants by engine.accessGrants.collectAsState()
    val activePermsCount = activeGrants.count { it.patientUid == patientUid && it.isCurrentlyActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_passport_home_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HealthPassportHeader(
                healthId = profile.healthId,
                privacyStatusText = if (activePermsCount > 0) "SHARED WITH $activePermsCount PROVIDER(S)" else "PRIVATE BY DEFAULT",
                lastUpdatedText = "Updated Today",
                onQrClick = { onNavigate("HEALTH_QR") },
                onPrivacyClick = { onNavigate("ACTIVE_PERMISSIONS") }
            )
        }

        item {
            HealthPrivacyBanner(
                title = "Zero-Knowledge Medical Passport",
                description = "Isolated from social feeds and marketplace. Access requires your explicit authorization."
            )
        }

        // Quick Primary Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HealthogramPrimaryButton(
                    text = "Share Vault",
                    onClick = { onNavigate("HEALTH_SHARING") },
                    icon = Icons.Default.Share,
                    modifier = Modifier.weight(1f),
                    size = ButtonSize.SMALL
                )
                HealthogramSecondaryButton(
                    text = if (pendingCount > 0) "Requests ($pendingCount)" else "Requests",
                    onClick = { onNavigate("ACCESS_REQUESTS") },
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f),
                    size = ButtonSize.SMALL
                )
                HealthogramSecondaryButton(
                    text = "Timeline",
                    onClick = { onNavigate("TIMELINE") },
                    icon = Icons.Default.Timeline,
                    modifier = Modifier.weight(1f),
                    size = ButtonSize.SMALL
                )
            }
        }

        // Dashboard Category Cards Grid
        item {
            Text(
                text = "Clinical Records",
                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        item {
            HealthPassportCard(
                title = "Health Profile",
                count = 1,
                subtitle = "Blood Group ${profile.bloodGroup} • Emergency Info",
                icon = Icons.Default.AccountCircle,
                onClick = { onNavigate("HEALTH_PROFILE") }
            )
        }

        item {
            HealthPassportCard(
                title = "Medical History",
                count = conditions.size + visits.size,
                subtitle = "Past diagnoses, surgeries & clinical background",
                icon = Icons.Default.History,
                onClick = { onNavigate("MEDICAL_HISTORY") }
            )
        }

        item {
            HealthPassportCard(
                title = "Conditions & Diagnoses",
                count = conditions.count { it.patientUid == patientUid },
                subtitle = "Active chronic and acute medical conditions",
                icon = Icons.Default.FactCheck,
                onClick = { onNavigate("CONDITIONS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Allergies & Advisories",
                count = allergies.count { it.patientUid == patientUid },
                subtitle = "Critical allergen sensitivities & warnings",
                icon = Icons.Default.WarningAmber,
                color = HealthogramTheme.colors.error,
                onClick = { onNavigate("ALLERGIES") }
            )
        }

        item {
            HealthPassportCard(
                title = "Medications",
                count = medications.count { it.patientUid == patientUid },
                subtitle = "Active clinical prescriptions & schedules",
                icon = Icons.Default.Medication,
                onClick = { onNavigate("MEDICATIONS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Doctor Visits",
                count = visits.count { it.patientUid == patientUid },
                subtitle = "Consultation history & clinical summaries",
                icon = Icons.Default.MedicalServices,
                onClick = { onNavigate("DOCTOR_VISITS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Tests & Diagnostics",
                count = tests.count { it.patientUid == patientUid },
                subtitle = "Diagnostic orders & test statuses",
                icon = Icons.Default.Biotech,
                onClick = { onNavigate("TESTS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Laboratory Reports",
                count = labReports.count { it.patientUid == patientUid },
                subtitle = "Verified pathology & metabolic panels",
                icon = Icons.Default.Science,
                onClick = { onNavigate("LAB_REPORTS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Prescriptions",
                count = prescriptions.count { it.patientUid == patientUid },
                subtitle = "Digital physician signed prescriptions",
                icon = Icons.Default.Description,
                onClick = { onNavigate("PRESCRIPTIONS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Upload Paper Prescription",
                count = documents.count { it.patientUid == patientUid && it.documentType == "paper_prescription" },
                subtitle = "Camera & gallery prescription uploads",
                icon = Icons.Default.CameraAlt,
                color = HealthogramTheme.colors.secondary,
                onClick = { onNavigate("PAPER_PRESCRIPTION_UPLOAD") }
            )
        }

        item {
            HealthPassportCard(
                title = "Medical Documents",
                count = documents.count { it.patientUid == patientUid },
                subtitle = "Discharge notes, radiology & certificates",
                icon = Icons.Default.FolderShared,
                onClick = { onNavigate("MEDICAL_DOCUMENTS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Medical Bills",
                count = bills.count { it.patientUid == patientUid },
                subtitle = "Private hospital & clinic expenses",
                icon = Icons.Default.ReceiptLong,
                onClick = { onNavigate("MEDICAL_BILLS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Health Notes",
                count = engine.notes.value.count { it.patientUid == patientUid },
                subtitle = "Private symptom observations & diary",
                icon = Icons.Default.Notes,
                onClick = { onNavigate("HEALTH_NOTES") }
            )
        }

        // Privacy & Governance Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Privacy & Authorization Controls",
                style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = HealthogramTheme.colors.textPrimary
            )
        }

        item {
            HealthPassportCard(
                title = "Active Permissions",
                count = activePermsCount,
                subtitle = "Inspect and revoke active provider grants",
                icon = Icons.Default.VpnKey,
                color = HealthogramTheme.colors.success,
                onClick = { onNavigate("ACTIVE_PERMISSIONS") }
            )
        }

        item {
            HealthPassportCard(
                title = "Access History & Audit Logs",
                count = engine.getAccessLogsForPatient(patientUid).size,
                subtitle = "Immutable record of all access events",
                icon = Icons.Default.Security,
                onClick = { onNavigate("ACCESS_HISTORY") }
            )
        }

        item {
            HealthPassportCard(
                title = "Secure Export / Download",
                count = 1,
                subtitle = "Download encrypted passport archive",
                icon = Icons.Default.Download,
                onClick = { onNavigate("SECURE_EXPORT") }
            )
        }

        item {
            HealthPassportCard(
                title = "Health Passport Settings",
                count = 0,
                subtitle = "Offline vault, auto-lock & re-auth",
                icon = Icons.Default.Settings,
                onClick = { onNavigate("PASSPORT_SETTINGS") }
            )
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * PAGE 2: HealthPassportTimelinePage
 */
@Composable
fun HealthPassportTimelinePage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val visits by engine.visits.collectAsState()
    val labReports by engine.labReports.collectAsState()
    val prescriptions by engine.prescriptions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_passport_timeline_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Medical Timeline", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(description = "Chronological sequence of verified doctor visits, lab tests, and prescriptions.")
        }

        items(visits.filter { it.patientUid == patientUid }) { visit ->
            HealthTimelineItem(
                title = visit.visitType,
                category = "Doctor Visit",
                date = visit.visitDate,
                provider = visit.organizationId,
                summary = visit.clinicalSummary,
                icon = Icons.Default.MedicalServices
            )
        }

        items(prescriptions.filter { it.patientUid == patientUid }) { rx ->
            HealthTimelineItem(
                title = "Digital Prescription Issued",
                category = "Prescription",
                date = rx.prescriptionDate,
                provider = rx.organizationId,
                summary = rx.medications.joinToString(", ") { "${it.medicineName} (${it.dosage})" },
                icon = Icons.Default.Medication
            )
        }

        items(labReports.filter { it.patientUid == patientUid }) { report ->
            HealthTimelineItem(
                title = "Lab Report Uploaded",
                category = "Laboratory",
                date = report.reportDate,
                provider = report.organizationId,
                summary = report.summary,
                icon = Icons.Default.Science
            )
        }

        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 3: HealthProfilePage
 */
@Composable
fun HealthProfilePage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val profile = engine.getOrCreateProfile(patientUid)
    var bloodGroup by remember { mutableStateOf(profile.bloodGroup) }
    var height by remember { mutableStateOf(profile.height) }
    var weight by remember { mutableStateOf(profile.weight) }
    var emergencyContactName by remember { mutableStateOf(profile.emergencyContactName) }
    var emergencyContactPhone by remember { mutableStateOf(profile.emergencyContactPhone) }
    var isSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_profile_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Health Profile", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Health ID: ${profile.healthId}", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = HealthogramTheme.colors.primary)
                    Text("Status: ${profile.profileStatus} • Country: ${profile.countryCode}", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)

                    HealthogramTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = "Blood Group",
                        leadingIcon = Icons.Default.Bloodtype
                    )

                    HealthogramTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = "Height",
                        leadingIcon = Icons.Default.Height
                    )

                    HealthogramTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = "Weight",
                        leadingIcon = Icons.Default.Scale
                    )

                    HealthogramTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = "Emergency Contact Name",
                        leadingIcon = Icons.Default.Person
                    )

                    HealthogramTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = "Emergency Contact Phone",
                        leadingIcon = Icons.Default.Phone
                    )

                    HealthogramPrimaryButton(
                        text = if (isSaved) "Profile Updated" else "Save Health Profile",
                        onClick = {
                            engine.updateProfile(
                                patientUid,
                                profile.copy(
                                    bloodGroup = bloodGroup,
                                    height = height,
                                    weight = weight,
                                    emergencyContactName = emergencyContactName,
                                    emergencyContactPhone = emergencyContactPhone
                                )
                            )
                            isSaved = true
                        },
                        icon = Icons.Default.Save,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * PAGE 4: MedicalHistoryPage
 */
@Composable
fun MedicalHistoryPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val conditions by engine.conditions.collectAsState()
    val visits by engine.visits.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("medical_history_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Medical History", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        item { HealthPrivacyBanner(description = "Authorized overview of chronic conditions and historical clinical encounters.") }

        items(conditions.filter { it.patientUid == patientUid }) { ConditionCard(it) }
        items(visits.filter { it.patientUid == patientUid }) { VisitCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 5: ConditionsPage
 */
@Composable
fun ConditionsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val conditions by engine.conditions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("conditions_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Conditions & Diagnoses", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(conditions.filter { it.patientUid == patientUid }) { ConditionCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 6: AllergiesPage
 */
@Composable
fun AllergiesPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val allergies by engine.allergies.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("allergies_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Allergies & Critical Warnings", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(allergies.filter { it.patientUid == patientUid }) { AllergyCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 7: MedicationsPage
 */
@Composable
fun MedicationsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val medications by engine.medications.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("medications_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Active Medications", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(medications.filter { it.patientUid == patientUid }) { MedicationCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}
