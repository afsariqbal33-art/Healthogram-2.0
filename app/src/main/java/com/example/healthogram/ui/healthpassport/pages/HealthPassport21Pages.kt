package com.example.healthogram.ui.healthpassport.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.example.healthogram.healthpassport.*
import com.example.healthogram.healthpassport.fhir.*

/**
 * HealthDataControlCenterPage
 * Central dashboard for user control of integrations, Health Connect, exports, and access grants.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthDataControlCenterPage(
    patientUid: String = "user_patient_demo",
    onNavigateBack: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToConsent: () -> Unit = {}
) {
    val healthConnect = remember { HealthConnectService.getInstance() }
    val consentService = remember { ConsentManagementService.getInstance() }
    val exportService = remember { HealthDataExportService.getInstance() }

    var connection by remember { mutableStateOf(healthConnect.getConnection(patientUid)) }
    val activeGrants = remember { consentService.getActiveGrants(patientUid) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Data Control Center") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_control_center")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("btn_access_history")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "Access History")
                    }
                }
            )
        },
        modifier = Modifier.testTag("page_health_data_control_center")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Android Health Connect Status
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_health_connect_status"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Health Connect",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Android Health Connect",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Badge(
                                containerColor = if (connection.connectionStatus == HealthConnectStatus.CONNECTED) Color(0xFF2E7D32) else Color.Gray
                            ) {
                                Text(
                                    connection.connectionStatus.name,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Granular data sync for steps, heart rate, sleep, and vitals under Android 16 privacy rules.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (connection.connectionStatus == HealthConnectStatus.CONNECTED) {
                                Button(
                                    onClick = {
                                        connection = healthConnect.revokeConnection(patientUid)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.testTag("btn_disconnect_health_connect")
                                ) {
                                    Text("Disconnect & Revoke")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        connection = healthConnect.requestAndGrantPermissions(
                                            patientUid,
                                            setOf("STEPS", "HEART_RATE", "SLEEP", "WEIGHT")
                                        )
                                    },
                                    modifier = Modifier.testTag("btn_connect_health_connect")
                                ) {
                                    Text("Connect Health Connect")
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Active Healthcare Provider Grants
            item {
                Text(
                    "Active Healthcare Provider Access Grants",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "You have granted scoped access to these verified healthcare organizations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (activeGrants.isEmpty()) {
                item {
                    Text(
                        "No active provider grants. Your Health Passport is completely private.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(activeGrants) { grant ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_${grant.consentId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    grant.organizationName ?: "Verified Clinician",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Purpose: ${grant.purpose}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Categories: ${grant.allowedRecordCategories.joinToString()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    consentService.revokeConsent(patientUid, grant.consentId)
                                },
                                modifier = Modifier.testTag("btn_revoke_${grant.consentId}")
                            ) {
                                Text("Revoke Access Immediately")
                            }
                        }
                    }
                }
            }

            // Section 3: Data Portability & Exports
            item {
                Text(
                    "Health Data Portability & Exports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Export your complete records with cryptographic provenance preserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val rec = exportService.createExportRecord(
                                patientUid = patientUid,
                                requestedByUid = patientUid,
                                format = ExportFormat.FHIR_R4_BUNDLE,
                                categories = listOf("CONDITIONS", "ALLERGIES", "OBSERVATIONS")
                            )
                            exportFeedback = "FHIR R4 Bundle generated. Link expires in 24h: ${rec.exportId.take(8)}"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_fhir")
                    ) {
                        Text("Export FHIR", fontSize = 13.sp)
                    }
                    Button(
                        onClick = {
                            val rec = exportService.createExportRecord(
                                patientUid = patientUid,
                                requestedByUid = patientUid,
                                format = ExportFormat.PDF_SUMMARY,
                                categories = listOf("ALL")
                            )
                            exportFeedback = "Human-readable PDF summary ready: ${rec.exportId.take(8)}"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_export_pdf")
                    ) {
                        Text("Export PDF", fontSize = 13.sp)
                    }
                }
                exportFeedback?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Section 4: Privacy Settings Shortcut
            item {
                OutlinedButton(
                    onClick = onNavigateToConsent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_consent_center")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manage Permissions in Consent Center")
                }
            }
        }
    }
}

/**
 * HealthAccessHistory21Page
 * Auditable log showing who accessed the patient's records, for what purpose, and when.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthAccessHistory21Page(
    patientUid: String = "user_patient_demo",
    onNavigateBack: () -> Unit = {}
) {
    val consentService = remember { ConsentManagementService.getInstance() }
    val audits = remember { consentService.getAuditTrail(patientUid) }
    val emergencyLogs = remember { consentService.getEmergencyLogs(patientUid) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Access History") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_history")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.testTag("page_health_access_history")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Immutable Access Audit Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Every read, export, consent update, and emergency override is recorded here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Emergency Overrides Section (if any)
            if (emergencyLogs.isNotEmpty()) {
                item {
                    Text(
                        "Emergency Access Events",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(emergencyLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("emergency_log_${log.logId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("EMERGENCY OVERRIDE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Clinician: ${log.clinicianRole} at ${log.facilityName}", style = MaterialTheme.typography.bodySmall)
                            Text("Justification: ${log.clinicalJustification}", style = MaterialTheme.typography.bodySmall)
                            Text("Timestamp: ${java.time.Instant.ofEpochMilli(log.timestamp)}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // General Consent and Access Audits
            if (audits.isEmpty() && emergencyLogs.isEmpty()) {
                item {
                    Text(
                        "No access events recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(audits) { audit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("audit_item_${audit.auditId}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Action: ${audit.action}", fontWeight = FontWeight.Bold)
                                Text(
                                    java.time.Instant.ofEpochMilli(audit.timestamp).toString().take(10),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            audit.justification?.let {
                                Text("Notes: $it", style = MaterialTheme.typography.bodySmall)
                            }
                            if (audit.accessedCategories.isNotEmpty()) {
                                Text("Categories: ${audit.accessedCategories.joinToString()}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * ConsentCenterPage
 * Granular toggles for Health Passport, Health Connect, AI, Translation, Marketing, Analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentCenterPage(
    patientUid: String = "user_patient_demo",
    onNavigateBack: () -> Unit = {}
) {
    var healthPassportSharing by remember { mutableStateOf(false) }
    var healthConnectSync by remember { mutableStateOf(true) }
    var aiHealthAssistance by remember { mutableStateOf(false) }
    var translationConsent by remember { mutableStateOf(true) }
    var analyticsConsent by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consent Center") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_consent")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.testTag("page_consent_center")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Privacy & Consent Governance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Control how your data is utilized. Health Passport data is never used for advertising or marketing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Toggle 1: Health Passport Sharing
            item {
                ConsentToggleCard(
                    title = "Healthcare Provider Sharing",
                    description = "Allow verified doctors and hospitals to request scoped clinical data.",
                    isChecked = healthPassportSharing,
                    onCheckedChange = { healthPassportSharing = it },
                    tag = "toggle_provider_sharing"
                )
            }

            // Toggle 2: Health Connect Sync
            item {
                ConsentToggleCard(
                    title = "Android Health Connect Sync",
                    description = "Synchronize steps, heart rate, and sleep from your Android device.",
                    isChecked = healthConnectSync,
                    onCheckedChange = { healthConnectSync = it },
                    tag = "toggle_health_connect"
                )
            }

            // Toggle 3: Healthcare AI Document Assistance
            item {
                ConsentToggleCard(
                    title = "AI Document & Clinical Note Assistance",
                    description = "Allow AI assistance for document classification and summaries. AI never diagnoses.",
                    isChecked = aiHealthAssistance,
                    onCheckedChange = { aiHealthAssistance = it },
                    tag = "toggle_ai_assistance"
                )
            }

            // Toggle 4: Medical Translation
            item {
                ConsentToggleCard(
                    title = "Clinical & Message Translation",
                    description = "Translate healthcare communication and provider summaries safely.",
                    isChecked = translationConsent,
                    onCheckedChange = { translationConsent = it },
                    tag = "toggle_translation"
                )
            }

            // Toggle 5: Telemetry & Platform Analytics
            item {
                ConsentToggleCard(
                    title = "Anonymized Platform Telemetry",
                    description = "Help improve application stability. Never transmits private clinical records.",
                    isChecked = analyticsConsent,
                    onCheckedChange = { analyticsConsent = it },
                    tag = "toggle_analytics"
                )
            }
        }
    }
}

@Composable
fun ConsentToggleCard(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_$tag"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}

/**
 * EmergencyHealthCardPage
 * Displays patient-controlled emergency medical summary for quick first-responder identification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyHealthCardPage(
    card: EmergencyHealthCard = EmergencyHealthCard(
        patientUid = "user_patient_demo",
        fullName = "Ahmed Al-Hinai",
        dateOfBirth = "1988-04-12",
        bloodType = "O+",
        criticalAllergies = listOf("Penicillin (Anaphylaxis)", "Peanuts"),
        lifeSustainingMedications = listOf("Insulin Glargine 20u qhs", "Salbutamol Inhaler PRN"),
        majorConditions = listOf("Type 1 Diabetes Mellitus", "Bronchial Asthma"),
        emergencyContacts = listOf(
            EmergencyContactEntry("Fatima Al-Hinai", "Spouse", "+968 9123 4567", true)
        ),
        specialDirectives = "Wear medical alert bracelet. Hypoglycemia protocol: administer oral glucose if conscious.",
        organDonor = true
    ),
    onNavigateBack: () -> Unit = {}
) {
    var qrAccessEnabled by remember { mutableStateOf(card.qrAccessEnabled) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Health Card") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_emergency_card")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier.testTag("page_emergency_health_card")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_emergency_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Emergency, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "CRITICAL MEDICAL INFORMATION",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Patient: ${card.fullName} (${card.dateOfBirth})",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            "Blood Type: ${card.bloodType}",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            item {
                Text(
                    "Critical Allergies",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                card.criticalAllergies.forEach { allergy ->
                    Text("• $allergy", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Text(
                    "Life-Sustaining Medications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                card.lifeSustainingMedications.forEach { med ->
                    Text("• $med", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Text(
                    "Major Conditions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                card.majorConditions.forEach { cond ->
                    Text("• $cond", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Text(
                    "Emergency Contacts",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                card.emergencyContacts.forEach { contact ->
                    Text(
                        "• ${contact.name} (${contact.relationship}): ${contact.phone}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Text(
                    "Special Directives",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(card.specialDirectives, style = MaterialTheme.typography.bodySmall)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Ephemeral QR Emergency Access", fontWeight = FontWeight.Medium)
                    Switch(
                        checked = qrAccessEnabled,
                        onCheckedChange = { qrAccessEnabled = it },
                        modifier = Modifier.testTag("toggle_emergency_qr")
                    )
                }
            }
        }
    }
}
