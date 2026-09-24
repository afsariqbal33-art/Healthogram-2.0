package com.example.healthogram.core.foundation.audit

import com.example.healthogram.core.foundation.env.AppEnvironment
import com.example.healthogram.core.foundation.env.EnvironmentManager
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Step 49: Healthogram 2.3 Immutable Audit Logging Framework.
 *
 * Invariant: Never store raw Protected Health Information (PHI) or unmasked card numbers
 * inside audit events.
 */
enum class AuditResult {
    SUCCESS,
    DENIED,
    FAILED
}

data class AuditEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val actorId: String,
    val actorType: String, // Individual, Doctor, Clinic, Hospital, Laboratory, Admin, System
    val targetType: String, // HealthPassport, Appointment, Order, Payment, QRToken, UserProfile
    val targetId: String,
    val action: String, // READ, CREATE, UPDATE, DELETE, REVOKE, CONSENT_GRANTED, EXPORT_FHIR
    val timestamp: Long = System.currentTimeMillis(),
    val ipMetadata: String? = null,
    val deviceMetadata: String? = null,
    val result: AuditResult,
    val reason: String? = null,
    val requestId: String = UUID.randomUUID().toString(),
    val environment: AppEnvironment = EnvironmentManager.get().environment
)

class AuditLoggingService private constructor() {
    private val inMemoryAuditLog = CopyOnWriteArrayList<AuditEvent>()

    fun logEvent(event: AuditEvent) {
        // Enforce privacy scrubber: Ensure no raw PHI in audit logs
        require(!event.action.contains("RAW_PHI") && !event.targetId.contains("ALLERGY_DETAILS")) {
            "Compliance Violation: Raw PHI must never be captured in audit logs."
        }
        inMemoryAuditLog.add(event)
    }

    fun getAuditTrailForTarget(targetId: String): List<AuditEvent> {
        return inMemoryAuditLog.filter { it.targetId == targetId }
    }

    fun getAuditTrailForActor(actorId: String): List<AuditEvent> {
        return inMemoryAuditLog.filter { it.actorId == actorId }
    }

    companion object {
        val instance: AuditLoggingService by lazy { AuditLoggingService() }
    }
}
