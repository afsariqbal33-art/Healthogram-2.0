package com.example.healthogram.verification

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus

/**
 * FlutterFlow Custom Functions & Actions for Step 07 Verification System.
 */
object VerificationFunctions {

    // =========================================================================
    // FLUTTERFLOW CUSTOM FUNCTIONS (UI & PRESENTATION HELPERS)
    // =========================================================================

    fun getVerificationStatusLabel(status: VerificationStatus): String {
        return when (status) {
            VerificationStatus.NOT_STARTED -> "Not Started"
            VerificationStatus.DRAFT -> "Draft"
            VerificationStatus.SUBMITTED -> "Submitted"
            VerificationStatus.UNDER_REVIEW -> "Under Review"
            VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> "Additional Info Required"
            VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> "Verified"
            VerificationStatus.REJECTED -> "Rejected"
            VerificationStatus.SUSPENDED -> "Suspended"
            VerificationStatus.EXPIRED -> "Expired"
            VerificationStatus.REVOKED -> "Revoked"
        }
    }

    fun getVerificationStatusMessage(status: VerificationStatus): String {
        return when (status) {
            VerificationStatus.NOT_STARTED -> "Verify your identity or healthcare credentials to receive the Healthogram Verified Badge."
            VerificationStatus.DRAFT -> "Your verification application is saved as a draft. Upload required documents to submit."
            VerificationStatus.SUBMITTED -> "Your application has been received and is queued for verification audit."
            VerificationStatus.UNDER_REVIEW -> "Your credentials and compliance documents are actively being audited by our verification board."
            VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> "The reviewer requested updated or clearer documentation. Please review missing items."
            VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> "Your account is officially verified on Healthogram Trust Network."
            VerificationStatus.REJECTED -> "Your verification application could not be approved at this time. Please see the resolution guide."
            VerificationStatus.SUSPENDED -> "Verification is temporarily suspended pending document or policy clarification."
            VerificationStatus.EXPIRED -> "Your verification has expired. Please renew your documents to restore your verified badge."
            VerificationStatus.REVOKED -> "Verification was permanently revoked due to credential invalidation."
        }
    }

    fun getVerificationProgress(status: VerificationStatus): Float {
        return when (status) {
            VerificationStatus.NOT_STARTED -> 0.0f
            VerificationStatus.DRAFT -> 0.25f
            VerificationStatus.SUBMITTED -> 0.5f
            VerificationStatus.UNDER_REVIEW -> 0.75f
            VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED -> 0.6f
            VerificationStatus.VERIFIED, VerificationStatus.APPROVED -> 1.0f
            VerificationStatus.REJECTED -> 1.0f
            VerificationStatus.SUSPENDED -> 0.5f
            VerificationStatus.EXPIRED -> 0.0f
            VerificationStatus.REVOKED -> 0.0f
        }
    }

    fun isVerificationActive(profile: VerificationProfile?): Boolean {
        if (profile == null) return false
        return profile.verifiedBadge &&
                profile.verificationStatus.isVerifiedState &&
                !profile.isExpired
    }

    fun isVerificationExpired(profile: VerificationProfile?): Boolean {
        if (profile == null) return false
        return profile.isExpired
    }

    fun shouldShowVerifiedBadge(status: VerificationStatus, isVerified: Boolean): Boolean {
        return isVerified && status.isVerifiedState
    }

    fun getVerificationDocumentLabel(documentType: String): String {
        return when (documentType.lowercase()) {
            "national_id" -> "Government National ID / Passport"
            "medical_license", "scfhs_license" -> "Professional Medical License"
            "facility_license" -> "Healthcare Facility License"
            "commercial_registration", "business_registration" -> "Commercial Business Registration"
            "lab_license" -> "Clinical Laboratory Accreditation"
            else -> documentType.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    fun getVerificationExpiryMessage(expiresAt: Long?): String {
        if (expiresAt == null) return "No expiration set"
        val diffDays = (expiresAt - System.currentTimeMillis()) / (24 * 3600 * 1000L)
        return when {
            diffDays < 0 -> "Expired ${-diffDays} days ago"
            diffDays == 0L -> "Expires today"
            diffDays <= 7 -> "Expires in $diffDays days (Urgent Renewal)"
            diffDays <= 30 -> "Expires in $diffDays days (Renewal Recommended)"
            else -> "Expires in $diffDays days"
        }
    }

    fun getAccountVerificationType(accountType: AccountType): String {
        return when (accountType) {
            AccountType.INDIVIDUAL -> "Individual Identity Verification"
            AccountType.DOCTOR -> "Physician & Specialist Clinical License"
            AccountType.CLINIC -> "Outpatient Healthcare Facility Verification"
            AccountType.HOSPITAL -> "Inpatient Hospital Enterprise Accreditation"
            AccountType.LABORATORY -> "Diagnostic Laboratory Operating Certification"
        }
    }

    // =========================================================================
    // FLUTTERFLOW CUSTOM ACTIONS
    // =========================================================================

    fun startVerificationApplication(
        applicant: User,
        countryCode: String,
        legalName: String,
        specialty: String = "",
        facilityAddress: String = ""
    ): Result<VerificationApplication> {
        return VerificationEngine.getInstance().startApplication(
            applicant = applicant,
            countryCode = countryCode,
            legalName = legalName,
            specialty = specialty,
            facilityAddress = facilityAddress
        )
    }

    fun uploadVerificationDocument(
        applicant: User,
        applicationId: String,
        documentType: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        documentNumber: String = "",
        issuingAuthority: String = "",
        expiryDate: Long? = null
    ): Result<VerificationDocument> {
        return VerificationEngine.getInstance().uploadDocument(
            applicant = applicant,
            applicationId = applicationId,
            documentType = documentType,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            documentNumberFull = documentNumber,
            issuingAuthority = issuingAuthority,
            expiryDate = expiryDate
        )
    }

    fun submitVerification(applicant: User, applicationId: String): Result<VerificationApplication> {
        return VerificationEngine.getInstance().submitApplication(applicant, applicationId)
    }

    fun checkVerificationStatus(user: User): VerificationProfile {
        return VerificationEngine.getInstance().getOrCreateProfile(user)
    }

    fun requestVerificationResubmission(
        reviewer: User,
        applicationId: String,
        details: String
    ): Result<VerificationApplication> {
        return VerificationEngine.getInstance().requestAdditionalInformation(reviewer, applicationId, details)
    }

    fun openVerificationDocument(requester: User, documentId: String): Result<VerificationDocument> {
        return VerificationEngine.getInstance().getDocumentSecurely(requester, documentId)
    }

    fun startVerificationRenewal(user: User): Result<VerificationApplication> {
        return VerificationEngine.getInstance().startApplication(
            applicant = user,
            countryCode = user.countryCode,
            legalName = user.displayName
        )
    }

    fun checkHealthcareVerificationEligibility(user: User): Result<Unit> {
        return VerificationEngine.getInstance().validateHealthcareScannerEligibility(user)
    }
}
