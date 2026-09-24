package com.example.healthogram.core.foundation.auth

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.MarketplaceRole
import com.example.healthogram.core.foundation.audit.AuditEvent
import com.example.healthogram.core.foundation.audit.AuditLoggingService
import com.example.healthogram.core.foundation.audit.AuditResult
import com.example.healthogram.core.foundation.error.HealthAccessDeniedException

/**
 * Step 49: Healthogram 2.3 Centralized Authorization & Health Passport Access Control.
 *
 * Enforces the zero-trust workflow:
 * authenticate -> authorize -> verify consent -> determine scope -> execute operation -> audit
 */
data class AuthSession(
    val uid: String,
    val accountType: AccountType,
    val marketplaceRole: MarketplaceRole = MarketplaceRole.NONE,
    val isAdmin: Boolean = false,
    val isOwner: Boolean = false,
    val isVerified: Boolean = false,
    val organizationId: String? = null
)

data class ConsentGrant(
    val grantId: String,
    val patientUid: String,
    val accessorUid: String,
    val allowedScopes: Set<String>,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 3600000L, // 1 hour
    var isRevoked: Boolean = false
) {
    fun isValid(): Boolean {
        return !isRevoked && System.currentTimeMillis() < expiresAt
    }
}

class AuthorizationService private constructor() {

    fun isAuthenticated(session: AuthSession?): Boolean {
        return session != null && session.uid.isNotBlank()
    }

    fun isOwner(session: AuthSession?): Boolean = session?.isOwner == true

    fun isAdmin(session: AuthSession?): Boolean = session?.isAdmin == true || session?.isOwner == true

    fun isVerifiedHealthcareAccount(session: AuthSession?): Boolean {
        if (session == null || !session.isVerified) return false
        return session.accountType.isHealthcareOrganization || session.accountType == AccountType.DOCTOR
    }

    fun isIndividual(session: AuthSession?): Boolean = session?.accountType == AccountType.INDIVIDUAL

    fun isDoctor(session: AuthSession?): Boolean = session?.accountType == AccountType.DOCTOR

    fun isClinic(session: AuthSession?): Boolean = session?.accountType == AccountType.CLINIC

    fun isHospital(session: AuthSession?): Boolean = session?.accountType == AccountType.HOSPITAL

    fun isLaboratory(session: AuthSession?): Boolean = session?.accountType == AccountType.LABORATORY

    fun isMarketplaceCustomer(session: AuthSession?): Boolean = session?.marketplaceRole == MarketplaceRole.CUSTOMER

    fun isMarketplaceSeller(session: AuthSession?): Boolean = session?.marketplaceRole == MarketplaceRole.SELLER

    /**
     * Executes the strict Health Passport access pipeline.
     */
    fun <T> executeHealthPassportOperation(
        session: AuthSession?,
        targetPatientUid: String,
        requiredScope: String,
        consentGrant: ConsentGrant?,
        operationName: String,
        block: () -> T
    ): T {
        // 1. Authenticate
        if (!isAuthenticated(session)) {
            AuditLoggingService.instance.logEvent(
                AuditEvent(
                    actorId = session?.uid ?: "ANONYMOUS",
                    actorType = "UNKNOWN",
                    targetType = "HealthPassport",
                    targetId = targetPatientUid,
                    action = operationName,
                    result = AuditResult.DENIED,
                    reason = "Unauthenticated access attempt"
                )
            )
            throw HealthAccessDeniedException("User must be authenticated")
        }

        val actor = session!!

        // 2. Authorize
        // Patient has sovereign access to their own record
        val isSelf = actor.uid == targetPatientUid

        if (!isSelf) {
            // Must be verified healthcare practitioner/institution with valid consent
            if (!isVerifiedHealthcareAccount(actor)) {
                AuditLoggingService.instance.logEvent(
                    AuditEvent(
                        actorId = actor.uid,
                        actorType = actor.accountType.name,
                        targetType = "HealthPassport",
                        targetId = targetPatientUid,
                        action = operationName,
                        result = AuditResult.DENIED,
                        reason = "Actor is not a verified healthcare professional or organization"
                    )
                )
                throw HealthAccessDeniedException("Only verified healthcare providers may access third-party health records")
            }

            // 3. Verify consent
            if (consentGrant == null || !consentGrant.isValid() || consentGrant.patientUid != targetPatientUid || consentGrant.accessorUid != actor.uid) {
                AuditLoggingService.instance.logEvent(
                    AuditEvent(
                        actorId = actor.uid,
                        actorType = actor.accountType.name,
                        targetType = "HealthPassport",
                        targetId = targetPatientUid,
                        action = operationName,
                        result = AuditResult.DENIED,
                        reason = "Missing, expired, or revoked patient consent"
                    )
                )
                throw HealthAccessDeniedException("Missing, expired, or revoked patient consent")
            }

            // 4. Determine scope
            if (!consentGrant.allowedScopes.contains(requiredScope) && !consentGrant.allowedScopes.contains("SCOPE_FULL_CLINICAL_TIMELINE")) {
                AuditLoggingService.instance.logEvent(
                    AuditEvent(
                        actorId = actor.uid,
                        actorType = actor.accountType.name,
                        targetType = "HealthPassport",
                        targetId = targetPatientUid,
                        action = operationName,
                        result = AuditResult.DENIED,
                        reason = "Requested scope $requiredScope not granted in consent"
                    )
                )
                throw HealthAccessDeniedException("Requested clinical scope is not authorized by the patient")
            }
        }

        // 5. Execute operation
        return try {
            val result = block()

            // 6. Audit
            AuditLoggingService.instance.logEvent(
                AuditEvent(
                    actorId = actor.uid,
                    actorType = actor.accountType.name,
                    targetType = "HealthPassport",
                    targetId = targetPatientUid,
                    action = operationName,
                    result = AuditResult.SUCCESS,
                    reason = if (isSelf) "Patient sovereign access" else "Authorized clinical consultation with valid consent"
                )
            )
            result
        } catch (e: Exception) {
            AuditLoggingService.instance.logEvent(
                AuditEvent(
                    actorId = actor.uid,
                    actorType = actor.accountType.name,
                    targetType = "HealthPassport",
                    targetId = targetPatientUid,
                    action = operationName,
                    result = AuditResult.FAILED,
                    reason = e.message
                )
            )
            throw e
        }
    }

    companion object {
        val instance: AuthorizationService by lazy { AuthorizationService() }
    }
}
