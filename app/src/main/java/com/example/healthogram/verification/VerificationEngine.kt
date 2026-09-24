package com.example.healthogram.verification

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Platform Verification Engine for Healthogram (Step 07).
 *
 * Implements strict zero-trust role-based verification workflows,
 * dynamic multi-country requirement management, private storage isolation,
 * immutable audit logging, automatic expiration, and scanner eligibility gating.
 */
class VerificationEngine private constructor() {

    // =========================================================================
    // REPOSITORIES & IN-MEMORY FIRESTORE CACHES
    // =========================================================================

    private val _countries = MutableStateFlow<Map<String, CountryVerificationConfig>>(emptyMap())
    val countries: StateFlow<Map<String, CountryVerificationConfig>> = _countries.asStateFlow()

    private val _requirements = MutableStateFlow<List<CountryVerificationRequirement>>(emptyList())
    val requirements: StateFlow<List<CountryVerificationRequirement>> = _requirements.asStateFlow()

    private val _profiles = MutableStateFlow<Map<String, VerificationProfile>>(emptyMap())
    val profiles: StateFlow<Map<String, VerificationProfile>> = _profiles.asStateFlow()

    private val _applications = MutableStateFlow<Map<String, VerificationApplication>>(emptyMap())
    val applications: StateFlow<Map<String, VerificationApplication>> = _applications.asStateFlow()

    private val _documents = MutableStateFlow<Map<String, VerificationDocument>>(emptyMap())
    val documents: StateFlow<Map<String, VerificationDocument>> = _documents.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<VerificationAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<VerificationAuditLog>> = _auditLogs.asStateFlow()

    private val _queue = MutableStateFlow<List<VerificationQueueItem>>(emptyList())
    val queue: StateFlow<List<VerificationQueueItem>> = _queue.asStateFlow()

    // Role mapping for review staff
    private val userRoles = mutableMapOf<String, VerificationRole>()

    init {
        seedInitialConfigurations()
    }

    companion object {
        @Volatile
        private var instance: VerificationEngine? = null

        fun getInstance(): VerificationEngine {
            return instance ?: synchronized(this) {
                instance ?: VerificationEngine().also { instance = it }
            }
        }
    }

    fun resetForTesting() {
        _countries.value = emptyMap()
        _requirements.value = emptyList()
        _profiles.value = emptyMap()
        _applications.value = emptyMap()
        _documents.value = emptyMap()
        _auditLogs.value = emptyList()
        _queue.value = emptyList()
        userRoles.clear()
        seedInitialConfigurations()
    }

    fun setUserRole(uid: String, role: VerificationRole) {
        userRoles[uid] = role
    }

    fun assignRoleForTesting(uid: String, role: VerificationRole) {
        setUserRole(uid, role)
    }

    fun updateProfileForTesting(uid: String, profile: VerificationProfile) {
        _profiles.value = _profiles.value + (uid to profile)
    }

    fun getUserRole(uid: String): VerificationRole {
        return userRoles[uid] ?: VerificationRole.USER
    }

    // =========================================================================
    // 1. DYNAMIC COUNTRY CONFIGURATION
    // =========================================================================

