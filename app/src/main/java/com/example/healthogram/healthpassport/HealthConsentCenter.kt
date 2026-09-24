package com.example.healthogram.healthpassport

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Step 32: HealthConsentCenter
 *
 * Patient-controlled consent cockpit providing full transparency:
 * - Inbound provider access requests
 * - Granular category scoping
 * - Active grants with time-to-live
 * - Immediate revocation and scope reduction
 * - Complete audit history of clinical views
 */
class HealthConsentCenter private constructor() {

    enum class RequestStatus {
        PENDING,
        APPROVED,
        REJECTED,
        REVOKED,
        EXPIRED
    }

    data class AccessRequest(
        val requestId: String = UUID.randomUUID().toString(),
        val patientUid: String,
        val requesterUid: String,
        val requesterName: String,
        val organizationId: String? = null,
        val organizationName: String? = null,
        val requesterAccountType: String, // DOCTOR, CLINIC, HOSPITAL, LABORATORY
        val requestedCategories: Set<String>, // ALLERGIES, MEDICATIONS, CONDITIONS, LAB_REPORTS, VITALS, PROCEDURES
        val purpose: String, // TREATMENT, SECOND_OPINION, LAB_ANALYSIS, ADMISSION
        val requestedDurationHours: Long = 24L,
        val status: RequestStatus = RequestStatus.PENDING,
        val requestedAt: Long = System.currentTimeMillis(),
        val resolvedAt: Long? = null,
        val resultingConsentId: String? = null
    )

    private val inboundRequests = ConcurrentHashMap<String, MutableList<AccessRequest>>()
    private val consentService = ConsentManagementService.getInstance()

    companion object {
        @Volatile
        private var instance: HealthConsentCenter? = null

        fun getInstance(): HealthConsentCenter {
            return instance ?: synchronized(this) {
                instance ?: HealthConsentCenter().also { instance = it }
            }
        }
    }

    /**
     * Provider submits an access request to the patient
     */
    fun submitAccessRequest(
        patientUid: String,
        requesterUid: String,
        requesterName: String,
        organizationId: String?,
        organizationName: String?,
        requesterAccountType: String,
        requestedCategories: Set<String>,
        purpose: String,
        durationHours: Long = 24L
    ): AccessRequest {
        val request = AccessRequest(
            patientUid = patientUid,
            requesterUid = requesterUid,
            requesterName = requesterName,
            organizationId = organizationId,
            organizationName = organizationName,
            requesterAccountType = requesterAccountType,
            requestedCategories = requestedCategories,
            purpose = purpose,
            requestedDurationHours = durationHours
        )

        val list = inboundRequests.computeIfAbsent(patientUid) { mutableListOf() }
        synchronized(list) {
            list.add(request)
        }
        return request
    }

    /**
     * Patient approves access with optional category narrowing
     */
    fun approveRequest(
        patientUid: String,
        requestId: String,
        approvedCategories: Set<String>? = null,
        approvedDurationHours: Long? = null
    ): ScopedConsentGrant {
        val list = inboundRequests[patientUid] ?: throw NoSuchElementException("No requests found for patient")
        val req = synchronized(list) {
            val idx = list.indexOfFirst { it.requestId == requestId && it.status == RequestStatus.PENDING }
            if (idx == -1) throw IllegalStateException("Request not found or already resolved")
            val current = list[idx]

            val finalCategories = approvedCategories ?: current.requestedCategories
            val finalDuration = approvedDurationHours ?: current.requestedDurationHours

            val grant = consentService.grantConsent(
                patientUid = patientUid,
                recipientUid = current.requesterUid,
                organizationId = current.organizationId,
                organizationName = current.organizationName,
                purpose = current.purpose,
                allowedCategories = finalCategories,
                durationHours = finalDuration
            )

            list[idx] = current.copy(
                status = RequestStatus.APPROVED,
                resolvedAt = System.currentTimeMillis(),
                resultingConsentId = grant.consentId
            )
            grant
        }
        return req
    }

    /**
     * Patient rejects access request
     */
    fun rejectRequest(patientUid: String, requestId: String): Boolean {
        val list = inboundRequests[patientUid] ?: return false
        synchronized(list) {
            val idx = list.indexOfFirst { it.requestId == requestId && it.status == RequestStatus.PENDING }
            if (idx != -1) {
                val current = list[idx]
                list[idx] = current.copy(
                    status = RequestStatus.REJECTED,
                    resolvedAt = System.currentTimeMillis()
                )
                return true
            }
        }
        return false
    }

    /**
     * Revoke active grant
     */
    fun revokeActiveConsent(patientUid: String, consentId: String, reason: String = "Revoked by patient"): Boolean {
        return consentService.revokeConsent(patientUid, consentId, reason)
    }

    fun revokeActiveGrant(patientUid: String, consentId: String, reason: String = "Revoked by patient"): Boolean {
        return revokeActiveConsent(patientUid, consentId, reason)
    }

    /**
     * Extend active grant
     */
    fun extendActiveConsent(
        patientUid: String,
        consentId: String,
        additionalHours: Long
    ): Boolean {
        val grants = consentService.getActiveGrants(patientUid)
        val grant = grants.find { it.consentId == consentId && it.status == "ACTIVE" } ?: return false
        // Revoke old, issue new with extended window
        consentService.revokeConsent(patientUid, consentId, "Replaced by extended grant")
        consentService.grantConsent(
            patientUid = patientUid,
            recipientUid = grant.recipientUid,
            organizationId = grant.organizationId,
            organizationName = grant.organizationName,
            purpose = grant.purpose,
            allowedCategories = grant.allowedRecordCategories,
            durationHours = (grant.expiresAt - System.currentTimeMillis()) / (3600 * 1000L) + additionalHours
        )
        return true
    }

    /**
     * Limit categories on active grant
     */
    fun limitConsentCategories(
        patientUid: String,
        consentId: String,
        reducedCategories: Set<String>
    ): ScopedConsentGrant {
        val grants = consentService.getActiveGrants(patientUid)
        val grant = grants.find { it.consentId == consentId && it.status == "ACTIVE" }
            ?: throw NoSuchElementException("Active grant not found")

        require(grant.allowedRecordCategories.containsAll(reducedCategories)) {
            "Can only reduce existing allowed categories, not add new ones"
        }

        consentService.revokeConsent(patientUid, consentId, "Scope reduced by patient")
        val remainingHours = maxOf(1L, (grant.expiresAt - System.currentTimeMillis()) / (3600 * 1000L))

        return consentService.grantConsent(
            patientUid = patientUid,
            recipientUid = grant.recipientUid,
            organizationId = grant.organizationId,
            organizationName = grant.organizationName,
            purpose = grant.purpose,
            allowedCategories = reducedCategories,
            durationHours = remainingHours
        )
    }

    fun getPendingRequests(patientUid: String): List<AccessRequest> {
        return inboundRequests[patientUid]?.filter { it.status == RequestStatus.PENDING } ?: emptyList()
    }

    fun getAllRequests(patientUid: String): List<AccessRequest> {
        return inboundRequests[patientUid] ?: emptyList()
    }

    fun getActiveGrants(patientUid: String): List<ScopedConsentGrant> {
        return consentService.getActiveGrants(patientUid)
    }

    fun getConsentAuditHistory(patientUid: String): List<ConsentAuditRecord> {
        return consentService.getAuditTrail(patientUid)
    }

    fun clear() {
        inboundRequests.clear()
    }
}
