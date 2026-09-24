package com.example.healthogram.healthpassport

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import java.security.SecureRandom
import java.util.UUID

/**
 * Granular permissions granted by a patient for their Health Passport.
 */
enum class HealthAccessScope(val displayName: String) {
    FULL("Full Health History"),
    DIAGNOSTIC_ONLY("Diagnoses & Clinical Notes"),
    PRESCRIPTIONS_ONLY("Medications & Prescriptions"),
    LAB_ONLY("Laboratory Reports & Orders"),
    EMERGENCY_ONLY("Blood Group, Allergies & Emergency Contact")
}

/**
 * Access consent status.
 */
enum class AccessConsentStatus {
    PENDING_PATIENT_APPROVAL,
    ACTIVE,
    REVOKED_BY_PATIENT,
    EXPIRED,
    REJECTED
}

/**
 * QR Code Access Token Representation.
 *
 * CRITICAL ARCHITECTURAL RULE:
 * The QR code does NOT contain raw medical records.
 * It contains a short-lived cryptographically secure session ticket used to initiate
 * authenticated, role-verified, and consent-driven access workflows.
 */
data class HealthPassportQRCode(
    val patientUid: String,
    val healthId: String,
    val sessionTicket: String = generateSecureTicket(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minute ticket
) {
    val isTicketExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt

    companion object {
        fun generateSecureTicket(): String {
            val random = SecureRandom()
            val bytes = ByteArray(24)
            random.nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

/**
 * Patient Authorization Consent record.
 */
data class PatientConsent(
    val consentId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val authorizedEntityUid: String,
    val authorizedEntityName: String,
    val authorizedAccountType: AccountType,
    val grantedScope: HealthAccessScope,
    val status: AccessConsentStatus = AccessConsentStatus.ACTIVE,
    val grantedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000), // Default 24 hours
    val revokedAt: Long? = null
) {
    val isCurrentlyValid: Boolean
        get() = status == AccessConsentStatus.ACTIVE && System.currentTimeMillis() <= expiresAt
}

/**
 * Immutable Audit Record for every sensitive access event to patient health information.
 */
data class HealthAuditRecord(
    val auditId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val accessedByUid: String,
    val accessedByName: String,
    val organizationName: String,
    val requesterAccountType: AccountType,
    val requestedScope: HealthAccessScope,
    val purpose: String,
    val timestamp: Long = System.currentTimeMillis(),
    val ipAddressOrDevice: String = "MobileClient",
    val isGranted: Boolean,
    val denialReason: String? = null
)

/**
 * Core Security Manager for Health Passport operations.
 */
class HealthPassportSecurityManager {

    private val auditLogs = mutableListOf<HealthAuditRecord>()
    private val activeConsents = mutableMapOf<String, PatientConsent>()

    /**
     * Evaluates whether an account is eligible to scan and initiate access.
     */
    fun validateScannerEligibility(scanner: User): Result<Unit> {
        if (!scanner.isVerified) {
            return Result.failure(
                SecurityException("Access Denied: Only verified healthcare accounts may access Health Passport scanning.")
            )
        }

        val eligibleTypes = setOf(
            AccountType.DOCTOR,
            AccountType.CLINIC,
            AccountType.HOSPITAL,
            AccountType.LABORATORY
        )

        if (scanner.accountType !in eligibleTypes) {
            return Result.failure(
                SecurityException("Access Denied: Account category ${scanner.accountType} is not authorized for clinical scanning.")
            )
        }

        return Result.success(Unit)
    }

    /**
     * Executes access request with comprehensive audit logging and consent validation.
     */
    fun evaluateAccessRequest(
        scanner: User,
        patient: User,
        requestedScope: HealthAccessScope,
        purpose: String,
        existingConsent: PatientConsent?
    ): Result<PatientConsent> {
        // Step 1: Scanner verification check
        val eligibility = validateScannerEligibility(scanner)
        if (eligibility.isFailure) {
            val log = HealthAuditRecord(
                patientUid = patient.uid,
                accessedByUid = scanner.uid,
                accessedByName = scanner.displayName,
                organizationName = scanner.displayName,
                requesterAccountType = scanner.accountType,
                requestedScope = requestedScope,
                purpose = purpose,
                isGranted = false,
                denialReason = eligibility.exceptionOrNull()?.message
            )
            auditLogs.add(log)
            return Result.failure(eligibility.exceptionOrNull()!!)
        }

        // Step 2: Consent validation
        if (existingConsent == null || !existingConsent.isCurrentlyValid) {
            val log = HealthAuditRecord(
                patientUid = patient.uid,
                accessedByUid = scanner.uid,
                accessedByName = scanner.displayName,
                organizationName = scanner.displayName,
                requesterAccountType = scanner.accountType,
                requestedScope = requestedScope,
                purpose = purpose,
                isGranted = false,
                denialReason = "Explicit patient consent is required before accessing records."
            )
            auditLogs.add(log)
            return Result.failure(
                IllegalStateException("Active patient consent required. Request sent to patient.")
            )
        }

        // Step 3: Success audit log
        val log = HealthAuditRecord(
            patientUid = patient.uid,
            accessedByUid = scanner.uid,
            accessedByName = scanner.displayName,
            organizationName = scanner.displayName,
            requesterAccountType = scanner.accountType,
            requestedScope = requestedScope,
            purpose = purpose,
            isGranted = true
        )
        auditLogs.add(log)
        return Result.success(existingConsent)
    }

    fun getAuditLogsForPatient(patientUid: String): List<HealthAuditRecord> {
        return auditLogs.filter { it.patientUid == patientUid }
    }
}
