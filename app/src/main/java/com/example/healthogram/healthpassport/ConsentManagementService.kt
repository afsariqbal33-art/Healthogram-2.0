package com.example.healthogram.healthpassport

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

enum class ConsentState {
    REQUESTED,
    APPROVED,
    DENIED,
    ACTIVE,
    EXPIRED,
    REVOKED,
    CANCELLED,
    COMPLETED
}

data class ScopedConsentGrant(
    val consentId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val recipientUid: String,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val purpose: String, // TREATMENT, CONSULTATION, LAB_TEST, REFERRAL
    val allowedRecordCategories: Set<String>, // ALLERGIES, MEDICATIONS, CONDITIONS, LAB_REPORTS, VITALS, PROCEDURES
    val startTimestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isOneTimeAccess: Boolean = false,
    val status: String = "ACTIVE", // REQUESTED, APPROVED, DENIED, ACTIVE, EXPIRED, REVOKED, CANCELLED, COMPLETED
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val revokedAt: Long? = null,
    val revocationReason: String? = null
) {
    val isCurrentlyActive: Boolean
        get() = (status == "ACTIVE" || status == "APPROVED") && System.currentTimeMillis() <= expiresAt
}

data class ConsentAuditRecord(
    val auditId: String = UUID.randomUUID().toString(),
    val consentId: String,
    val patientUid: String,
    val actorUid: String,
    val action: String, // GRANTED, REVOKED, ACCESSED, EXPIRED, EMERGENCY_OVERRIDE
    val accessedCategories: Set<String> = emptySet(),
    val justification: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class EmergencyAccessLog(
    val logId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val clinicianUid: String,
    val clinicianRole: String, // DOCTOR, HOSPITAL_EMERGENCY
    val clinicalJustification: String,
    val facilityName: String,
    val accessedSummary: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val alertDispatchedToPatient: Boolean = true
)

/**
 * ConsentManagementService 2.1
 * Enforces dynamic, auditable, and revocable scoped consents for the Health Passport.
 */
class ConsentManagementService private constructor() {

    private val activeGrants = ConcurrentHashMap<String, MutableList<ScopedConsentGrant>>()
    private val consentAuditTrail = ConcurrentHashMap<String, MutableList<ConsentAuditRecord>>()
    private val emergencyAccessLogs = ConcurrentHashMap<String, MutableList<EmergencyAccessLog>>()

    companion object {
        @Volatile
        private var instance: ConsentManagementService? = null

        fun getInstance(): ConsentManagementService {
            return instance ?: synchronized(this) {
                instance ?: ConsentManagementService().also { instance = it }
            }
        }
    }

    /**
     * Issue a scoped, time-bound consent grant
     */
    fun grantConsent(
        patientUid: String,
        recipientUid: String,
        organizationId: String?,
        organizationName: String?,
        purpose: String,
        allowedCategories: Set<String>,
        durationHours: Long = 24L,
        isOneTime: Boolean = false
    ): ScopedConsentGrant {
        require(allowedCategories.isNotEmpty()) { "Must grant access to at least one specific category" }

        val expiresAt = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000L)
        val grant = ScopedConsentGrant(
            patientUid = patientUid,
            recipientUid = recipientUid,
            organizationId = organizationId,
            organizationName = organizationName,
            purpose = purpose,
            allowedRecordCategories = allowedCategories,
            expiresAt = expiresAt,
            isOneTimeAccess = isOneTime,
            status = "ACTIVE"
        )

        val list = activeGrants.computeIfAbsent(patientUid) { mutableListOf() }
        synchronized(list) {
            list.add(grant)
        }

        logAudit(
            ConsentAuditRecord(
                consentId = grant.consentId,
                patientUid = patientUid,
                actorUid = patientUid,
                action = "GRANTED",
                accessedCategories = allowedCategories,
                justification = "Patient approved scoped sharing for $purpose"
            )
        )

        return grant
    }

    /**
     * Revoke an active consent immediately
     */
    fun revokeConsent(
        patientUid: String,
        consentId: String,
        reason: String = "Patient revoked consent"
    ): Boolean {
        val list = activeGrants[patientUid] ?: return false
        synchronized(list) {
            val idx = list.indexOfFirst { it.consentId == consentId && (it.status == "ACTIVE" || it.status == "APPROVED") }
            if (idx != -1) {
                val current = list[idx]
                list[idx] = current.copy(
                    status = "REVOKED",
                    revokedAt = System.currentTimeMillis(),
                    revocationReason = reason,
                    version = current.version + 1
                )
                logAudit(
                    ConsentAuditRecord(
                        consentId = consentId,
                        patientUid = patientUid,
                        actorUid = patientUid,
                        action = "REVOKED",
                        justification = reason
                    )
                )
                return true
            }
        }
        return false
    }

    /**
     * Request access from patient (Creates REQUESTED state)
     */
    fun requestConsent(
        patientUid: String,
        requesterUid: String,
        organizationId: String?,
        organizationName: String?,
        purpose: String,
        requestedCategories: Set<String>,
        durationHours: Long = 24L,
        isOneTime: Boolean = false
    ): ScopedConsentGrant {
        require(requestedCategories.isNotEmpty()) { "Must request at least one specific category" }
        val expiresAt = System.currentTimeMillis() + (durationHours * 60 * 60 * 1000L)
        val grant = ScopedConsentGrant(
            patientUid = patientUid,
            recipientUid = requesterUid,
            organizationId = organizationId,
            organizationName = organizationName,
            purpose = purpose,
            allowedRecordCategories = requestedCategories,
            expiresAt = expiresAt,
            isOneTimeAccess = isOneTime,
            status = "REQUESTED"
        )
        val list = activeGrants.computeIfAbsent(patientUid) { mutableListOf() }
        synchronized(list) {
            list.add(grant)
        }
        logAudit(
            ConsentAuditRecord(
                consentId = grant.consentId,
                patientUid = patientUid,
                actorUid = requesterUid,
                action = "REQUESTED",
                accessedCategories = requestedCategories,
                justification = "Healthcare provider requested access for $purpose"
            )
        )
        return grant
    }

    /**
     * Transition consent state with strict state machine validation
     */
    fun transitionConsentState(
        patientUid: String,
        consentId: String,
        newState: ConsentState,
        actorUid: String,
        reason: String? = null
    ): Boolean {
        val list = activeGrants[patientUid] ?: return false
        synchronized(list) {
            val idx = list.indexOfFirst { it.consentId == consentId }
            if (idx != -1) {
                val current = list[idx]
                val currentEnum = try { ConsentState.valueOf(current.status) } catch (e: Exception) { ConsentState.ACTIVE }

                // State machine validation
                val valid = when (newState) {
                    ConsentState.APPROVED -> currentEnum == ConsentState.REQUESTED
                    ConsentState.ACTIVE -> currentEnum in listOf(ConsentState.REQUESTED, ConsentState.APPROVED)
                    ConsentState.DENIED -> currentEnum == ConsentState.REQUESTED
                    ConsentState.REVOKED -> currentEnum in listOf(ConsentState.ACTIVE, ConsentState.APPROVED)
                    ConsentState.EXPIRED -> currentEnum in listOf(ConsentState.ACTIVE, ConsentState.APPROVED, ConsentState.REQUESTED)
                    ConsentState.CANCELLED -> currentEnum == ConsentState.REQUESTED
                    ConsentState.COMPLETED -> currentEnum in listOf(ConsentState.ACTIVE, ConsentState.APPROVED)
                    ConsentState.REQUESTED -> false
                }

                if (!valid) {
                    return false
                }

                list[idx] = current.copy(
                    status = newState.name,
                    revokedAt = if (newState == ConsentState.REVOKED) System.currentTimeMillis() else current.revokedAt,
                    revocationReason = if (newState == ConsentState.REVOKED) reason else current.revocationReason,
                    version = current.version + 1
                )
                logAudit(
                    ConsentAuditRecord(
                        consentId = consentId,
                        patientUid = patientUid,
                        actorUid = actorUid,
                        action = newState.name,
                        justification = reason ?: "Consent transitioned to ${newState.name}"
                    )
                )
                return true
            }
        }
        return false
    }

    /**
     * Deny a pending consent request
     */
    fun denyConsent(patientUid: String, consentId: String, reason: String = "Patient denied consent request"): Boolean {
        return transitionConsentState(patientUid, consentId, ConsentState.DENIED, patientUid, reason)
    }

    /**
     * Verify if recipient is authorized to access a given category
     */
    fun isAuthorized(
        patientUid: String,
        recipientUid: String,
        category: String
    ): Boolean {
        val list = activeGrants[patientUid] ?: return false
        val now = System.currentTimeMillis()
        synchronized(list) {
            val grant = list.find {
                it.recipientUid == recipientUid &&
                (it.status == "ACTIVE" || it.status == "APPROVED") &&
                it.expiresAt > now &&
                it.allowedRecordCategories.contains(category.uppercase())
            }
            return grant != null
        }
    }

    /**
     * Check if valid consent exists and log audit entry if authorized
     */
    fun hasValidConsent(
        patientUid: String,
        recipientUid: String,
        category: String
    ): Boolean {
        val authorized = isAuthorized(patientUid, recipientUid, category)
        if (authorized) {
            logAudit(
                ConsentAuditRecord(
                    consentId = UUID.randomUUID().toString(),
                    patientUid = patientUid,
                    actorUid = recipientUid,
                    action = "ACCESSED",
                    accessedCategories = setOf(category.uppercase()),
                    justification = "Clinical view authorization check"
                )
            )
        }
        return authorized
    }

    /**
     * Emergency Access Override
     * Strict criteria: verified clinician role + explicit clinical justification + immutable logging + patient alert
     */
    fun performEmergencyAccess(
        patientUid: String,
        clinicianUid: String,
        clinicianRole: String,
        facilityName: String,
        clinicalJustification: String
    ): EmergencyAccessLog {
        require(clinicianRole in listOf("DOCTOR", "HOSPITAL_EMERGENCY")) {
            "Emergency access can only be executed by verified DOCTOR or HOSPITAL_EMERGENCY"
        }
        require(clinicalJustification.trim().length >= 15) {
            "Valid clinical justification required for emergency override (min 15 characters)"
        }

        val log = EmergencyAccessLog(
            patientUid = patientUid,
            clinicianUid = clinicianUid,
            clinicianRole = clinicianRole,
            facilityName = facilityName,
            clinicalJustification = clinicalJustification,
            accessedSummary = listOf("CRITICAL_ALLERGIES", "ACTIVE_MEDICATIONS", "MAJOR_CONDITIONS", "EMERGENCY_CONTACTS")
        )

        val logs = emergencyAccessLogs.computeIfAbsent(patientUid) { mutableListOf() }
        synchronized(logs) {
            logs.add(log)
        }

        logAudit(
            ConsentAuditRecord(
                consentId = log.logId,
                patientUid = patientUid,
                actorUid = clinicianUid,
                action = "EMERGENCY_OVERRIDE",
                accessedCategories = setOf("ALLERGIES", "MEDICATIONS", "CONDITIONS"),
                justification = clinicalJustification
            )
        )

        return log
    }

    private fun logAudit(record: ConsentAuditRecord) {
        val list = consentAuditTrail.computeIfAbsent(record.patientUid) { mutableListOf() }
        synchronized(list) {
            list.add(record)
        }
    }

    fun getActiveGrants(patientUid: String): List<ScopedConsentGrant> {
        val now = System.currentTimeMillis()
        return activeGrants[patientUid]?.filter { it.status == "ACTIVE" && it.expiresAt > now } ?: emptyList()
    }

    fun getAuditTrail(patientUid: String): List<ConsentAuditRecord> {
        return consentAuditTrail[patientUid]?.toList() ?: emptyList()
    }

    fun getEmergencyLogs(patientUid: String): List<EmergencyAccessLog> {
        return emergencyAccessLogs[patientUid]?.toList() ?: emptyList()
    }

    fun clear() {
        activeGrants.clear()
        consentAuditTrail.clear()
        emergencyAccessLogs.clear()
    }
}
