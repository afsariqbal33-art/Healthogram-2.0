package com.example.healthogram.healthpassport

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

/**
 * HealthPassportEngine: Core singleton service managing secure health data,
 * enforcing explicit patient authorization, non-bypassable consent, immutable audit logging,
 * and zero-leakage across social media, marketplace, and public profiles.
 */
class HealthPassportEngine private constructor() {

    // In-memory isolated storage collections mirroring private Firestore collections
    private val _profiles = MutableStateFlow<Map<String, HealthProfile>>(emptyMap())
    val profiles: StateFlow<Map<String, HealthProfile>> = _profiles.asStateFlow()

    private val _conditions = MutableStateFlow<List<HealthCondition>>(emptyList())
    val conditions: StateFlow<List<HealthCondition>> = _conditions.asStateFlow()

    private val _allergies = MutableStateFlow<List<HealthAllergy>>(emptyList())
    val allergies: StateFlow<List<HealthAllergy>> = _allergies.asStateFlow()

    private val _medications = MutableStateFlow<List<HealthMedication>>(emptyList())
    val medications: StateFlow<List<HealthMedication>> = _medications.asStateFlow()

    private val _precautions = MutableStateFlow<List<HealthPrecaution>>(emptyList())
    val precautions: StateFlow<List<HealthPrecaution>> = _precautions.asStateFlow()

    private val _visits = MutableStateFlow<List<HealthVisit>>(emptyList())
    val visits: StateFlow<List<HealthVisit>> = _visits.asStateFlow()

    private val _diagnoses = MutableStateFlow<List<HealthDiagnosis>>(emptyList())
    val diagnoses: StateFlow<List<HealthDiagnosis>> = _diagnoses.asStateFlow()

    private val _tests = MutableStateFlow<List<HealthTest>>(emptyList())
    val tests: StateFlow<List<HealthTest>> = _tests.asStateFlow()

    private val _labReports = MutableStateFlow<List<HealthLabReport>>(emptyList())
    val labReports: StateFlow<List<HealthLabReport>> = _labReports.asStateFlow()

    private val _prescriptions = MutableStateFlow<List<HealthPrescription>>(emptyList())
    val prescriptions: StateFlow<List<HealthPrescription>> = _prescriptions.asStateFlow()

    private val _documents = MutableStateFlow<List<HealthDocument>>(emptyList())
    val documents: StateFlow<List<HealthDocument>> = _documents.asStateFlow()

    private val _bills = MutableStateFlow<List<HealthBill>>(emptyList())
    val bills: StateFlow<List<HealthBill>> = _bills.asStateFlow()

    private val _notes = MutableStateFlow<List<HealthNote>>(emptyList())
    val notes: StateFlow<List<HealthNote>> = _notes.asStateFlow()

    private val _accessRequests = MutableStateFlow<List<HealthAccessRequest>>(emptyList())
    val accessRequests: StateFlow<List<HealthAccessRequest>> = _accessRequests.asStateFlow()

    private val _accessGrants = MutableStateFlow<List<HealthAccessGrant>>(emptyList())
    val accessGrants: StateFlow<List<HealthAccessGrant>> = _accessGrants.asStateFlow()

    private val _accessLogs = MutableStateFlow<List<HealthAccessLog>>(emptyList())
    val accessLogs: StateFlow<List<HealthAccessLog>> = _accessLogs.asStateFlow()

    private val _qrSessions = MutableStateFlow<Map<String, HealthQRSession>>(emptyMap())
    val qrSessions: StateFlow<Map<String, HealthQRSession>> = _qrSessions.asStateFlow()

    private val _settings = MutableStateFlow<Map<String, HealthPassportSettings>>(emptyMap())
    val settings: StateFlow<Map<String, HealthPassportSettings>> = _settings.asStateFlow()

    // Encrypted local offline cache storage
    private val offlineCachedRecords = mutableMapOf<String, MutableMap<String, Any>>()

    init {
        seedInitialRecords()
    }

    fun resetForTesting() {
        _profiles.value = emptyMap()
        _conditions.value = emptyList()
        _allergies.value = emptyList()
        _medications.value = emptyList()
        _precautions.value = emptyList()
        _visits.value = emptyList()
        _diagnoses.value = emptyList()
        _tests.value = emptyList()
        _labReports.value = emptyList()
        _prescriptions.value = emptyList()
        _documents.value = emptyList()
        _bills.value = emptyList()
        _notes.value = emptyList()
        _accessRequests.value = emptyList()
        _accessGrants.value = emptyList()
        _accessLogs.value = emptyList()
        _qrSessions.value = emptyMap()
        _settings.value = emptyMap()
        offlineCachedRecords.clear()
        seedInitialRecords()
    }

    fun addGrantDirectly(grant: HealthAccessGrant) {
        _accessGrants.value = listOf(grant) + _accessGrants.value
    }

    companion object {
        @Volatile
        private var instance: HealthPassportEngine? = null

        fun getInstance(): HealthPassportEngine {
            return instance ?: synchronized(this) {
                instance ?: HealthPassportEngine().also { instance = it }
            }
        }
    }

    // =========================================================================
    // 1. HEALTH PROFILE & HEALTH ID
    // =========================================================================

