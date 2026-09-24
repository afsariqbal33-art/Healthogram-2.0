package com.example.healthogram.healthpassport

import com.example.healthogram.core.AccountType
import java.security.SecureRandom
import java.util.UUID

/**
 * Health ID: Permanent, opaque identifier format: HG-XXXXXXXXXXXX
 * Never encodes national IDs, phone numbers, emails, or medical data.
 */
data class HealthID(
    val healthId: String,
    val uid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val countryCode: String = "US",
    val version: Int = 1
) {
    companion object {
        fun generate(uid: String, countryCode: String = "US"): HealthID {
            val random = SecureRandom()
            val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val sb = StringBuilder("HG-")
            for (i in 0 until 12) {
                sb.append(chars[random.nextInt(chars.length)])
            }
            return HealthID(
                healthId = sb.toString(),
                uid = uid,
                countryCode = countryCode
            )
        }
    }
}

/**
 * health_profiles/{uid}
 * Core physical attributes & emergency metadata. Kept separate from clinical history.
 */
data class HealthProfile(
    val uid: String,
    val healthId: String,
    val dateOfBirth: String = "1990-01-01",
    val bloodGroup: String = "O+",
    val height: String = "175 cm",
    val weight: String = "70 kg",
    val countryCode: String = "US",
    val emergencyContactName: String = "Jane Mercer (Spouse)",
    val emergencyContactPhone: String = "+1 (555) 019-2834",
    val profileStatus: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val version: Int = 1
)

/**
 * health_conditions/{recordId}
 */
data class HealthCondition(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val conditionName: String,
    val conditionCode: String = "ICD-10",
    val description: String = "",
    val status: String = "ACTIVE", // ACTIVE, RESOLVED, CHRONIC
    val diagnosedDate: String = "2024-03-15",
    val resolvedDate: String? = null,
    val severity: String = "MODERATE", // MILD, MODERATE, SEVERE
    val notes: String = "",
    val createdByUid: String,
    val createdByRole: String = "DOCTOR",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sourceProviderId: String = "",
    val version: Int = 1
)

/**
 * health_allergies/{recordId}
 */
