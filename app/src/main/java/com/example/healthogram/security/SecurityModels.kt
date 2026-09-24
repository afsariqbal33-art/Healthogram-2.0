package com.example.healthogram.security

import java.util.UUID

/**
 * Healthogram Step 19: Security Hardening & Production Security Architecture.
 * Authoritative Data Models, Threat Classifications, and Zero-Trust Guard Structures.
 */

// =========================================================================
// 1. DATA CLASSIFICATION TIERS (Section 5)
// =========================================================================

enum class DataClassification(val tierName: String, val level: Int) {
    PUBLIC("Public", 1),
    INTERNAL("Internal Operational", 2),
    CONFIDENTIAL("Confidential", 3),
    HIGHLY_CONFIDENTIAL("Highly Confidential", 4),
    RESTRICTED_HEALTH("Restricted Health Passport", 5),
    CRITICAL("Critical Security Secrets", 6)
}

// =========================================================================
// 2. THREAT MODEL & EVENT CATEGORIES (Section 4 & 73)
// =========================================================================

enum class SecurityThreatType {
    ACCOUNT_TAKEOVER,
    CREDENTIAL_STUFFING,
    PASSWORD_SPRAYING,
    OTP_ABUSE,
    SESSION_THEFT,
    TOKEN_THEFT,
    DEVICE_COMPROMISE,
    API_ABUSE,
    AUTOMATED_BOTS,
    FAKE_ACCOUNTS,
    PRIVILEGE_ESCALATION,
    IDOR,
    BROKEN_ACCESS_CONTROL,
    DATA_ENUMERATION,
    FIRESTORE_RULE_BYPASS,
    STORAGE_ENUMERATION,
    MALICIOUS_UPLOADS,
    MALICIOUS_LINKS,
    PAYMENT_MANIPULATION,
    PRICE_MANIPULATION,
    COMMISSION_MANIPULATION,
    PAYOUT_FRAUD,
    WITHDRAWAL_FRAUD,
    WEBHOOK_REPLAY,
    DUPLICATE_PAYMENT,
    QR_ABUSE,
    HEALTH_PASSPORT_UNAUTHORIZED_ACCESS,
    CONSENT_BYPASS,
    ADMIN_ABUSE,
    OWNER_ACCOUNT_COMPROMISE,
    INSIDER_THREAT,
    DATA_SCRAPING,
    SPAM_HARASSMENT,
    PROMPT_INJECTION,
    AI_DATA_LEAKAGE,
    TRANSLATION_DATA_LEAKAGE,
    CALL_PRIVACY_ABUSE,
    RATE_LIMIT_EXCEEDED,
    APP_CHECK_FAILURE
}

enum class SecuritySeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class SecurityEventCategory {
    LOGIN_FAILURE,
    MFA_FAILURE,
    SUSPICIOUS_LOGIN,
    SESSION_REVOKED,
    PRIVILEGE_CHANGE,
    APP_CHECK_FAILURE,
    RATE_LIMIT_TRIGGERED,
    HEALTH_ACCESS_ANOMALY,
    QR_ABUSE,
    PAYMENT_ANOMALY,
    PAYOUT_ANOMALY,
    OWNER_SECURITY_EVENT,
    ADMIN_SECURITY_EVENT,
    FILE_SECURITY_EVENT,
    API_ABUSE,
    ACCOUNT_TAKEOVER_SUSPECTED
}

// =========================================================================
// 3. SESSION MANAGEMENT (Sections 11 & 12)
// Maximum 4 simultaneous sessions by default.
// =========================================================================

enum class SessionDeviceStatus {
    ACTIVE,
    REVOKED,
    EXPIRED,
    CHALLENGED,
    COMPROMISED
}

