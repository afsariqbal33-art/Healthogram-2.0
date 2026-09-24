package com.example.healthogram.ui.healthpassport

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.healthogram.ui.healthpassport.pages.*

/**
 * HealthPassportPage: Top-level entry point coordinating the 28 Secure Health Passport screens.
 */
@Composable
fun HealthPassportPage(
    modifier: Modifier = Modifier,
    patientUid: String = "user_patient_demo",
    onScanQRClick: () -> Unit = {}
) {
    var currentSubRoute by remember { mutableStateOf("HOME") }

    Box(modifier = modifier.fillMaxSize()) {
        when (currentSubRoute) {
            "HOME" -> HealthPassportHomePage(
                patientUid = patientUid,
                onNavigate = { route ->
                    if (route == "SCANNER") {
                        onScanQRClick()
                    }
                    currentSubRoute = route
                },
                onScanQR = onScanQRClick
            )
            "TIMELINE" -> HealthPassportTimelinePage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTH_PROFILE" -> HealthProfilePage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "MEDICAL_HISTORY" -> MedicalHistoryPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "CONDITIONS" -> ConditionsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "ALLERGIES" -> AllergiesPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "MEDICATIONS" -> MedicationsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "PRECAUTIONS" -> PrecautionsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "DOCTOR_VISITS" -> DoctorVisitsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "DIAGNOSES" -> DiagnosesPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "TESTS" -> TestsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "LAB_REPORTS" -> LabReportsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "PRESCRIPTIONS" -> PrescriptionsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "PAPER_PRESCRIPTION_UPLOAD" -> PaperPrescriptionUploadPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "MEDICAL_DOCUMENTS" -> MedicalDocumentsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "MEDICAL_BILLS" -> MedicalBillsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTH_NOTES" -> HealthNotesPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTHCARE_PROVIDERS" -> HealthcareProvidersPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "ACCESS_REQUESTS" -> HealthAccessRequestsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "ACTIVE_PERMISSIONS" -> HealthActivePermissionsPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "ACCESS_HISTORY" -> HealthAccessHistoryPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTH_SHARING" -> HealthSharingPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTH_ID" -> HealthIDPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "HEALTH_QR" -> HealthQRPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "PASSPORT_SETTINGS" -> HealthPassportSettingsPage(
                patientUid = patientUid,
                onBack = { currentSubRoute = "HOME" },
                onDeleteData = { currentSubRoute = "DELETE_DATA" }
            )
            "SECURE_EXPORT" -> SecureHealthExportPage(patientUid = patientUid, onBack = { currentSubRoute = "HOME" })
            "DELETE_DATA" -> HealthDeleteRequestPage(patientUid = patientUid, onBack = { currentSubRoute = "PASSPORT_SETTINGS" })
            "SCANNER" -> HealthcareQRScannerPage(onBack = { currentSubRoute = "HOME" })
            else -> HealthPassportHomePage(
                patientUid = patientUid,
                onNavigate = { currentSubRoute = it },
                onScanQR = onScanQRClick
            )
        }
    }
}