    private fun seedInitialConfigurations() {
        val saudiArabia = CountryVerificationConfig(
            countryCode = "SA",
            countryName = "Saudi Arabia",
            verificationEnabled = true,
            individualVerificationEnabled = true,
            doctorVerificationEnabled = true,
            clinicVerificationEnabled = true,
            hospitalVerificationEnabled = true,
            laboratoryVerificationEnabled = true,
            requiredDocuments = listOf("national_id", "scfhs_license", "commercial_registration"),
            licenseRequired = true,
            businessRegistrationRequired = true,
            identityRequired = true,
            verificationLanguage = "ar"
        )

        val unitedStates = CountryVerificationConfig(
            countryCode = "US",
            countryName = "United States",
            verificationEnabled = true,
            individualVerificationEnabled = true,
            doctorVerificationEnabled = true,
            clinicVerificationEnabled = true,
            hospitalVerificationEnabled = true,
            laboratoryVerificationEnabled = true,
            requiredDocuments = listOf("government_id", "state_medical_license", "business_license"),
            licenseRequired = true,
            businessRegistrationRequired = true,
            identityRequired = true,
            verificationLanguage = "en"
        )

        val unitedKingdom = CountryVerificationConfig(
            countryCode = "GB",
            countryName = "United Kingdom",
            verificationEnabled = true,
            individualVerificationEnabled = true,
            doctorVerificationEnabled = true,
            clinicVerificationEnabled = true,
            hospitalVerificationEnabled = true,
            laboratoryVerificationEnabled = true,
            requiredDocuments = listOf("passport_or_dl", "gmc_license", "companies_house_reg"),
            licenseRequired = true,
            businessRegistrationRequired = true,
            identityRequired = true,
            verificationLanguage = "en"
        )

        val uae = CountryVerificationConfig(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            verificationEnabled = true,
            individualVerificationEnabled = true,
            doctorVerificationEnabled = true,
            clinicVerificationEnabled = true,
            hospitalVerificationEnabled = true,
            laboratoryVerificationEnabled = true,
            requiredDocuments = listOf("emirates_id", "mohap_dha_doh_license", "trade_license"),
            licenseRequired = true,
            businessRegistrationRequired = true,
            identityRequired = true,
            verificationLanguage = "ar"
        )

        _countries.value = mapOf(
            "SA" to saudiArabia,
            "US" to unitedStates,
            "GB" to unitedKingdom,
            "AE" to uae
        )

        val reqs = mutableListOf<CountryVerificationRequirement>()

        // Saudi Arabia (SA)
        reqs.add(CountryVerificationRequirement("req_sa_ind_id", "SA", AccountType.INDIVIDUAL, "national_id", true, "Saudi National ID / Iqama", "Official government identification"))
        reqs.add(CountryVerificationRequirement("req_sa_doc_id", "SA", AccountType.DOCTOR, "national_id", true, "Saudi National ID / Iqama", "Doctor national identity card"))
        reqs.add(CountryVerificationRequirement("req_sa_doc_lic", "SA", AccountType.DOCTOR, "scfhs_license", true, "SCFHS Professional Medical License", "Saudi Commission for Health Specialties license", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_sa_cli_id", "SA", AccountType.CLINIC, "national_id", true, "Authorized Representative ID", "National ID of authorized facility director"))
        reqs.add(CountryVerificationRequirement("req_sa_cli_reg", "SA", AccountType.CLINIC, "commercial_registration", true, "Commercial Registration (CR)", "Ministry of Commerce CR certificate"))
        reqs.add(CountryVerificationRequirement("req_sa_cli_lic", "SA", AccountType.CLINIC, "facility_license", true, "MOH Healthcare Facility License", "Ministry of Health operational facility license", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_sa_hosp_id", "SA", AccountType.HOSPITAL, "national_id", true, "Hospital Director ID", "Authorized hospital administrator identity card"))
        reqs.add(CountryVerificationRequirement("req_sa_hosp_reg", "SA", AccountType.HOSPITAL, "commercial_registration", true, "Hospital Commercial Registration", "Commercial registration for enterprise hospital"))
        reqs.add(CountryVerificationRequirement("req_sa_hosp_lic", "SA", AccountType.HOSPITAL, "facility_license", true, "MOH Hospital License & CBAHI", "Ministry of Health license and CBAHI accreditation", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_sa_lab_id", "SA", AccountType.LABORATORY, "national_id", true, "Laboratory Director ID", "Authorized laboratory director identity card"))
        reqs.add(CountryVerificationRequirement("req_sa_lab_reg", "SA", AccountType.LABORATORY, "commercial_registration", true, "Laboratory Commercial Registration", "Commercial registration for diagnostic laboratory"))
        reqs.add(CountryVerificationRequirement("req_sa_lab_lic", "SA", AccountType.LABORATORY, "lab_license", true, "Diagnostic Laboratory Operating License", "Accredited clinical diagnostic laboratory license", expiryRequired = true))

        // United States (US)
        reqs.add(CountryVerificationRequirement("req_us_ind_id", "US", AccountType.INDIVIDUAL, "national_id", true, "US Driver's License or Passport", "Government issued photo identification"))
        reqs.add(CountryVerificationRequirement("req_us_doc_id", "US", AccountType.DOCTOR, "national_id", true, "State Driver's License / Passport", "Doctor government identity document"))
        reqs.add(CountryVerificationRequirement("req_us_doc_lic", "US", AccountType.DOCTOR, "medical_license", true, "State Medical Board License & NPI", "Active state medical board license with NPI registry confirmation", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_us_cli_id", "US", AccountType.CLINIC, "national_id", true, "Authorized Administrator ID", "Identity document of clinic administrator"))
        reqs.add(CountryVerificationRequirement("req_us_cli_reg", "US", AccountType.CLINIC, "business_registration", true, "State Articles of Organization / Business License", "Official business filing and EIN certificate"))
        reqs.add(CountryVerificationRequirement("req_us_cli_lic", "US", AccountType.CLINIC, "facility_license", true, "State Health Department Facility License", "State clinic license or CLIA certificate", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_us_hosp_id", "US", AccountType.HOSPITAL, "national_id", true, "Hospital Authorized Representative ID", "Executive representative photo identification"))
        reqs.add(CountryVerificationRequirement("req_us_hosp_reg", "US", AccountType.HOSPITAL, "business_registration", true, "Hospital Corporate Registration", "Hospital corporate enterprise certificate"))
        reqs.add(CountryVerificationRequirement("req_us_hosp_lic", "US", AccountType.HOSPITAL, "facility_license", true, "Joint Commission / State Hospital License", "State operating license and Joint Commission accreditation", expiryRequired = true))
        reqs.add(CountryVerificationRequirement("req_us_lab_id", "US", AccountType.LABORATORY, "national_id", true, "Lab Director Identity ID", "Laboratory director identity card"))
        reqs.add(CountryVerificationRequirement("req_us_lab_reg", "US", AccountType.LABORATORY, "business_registration", true, "Laboratory Business Registration", "Commercial corporate registration"))
        reqs.add(CountryVerificationRequirement("req_us_lab_lic", "US", AccountType.LABORATORY, "lab_license", true, "CLIA Laboratory Certificate of Accreditation", "Federal CLIA certificate for diagnostic testing", expiryRequired = true))

        _requirements.value = reqs
    }

    fun getRequirementsForAccount(accountType: AccountType, countryCode: String): List<CountryVerificationRequirement> {
        val code = countryCode.uppercase()
        val specific = _requirements.value.filter { it.countryCode == code && it.accountType == accountType && it.active }
        if (specific.isNotEmpty()) return specific

        // Fallback generic requirements for unsupported/other countries
        return when (accountType) {
            AccountType.INDIVIDUAL -> listOf(
                CountryVerificationRequirement("req_gen_ind", code, accountType, "national_id", true, "National ID or Passport", "Government issued photo identity")
            )
            AccountType.DOCTOR -> listOf(
                CountryVerificationRequirement("req_gen_doc_id", code, accountType, "national_id", true, "National ID or Passport", "Doctor photo identity"),
                CountryVerificationRequirement("req_gen_doc_lic", code, accountType, "medical_license", true, "Professional Medical License", "National/State medical practice license", expiryRequired = true)
            )
            AccountType.CLINIC -> listOf(
                CountryVerificationRequirement("req_gen_cli_id", code, accountType, "national_id", true, "Representative Photo ID", "Authorized representative identity"),
                CountryVerificationRequirement("req_gen_cli_reg", code, accountType, "business_registration", true, "Commercial Registration", "Clinic commercial registration"),
                CountryVerificationRequirement("req_gen_cli_lic", code, accountType, "facility_license", true, "Healthcare Facility Operating License", "Outpatient clinic license", expiryRequired = true)
            )
            AccountType.HOSPITAL -> listOf(
                CountryVerificationRequirement("req_gen_hosp_id", code, accountType, "national_id", true, "Executive Photo ID", "Hospital administrator photo identity"),
                CountryVerificationRequirement("req_gen_hosp_reg", code, accountType, "business_registration", true, "Hospital Enterprise Registration", "Commercial corporate registration"),
                CountryVerificationRequirement("req_gen_hosp_lic", code, accountType, "facility_license", true, "Hospital Facility Accreditation", "National hospital operating license", expiryRequired = true)
            )
            AccountType.LABORATORY -> listOf(
                CountryVerificationRequirement("req_gen_lab_id", code, accountType, "national_id", true, "Laboratory Director Photo ID", "Director identity document"),
                CountryVerificationRequirement("req_gen_lab_reg", code, accountType, "business_registration", true, "Commercial Business Registration", "Laboratory business registration"),
                CountryVerificationRequirement("req_gen_lab_lic", code, accountType, "lab_license", true, "Diagnostic Laboratory Operating License", "Clinical laboratory accreditation", expiryRequired = true)
            )
        }
    }

    // =========================================================================
    // 2. VERIFICATION PROFILES
    // =========================================================================

    fun getOrCreateProfile(user: User): VerificationProfile {
        val existing = _profiles.value[user.uid]
        if (existing != null) return existing

        val newProfile = VerificationProfile(
            uid = user.uid,
            accountType = user.accountType,
            countryCode = user.countryCode,
            verificationStatus = user.verificationStatus,
            verifiedBadge = user.isVerified && user.verificationStatus.isVerifiedState,
            verifiedAt = user.verificationApprovedAt,
            verificationExpiresAt = user.verificationExpiresAt
        )
        _profiles.value = _profiles.value + (user.uid to newProfile)
        return newProfile
    }

    // =========================================================================
    // 3. APPLICATION LIFECYCLE (CLIENT/APPLICANT SIDE)
    // =========================================================================

    /**
     * Start a new verification application draft.
     * Prevents duplicate active applications.
     * Rejects forbidden Pharmacy category.
     */
    fun startApplication(
        applicant: User,
        accountType: AccountType,
        countryCode: String = applicant.countryCode,
        legalName: String = applicant.displayName,
        specialty: String = "",
        facilityAddress: String = ""
    ): Result<VerificationApplication> = startApplication(
        applicant = if (applicant.accountType != accountType) applicant.copy(accountType = accountType) else applicant,
        countryCode = countryCode,
        legalName = legalName,
        specialty = specialty,
        facilityAddress = facilityAddress
    )

    fun startApplication(
        applicant: User,
        countryCode: String,
        legalName: String = applicant.displayName,
        specialty: String = "",
        facilityAddress: String = ""
    ): Result<VerificationApplication> {
        // Enforce Architectural Rule: Pharmacy is forbidden
        if (applicant.accountType.name.equals("PHARMACY", ignoreCase = true)) {
            return Result.failure(SecurityException("Architectural Violation: Pharmacy verification does not exist in Healthogram."))
        }

        // Duplicate active application protection
        val existingActive = _applications.value.values.find {
            it.uid == applicant.uid &&
                    it.status in listOf(VerificationStatus.SUBMITTED, VerificationStatus.UNDER_REVIEW, VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED)
        }
        if (existingActive != null) {
            return Result.failure(IllegalStateException("Duplicate Application: You already have an active application in review (ID: ${existingActive.applicationId})."))
        }

        // Rate limiting / cooldown check on resubmissions
        val profile = getOrCreateProfile(applicant)
        if (profile.nextAllowedSubmissionAt != null && System.currentTimeMillis() < profile.nextAllowedSubmissionAt) {
            val remainingMins = (profile.nextAllowedSubmissionAt - System.currentTimeMillis()) / 60000
            return Result.failure(IllegalStateException("Rate Limited: Please wait $remainingMins minutes before submitting another verification application."))
        }

        val appId = "app_${UUID.randomUUID()}"
        val application = VerificationApplication(
            applicationId = appId,
            uid = applicant.uid,
            accountType = applicant.accountType,
            countryCode = countryCode.uppercase(),
            status = VerificationStatus.DRAFT,
            legalName = legalName,
            specialty = specialty,
            facilityAddress = facilityAddress,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        _applications.value = _applications.value + (appId to application)

        logAudit(
            uid = applicant.uid,
            applicationId = appId,
            action = VerificationAction.APPLICATION_CREATED,
            performedByUid = applicant.uid,
            performedByRole = "user",
            statusBefore = "not_started",
            statusAfter = "draft",
            countryCode = countryCode,
            accountType = applicant.accountType.name
        )

        return Result.success(application)
    }

    /**
     * Upload a private verification document to the application.
     * Strict private storage: verification_private/{uid}/{applicationId}/{documentId}
     * Tokens/masks numbers: stores only last 4 digits.
     */
    fun uploadDocument(
        user: User,
        applicationId: String,
        documentType: String,
        documentName: String = "",
        fileName: String,
        fileSizeBytes: Long = 1024L,
        mimeType: String = "application/pdf",
        documentNumber: String = "",
        issuingAuthority: String = "",
        expiryDate: Long? = null
    ): Result<VerificationDocument> = uploadDocument(
        applicant = user,
        applicationId = applicationId,
        documentType = documentType,
        fileName = fileName,
        mimeType = mimeType,
        fileSize = fileSizeBytes,
        documentNumberFull = documentNumber,
        issuingAuthority = issuingAuthority,
        expiryDate = expiryDate
    )

    fun uploadDocument(
        applicant: User,
        applicationId: String,
        documentType: String,
        fileName: String,
        mimeType: String,
        fileSize: Long,
        documentNumberFull: String = "",
        issuingAuthority: String = "",
        expiryDate: Long? = null
    ): Result<VerificationDocument> {
        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        // Security check: Only document owner may upload
        if (app.uid != applicant.uid) {
            return Result.failure(SecurityException("Access Denied: You can only upload documents to your own verification application."))
        }

        // Only allow uploads while DRAFT or when additional info requested
        if (app.status != VerificationStatus.DRAFT && app.status != VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED) {
            return Result.failure(IllegalStateException("Cannot upload documents when application is in status '${app.status}'."))
        }

        // Normalize & Validate MIME type
        val normalizedMime = when {
            mimeType.contains("pdf", ignoreCase = true) || fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            mimeType.contains("png", ignoreCase = true) || fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            mimeType.contains("jp", ignoreCase = true) || fileName.endsWith(".jpg", ignoreCase = true) || fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            else -> mimeType.lowercase()
        }
        val acceptedTypes = listOf("application/pdf", "image/jpeg", "image/png", "image/jpg")
        if (normalizedMime !in acceptedTypes) {
            return Result.failure(IllegalArgumentException("Unsupported file type: $mimeType. Accepted types: PDF, JPG, PNG."))
        }

        // Validate file size (max 10MB)
        if (fileSize > 10 * 1024 * 1024L) {
            return Result.failure(IllegalArgumentException("File size exceeds 10MB limit."))
        }

        val docId = "doc_${UUID.randomUUID()}"
        val storagePath = "verification_private/${applicant.uid}/$applicationId/$docId"

        // Mask document number (keep only last 4 digits)
        val cleanNum = documentNumberFull.trim().replace(" ", "").replace("-", "")
        val last4 = if (cleanNum.length >= 4) cleanNum.takeLast(4) else cleanNum

        val doc = VerificationDocument(
            documentId = docId,
            applicationId = applicationId,
            uid = applicant.uid,
            countryCode = app.countryCode,
            accountType = app.accountType,
            documentType = documentType,
            storagePath = storagePath,
            fileName = fileName,
            mimeType = normalizedMime,
            fileSize = fileSize,
            documentNumberLast4 = last4,
            expiryDate = expiryDate,
            issuingAuthority = issuingAuthority,
            verificationStatus = "pending",
            uploadedAt = System.currentTimeMillis()
        )

        _documents.value = _documents.value + (docId to doc)

        logAudit(
            uid = applicant.uid,
            applicationId = applicationId,
            action = VerificationAction.DOCUMENT_UPLOADED,
            performedByUid = applicant.uid,
            performedByRole = "user",
            statusBefore = app.status.name.lowercase(),
            statusAfter = app.status.name.lowercase(),
            countryCode = app.countryCode,
            accountType = app.accountType.name,
            metadata = mapOf("documentType" to documentType, "storagePath" to storagePath)
        )

        return Result.success(doc)
    }

    /**
     * Delete document before application submission.
     * Prevents normal users from deleting documents once application is in active review.
     */
    fun deleteDocument(user: User, documentId: String): Result<Unit> {
        val doc = _documents.value[documentId]
            ?: return Result.failure(NoSuchElementException("Document not found."))

        if (doc.uid != user.uid) {
            return Result.failure(SecurityException("Access Denied: You cannot delete another user's document."))
        }

        val app = _applications.value[doc.applicationId]
        if (app != null && app.status in listOf(VerificationStatus.SUBMITTED, VerificationStatus.UNDER_REVIEW)) {
            return Result.failure(IllegalStateException("Cannot delete documents while application is actively under review."))
        }

        _documents.value = _documents.value - documentId

        logAudit(
            uid = user.uid,
            applicationId = doc.applicationId,
            action = VerificationAction.DOCUMENT_REMOVED,
            performedByUid = user.uid,
            performedByRole = "user",
            statusBefore = "draft",
            statusAfter = "draft",
            countryCode = doc.countryCode,
            accountType = doc.accountType.name,
            metadata = mapOf("documentId" to documentId)
        )

        return Result.success(Unit)
    }

    /**
     * Submit verification application for professional review.
     * Runs strict compliance checks against country requirements.
     */
    fun submitApplication(applicant: User, applicationId: String): Result<VerificationApplication> {
        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        if (app.uid != applicant.uid) {
            return Result.failure(SecurityException("Access Denied: You cannot submit another user's application."))
        }

        if (app.status != VerificationStatus.DRAFT && app.status != VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED) {
            return Result.failure(IllegalStateException("Application is already in status '${app.status}'."))
        }

        // Validate mandatory documents for country and account type
        val required = getRequirementsForAccount(app.accountType, app.countryCode).filter { it.required }
        val uploadedDocs = _documents.value.values.filter { it.applicationId == applicationId }
        val uploadedTypes = uploadedDocs.map { it.documentType.lowercase() }.toSet()

        fun matchesRequirement(requiredType: String, uploaded: Set<String>): Boolean {
            val reqLower = requiredType.lowercase()
            if (reqLower in uploaded) return true
            // Support normalized aliases across countries
            if (reqLower in listOf("national_id", "passport_or_dl", "emirates_id", "government_id")) {
                return uploaded.any { it in listOf("national_id", "passport_or_dl", "emirates_id", "government_id") }
            }
            if (reqLower in listOf("medical_license", "scfhs_license", "gmc_license", "mohap_dha_doh_license", "state_medical_license")) {
                return uploaded.any { it in listOf("medical_license", "scfhs_license", "gmc_license", "mohap_dha_doh_license", "state_medical_license") }
            }
            if (reqLower in listOf("commercial_registration", "business_registration", "companies_house_reg", "trade_license", "business_license")) {
                return uploaded.any { it in listOf("commercial_registration", "business_registration", "companies_house_reg", "trade_license", "business_license") }
            }
            if (reqLower in listOf("facility_license", "clinic_license", "hospital_license")) {
                return uploaded.any { it in listOf("facility_license", "clinic_license", "hospital_license") }
            }
            if (reqLower in listOf("lab_license", "clia_certificate", "laboratory_license")) {
                return uploaded.any { it in listOf("lab_license", "clia_certificate", "laboratory_license") }
            }
            return false
        }

        val missing = required.filter { !matchesRequirement(it.documentType, uploadedTypes) }
        if (missing.isNotEmpty()) {
            val missingNames = missing.joinToString(", ") { it.displayName }
            return Result.failure(IllegalArgumentException("Missing mandatory verification documents: $missingNames"))
        }

        val now = System.currentTimeMillis()
        val submittedApp = app.copy(
            status = VerificationStatus.SUBMITTED,
            submittedAt = now,
            updatedAt = now
        )
        _applications.value = _applications.value + (applicationId to submittedApp)

        // Update profile
        val profile = getOrCreateProfile(applicant)
        _profiles.value = _profiles.value + (applicant.uid to profile.copy(
            verificationStatus = VerificationStatus.SUBMITTED,
            lastSubmittedAt = now,
            updatedAt = now
        ))

        // Add to review queue
        val queueItem = VerificationQueueItem(
            queueId = "q_${UUID.randomUUID()}",
            applicationId = applicationId,
            uid = applicant.uid,
            priority = "normal",
            countryCode = app.countryCode,
            accountType = app.accountType,
            status = "unassigned",
            createdAt = now,
            updatedAt = now
        )
        _queue.value = _queue.value + queueItem

        logAudit(
            uid = applicant.uid,
            applicationId = applicationId,
            action = if (app.status == VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED) VerificationAction.APPLICATION_RESUBMITTED else VerificationAction.APPLICATION_SUBMITTED,
            performedByUid = applicant.uid,
            performedByRole = "user",
            statusBefore = app.status.name.lowercase(),
            statusAfter = "submitted",
            countryCode = app.countryCode,
            accountType = app.accountType.name
        )

        return Result.success(submittedApp)
    }

    // =========================================================================
    // 4. REVIEWER & MANAGER WORKFLOW (AUTHORIZED STAFF ONLY)
    // =========================================================================

    private fun checkReviewerAuthorization(reviewer: User): Result<VerificationRole> {
        val role = getUserRole(reviewer.uid)
        if (role !in listOf(VerificationRole.VERIFICATION_REVIEWER, VerificationRole.VERIFICATION_MANAGER, VerificationRole.ADMIN, VerificationRole.OWNER)) {
            return Result.failure(SecurityException("Access Denied: Verification reviewer privileges required."))
        }
        return Result.success(role)
    }

    private fun checkManagerAuthorization(manager: User): Result<VerificationRole> {
        val role = getUserRole(manager.uid)
        if (role !in listOf(VerificationRole.VERIFICATION_MANAGER, VerificationRole.OWNER)) {
            return Result.failure(SecurityException("Access Denied: High-privilege Verification Manager authorization required."))
        }
        return Result.success(role)
    }

    /**
     * Verification reviewer opens and locks application for review.
     */
    fun startReview(reviewer: User, applicationId: String): Result<VerificationApplication> {
        checkReviewerAuthorization(reviewer).onFailure { return Result.failure(it) }

        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        // User cannot review their own application
        if (app.uid == reviewer.uid) {
            return Result.failure(SecurityException("Self-Review Denied: You cannot review or approve your own application."))
        }

        val now = System.currentTimeMillis()
        val inReviewApp = app.copy(
            status = VerificationStatus.UNDER_REVIEW,
            reviewerUid = reviewer.uid,
            reviewStartedAt = now,
            updatedAt = now
        )
        _applications.value = _applications.value + (applicationId to inReviewApp)

        // Update profile
        _profiles.value[app.uid]?.let { p ->
            _profiles.value = _profiles.value + (app.uid to p.copy(
                verificationStatus = VerificationStatus.UNDER_REVIEW,
                reviewerUid = reviewer.uid,
                updatedAt = now
            ))
        }

        // Update queue
        _queue.value = _queue.value.map {
            if (it.applicationId == applicationId) it.copy(status = "in_review", assignedReviewerUid = reviewer.uid, updatedAt = now) else it
        }

        logAudit(
            uid = app.uid,
            applicationId = applicationId,
            action = VerificationAction.REVIEW_STARTED,
            performedByUid = reviewer.uid,
            performedByRole = getUserRole(reviewer.uid).name.lowercase(),
            statusBefore = app.status.name.lowercase(),
            statusAfter = "under_review",
            countryCode = app.countryCode,
            accountType = app.accountType.name
        )

        return Result.success(inReviewApp)
    }

    /**
     * Reviewer requests additional or corrected documentation.
     */
    fun requestAdditionalInformation(
        reviewer: User,
        applicationId: String,
        details: String
    ): Result<VerificationApplication> {
        checkReviewerAuthorization(reviewer).onFailure { return Result.failure(it) }

        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        if (app.uid == reviewer.uid) {
            return Result.failure(SecurityException("Self-Review Denied: You cannot perform review operations on your own application."))
        }

        val now = System.currentTimeMillis()
        val updatedApp = app.copy(
            status = VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED,
            additionalInformationRequired = details,
            reviewerUid = reviewer.uid,
            updatedAt = now
        )
        _applications.value = _applications.value + (applicationId to updatedApp)

        _profiles.value[app.uid]?.let { p ->
            _profiles.value = _profiles.value + (app.uid to p.copy(
                verificationStatus = VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED,
                additionalInformationRequired = details,
                reviewerUid = reviewer.uid,
                updatedAt = now
            ))
        }

        logAudit(
            uid = app.uid,
            applicationId = applicationId,
            action = VerificationAction.ADDITIONAL_INFORMATION_REQUESTED,
            performedByUid = reviewer.uid,
            performedByRole = getUserRole(reviewer.uid).name.lowercase(),
            statusBefore = app.status.name.lowercase(),
            statusAfter = "additional_information_required",
            countryCode = app.countryCode,
            accountType = app.accountType.name,
            metadata = mapOf("details" to details)
        )

        return Result.success(updatedApp)
    }

    /**
     * Approves verification application, activates verified badge, unlocks scanner.
     * Enforces that normal users cannot approve their own application.
     */
    fun approveApplication(
        reviewer: User,
        applicationId: String,
        durationDays: Int = 365
    ): Result<VerificationApplication> {
        checkReviewerAuthorization(reviewer).onFailure { return Result.failure(it) }

        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        // User cannot approve their own application
        if (app.uid == reviewer.uid) {
            return Result.failure(SecurityException("Self-Approval Denied: You cannot approve your own verification application."))
        }

        val now = System.currentTimeMillis()
        val expiresAt = now + (durationDays.toLong() * 24 * 3600 * 1000L)

        val approvedApp = app.copy(
            status = VerificationStatus.VERIFIED,
            reviewerUid = reviewer.uid,
            reviewCompletedAt = now,
            updatedAt = now
        )
        _applications.value = _applications.value + (applicationId to approvedApp)

        // Update profile
        val profile = _profiles.value[app.uid] ?: VerificationProfile(uid = app.uid, accountType = app.accountType, countryCode = app.countryCode)
        _profiles.value = _profiles.value + (app.uid to profile.copy(
            verificationStatus = VerificationStatus.VERIFIED,
            verifiedBadge = true,
            verifiedAt = now,
            verificationExpiresAt = expiresAt,
            reviewerUid = reviewer.uid,
            lastReviewedAt = now,
            rejectionReasonCode = null,
            additionalInformationRequired = null,
            updatedAt = now
        ))

        // Update queue
        _queue.value = _queue.value.map {
            if (it.applicationId == applicationId) it.copy(status = "completed", updatedAt = now) else it
        }

        logAudit(
            uid = app.uid,
            applicationId = applicationId,
            action = VerificationAction.APPLICATION_APPROVED,
            performedByUid = reviewer.uid,
            performedByRole = getUserRole(reviewer.uid).name.lowercase(),
            statusBefore = app.status.name.lowercase(),
            statusAfter = "verified",
            countryCode = app.countryCode,
            accountType = app.accountType.name
        )

        logAudit(
            uid = app.uid,
            applicationId = applicationId,
            action = VerificationAction.BADGE_ACTIVATED,
            performedByUid = reviewer.uid,
            performedByRole = getUserRole(reviewer.uid).name.lowercase(),
            statusBefore = "badge_off",
            statusAfter = "badge_on",
            countryCode = app.countryCode,
            accountType = app.accountType.name
        )

        return Result.success(approvedApp)
    }

    /**
     * Rejects verification application with a standardized safe reason code.
     * Prevents leaking internal reviewer notes to end users.
     */
    fun rejectApplication(
        reviewer: User,
        applicationId: String,
        reasonCode: RejectionReasonCode,
        resubmissionAllowed: Boolean = true,
        cooldownHours: Int = 24,
        internalNotes: String = ""
    ): Result<VerificationApplication> {
        checkReviewerAuthorization(reviewer).onFailure { return Result.failure(it) }

        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        if (app.uid == reviewer.uid) {
            return Result.failure(SecurityException("Self-Action Denied: You cannot reject your own application."))
        }

        val now = System.currentTimeMillis()
        val nextAllowed = if (resubmissionAllowed) now + (cooldownHours.toLong() * 3600 * 1000L) else null

        val rejectedApp = app.copy(
            status = VerificationStatus.REJECTED,
            decisionReasonCode = reasonCode.code,
            reviewerUid = reviewer.uid,
            reviewCompletedAt = now,
            resubmissionAllowed = resubmissionAllowed,
            updatedAt = now
        )
        _applications.value = _applications.value + (applicationId to rejectedApp)

        val profile = _profiles.value[app.uid] ?: VerificationProfile(uid = app.uid, accountType = app.accountType, countryCode = app.countryCode)
        _profiles.value = _profiles.value + (app.uid to profile.copy(
            verificationStatus = VerificationStatus.REJECTED,
            verifiedBadge = false,
            reviewerUid = reviewer.uid,
            lastReviewedAt = now,
            rejectionReasonCode = reasonCode.code,
            resubmissionCount = profile.resubmissionCount + 1,
            lastRejectionAt = now,
            nextAllowedSubmissionAt = nextAllowed,
            updatedAt = now
        ))

        // Update queue
        _queue.value = _queue.value.map {
            if (it.applicationId == applicationId) it.copy(status = "completed", updatedAt = now) else it
        }

        logAudit(
            uid = app.uid,
            applicationId = applicationId,
            action = VerificationAction.APPLICATION_REJECTED,
            performedByUid = reviewer.uid,
            performedByRole = getUserRole(reviewer.uid).name.lowercase(),
            statusBefore = app.status.name.lowercase(),
            statusAfter = "rejected",
            reasonCode = reasonCode.code,
            countryCode = app.countryCode,
            accountType = app.accountType.name
        )

        return Result.success(rejectedApp)
    }

    /**
     * Suspend verification (Requires Verification Manager role).
     * Disables verified badge and healthcare scanner.
     */
    fun suspendVerification(
        manager: User,
        targetUid: String,
        reason: String = "Policy review"
    ): Result<Unit> {
        checkManagerAuthorization(manager).onFailure { return Result.failure(it) }

        val profile = _profiles.value[targetUid]
            ?: return Result.failure(NoSuchElementException("Verification profile not found."))

        val now = System.currentTimeMillis()
        _profiles.value = _profiles.value + (targetUid to profile.copy(
            verificationStatus = VerificationStatus.SUSPENDED,
            verifiedBadge = false,
            updatedAt = now
        ))

        logAudit(
            uid = targetUid,
            applicationId = "sys_suspension",
            action = VerificationAction.VERIFICATION_SUSPENDED,
            performedByUid = manager.uid,
            performedByRole = getUserRole(manager.uid).name.lowercase(),
            statusBefore = profile.verificationStatus.name.lowercase(),
            statusAfter = "suspended",
            countryCode = profile.countryCode,
            accountType = profile.accountType.name,
            metadata = mapOf("reason" to reason)
        )

        logAudit(
            uid = targetUid,
            applicationId = "sys_suspension",
            action = VerificationAction.BADGE_REMOVED,
            performedByUid = manager.uid,
            performedByRole = getUserRole(manager.uid).name.lowercase(),
            statusBefore = "badge_on",
            statusAfter = "badge_off",
            countryCode = profile.countryCode,
            accountType = profile.accountType.name
        )

        return Result.success(Unit)
    }

    /**
     * Revoke verification permanently (Requires Verification Manager role).
     * Disables verified badge and healthcare scanner permanently until full re-application.
     */
    fun revokeVerification(
        manager: User,
        targetUid: String,
        reason: String = "Revoked due to license invalidation"
    ): Result<Unit> {
        checkManagerAuthorization(manager).onFailure { return Result.failure(it) }

        val profile = _profiles.value[targetUid]
            ?: return Result.failure(NoSuchElementException("Verification profile not found."))

        val now = System.currentTimeMillis()
        _profiles.value = _profiles.value + (targetUid to profile.copy(
            verificationStatus = VerificationStatus.REVOKED,
            verifiedBadge = false,
            updatedAt = now
        ))

        logAudit(
            uid = targetUid,
            applicationId = "sys_revocation",
            action = VerificationAction.VERIFICATION_REVOKED,
            performedByUid = manager.uid,
            performedByRole = getUserRole(manager.uid).name.lowercase(),
            statusBefore = profile.verificationStatus.name.lowercase(),
            statusAfter = "revoked",
            countryCode = profile.countryCode,
            accountType = profile.accountType.name,
            metadata = mapOf("reason" to reason)
        )

        logAudit(
            uid = targetUid,
            applicationId = "sys_revocation",
            action = VerificationAction.BADGE_REMOVED,
            performedByUid = manager.uid,
            performedByRole = getUserRole(manager.uid).name.lowercase(),
            statusBefore = "badge_on",
            statusAfter = "badge_off",
            countryCode = profile.countryCode,
            accountType = profile.accountType.name
        )

        return Result.success(Unit)
    }

    /**
     * Scheduled automated task to process expired verifications.
     * Deactivates badge and flags scanner eligibility.
     */
    fun processExpiredVerifications(): Int {
        val now = System.currentTimeMillis()
        var expiredCount = 0

        _profiles.value.values.forEach { profile ->
            if (profile.verificationStatus == VerificationStatus.VERIFIED &&
                profile.verificationExpiresAt != null &&
                profile.verificationExpiresAt <= now
            ) {
                _profiles.value = _profiles.value + (profile.uid to profile.copy(
                    verificationStatus = VerificationStatus.EXPIRED,
                    verifiedBadge = false,
                    updatedAt = now
                ))

                logAudit(
                    uid = profile.uid,
                    applicationId = "sys_expiration_job",
                    action = VerificationAction.VERIFICATION_EXPIRED,
                    performedByUid = "system_cron",
                    performedByRole = "system",
                    statusBefore = "verified",
                    statusAfter = "expired",
                    countryCode = profile.countryCode,
                    accountType = profile.accountType.name
                )

                logAudit(
                    uid = profile.uid,
                    applicationId = "sys_expiration_job",
                    action = VerificationAction.BADGE_REMOVED,
                    performedByUid = "system_cron",
                    performedByRole = "system",
                    statusBefore = "badge_on",
                    statusAfter = "badge_off",
                    countryCode = profile.countryCode,
                    accountType = profile.accountType.name
                )

                expiredCount++
            }
        }
        return expiredCount
    }

    // =========================================================================
    // 5. HEALTHCARE SCANNER INTEGRATION & GATING (STEP 07 SECTION 34)
    // =========================================================================

    /**
     * Validates if a user is authorized to open the healthcare QR scanner.
     * Rule:
     * 1. Account type is Doctor, Clinic, Hospital, Laboratory
     * 2. verification_status == verified
     * 3. Verification has not expired
     * 4. Account is not suspended or revoked
     */
    fun validateHealthcareScannerEligibility(user: User): Result<Unit> {
        val allowedTypes = setOf(AccountType.DOCTOR, AccountType.CLINIC, AccountType.HOSPITAL, AccountType.LABORATORY)
        if (user.accountType !in allowedTypes) {
            return Result.failure(SecurityException("Healthcare verification is required to use the Health Passport scanner."))
        }

        val profile = _profiles.value[user.uid]
        val status = profile?.verificationStatus ?: user.verificationStatus

        if (status == VerificationStatus.SUSPENDED) {
            return Result.failure(SecurityException("Scanner unavailable: Healthcare account verification is suspended."))
        }

        if (status == VerificationStatus.REVOKED) {
            return Result.failure(SecurityException("Scanner unavailable: Healthcare account verification has been revoked."))
        }

        if (status == VerificationStatus.EXPIRED || (profile?.isExpired == true)) {
            return Result.failure(SecurityException("Scanner unavailable: Healthcare license verification has expired. Renewal required."))
        }

        if (!status.isVerifiedState || (profile != null && !profile.verifiedBadge)) {
            return Result.failure(SecurityException("Healthcare verification is required to use the Health Passport scanner."))
        }

        return Result.success(Unit)
    }

    // =========================================================================
    // 6. PRIVATE STORAGE ACCESS & DOCUMENT PERMISSION CONTROL
    // =========================================================================

    /**
     * Enforces private storage access rules:
     * - Document owner can read their own documents.
     * - Authorized reviewers / managers can read documents assigned for review.
     * - Other users and normal admins cannot read documents.
     */
    fun getDocumentSecurely(requester: User, documentId: String): Result<VerificationDocument> {
        val doc = _documents.value[documentId]
            ?: return Result.failure(NoSuchElementException("Verification document not found."))

        // Owner access
        if (doc.uid == requester.uid) {
            return Result.success(doc)
        }

        // Review staff access
        val role = getUserRole(requester.uid)
        if (role in listOf(VerificationRole.VERIFICATION_REVIEWER, VerificationRole.VERIFICATION_MANAGER, VerificationRole.OWNER)) {
            return Result.success(doc)
        }

        // Normal admin or unauthorized users are strictly denied
        return Result.failure(SecurityException("Access Denied: Verification documents are strictly isolated in private storage."))
    }

    /**
     * Retrieves all documents for an application with access control.
     */
    fun getDocumentsForApplication(requester: User, applicationId: String): Result<List<VerificationDocument>> {
        val app = _applications.value[applicationId]
            ?: return Result.failure(NoSuchElementException("Application not found."))

        // Owner check
        if (app.uid == requester.uid) {
            val docs = _documents.value.values.filter { it.applicationId == applicationId }
            return Result.success(docs)
        }

        // Review staff check
        val role = getUserRole(requester.uid)
        if (role in listOf(VerificationRole.VERIFICATION_REVIEWER, VerificationRole.VERIFICATION_MANAGER, VerificationRole.OWNER)) {
            val docs = _documents.value.values.filter { it.applicationId == applicationId }
            return Result.success(docs)
        }

        return Result.failure(SecurityException("Access Denied: You are not authorized to view verification documents for this application."))
    }

    // =========================================================================
    // 7. ACCOUNT TYPE & COUNTRY CHANGE PROTECTION (SECTIONS 29, 30)
    // =========================================================================

    /**
     * Prevents direct unauthorized account category modifications.
     * If account type changes, any existing professional verification is invalidated.
     */
    fun handleAccountTypeChange(user: User, newType: AccountType): Result<Unit> {
        if (newType.name.equals("PHARMACY", ignoreCase = true)) {
            return Result.failure(SecurityException("Architectural Violation: Pharmacy account type is forbidden."))
        }

        val profile = _profiles.value[user.uid]
        if (profile != null && profile.isVerified && profile.accountType != newType) {
            // Invalidate existing verification
            _profiles.value = _profiles.value + (user.uid to profile.copy(
                verificationStatus = VerificationStatus.NOT_STARTED,
                verifiedBadge = false,
                accountType = newType,
                updatedAt = System.currentTimeMillis()
            ))

            logAudit(
                uid = user.uid,
                applicationId = "sys_type_change",
                action = VerificationAction.BADGE_REMOVED,
                performedByUid = user.uid,
                performedByRole = "user",
                statusBefore = "verified",
                statusAfter = "not_started",
                countryCode = profile.countryCode,
                accountType = newType.name,
                metadata = mapOf("reason" to "Account type changed from ${profile.accountType} to $newType")
            )
        }
        return Result.success(Unit)
    }

    /**
     * If user changes country, healthcare verification is re-evaluated.
     */
    fun handleCountryChange(user: User, newCountryCode: String): Result<Unit> {
        val profile = _profiles.value[user.uid]
        if (profile != null && profile.accountType != AccountType.INDIVIDUAL && profile.isVerified) {
            // Healthcare verification does not automatically transfer across countries
            _profiles.value = _profiles.value + (user.uid to profile.copy(
                verificationStatus = VerificationStatus.ADDITIONAL_INFORMATION_REQUIRED,
                countryCode = newCountryCode.uppercase(),
                additionalInformationRequired = "Jurisdiction change detected: Please submit valid medical licenses for $newCountryCode.",
                updatedAt = System.currentTimeMillis()
            ))
        }
        return Result.success(Unit)
    }

    // =========================================================================
    // 8. AUDIT LOGGING (IMMUTABLE, CLIENT CANNOT WRITE)
    // =========================================================================

    private fun logAudit(
        uid: String,
        applicationId: String,
        action: VerificationAction,
        performedByUid: String,
        performedByRole: String,
        statusBefore: String,
        statusAfter: String,
        reasonCode: String? = null,
        countryCode: String,
        accountType: String,
        metadata: Map<String, String> = emptyMap()
    ) {
        val log = VerificationAuditLog(
            logId = "val_${UUID.randomUUID()}",
            uid = uid,
            applicationId = applicationId,
            action = action,
            performedByUid = performedByUid,
            performedByRole = performedByRole,
            timestamp = System.currentTimeMillis(),
            statusBefore = statusBefore,
            statusAfter = statusAfter,
            reasonCode = reasonCode,
            countryCode = countryCode,
            accountType = accountType,
            metadata = metadata
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }
}