data class UserSessionDevice(
    val deviceId: String = UUID.randomUUID().toString(),
    val uid: String,
    val platform: String = "Android",
    val appVersion: String = "1.0.0",
    val deviceName: String = "Android Device",
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val ipReference: String = "127.0.0.1",
    val sessionStatus: SessionDeviceStatus = SessionDeviceStatus.ACTIVE,
    val refreshReference: String = UUID.randomUUID().toString().take(16),
    val securityStatus: String = "TRUSTED",
    val isCurrentDevice: Boolean = false
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "deviceId" to deviceId,
        "uid" to uid,
        "platform" to platform,
        "appVersion" to appVersion,
        "deviceName" to deviceName,
        "lastSeen" to lastSeen,
        "createdAt" to createdAt,
        "ipReference" to ipReference,
        "sessionStatus" to sessionStatus.name,
        "refreshReference" to refreshReference,
        "securityStatus" to securityStatus
    )
}

// =========================================================================
// 4. MULTI-FACTOR AUTHENTICATION & RE-AUTHENTICATION (Sections 9 & 10)
// =========================================================================

enum class MfaType {
    NONE,
    TOTP_AUTHENTICATOR,
    BIOMETRIC_KEY,
    HARDWARE_TOKEN,
    BACKUP_RECOVERY_CODE
}

data class MfaStatusRecord(
    val uid: String,
    val isMfaEnabled: Boolean = false,
    val primaryMfaType: MfaType = MfaType.NONE,
    val enrolledAt: Long = 0L,
    val lastUsedAt: Long = 0L,
    val backupCodesRemaining: Int = 10,
    val isMandatoryForRole: Boolean = false
)

data class ReauthChallenge(
    val challengeId: String = UUID.randomUUID().toString(),
    val uid: String,
    val targetOperation: String,
    val issuedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000L), // 15 mins validity
    val isVerified: Boolean = false,
    val verificationMethod: String = "PASSWORD_OR_BIOMETRIC"
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
}

// =========================================================================
// 5. HEALTH PASSPORT SECURITY & ACCESS GRANTS (Sections 20-27)
// =========================================================================

enum class HealthAccessScope(val scopeKey: String, val displayName: String) {
    PROFILE("profile", "Demographics & Emergency Info"),
    CONDITIONS("conditions", "Chronic Conditions"),
    ALLERGIES("allergies", "Allergies & Reactions"),
    MEDICATIONS("medications", "Active Prescriptions & Dosages"),
    VISITS("visits", "Clinical Visit Logs"),
    DIAGNOSES("diagnoses", "Formal Clinical Diagnoses"),
    TESTS("tests", "Diagnostic Test Orders"),
    LAB_REPORTS("lab_reports", "Laboratory Test Results"),
    PRESCRIPTIONS("prescriptions", "Prescription Fulfillment"),
    DOCUMENTS("documents", "Clinical Attachments & PDFs"),
    BILLS("bills", "Medical Invoices & Insurance Claims")
}

enum class HealthGrantStatus {
    REQUESTED,
    APPROVED,
    ACTIVE,
    EXPIRED,
    REVOKED,
    DENIED
}

data class HealthAccessGrantRecord(
    val grantId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val requesterAccountType: String,
    val organizationId: String = "",
    val purpose: String,
    val scopes: Set<HealthAccessScope> = setOf(HealthAccessScope.PROFILE),
    val startAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (2 * 60 * 60 * 1000L), // 2 hours default
    val status: HealthGrantStatus = HealthGrantStatus.REQUESTED,
    val approvedAt: Long? = null,
    val revokedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isCurrentlyActive: Boolean
        get() = (status == HealthGrantStatus.ACTIVE || status == HealthGrantStatus.APPROVED) &&
                System.currentTimeMillis() in startAt..expiresAt
}

data class HealthAccessAuditLogRecord(
    val logId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val requesterUid: String,
    val organizationId: String = "",
    val purpose: String,
    val requestedScopes: Set<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val action: String,
    val status: String,
    val grantId: String,
    val requesterIp: String = "127.0.0.1"
)

data class HealthQrAccessSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val patientUid: String,
    val opaqueToken: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (10 * 60 * 1000L), // 10 minutes lifetime
    val isSingleUse: Boolean = true,
    val isConsumed: Boolean = false,
    val scanCount: Int = 0,
    val maxScansAllowed: Int = 1
) {
    val isUsable: Boolean
        get() = !isConsumed && scanCount < maxScansAllowed && System.currentTimeMillis() <= expiresAt
}

