package com.example.healthogram.ui.healthpassport.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.healthpassport.*
import com.example.healthogram.ui.healthpassport.components.*

/**
 * PAGE 8: PrecautionsPage
 */
@Composable
fun PrecautionsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val precautions by engine.precautions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("precautions_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Clinical Precautions", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(precautions.filter { it.patientUid == patientUid }) { precaution ->
            MedicalRecordCard(
                title = precaution.precaution,
                category = "Precaution",
                date = "Active Advisory",
                statusText = precaution.severity,
                statusColor = HealthogramTheme.colors.warning
            ) {
                Text("Reason: ${precaution.reason}", style = HealthogramTheme.typography.bodySmall)
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 9: DoctorVisitsPage
 */
@Composable
fun DoctorVisitsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val visits by engine.visits.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("doctor_visits_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Doctor Consultations", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(visits.filter { it.patientUid == patientUid }) { VisitCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 10: DiagnosesPage
 */
@Composable
fun DiagnosesPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val diagnoses by engine.diagnoses.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("diagnoses_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Diagnoses", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(diagnoses.filter { it.patientUid == patientUid }) { diag ->
            MedicalRecordCard(
                title = diag.diagnosis,
                category = "Clinical Diagnosis",
                date = diag.diagnosedDate,
                subtitle = "Code: ${diag.diagnosisCode} • ${diag.organizationId}",
                statusText = diag.status,
                statusColor = HealthogramTheme.colors.primary
            ) {
                if (diag.notes.isNotBlank()) {
                    Text(diag.notes, style = HealthogramTheme.typography.bodySmall)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 11: TestsPage
 */
@Composable
fun TestsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val tests by engine.tests.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("tests_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Diagnostic Tests", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(tests.filter { it.patientUid == patientUid }) { test ->
            MedicalRecordCard(
                title = test.testName,
                category = "Diagnostic Test",
                date = "Ordered: ${test.requestedDate}",
                subtitle = "Code: ${test.testCode} • ${test.organizationId}",
                statusText = test.status,
                statusColor = if (test.status == "COMPLETED") HealthogramTheme.colors.success else HealthogramTheme.colors.primary
            ) {
                Text("Result Status: ${test.resultStatus}", style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 12: LabReportsPage
 */
@Composable
fun LabReportsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val labReports by engine.labReports.collectAsState()
    var downloadedUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("lab_reports_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Laboratory Reports", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        if (downloadedUrl != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.successContainer)
                ) {
                    Text(
                        text = "Secure Download Generated (Exp: 5m):\n$downloadedUrl",
                        modifier = Modifier.padding(12.dp),
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.success
                    )
                }
            }
        }

        items(labReports.filter { it.patientUid == patientUid }) { report ->
            LabReportCard(
                report = report,
                onDownloadClick = {
                    val result = engine.secureDocumentDownload(patientUid, patientUid, report.recordId)
                    downloadedUrl = result.getOrNull()
                }
            )
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 13: PrescriptionsPage
 */
@Composable
fun PrescriptionsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val prescriptions by engine.prescriptions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("prescriptions_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Prescriptions", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        items(prescriptions.filter { it.patientUid == patientUid }) { PrescriptionCard(it) }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 14: PaperPrescriptionUploadPage (Step 50 Core 2.3 Feature Implementation)
 */
@Composable
fun PaperPrescriptionUploadPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("Dr. Tariq Al-Mansoor") }
    var prescriptionDate by remember { mutableStateOf("2026-09-22") }
    var clinicalNotes by remember { mutableStateOf("Post-consultation antibiotic course") }
    var uploadMethod by remember { mutableStateOf("CAMERA") } // CAMERA, GALLERY, PDF
    var selectedFile by remember { mutableStateOf("prescription_capture_2026.jpg") }
    var userConfirmedExtraction by remember { mutableStateOf(false) }
    var uploadSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("paper_prescription_upload_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Upload Paper Prescription", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(
                title = "Confidential Clinical Storage (CMEK Encrypted)",
                description = "Prescriptions are stored in private health vaults (health/uid/prescriptions/...). Zero public accessibility."
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Source Capture Method",
                        style = HealthogramTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = HealthogramTheme.colors.textPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uploadMethod == "CAMERA",
                            onClick = {
                                uploadMethod = "CAMERA"
                                selectedFile = "camera_rx_capture_${System.currentTimeMillis()}.jpg"
                            },
                            label = { Text("Camera Capture") },
                            leadingIcon = { Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = uploadMethod == "GALLERY",
                            onClick = {
                                uploadMethod = "GALLERY"
                                selectedFile = "gallery_rx_photo_${System.currentTimeMillis()}.png"
                            },
                            label = { Text("Photo Gallery") },
                            leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = uploadMethod == "PDF",
                            onClick = {
                                uploadMethod = "PDF"
                                selectedFile = "scanned_rx_document_${System.currentTimeMillis()}.pdf"
                            },
                            label = { Text("PDF Document") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }

                    HealthogramTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Prescription Title (e.g. Dental Treatment Rx)",
                        leadingIcon = Icons.Default.Description
                    )

                    HealthogramTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = "Prescribing Physician / Clinic",
                        leadingIcon = Icons.Default.LocalHospital
                    )

                    HealthogramTextField(
                        value = prescriptionDate,
                        onValueChange = { prescriptionDate = it },
                        label = "Date Prescribed (YYYY-MM-DD)",
                        leadingIcon = Icons.Default.CalendarToday
                    )

                    HealthogramTextField(
                        value = clinicalNotes,
                        onValueChange = { clinicalNotes = it },
                        label = "Doctor Instructions / Notes",
                        leadingIcon = Icons.Default.Notes
                    )

                    SecureUploadCard(
                        onUploadClick = { selectedFile = "rx_${uploadMethod.lowercase()}_${System.currentTimeMillis()}.pdf" }
                    )

                    Text(
                        text = "Selected File: $selectedFile (${if (uploadMethod == "PDF") "1.4 MB, application/pdf" else "3.2 MB, image/jpeg"})",
                        style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = HealthogramTheme.colors.primary
                    )

                    // Step 50 Machine-Assisted Extraction Notice
                    Surface(
                        shape = HealthogramTheme.shapes.medium,
                        color = HealthogramTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, HealthogramTheme.colors.borderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = HealthogramTheme.colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Machine-Assisted Extraction Preview",
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                    color = HealthogramTheme.colors.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Detected: Amoxicillin 500mg, 1 capsule orally every 8 hours for 7 days.",
                                style = HealthogramTheme.typography.caption,
                                color = HealthogramTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Invariant: Machine-extracted medical data is not confirmed until verified by the patient. The original document is permanently preserved and never overwritten.",
                                style = HealthogramTheme.typography.caption.copy(color = HealthogramTheme.colors.textSecondary)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { userConfirmedExtraction = !userConfirmedExtraction }
                            ) {
                                Checkbox(
                                    checked = userConfirmedExtraction,
                                    onCheckedChange = { userConfirmedExtraction = it }
                                )
                                Text(
                                    text = "I confirm extracted medication details match the paper prescription.",
                                    style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                                    color = HealthogramTheme.colors.textPrimary
                                )
                            }
                        }
                    }

                    if (uploadSuccess) {
                        Surface(
                            shape = HealthogramTheme.shapes.medium,
                            color = HealthogramTheme.colors.successContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Prescription successfully saved to your private Health Passport!",
                                modifier = Modifier.padding(12.dp),
                                style = HealthogramTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                                color = HealthogramTheme.colors.success
                            )
                        }
                    }

                    HealthogramPrimaryButton(
                        text = "Upload & Store Securely",
                        state = if (uploadSuccess) ButtonState.SUCCESS else if (!userConfirmedExtraction) ButtonState.DISABLED else ButtonState.NORMAL,
                        onClick = {
                            engine.uploadPaperPrescription(
                                patientUid = patientUid,
                                title = title.ifBlank { "Physical Paper Prescription" },
                                fileName = selectedFile,
                                fileBytesSize = 1420000L,
                                mimeType = if (uploadMethod == "PDF") "application/pdf" else "image/jpeg"
                            )
                            uploadSuccess = true
                        },
                        icon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
