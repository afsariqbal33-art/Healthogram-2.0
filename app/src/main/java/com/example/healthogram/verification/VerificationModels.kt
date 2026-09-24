package com.example.healthogram.verification

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationStatus

/**
 * Roles for verification authorization in Step 07.
 */
enum class VerificationRole {
    USER,
    VERIFICATION_REVIEWER,
    VERIFICATION_MANAGER,
    ADMIN,
    OWNER
}

/**
 * Standardized reason codes for verification rejection and information requests.
 * Standardized to prevent leaking internal reviewer notes to end users.
 */
enum class RejectionReasonCode(val code: String, val safeUserExplanation: String) {
    DOCUMENT_UNREADABLE("document_unreadable", "Submitted document image or scan is unreadable or blurry."),
    DOCUMENT_EXPIRED("document_expired", "The submitted license or identification document has expired."),
    DOCUMENT_MISSING("document_missing", "One or more mandatory compliance documents are missing."),
    IDENTITY_MISMATCH("identity_mismatch", "Name or organization details on document do not match profile records."),
    LICENSE_INVALID("license_invalid", "Professional license could not be authenticated with the issuing registry."),
    BUSINESS_REGISTRATION_MISSING("business_registration_missing", "Official commercial facility registration document is missing or invalid."),
    INFORMATION_INCOMPLETE("information_incomplete", "Submitted verification information has missing mandatory fields."),
    DUPLICATE_APPLICATION("duplicate_application", "An active verification application is already in review for this account."),
    COUNTRY_REQUIREMENT_NOT_MET("country_requirement_not_met", "Application does not satisfy statutory regulatory standards for this jurisdiction."),
    VERIFICATION_POLICY_ISSUE("verification_policy_issue", "Application does not meet Healthogram platform trust and verification policies.");

    companion object {
        fun fromCode(code: String?): RejectionReasonCode {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: VERIFICATION_POLICY_ISSUE
        }
    }
}

/**
 * Visual badge states for the Healthogram Verified Badge component.
 */
enum class VerificationBadgeState {
    VERIFIED,
    NOT_VERIFIED,
    PENDING,
    EXPIRED,
    SUSPENDED
}

/**
 * Firestore model: countries/{countryCode}
 */