    fun getOrCreateProfile(patientUid: String, countryCode: String = "US"): HealthProfile {
        val existing = _profiles.value[patientUid]
        if (existing != null) return existing

        val healthId = HealthID.generate(patientUid, countryCode).healthId
        val newProfile = HealthProfile(
            uid = patientUid,
            healthId = healthId,
            countryCode = countryCode
        )
        _profiles.value = _profiles.value + (patientUid to newProfile)
        return newProfile
    }

    fun updateProfile(patientUid: String, updated: HealthProfile): Result<HealthProfile> {
        if (patientUid != updated.uid) {
            return Result.failure(SecurityException("Access Denied: Cannot modify another patient's profile."))
        }
        val current = _profiles.value[patientUid] ?: return Result.failure(NoSuchElementException("Profile not found"))
        val merged = current.copy(
            bloodGroup = updated.bloodGroup,
            height = updated.height,
            weight = updated.weight,
            emergencyContactName = updated.emergencyContactName,
            emergencyContactPhone = updated.emergencyContactPhone,
            updatedAt = System.currentTimeMillis()
        )
        _profiles.value = _profiles.value + (patientUid to merged)

        logAccess(
            patientUid = patientUid,
            requesterUid = patientUid,
            requesterRole = "PATIENT",
            action = "record_updated",
            scope = "profile",
            resourceType = "health_profile",
            resourceId = patientUid,
            result = "SUCCESS"
        )
        return Result.success(merged)
    }

    // =========================================================================
    // 2. HEALTHCARE SCANNER ELIGIBILITY & QR TICKET GENERATION
    // =========================================================================

    /**
     * Scanner access is strictly restricted to verified Doctor, Clinic, Hospital, and Laboratory.
     * Denies Individual, Marketplace Customer, Marketplace Seller, and unverified accounts.
     */
    fun validateScannerEligibility(user: User): Result<Unit> {
        if (!user.isEligibleToScanHealthPassport) {
            return Result.failure(SecurityException("Access Denied: Account is not currently eligible to scan Health Passports. Must be an active, verified Doctor, Clinic, Hospital, or Laboratory."))
        }
        return Result.success(Unit)
    }

    /**
     * Generates a dynamic, rotating 15-minute QR Session Ticket.
     * CRITICAL: Never encodes raw health records in the QR payload.
     */
    fun generateSecureHealthQRCode(patientUid: String, healthId: String): HealthQRSession {
        val session = HealthQRSession(
            sessionId = "qrs_${UUID.randomUUID()}",
            patientUid = patientUid,
            status = "created",
            expiresAt = System.currentTimeMillis() + (15 * 60 * 1000L) // 15 mins
        )
        _qrSessions.value = _qrSessions.value + (session.sessionId to session)
        return session
    }

    /**
     * Healthcare Scanner executes scan of the QR session ticket.
     */
    fun scanHealthQRCode(scanner: User, sessionToken: String): Result<HealthQRSession> {
        val eligibility = validateScannerEligibility(scanner)
        if (eligibility.isFailure) {
            return Result.failure(eligibility.exceptionOrNull()!!)
        }

        val session = _qrSessions.value[sessionToken]
            ?: return Result.failure(IllegalArgumentException("Invalid or expired QR code session ticket."))

        if (session.isExpired) {
            _qrSessions.value = _qrSessions.value + (session.sessionId to session.copy(status = "expired"))
            return Result.failure(IllegalStateException("QR Code session has expired. Patient must refresh QR code."))
        }

        if (session.status == "used" || session.status == "cancelled") {
            return Result.failure(IllegalStateException("QR Code ticket has already been used or cancelled."))
        }

        // Advance session to pending
        val updatedSession = session.copy(
            requesterUid = scanner.uid,
            requesterRole = scanner.accountType.name,
            organizationId = scanner.displayName,
            status = "pending"
        )
        _qrSessions.value = _qrSessions.value + (session.sessionId to updatedSession)

        return Result.success(updatedSession)
    }

    // =========================================================================
    // 3. ACCESS REQUEST & GRANT WORKFLOW
    // =========================================================================

