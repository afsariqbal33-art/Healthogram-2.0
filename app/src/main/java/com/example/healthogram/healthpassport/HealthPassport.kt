package com.example.healthogram.healthpassport

import java.util.UUID

/**
 * Health Record Category Types.
 */
enum class HealthRecordType {
    DOCTOR_VISIT,
    DIAGNOSIS,
    DISEASE,
    ALLERGY,
    MEDICATION,
    PRECAUTION,
    TEST,
    LAB_REPORT,
    PRESCRIPTION,
    PAPER_PRESCRIPTION,
    MEDICAL_DOCUMENT,
    MEDICAL_BILL,
    TREATMENT_HISTORY
}

/**
 * Individual medical record entry within a patient's Health Passport.
 */
data class HealthRecord(
    val recordId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val type: HealthRecordType,
    val title: String,
    val description: String,
    val attendingDoctorUid: String? = null,
    val organizationUid: String? = null,
    val organizationName: String? = null,
    val documentSecureUrl: String? = null,
    val isConfidential: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Health Passport: A secure, private, user-controlled medical profile.
 *
 * Stored in private collections (health_passports/{patientUid}) isolated from public profiles.
 * Never exposed through public social APIs or normal push notifications.
 */
data class HealthPassport(
    val healthId: String,
    val patientUid: String,
    val bloodGroup: String = "O+",
    val knownAllergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val recordCount: Int = 0,
    val isLocked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