data class CountryVerificationConfig(
    val countryCode: String,
    val countryName: String,
    val verificationEnabled: Boolean = true,
    val individualVerificationEnabled: Boolean = true,
    val doctorVerificationEnabled: Boolean = true,
    val clinicVerificationEnabled: Boolean = true,
    val hospitalVerificationEnabled: Boolean = true,
    val laboratoryVerificationEnabled: Boolean = true,
    val requiredDocuments: List<String> = emptyList(),
    val optionalDocuments: List<String> = emptyList(),
    val licenseRequired: Boolean = true,
    val businessRegistrationRequired: Boolean = true,
    val identityRequired: Boolean = true,
    val licenseExpiryRequired: Boolean = true,
    val manualReviewRequired: Boolean = true,
    val autoReviewEnabled: Boolean = false,
    val documentRetentionDays: Int = 365,
    val verificationLanguage: String = "en",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Firestore model: country_verification_requirements/{requirementId}
 */
data class CountryVerificationRequirement(
    val requirementId: String,
    val countryCode: String,
    val accountType: AccountType,
    val documentType: String,
    val required: Boolean = true,
    val displayName: String,
    val description: String,
    val acceptedFileTypes: List<String> = listOf("pdf", "jpg", "jpeg", "png"),
    val maxFileSizeMb: Int = 10,
    val validityRequired: Boolean = true,
    val expiryRequired: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Firestore model: verification_profiles/{uid}
 * Contains high-level verification state without sensitive documents.
 */
data class VerificationProfile(
    val uid: String,
    val accountType: AccountType,
    val countryCode: String,
    val verificationStatus: VerificationStatus = VerificationStatus.NOT_STARTED,
    val verifiedBadge: Boolean = false,
    val verifiedAt: Long? = null,
    val verificationExpiresAt: Long? = null,
    val reviewPriority: String = "normal",
    val lastSubmittedAt: Long? = null,
    val lastReviewedAt: Long? = null,
    val reviewerUid: String? = null,
    val rejectionReasonCode: String? = null,
    val additionalInformationRequired: String? = null,
    val resubmissionCount: Int = 0,
    val lastRejectionAt: Long? = null,
    val nextAllowedSubmissionAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isVerified: Boolean
        get() = verifiedBadge && verificationStatus.isVerifiedState &&
                (verificationExpiresAt == null || System.currentTimeMillis() < verificationExpiresAt)

    val isExpired: Boolean
        get() = verificationStatus == VerificationStatus.EXPIRED ||
                (verificationExpiresAt != null && System.currentTimeMillis() >= verificationExpiresAt)

    val isSuspended: Boolean
        get() = verificationStatus == VerificationStatus.SUSPENDED

    val isRevoked: Boolean
        get() = verificationStatus == VerificationStatus.REVOKED
}

/**
 * Firestore model: verification_applications/{applicationId}
 */
data class VerificationApplication(
    val applicationId: String,
    val uid: String,
    val accountType: AccountType,
    val countryCode: String,
    val status: VerificationStatus = VerificationStatus.DRAFT,
    val submittedAt: Long? = null,
    val reviewStartedAt: Long? = null,
    val reviewCompletedAt: Long? = null,
    val reviewerUid: String? = null,
    val decisionReasonCode: String? = null,
    val additionalInformationRequired: String? = null,
    val resubmissionAllowed: Boolean = true,
    val legalName: String = "",
    val licenseNumberMasked: String = "",
    val issuingAuthority: String = "",
    val specialty: String = "",
    val facilityAddress: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Firestore model: verification_documents/{documentId}
 * Strict security: Never store full document/national ID numbers.
 */
data class VerificationDocument(
    val documentId: String,
    val applicationId: String,
    val uid: String,
    val countryCode: String,
    val accountType: AccountType,
    val documentType: String,
    val storagePath: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val documentNumberLast4: String = "",
    val issuedDate: Long? = null,
    val expiryDate: Long? = null,
    val issuingAuthority: String = "",
    val verificationStatus: String = "pending", // pending, approved, rejected
    val uploadedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewerUid: String? = null,
    val rejectionReasonCode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val maskedDocumentNumber: String
        get() = documentNumberLast4
}

/**
 * Audit actions for the verification system.
 */
enum class VerificationAction {
    APPLICATION_CREATED,
    DOCUMENT_UPLOADED,
    DOCUMENT_REMOVED,
    APPLICATION_SUBMITTED,
    REVIEW_STARTED,
    ADDITIONAL_INFORMATION_REQUESTED,
    DOCUMENT_APPROVED,
    DOCUMENT_REJECTED,
    APPLICATION_APPROVED,
    APPLICATION_REJECTED,
    VERIFICATION_SUSPENDED,
    VERIFICATION_REVOKED,
    VERIFICATION_EXPIRED,
    BADGE_ACTIVATED,
    BADGE_REMOVED,
    APPLICATION_RESUBMITTED
}

/**
 * Firestore model: verification_audit_logs/{logId}
 * Immutable audit logs. Cannot be modified or deleted by clients.
 */
data class VerificationAuditLog(
    val logId: String,
    val uid: String,
    val applicationId: String,
    val action: VerificationAction,
    val performedByUid: String,
    val performedByRole: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statusBefore: String,
    val statusAfter: String,
    val reasonCode: String? = null,
    val countryCode: String,
    val accountType: String,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Firestore model: verification_queue/{queueId}
 */
data class VerificationQueueItem(
    val queueId: String,
    val applicationId: String,
    val uid: String,
    val priority: String = "normal",
    val countryCode: String,
    val accountType: AccountType,
    val assignedReviewerUid: String? = null,
    val status: String = "unassigned", // unassigned, assigned, in_review, escalated, completed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