// =========================================================================
// 6. WEBHOOK REPLAY & IDEMPOTENCY (Sections 46 & 47)
// =========================================================================

data class ProcessedWebhookEventRecord(
    val eventId: String,
    val provider: String,
    val eventType: String,
    val receivedAt: Long = System.currentTimeMillis(),
    val processedAt: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
    val resourceId: String,
    val requestHash: String
)

// =========================================================================
// 7. SECURITY EVENTS, ALERTS, AND INCIDENTS (Sections 73-75)
// =========================================================================

data class SecurityEventRecord(
    val eventId: String = UUID.randomUUID().toString(),
    val eventCategory: SecurityEventCategory,
    val severity: SecuritySeverity,
    val uid: String,
    val ipAddress: String = "127.0.0.1",
    val userAgent: String = "Healthogram-Android-Client/1.0",
    val deviceId: String,
    val country: String = "US",
    val details: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    DISMISSED
}

data class SecurityAlertRecord(
    val alertId: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val severity: SecuritySeverity,
    val category: SecurityEventCategory,
    val affectedUid: String,
    val sourceIp: String = "127.0.0.1",
    val status: AlertStatus = AlertStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

enum class IncidentStatus {
    OPEN,
    INVESTIGATING,
    CONTAINED,
    RESOLVED,
    CLOSED
}

data class SecurityIncidentRecord(
    val incidentId: String = UUID.randomUUID().toString(),
    val severity: SecuritySeverity,
    val type: String,
    val status: IncidentStatus = IncidentStatus.OPEN,
    val detectedAt: Long = System.currentTimeMillis(),
    val assignedTo: String = "Healthogram Security Team",
    val affectedService: String = "ALL",
    val affectedCountry: String = "GLOBAL",
    val containmentAction: String = "Under Initial Review",
    val resolution: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// =========================================================================
// 8. SECURITY SCORECARD & AUDIT COMPLIANCE (Section 121)
// =========================================================================

enum class ScoreStatus {
    PASS,
    WARNING,
    FAIL,
    CRITICAL
}

data class CategoryScore(
    val categoryName: String,
    val status: ScoreStatus,
    val passedChecks: Int,
    val totalChecks: Int,
    val details: String
)

data class SecurityFinding(
    val findingId: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val severity: SecuritySeverity,
    val description: String,
    val mitigation: String,
    val isResolved: Boolean = true
)

data class SecurityScorecardReport(
    val scorecardId: String = UUID.randomUUID().toString(),
    val generatedAt: Long = System.currentTimeMillis(),
    val overallScore: Int = 100, // 0 - 100
    val overallStatus: ScoreStatus = ScoreStatus.PASS,
    val categories: Map<String, CategoryScore> = emptyMap(),
    val findings: List<SecurityFinding> = emptyList()
)

// =========================================================================
// 9. EMERGENCY SECURITY CONTROLS (Section 76)
// =========================================================================

data class SecurityEmergencyToggles(
    val emergencyModeActive: Boolean = false,
    val registrationDisabled: Boolean = false,
    val loginDisabled: Boolean = false,
    val marketplaceDisabled: Boolean = false,
    val checkoutDisabled: Boolean = false,
    val payoutsDisabled: Boolean = false,
    val qrAccessDisabled: Boolean = false,
    val healthRequestsDisabled: Boolean = false,
    val uploadsDisabled: Boolean = false,
    val liveDisabled: Boolean = false,
    val messagingDisabled: Boolean = false,
    val callingDisabled: Boolean = false,
    val lastUpdatedBy: String = "owner_system",
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val auditEventId: String = ""
)

// =========================================================================
// 10. RATE LIMITING (Section 35 & 77)
// =========================================================================

data class RateLimitRule(
    val endpointKey: String,
    val maxAttempts: Int,
    val windowSeconds: Int
)

data class RateLimitResult(
    val isAllowed: Boolean,
    val remainingAttempts: Int,
    val retryAfterSeconds: Int,
    val reason: String = ""
)