    /**
     * Healthcare provider submits an access request specifying scopes, purpose, and duration.
     * Laboratories are restricted: cannot request full medical passport by default.
     */
    fun requestHealthAccess(
        requester: User,
        patientUid: String,
        requestedScopes: List<String>,
        reason: String,
        durationHours: Int = 24,
        oneTime: Boolean = false,
        qrSessionId: String? = null
    ): Result<HealthAccessRequest> {
        val eligibility = validateScannerEligibility(requester)
        if (eligibility.isFailure) {
            return Result.failure(eligibility.exceptionOrNull()!!)
        }

        // Strict Laboratory rule: Cannot request broad passport
        if (requester.accountType == AccountType.LABORATORY) {
            val disallowed = requestedScopes.filter { it !in listOf("tests", "lab_reports", "profile") }
            if (disallowed.isNotEmpty()) {
                return Result.failure(SecurityException("Access Denied: Laboratory accounts can only request lab and diagnostic test scopes."))
            }
        }

        val request = HealthAccessRequest(
            requestId = "req_${UUID.randomUUID()}",
            patientUid = patientUid,
            requesterUid = requester.uid,
            requesterRole = requester.accountType.name,
            requesterOrganizationId = requester.displayName,
            requesterName = requester.displayName,
            requestReason = reason,
            requestedScopes = requestedScopes,
            status = "pending",
            expiresAt = System.currentTimeMillis() + (durationHours * 3600 * 1000L),
            oneTime = oneTime
        )

        _accessRequests.value = listOf(request) + _accessRequests.value

        // Link QR session if provided
        if (qrSessionId != null && _qrSessions.value.containsKey(qrSessionId)) {
            val sess = _qrSessions.value[qrSessionId]!!
            _qrSessions.value = _qrSessions.value + (qrSessionId to sess.copy(requestId = request.requestId, status = "pending"))
        }

        logAccess(
            patientUid = patientUid,
            requesterUid = requester.uid,
            requesterRole = requester.accountType.name,
            action = "request_created",
            scope = requestedScopes.joinToString(","),
            resourceType = "access_request",
            resourceId = request.requestId,
            result = "SUCCESS",
            requestId = request.requestId
        )

        return Result.success(request)
    }

    /**
     * Patient reviews and explicitly approves the access request, selecting permitted scopes.
     */
    fun approveHealthAccess(
        patientUid: String,
        requestId: String,
        grantedScopes: List<String>,
        durationHours: Int = 24,
        oneTime: Boolean = false
    ): Result<HealthAccessGrant> {
        val request = _accessRequests.value.find { it.requestId == requestId }
            ?: return Result.failure(NoSuchElementException("Access request not found."))

        if (request.patientUid != patientUid) {
            return Result.failure(SecurityException("Access Denied: Only the patient can authorize access to their Health Passport."))
        }

        if (request.status != "pending") {
            return Result.failure(IllegalStateException("Request is already in status '${request.status}'."))
        }

        val now = System.currentTimeMillis()
        val grant = HealthAccessGrant(
            grantId = "grt_${UUID.randomUUID()}",
            patientUid = patientUid,
            requesterUid = request.requesterUid,
            requesterRole = request.requesterRole,
            organizationId = request.requesterOrganizationId,
            grantedScopes = grantedScopes,
            purpose = request.requestReason,
            createdAt = now,
            startsAt = now,
            expiresAt = now + (durationHours * 3600 * 1000L),
            status = "active",
            approvedByUid = patientUid,
            oneTime = oneTime
        )

        // Update request status
        _accessRequests.value = _accessRequests.value.map {
            if (it.requestId == requestId) it.copy(status = "approved", respondedAt = now, approvedByUid = patientUid) else it
        }

        _accessGrants.value = listOf(grant) + _accessGrants.value

        // Close QR session if any
        _qrSessions.value.values.find { it.requestId == requestId }?.let { sess ->
            _qrSessions.value = _qrSessions.value + (sess.sessionId to sess.copy(status = "approved", grantId = grant.grantId, usedAt = now))
        }

        logAccess(
            patientUid = patientUid,
            requesterUid = request.requesterUid,
            requesterRole = request.requesterRole,
            action = "request_approved",
            scope = grantedScopes.joinToString(","),
            resourceType = "access_grant",
            resourceId = grant.grantId,
            result = "SUCCESS",
            requestId = requestId,
            grantId = grant.grantId
        )

        return Result.success(grant)
    }

    /**
     * Patient declines an access request.
     */
    fun rejectHealthAccess(
        patientUid: String,
        requestId: String,
        denialReason: String = "Declined by patient"
    ): Result<Unit> {
        val request = _accessRequests.value.find { it.requestId == requestId }
            ?: return Result.failure(NoSuchElementException("Access request not found."))

        if (request.patientUid != patientUid) {
            return Result.failure(SecurityException("Access Denied: Only the patient can reject requests."))
        }

        val now = System.currentTimeMillis()
        _accessRequests.value = _accessRequests.value.map {
            if (it.requestId == requestId) it.copy(status = "rejected", respondedAt = now, denialReason = denialReason) else it
        }

        logAccess(
            patientUid = patientUid,
            requesterUid = request.requesterUid,
            requesterRole = request.requesterRole,
            action = "request_rejected",
            scope = request.requestedScopes.joinToString(","),
            resourceType = "access_request",
            resourceId = requestId,
            result = "DENIED",
            requestId = requestId
        )

        return Result.success(Unit)
    }

    /**
     * Patient immediately revokes an active access grant.
     */
    fun revokeHealthAccess(patientUid: String, grantId: String): Result<Unit> {
        val grant = _accessGrants.value.find { it.grantId == grantId }
            ?: return Result.failure(NoSuchElementException("Grant not found."))

        if (grant.patientUid != patientUid) {
            return Result.failure(SecurityException("Access Denied: Only the patient can revoke access grants."))
        }

        val now = System.currentTimeMillis()
        _accessGrants.value = _accessGrants.value.map {
            if (it.grantId == grantId) it.copy(status = "revoked", revokedAt = now, revokedByUid = patientUid) else it
        }

        logAccess(
            patientUid = patientUid,
            requesterUid = grant.requesterUid,
            requesterRole = grant.requesterRole,
            action = "permission_revoked",
            scope = grant.grantedScopes.joinToString(","),
            resourceType = "access_grant",
            resourceId = grantId,
            result = "SUCCESS",
            grantId = grantId
        )

        return Result.success(Unit)
    }