data class HealthAllergy(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val allergen: String,
    val reaction: String,
    val severity: String = "HIGH", // LOW, MODERATE, HIGH, LIFE_THREATENING
    val status: String = "ACTIVE",
    val notes: String = "",
    val createdByUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_medications/{recordId}
 */
data class HealthMedication(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val medicineName: String,
    val genericName: String = "",
    val dosage: String,
    val frequency: String,
    val route: String = "Oral",
    val startDate: String = "2026-08-01",
    val endDate: String? = null,
    val status: String = "ACTIVE", // ACTIVE, COMPLETED, DISCONTINUED
    val prescribedByUid: String = "",
    val prescriptionId: String? = null,
    val instructions: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_precautions/{recordId}
 */
data class HealthPrecaution(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val precaution: String,
    val reason: String,
    val severity: String = "MODERATE",
    val status: String = "ACTIVE",
    val createdByUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_visits/{recordId}
 */
data class HealthVisit(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val providerUid: String,
    val providerType: String = "DOCTOR",
    val organizationId: String = "",
    val appointmentId: String? = null,
    val visitDate: String = "2026-09-01",
    val visitType: String = "In-Person Consultation",
    val reason: String = "Routine Health Assessment",
    val clinicalSummary: String = "",
    val diagnosisSummary: String = "",
    val followUpDate: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_diagnoses/{recordId}
 */
data class HealthDiagnosis(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val diagnosis: String,
    val diagnosisCode: String = "ICD-10-CM",
    val status: String = "CONFIRMED", // SUSPECTED, CONFIRMED, REFUTED
    val diagnosedDate: String = "2026-08-20",
    val providerUid: String,
    val organizationId: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_tests/{recordId}
 */
data class HealthTest(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val testName: String,
    val testCode: String = "LOINC",
    val requestedByUid: String,
    val organizationId: String = "",
    val requestedDate: String = "2026-09-02",
    val testDate: String? = "2026-09-03",
    val status: String = "COMPLETED", // ORDERED, IN_PROGRESS, COMPLETED, CANCELLED
    val resultStatus: String = "NORMAL", // NORMAL, ABNORMAL, CRITICAL, PENDING
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_lab_reports/{recordId}
 * Note: reportFilePath points to private bucket; NEVER a public URL.
 */
data class HealthLabReport(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val laboratoryUid: String,
    val organizationId: String = "",
    val testId: String = "",
    val reportDate: String = "2026-09-04",
    val reportStatus: String = "FINAL", // PRELIMINARY, FINAL, AMENDED
    val summary: String = "",
    val reportFilePath: String = "health_private/patient/lab_reports/lab_01.pdf",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Prescribed medicine item within a prescription.
 */
data class PrescribedMedicineItem(
    val medicineName: String,
    val dosage: String,
    val frequency: String,
    val duration: String
)

/**
 * health_prescriptions/{recordId}
 */
data class HealthPrescription(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val doctorUid: String,
    val organizationId: String = "",
    val prescriptionDate: String = "2026-09-02",
    val medications: List<PrescribedMedicineItem> = emptyList(),
    val instructions: String = "",
    val diagnosisReference: String = "",
    val prescriptionFilePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_documents/{recordId}
 */
data class HealthDocument(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val documentType: String = "paper_prescription", // paper_prescription, radiology, lab_report, discharge_summary, bill, other
    val title: String,
    val description: String = "",
    val storagePath: String = "health_private/patient/documents/doc_01.pdf",
    val mimeType: String = "application/pdf",
    val fileSize: Long = 1024000L,
    val documentDate: String = "2026-09-05",
    val uploadedByUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_bills/{recordId}
 * Strict separation: Never connected to marketplace or commerce orders.
 */
data class HealthBill(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val providerUid: String,
    val organizationId: String = "",
    val billNumber: String = "INV-2026-098",
    val billDate: String = "2026-09-01",
    val amount: Double = 150.00,
    val currency: String = "USD",
    val description: String = "Cardiology Consultation & Diagnostic ECG",
    val documentPath: String = "health_private/patient/bills/bill_01.pdf",
    val status: String = "PAID", // PENDING, PAID, INSURANCE_PROCESSING
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * health_notes/{recordId}
 * Private self-notes, symptoms log, observations.
 */
data class HealthNote(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val title: String,
    val content: String,
    val createdByUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Granular Healthcare Scopes for authorization.
 */
enum class HealthScopeKey(val rawKey: String, val label: String) {
    PROFILE("profile", "Health Profile & Emergency Info"),
    MEDICAL_HISTORY("medical_history", "Medical History & Past Procedures"),
    CONDITIONS("conditions", "Health Conditions & Diagnoses"),
    ALLERGIES("allergies", "Allergies & Critical Warnings"),
    MEDICATIONS("medications", "Medications & Dosing Schedule"),
    PRECAUTIONS("precautions", "Clinical Precautions & Advisories"),
    VISITS("visits", "Doctor Visits & Consultations"),
    DIAGNOSES("diagnoses", "Official Clinical Diagnoses"),
    TESTS("tests", "Diagnostic Tests & Orders"),
    LAB_REPORTS("lab_reports", "Laboratory Diagnostic Reports"),
    PRESCRIPTIONS("prescriptions", "Prescriptions & Dosage"),
    DOCUMENTS("documents", "Medical Documents & Records"),
    BILLS("bills", "Medical Expense Bills"),
    NOTES("notes", "Health Notes & Logs");

    companion object {
        fun fromKey(key: String): HealthScopeKey? = entries.find { it.rawKey.equals(key, ignoreCase = true) }
    }
}

/**
 * health_access_requests/{requestId}
 */
data class HealthAccessRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val requesterRole: String, // DOCTOR, CLINIC, HOSPITAL, LABORATORY
    val requesterOrganizationId: String = "",
    val requesterName: String,
    val requestReason: String,
    val requestedScopes: List<String> = listOf("conditions", "allergies", "medications"),
    val status: String = "pending", // pending, approved, rejected, expired, revoked, cancelled
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val approvedByUid: String? = null,
    val denialReason: String? = null,
    val oneTime: Boolean = false,
    val accessLevel: String = "READ_ONLY",
    val countryCode: String = "US"
)

/**
 * health_access_grants/{grantId}
 */
data class HealthAccessGrant(
    val grantId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val requesterRole: String,
    val organizationId: String = "",
    val grantedScopes: List<String>,
    val purpose: String,
    val createdAt: Long = System.currentTimeMillis(),
    val startsAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val status: String = "active", // active, expired, revoked, completed
    val approvedByUid: String,
    val revokedAt: Long? = null,
    val revokedByUid: String? = null,
    val oneTime: Boolean = false,
    val lastAccessAt: Long? = null
) {
    val isCurrentlyActive: Boolean
        get() = status == "active" && System.currentTimeMillis() < expiresAt
}

/**
 * health_access_logs/{logId}
 * CRITICAL: Zero medical data payload inside logs.
 */
data class HealthAccessLog(
    val logId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val requesterRole: String,
    val organizationId: String = "",
    val action: String, // request_created, request_approved, request_rejected, record_viewed, record_created, record_updated, document_downloaded, document_uploaded, permission_revoked, permission_expired, export_requested, export_completed
    val scope: String,
    val resourceType: String,
    val resourceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String = "android_terminal_sec",
    val ipHash: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val result: String = "SUCCESS", // SUCCESS, DENIED, EXPIRED
    val requestId: String = "",
    val grantId: String = ""
)

/**
 * health_qr_sessions/{sessionId}
 * Short-lived token sessions. Never encode raw healthcare data in QR codes.
 */
data class HealthQRSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String = "",
    val requesterRole: String = "",
    val organizationId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000L), // 15 mins
    val status: String = "created", // created, pending, approved, rejected, expired, used, cancelled
    val requestId: String = "",
    val grantId: String = "",
    val nonceHash: String = UUID.randomUUID().toString().replace("-", ""),
    val usedAt: Long? = null
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
}

/**
 * Settings & safe offline configuration.
 */
data class HealthPassportSettings(
    val patientUid: String,
    val offlinePassportEnabled: Boolean = false,
    val permittedOfflineScopes: List<String> = listOf("allergies", "medications"),
    val autoLockTimeoutMinutes: Int = 5,
    val biometricUnlockRequired: Boolean = true,
    val hidePreviewsInNotifications: Boolean = true,
    val allowExportRequests: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
