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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.healthogram.designsystem.HealthogramTheme
import com.example.healthogram.designsystem.components.*
import com.example.healthogram.healthpassport.*
import com.example.healthogram.ui.healthpassport.components.*

/**
 * PAGE 15: MedicalDocumentsPage
 */
@Composable
fun MedicalDocumentsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val documents by engine.documents.collectAsState()
    var downloadedUrl by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("medical_documents_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Medical Documents", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        if (downloadedUrl != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.successContainer)
                ) {
                    Text(
                        text = "Encrypted download link ready:\n$downloadedUrl",
                        modifier = Modifier.padding(12.dp),
                        style = HealthogramTheme.typography.caption,
                        color = HealthogramTheme.colors.success
                    )
                }
            }
        }

        items(documents.filter { it.patientUid == patientUid }) { doc ->
            DocumentCard(
                document = doc,
                onDownloadClick = {
                    val res = engine.secureDocumentDownload(patientUid, patientUid, doc.recordId)
                    downloadedUrl = res.getOrNull()
                }
            )
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 16: MedicalBillsPage
 */
@Composable
fun MedicalBillsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val bills by engine.bills.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("medical_bills_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Medical Bills & Invoices", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
        item {
            HealthPrivacyBanner(
                title = "Strict Medical Expense Isolation",
                description = "Medical invoices are strictly isolated from e-commerce marketplace orders and seller records."
            )
        }
        items(bills.filter { it.patientUid == patientUid }) { bill ->
            MedicalRecordCard(
                title = bill.description,
                category = "Medical Invoice",
                date = bill.billDate,
                subtitle = "Bill #${bill.billNumber} • ${bill.organizationId}",
                statusText = "${bill.currency} ${bill.amount} • ${bill.status}",
                statusColor = HealthogramTheme.colors.success
            ) {
                Text("Protected financial healthcare record.", style = HealthogramTheme.typography.caption, color = HealthogramTheme.colors.textMuted)
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 17: HealthNotesPage
 */
@Composable
fun HealthNotesPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val notes by engine.notes.collectAsState()
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_notes_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Personal Health Notes", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = HealthogramTheme.colors.surface),
                shape = HealthogramTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Private Note", style = HealthogramTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    HealthogramTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = "Note Title / Symptom"
                    )
                    HealthogramTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = "Details / Observations"
                    )
                    HealthogramPrimaryButton(
                        text = "Save Note",
                        onClick = {
                            if (newNoteTitle.isNotBlank()) {
                                engine.addNote(
                                    HealthNote(
                                        patientUid = patientUid,
                                        title = newNoteTitle,
                                        content = newNoteContent,
                                        createdByUid = patientUid
                                    )
                                )
                                newNoteTitle = ""
                                newNoteContent = ""
                            }
                        },
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(notes.filter { it.patientUid == patientUid }) { note ->
            MedicalRecordCard(
                title = note.title,
                category = "Self-Note",
                date = HealthPassportFunctions.formatDate(note.createdAt)
            ) {
                Text(note.content, style = HealthogramTheme.typography.bodySmall)
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 18: HealthcareProvidersPage
 */
@Composable
fun HealthcareProvidersPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val grants by engine.accessGrants.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("healthcare_providers_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Authorized Healthcare Providers", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        val patientGrants = grants.filter { it.patientUid == patientUid }
        if (patientGrants.isEmpty()) {
            item {
                HealthEmptyState(
                    icon = Icons.Default.LocalHospital,
                    title = "No Healthcare Providers Authorized",
                    description = "Clinicians who request access will appear here once approved."
                )
            }
        } else {
            items(patientGrants) { grant ->
                PermissionCard(
                    grant = grant,
                    onRevoke = { engine.revokeHealthAccess(patientUid, grant.grantId) }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 19: HealthAccessRequestsPage
 */
@Composable
fun HealthAccessRequestsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val requests by engine.accessRequests.collectAsState()
    val pendingRequests = requests.filter { it.patientUid == patientUid && it.status == "pending" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_access_requests_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Incoming Access Requests", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(
                title = "Explicit Consent Mandatory",
                description = "Healthcare providers cannot access your records without your permission. You control specific data scopes."
            )
        }

        if (pendingRequests.isEmpty()) {
            item {
                HealthEmptyState(
                    icon = Icons.Default.CheckCircleOutline,
                    title = "No Pending Access Requests",
                    description = "All incoming clinician and hospital authorization requests have been handled."
                )
            }
        } else {
            items(pendingRequests) { req ->
                AccessRequestCard(
                    request = req,
                    onApprove = { scopes, durationHours ->
                        engine.approveHealthAccess(patientUid, req.requestId, scopes, durationHours)
                    },
                    onReject = {
                        engine.rejectHealthAccess(patientUid, req.requestId, "Declined by patient")
                    }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 20: HealthActivePermissionsPage
 */
@Composable
fun HealthActivePermissionsPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val grants by engine.accessGrants.collectAsState()
    val activeGrants = grants.filter { it.patientUid == patientUid && it.isCurrentlyActive }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_active_permissions_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Active Authorizations", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        if (activeGrants.isEmpty()) {
            item {
                HealthEmptyState(
                    icon = Icons.Default.Lock,
                    title = "Health Vault is Fully Locked",
                    description = "No clinical third-parties currently hold active permission to read your medical records."
                )
            }
        } else {
            items(activeGrants) { grant ->
                PermissionCard(
                    grant = grant,
                    onRevoke = { engine.revokeHealthAccess(patientUid, grant.grantId) }
                )
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}

/**
 * PAGE 21: HealthAccessHistoryPage
 */
@Composable
fun HealthAccessHistoryPage(
    patientUid: String = "user_patient_demo",
    onBack: () -> Unit = {}
) {
    val engine = remember { HealthPassportEngine.getInstance() }
    val logs by engine.accessLogs.collectAsState()
    val patientLogs = logs.filter { it.patientUid == patientUid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(HealthogramTheme.colors.background)
            .padding(horizontal = 16.dp)
            .testTag("health_access_history_page"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                Text("Access Audit Logs", style = HealthogramTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }

        item {
            HealthPrivacyBanner(
                title = "Tamper-Proof Audit Trail",
                description = "Every read, write, export, or authorization change is immutably logged with timestamp and provider identity."
            )
        }

        if (patientLogs.isEmpty()) {
            item {
                HealthEmptyState(
                    icon = Icons.Default.History,
                    title = "No Audit Events Yet",
                    description = "Any access to your medical vault will appear in this ledger."
                )
            }
        } else {
            items(patientLogs) { log ->
                AccessHistoryItem(log)
            }
        }
        item { Spacer(modifier = Modifier.height(48.dp)) }
    }
}