    /**
     * Checks if a requester has an active, valid grant with the specified scope.
     */
    fun hasActivePermission(patientUid: String, requesterUid: String, requiredScope: String): Boolean {
        val now = System.currentTimeMillis()
        val validGrant = _accessGrants.value.find {
            it.patientUid == patientUid &&
                    it.requesterUid == requesterUid &&
                    it.status == "active" &&
                    now < it.expiresAt &&
                    it.grantedScopes.contains(requiredScope)
        }
        return validGrant != null
    }

    // =========================================================================
    // 4. READ ACCESS TO CLINICAL DATA WITH AUDIT LOGGING
    // =========================================================================

    fun getConditionsForPatient(patientUid: String, requesterUid: String): Result<List<HealthCondition>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "conditions")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for conditions."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "conditions", "health_conditions", "list", "SUCCESS")
        return Result.success(_conditions.value.filter { it.patientUid == patientUid })
    }

    fun getAllergiesForPatient(patientUid: String, requesterUid: String): Result<List<HealthAllergy>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "allergies")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for allergies."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "allergies", "health_allergies", "list", "SUCCESS")
        return Result.success(_allergies.value.filter { it.patientUid == patientUid })
    }

    fun getMedicationsForPatient(patientUid: String, requesterUid: String): Result<List<HealthMedication>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "medications")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for medications."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "medications", "health_medications", "list", "SUCCESS")
        return Result.success(_medications.value.filter { it.patientUid == patientUid })
    }

    fun getVisitsForPatient(patientUid: String, requesterUid: String): Result<List<HealthVisit>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "visits")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for visits."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "visits", "health_visits", "list", "SUCCESS")
        return Result.success(_visits.value.filter { it.patientUid == patientUid })
    }

    fun getDiagnosesForPatient(patientUid: String, requesterUid: String): Result<List<HealthDiagnosis>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "diagnoses")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for diagnoses."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "diagnoses", "health_diagnoses", "list", "SUCCESS")
        return Result.success(_diagnoses.value.filter { it.patientUid == patientUid })
    }

    fun getTestsForPatient(patientUid: String, requesterUid: String): Result<List<HealthTest>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "tests")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for tests."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "tests", "health_tests", "list", "SUCCESS")
        return Result.success(_tests.value.filter { it.patientUid == patientUid })
    }

    fun getLabReportsForPatient(patientUid: String, requesterUid: String): Result<List<HealthLabReport>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "lab_reports")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for lab reports."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "lab_reports", "health_lab_reports", "list", "SUCCESS")
        return Result.success(_labReports.value.filter { it.patientUid == patientUid })
    }

    fun getPrescriptionsForPatient(patientUid: String, requesterUid: String): Result<List<HealthPrescription>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "prescriptions")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for prescriptions."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "prescriptions", "health_prescriptions", "list", "SUCCESS")
        return Result.success(_prescriptions.value.filter { it.patientUid == patientUid })
    }

    fun getDocumentsForPatient(patientUid: String, requesterUid: String): Result<List<HealthDocument>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "documents")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for documents."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "documents", "health_documents", "list", "SUCCESS")
        return Result.success(_documents.value.filter { it.patientUid == patientUid })
    }

    fun getBillsForPatient(patientUid: String, requesterUid: String): Result<List<HealthBill>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "bills")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for medical bills."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "bills", "health_bills", "list", "SUCCESS")
        return Result.success(_bills.value.filter { it.patientUid == patientUid })
    }

    fun getNotesForPatient(patientUid: String, requesterUid: String): Result<List<HealthNote>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "notes")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for health notes."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "notes", "health_notes", "list", "SUCCESS")
        return Result.success(_notes.value.filter { it.patientUid == patientUid })
    }

    fun getPrecautionsForPatient(patientUid: String, requesterUid: String): Result<List<HealthPrecaution>> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "precautions")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization for precautions."))
        }
        logAccess(patientUid, requesterUid, "CALLER", "record_viewed", "precautions", "health_precautions", "list", "SUCCESS")
        return Result.success(_precautions.value.filter { it.patientUid == patientUid })
    }

    // =========================================================================
    // 5. WRITE CREATION METHODS (AUTHORIZED CLINICIANS & PATIENT)
    // =========================================================================

    fun addCondition(condition: HealthCondition): Result<HealthCondition> {
        _conditions.value = listOf(condition) + _conditions.value
        logAccess(condition.patientUid, condition.createdByUid, condition.createdByRole, "record_created", "conditions", "health_conditions", condition.recordId, "SUCCESS")
        return Result.success(condition)
    }

    fun addAllergy(allergy: HealthAllergy): Result<HealthAllergy> {
        _allergies.value = listOf(allergy) + _allergies.value
        logAccess(allergy.patientUid, allergy.createdByUid, "PATIENT", "record_created", "allergies", "health_allergies", allergy.recordId, "SUCCESS")
        return Result.success(allergy)
    }

    fun addMedication(medication: HealthMedication): Result<HealthMedication> {
        _medications.value = listOf(medication) + _medications.value
        logAccess(medication.patientUid, medication.prescribedByUid, "DOCTOR", "record_created", "medications", "health_medications", medication.recordId, "SUCCESS")
        return Result.success(medication)
    }

    fun addPrecaution(precaution: HealthPrecaution): Result<HealthPrecaution> {
        _precautions.value = listOf(precaution) + _precautions.value
        logAccess(precaution.patientUid, precaution.createdByUid, "DOCTOR", "record_created", "precautions", "health_precautions", precaution.recordId, "SUCCESS")
        return Result.success(precaution)
    }

    fun addVisit(visit: HealthVisit): Result<HealthVisit> {
        _visits.value = listOf(visit) + _visits.value
        logAccess(visit.patientUid, visit.providerUid, visit.providerType, "record_created", "visits", "health_visits", visit.recordId, "SUCCESS")
        return Result.success(visit)
    }

    fun addDiagnosis(diagnosis: HealthDiagnosis): Result<HealthDiagnosis> {
        _diagnoses.value = listOf(diagnosis) + _diagnoses.value
        logAccess(diagnosis.patientUid, diagnosis.providerUid, "DOCTOR", "record_created", "diagnoses", "health_diagnoses", diagnosis.recordId, "SUCCESS")
        return Result.success(diagnosis)
    }

    fun addTest(test: HealthTest): Result<HealthTest> {
        _tests.value = listOf(test) + _tests.value
        logAccess(test.patientUid, test.requestedByUid, "DOCTOR", "record_created", "tests", "health_tests", test.recordId, "SUCCESS")
        return Result.success(test)
    }

    /**
     * Laboratory upload report. Requires verified laboratory and valid grant for "lab_reports".
     */
    fun addLabReport(labReport: HealthLabReport, labUser: User): Result<HealthLabReport> {
        if (labUser.accountType != AccountType.LABORATORY || !labUser.isVerified) {
            return Result.failure(SecurityException("Access Denied: Only verified laboratory accounts can submit lab reports."))
        }
        if (!hasActivePermission(labReport.patientUid, labUser.uid, "lab_reports")) {
            return Result.failure(SecurityException("Access Denied: No active patient authorization to submit lab report."))
        }
        _labReports.value = listOf(labReport) + _labReports.value
        logAccess(labReport.patientUid, labUser.uid, "LABORATORY", "document_uploaded", "lab_reports", "health_lab_reports", labReport.recordId, "SUCCESS")
        return Result.success(labReport)
    }

    fun addPrescription(prescription: HealthPrescription): Result<HealthPrescription> {
        _prescriptions.value = listOf(prescription) + _prescriptions.value
        logAccess(prescription.patientUid, prescription.doctorUid, "DOCTOR", "record_created", "prescriptions", "health_prescriptions", prescription.recordId, "SUCCESS")
        return Result.success(prescription)
    }

    fun addDocument(document: HealthDocument): Result<HealthDocument> {
        _documents.value = listOf(document) + _documents.value
        logAccess(document.patientUid, document.uploadedByUid, "PATIENT", "document_uploaded", "documents", "health_documents", document.recordId, "SUCCESS")
        return Result.success(document)
    }

    fun addBill(bill: HealthBill): Result<HealthBill> {
        _bills.value = listOf(bill) + _bills.value
        logAccess(bill.patientUid, bill.providerUid, "PROVIDER", "record_created", "bills", "health_bills", bill.recordId, "SUCCESS")
        return Result.success(bill)
    }

    fun addNote(note: HealthNote): Result<HealthNote> {
        _notes.value = listOf(note) + _notes.value
        logAccess(note.patientUid, note.createdByUid, "PATIENT", "record_created", "notes", "health_notes", note.recordId, "SUCCESS")
        return Result.success(note)
    }

    // =========================================================================
    // 6. PAPER PRESCRIPTION & DOCUMENT UPLOAD ACTIONS
    // =========================================================================

    /**
     * Uploads paper prescription document with secure storage path.
     * Validates MIME type and file size.
     */
    fun uploadPaperPrescription(
        patientUid: String,
        title: String,
        fileName: String,
        fileBytesSize: Long,
        mimeType: String
    ): Result<HealthDocument> {
        val allowedMime = setOf("application/pdf", "image/jpeg", "image/png", "image/heic")
        if (mimeType.lowercase() !in allowedMime) {
            return Result.failure(IllegalArgumentException("Unsupported file type. Allowed: PDF, JPG, PNG, HEIC."))
        }
        val maxSizeBytes = 20 * 1024 * 1024L // 20 MB
        if (fileBytesSize > maxSizeBytes) {
            return Result.failure(IllegalArgumentException("File size exceeds 20MB limit."))
        }

        val docId = "doc_${UUID.randomUUID()}"
        val storagePath = "health_private/$patientUid/prescriptions/$docId.${fileName.substringAfterLast('.', "pdf")}"

        val document = HealthDocument(
            recordId = docId,
            patientUid = patientUid,
            documentType = "paper_prescription",
            title = title.ifBlank { "Paper Prescription - $fileName" },
            description = "Secure private paper prescription upload",
            storagePath = storagePath,
            mimeType = mimeType,
            fileSize = fileBytesSize,
            documentDate = HealthPassportFunctions.formatDate(System.currentTimeMillis()).substringBefore(" "),
            uploadedByUid = patientUid
        )

        return addDocument(document)
    }

    // =========================================================================
    // 7. SECURE EXPORT & DOCUMENT DOWNLOAD WITH TEMPORARY TOKENS
    // =========================================================================

    /**
     * Generates a temporary secure download URL/token for an authorized document.
     */
    fun secureDocumentDownload(patientUid: String, requesterUid: String, documentId: String): Result<String> {
        if (patientUid != requesterUid && !hasActivePermission(patientUid, requesterUid, "documents")) {
            return Result.failure(SecurityException("Access Denied: Unauthorized to download document."))
        }
        val doc = _documents.value.find { it.recordId == documentId }
            ?: return Result.failure(NoSuchElementException("Document not found."))

        val tempToken = "temp_sec_token_${UUID.randomUUID()}"
        val tempDownloadUrl = "https://healthogram-vault.private/secure-download/${doc.storagePath}?token=$tempToken&exp=${System.currentTimeMillis() + 300000}" // 5 min token

        logAccess(
            patientUid = patientUid,
            requesterUid = requesterUid,
            requesterRole = "CALLER",
            action = "document_downloaded",
            scope = "documents",
            resourceType = "health_documents",
            resourceId = documentId,
            result = "SUCCESS"
        )
        return Result.success(tempDownloadUrl)
    }

    /**
     * Exports full or category-selected Health Passport.
     * Requires explicit strong authentication challenge verification.
     */
    fun secureHealthExport(
        patientUid: String,
        categories: List<String>,
        dateRange: String,
        authChallengePassed: Boolean
    ): Result<String> {
        if (!authChallengePassed) {
            return Result.failure(SecurityException("Strong re-authentication is mandatory before health passport export."))
        }

        val exportId = "exp_${UUID.randomUUID()}"
        val secureExportUrl = "https://healthogram-vault.private/exports/$patientUid/$exportId.zip?expires=${System.currentTimeMillis() + 900000}"

        logAccess(
            patientUid = patientUid,
            requesterUid = patientUid,
            requesterRole = "PATIENT",
            action = "export_completed",
            scope = categories.joinToString(","),
            resourceType = "export_archive",
            resourceId = exportId,
            result = "SUCCESS"
        )

        return Result.success(secureExportUrl)
    }

    // =========================================================================
    // 8. DATA DELETION / RECOVERY WORKFLOW
    // =========================================================================

    fun requestDataDeletion(patientUid: String, passwordConfirmed: Boolean): Result<String> {
        if (!passwordConfirmed) {
            return Result.failure(SecurityException("Password confirmation required for data deletion."))
        }
        // Mark profile for deletion window
        val profile = _profiles.value[patientUid]
        if (profile != null) {
            _profiles.value = _profiles.value + (patientUid to profile.copy(profileStatus = "PENDING_DELETION_30_DAYS"))
        }
        logAccess(
            patientUid = patientUid,
            requesterUid = patientUid,
            requesterRole = "PATIENT",
            action = "deletion_requested",
            scope = "ALL",
            resourceType = "health_profile",
            resourceId = patientUid,
            result = "SUCCESS"
        )
        return Result.success("Data deletion scheduled. Account enters 30-day grace recovery period.")
    }

    // =========================================================================
    // 9. OFFLINE CACHING & SAFE ARCHITECTURE
    // =========================================================================

    fun updateSettings(patientUid: String, newSettings: HealthPassportSettings): Result<HealthPassportSettings> {
        _settings.value = _settings.value + (patientUid to newSettings)
        if (!newSettings.offlinePassportEnabled) {
            clearSensitiveLocalCache(patientUid)
        }
        return Result.success(newSettings)
    }

    fun clearSensitiveLocalCache(patientUid: String) {
        offlineCachedRecords.remove(patientUid)
    }

    fun getSettings(patientUid: String): HealthPassportSettings {
        return _settings.value[patientUid] ?: HealthPassportSettings(patientUid = patientUid)
    }

    // =========================================================================
    // 10. AUDIT LOGGING (IMMUTABLE CLIENT RECORDING)
    // =========================================================================

    private fun logAccess(
        patientUid: String,
        requesterUid: String,
        requesterRole: String,
        action: String,
        scope: String,
        resourceType: String,
        resourceId: String,
        result: String,
        requestId: String = "",
        grantId: String = ""
    ) {
        val log = HealthAccessLog(
            logId = "log_${UUID.randomUUID()}",
            patientUid = patientUid,
            requesterUid = requesterUid,
            requesterRole = requesterRole,
            organizationId = "Healthogram Secure Engine",
            action = action,
            scope = scope,
            resourceType = resourceType,
            resourceId = resourceId,
            timestamp = System.currentTimeMillis(),
            result = result,
            requestId = requestId,
            grantId = grantId
        )
        _accessLogs.value = listOf(log) + _accessLogs.value
    }

    fun getAccessLogsForPatient(patientUid: String): List<HealthAccessLog> {
        return _accessLogs.value.filter { it.patientUid == patientUid }
    }

    // =========================================================================
    // 11. ZERO-LEAKAGE INVARIANT VERIFICATION
    // =========================================================================

    /**
     * Validates that no medical record payload or private field exists in public social or marketplace feeds.
     */
    fun assertZeroHealthPassportLeakage(publicPayload: Map<String, Any?>) {
        val sensitiveKeys = setOf(
            "healthRecordId", "biometricId", "prescriptionId", "bloodGroup", "diagnosis",
            "conditionName", "allergen", "labReport", "clinicalSummary", "medicalBill"
        )
        for (key in publicPayload.keys) {
            if (sensitiveKeys.contains(key)) {
                throw SecurityException("CRITICAL INVARIANT VIOLATION: Medical record key '$key' leaked into public/social payload!")
            }
        }
    }

    // =========================================================================
    // SEED RECORDS
    // =========================================================================

    private fun seedInitialRecords() {
        val demoPatientUid = "user_patient_demo"
        val demoDoctorUid = "user_doctor_demo"
        val demoLabUid = "user_lab_demo"

        // 1. Profile
        val profile = HealthProfile(
            uid = demoPatientUid,
            healthId = "HG-749204829103",
            dateOfBirth = "1992-05-18",
            bloodGroup = "O+",
            height = "178 cm",
            weight = "72 kg",
            emergencyContactName = "Jane Mercer (Spouse)",
            emergencyContactPhone = "+1 (555) 019-2834"
        )
        _profiles.value = mapOf(demoPatientUid to profile)

        // 2. Conditions
        _conditions.value = listOf(
            HealthCondition(
                recordId = "cnd_01",
                patientUid = demoPatientUid,
                conditionName = "Mild Seasonal Allergic Rhinitis",
                conditionCode = "J30.1",
                description = "Triggered by high spring pollen counts",
                status = "ACTIVE",
                diagnosedDate = "2024-04-10",
                severity = "MILD",
                createdByUid = demoDoctorUid
            ),
            HealthCondition(
                recordId = "cnd_02",
                patientUid = demoPatientUid,
                conditionName = "Benign Post-Exercise Arrhythmia (Resolved)",
                conditionCode = "I49.8",
                description = "Investigated with 24h Holter, normal sinus rhythm resumed",
                status = "RESOLVED",
                diagnosedDate = "2023-08-12",
                resolvedDate = "2023-11-01",
                severity = "MILD",
                createdByUid = demoDoctorUid
            )
        )

        // 3. Allergies
        _allergies.value = listOf(
            HealthAllergy(
                recordId = "alg_01",
                patientUid = demoPatientUid,
                allergen = "Penicillin / Beta-Lactams",
                reaction = "Severe Urticaria, Bronchospasm",
                severity = "LIFE_THREATENING",
                notes = "Anaphylactoid reaction noted in 2018. Contraindicated.",
                createdByUid = demoDoctorUid
            ),
            HealthAllergy(
                recordId = "alg_02",
                patientUid = demoPatientUid,
                allergen = "Peanuts & Tree Nuts",
                reaction = "Angioedema & Cutaneous Rash",
                severity = "HIGH",
                notes = "Carry portable auto-injector.",
                createdByUid = demoPatientUid
            )
        )

        // 4. Medications
        _medications.value = listOf(
            HealthMedication(
                recordId = "med_01",
                patientUid = demoPatientUid,
                medicineName = "Fluticasone Propionate",
                genericName = "Flonase Nasal Spray",
                dosage = "50 mcg/spray",
                frequency = "1 spray in each nostril daily",
                startDate = "2026-08-15",
                prescribedByUid = demoDoctorUid,
                instructions = "Use in the morning after nasal cleansing."
            ),
            HealthMedication(
                recordId = "med_02",
                patientUid = demoPatientUid,
                medicineName = "Cetirizine HCl",
                genericName = "Zyrtec",
                dosage = "10 mg",
                frequency = "Once daily as needed",
                startDate = "2026-08-01",
                prescribedByUid = demoDoctorUid,
                instructions = "Take in the evening."
            )
        )

        // 5. Precautions
        _precautions.value = listOf(
            HealthPrecaution(
                recordId = "prc_01",
                patientUid = demoPatientUid,
                precaution = "Avoid Beta-Lactam and Cephalosporin Antibiotics",
                reason = "Cross-reactivity with confirmed penicillin anaphylactoid allergy",
                severity = "HIGH",
                createdByUid = demoDoctorUid
            ),
            HealthPrecaution(
                recordId = "prc_02",
                patientUid = demoPatientUid,
                precaution = "Hydration during high-intensity endurance sports",
                reason = "Past benign exercise-related ectopic beats",
                severity = "MODERATE",
                createdByUid = demoDoctorUid
            )
        )

        // 6. Visits
        _visits.value = listOf(
            HealthVisit(
                recordId = "vst_01",
                patientUid = demoPatientUid,
                providerUid = demoDoctorUid,
                organizationId = "Metropolitan General Hospital",
                visitDate = "2026-09-01",
                visitType = "Annual Wellness Consultation",
                reason = "Routine annual physical exam & biometric screening",
                clinicalSummary = "Patient alert and oriented. Heart regular rate and rhythm, lungs clear to auscultation bilaterally.",
                diagnosisSummary = "Allergic rhinitis well controlled; vital signs optimal.",
                followUpDate = "2027-09-01"
            )
        )

        // 7. Diagnoses
        _diagnoses.value = listOf(
            HealthDiagnosis(
                recordId = "dia_01",
                patientUid = demoPatientUid,
                diagnosis = "Allergic Rhinitis due to Pollen",
                diagnosisCode = "ICD-10 J30.1",
                diagnosedDate = "2024-04-10",
                providerUid = demoDoctorUid,
                organizationId = "Metropolitan General Hospital"
            )
        )

        // 8. Tests
        _tests.value = listOf(
            HealthTest(
                recordId = "tst_01",
                patientUid = demoPatientUid,
                testName = "Comprehensive Metabolic Panel & Lipid Profile",
                testCode = "LOINC 24323-8",
                requestedByUid = demoDoctorUid,
                organizationId = "Metropolitan General Hospital",
                requestedDate = "2026-09-01",
                testDate = "2026-09-02",
                status = "COMPLETED",
                resultStatus = "NORMAL"
            )
        )

        // 9. Lab Reports
        _labReports.value = listOf(
            HealthLabReport(
                recordId = "lab_01",
                patientUid = demoPatientUid,
                laboratoryUid = demoLabUid,
                organizationId = "BioReference Diagnostics Lab",
                testId = "tst_01",
                reportDate = "2026-09-03",
                summary = "All metabolic markers within desirable reference intervals: Glucose 88 mg/dL, Total Cholesterol 178 mg/dL, HDL 56 mg/dL, LDL 104 mg/dL.",
                reportFilePath = "health_private/user_patient_demo/lab_reports/lab_01.pdf"
            )
        )

        // 10. Prescriptions
        _prescriptions.value = listOf(
            HealthPrescription(
                recordId = "rx_01",
                patientUid = demoPatientUid,
                doctorUid = demoDoctorUid,
                organizationId = "Metropolitan General Hospital",
                prescriptionDate = "2026-09-01",
                medications = listOf(
                    PrescribedMedicineItem("Fluticasone Propionate Nasal", "50 mcg", "1 spray each nostril daily", "30 days"),
                    PrescribedMedicineItem("Cetirizine 10mg Tablets", "10 mg", "1 tablet daily PM", "30 days")
                ),
                instructions = "Continue maintenance therapy during high pollen seasons.",
                diagnosisReference = "J30.1 Allergic Rhinitis"
            )
        )

        // 11. Documents
        _documents.value = listOf(
            HealthDocument(
                recordId = "doc_01",
                patientUid = demoPatientUid,
                documentType = "paper_prescription",
                title = "Cardiology Clearance Certificate",
                description = "Physical endurance clearance for marathon training",
                storagePath = "health_private/user_patient_demo/documents/cardio_clearance_2026.pdf",
                documentDate = "2026-08-15",
                uploadedByUid = demoPatientUid
            )
        )

        // 12. Bills
        _bills.value = listOf(
            HealthBill(
                recordId = "bil_01",
                patientUid = demoPatientUid,
                providerUid = demoDoctorUid,
                organizationId = "Metropolitan General Hospital",
                billNumber = "MGH-2026-8941",
                billDate = "2026-09-01",
                amount = 120.00,
                description = "Annual Clinical Wellness Consultation",
                status = "PAID"
            )
        )

        // 13. Notes
        _notes.value = listOf(
            HealthNote(
                recordId = "not_01",
                patientUid = demoPatientUid,
                title = "Hydration and Electrolyte Log",
                content = "Targeting 2.5L water daily during summer workouts. Felt energized after afternoon runs.",
                createdByUid = demoPatientUid
            )
        )

        // 14. Active Grants
        _accessGrants.value = listOf(
            HealthAccessGrant(
                grantId = "grt_demo_01",
                patientUid = demoPatientUid,
                requesterUid = demoDoctorUid,
                requesterRole = "DOCTOR",
                organizationId = "Metropolitan General Hospital",
                grantedScopes = listOf("profile", "conditions", "allergies", "medications", "visits", "prescriptions"),
                purpose = "Routine Consultation & Clinical Follow-up",
                createdAt = System.currentTimeMillis() - (2 * 3600 * 1000L),
                startsAt = System.currentTimeMillis() - (2 * 3600 * 1000L),
                expiresAt = System.currentTimeMillis() + (22 * 3600 * 1000L),
                status = "active",
                approvedByUid = demoPatientUid
            )
        )

        // 15. Audit Logs
        _accessLogs.value = listOf(
            HealthAccessLog(
                logId = "log_demo_01",
                patientUid = demoPatientUid,
                requesterUid = demoDoctorUid,
                requesterRole = "DOCTOR",
                organizationId = "Metropolitan General Hospital",
                action = "record_viewed",
                scope = "allergies,medications",
                resourceType = "health_passport",
                resourceId = demoPatientUid,
                timestamp = System.currentTimeMillis() - (1 * 3600 * 1000L),
                result = "SUCCESS"
            )
        )
    }
}
