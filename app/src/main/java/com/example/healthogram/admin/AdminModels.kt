package com.example.healthogram.admin

import java.util.UUID

/**
 * Healthogram Step 16: Admin Control Panel & Platform Operations Center.
 * Comprehensive Data Models for Multi-Role Platform Administration.
 */

// =========================================================================
// 1. ADMIN ROLE & PERMISSION DEFINITIONS
// =========================================================================

object AdminRoleIds {
    const val OWNER = "owner"
    const val SUPER_ADMIN = "super_admin"
    const val OPERATIONS_ADMIN = "operations_admin"
    const val VERIFICATION_ADMIN = "verification_admin"
    const val MODERATION_ADMIN = "moderation_admin"
    const val MARKETPLACE_ADMIN = "marketplace_admin"
    const val PAYMENT_ADMIN = "payment_admin"
    const val DELIVERY_ADMIN = "delivery_admin"
    const val SUPPORT_ADMIN = "support_admin"
    const val HEALTH_SECURITY_ADMIN = "health_security_admin"
    const val AI_ADMIN = "ai_admin"
    const val TRANSLATION_ADMIN = "translation_admin"
    const val NOTIFICATION_ADMIN = "notification_admin"
    const val COUNTRY_ADMIN = "country_admin"
    const val FINANCE_ADMIN = "finance_admin"
    const val AUDITOR = "auditor"
    const val READ_ONLY_ADMIN = "read_only_admin"
}

enum class PermissionSensitivity {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL // Requires re-authentication / MFA within last 15 minutes
}

data class AdminPermission(
    val permissionId: String,
    val permissionName: String,
    val module: String,
    val action: String,
    val sensitivity: PermissionSensitivity = PermissionSensitivity.NORMAL,
    val active: Boolean = true
)

data class AdminRole(
    val roleId: String,
    val roleName: String,
    val description: String,
    val active: Boolean = true,
    val permissionIds: Set<String> = emptySet(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AdminUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val roleId: String,
    val department: String = "Platform Operations",
    val active: Boolean = true,
    val countryScope: List<String> = emptyList(), // Empty = Global scope (Owner/SuperAdmin)
    val regionScope: List<String> = emptyList(),
    val permissionOverrides: Set<String> = emptySet(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val lastReauthAt: Long = System.currentTimeMillis(),
    val mfaEnabled: Boolean = true,
    val createdBy: String = "system",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// =========================================================================
// 2. ACCOUNT ACTIONS & SUSPENSIONS
// =========================================================================

enum class AccountActionType {
    WARNING,
    TEMPORARY_SUSPEND,
    PERMANENT_RESTRICTION,
    MARKETPLACE_SUSPEND,
    MESSAGING_SUSPEND,
    CALLING_SUSPEND,
    CONTENT_RESTRICTION,
    SELLER_FREEZE
}

enum class AccountActionStatus {
    ACTIVE,
    EXPIRED,
    REVERSED
}

data class AccountAction(
    val actionId: String = "act_${UUID.randomUUID().toString().take(8)}",
    val uid: String,
    val actionType: AccountActionType,
    val reasonCode: String,
    val reason: String,
    val durationHours: Long? = null,
    val expiresAt: Long? = null,
    val actorUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val reversedAt: Long? = null,
    val reversalActor: String? = null,
    val reversalReason: String? = null,
    val status: AccountActionStatus = AccountActionStatus.ACTIVE
)

// =========================================================================
// 3. SOCIAL MODERATION & APPEALS
// =========================================================================

enum class ModerationContentType {
    POST,
    REEL,
    VIDEO,
    STORY,
    COMMENT,
    ACCOUNT,
    LIVE_STREAM
}

enum class ModerationReportStatus {
    PENDING,
    UNDER_REVIEW,
    DISMISSED,
    ACTION_TAKEN,
    ESCALATED
}

enum class ModerationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

data class ModerationReport(
    val reportId: String = "rep_${UUID.randomUUID().toString().take(8)}",
    val reporterUid: String,
    val reportedUid: String,
    val contentType: ModerationContentType,
    val contentId: String,
    val reasonCode: String,
    val description: String,
    val evidenceReference: String = "",
    val priority: ModerationPriority = ModerationPriority.NORMAL,
    val status: ModerationReportStatus = ModerationReportStatus.PENDING,
    val assignedAdmin: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val resolution: String? = null
)

enum class AppealStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    ESCALATED
}

data class ModerationAppeal(
    val appealId: String = "app_${UUID.randomUUID().toString().take(8)}",
    val uid: String,
    val originalActionId: String,
    val reason: String,
    val evidence: String = "",
    val status: AppealStatus = AppealStatus.SUBMITTED,
    val assignedAdmin: String? = null,
    val resolution: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

// =========================================================================
// 4. HEALTH SECURITY & COMPLIANCE EVENTS (HIPAA/GDPR ISOLATED)
// =========================================================================

enum class HealthSecurityEventType {
    UNAUTHORIZED_ACCESS_ATTEMPT,
    UNUSUAL_QR_SCAN,
    REPEATED_DENIAL,
    SUSPICIOUS_GRANT,
    SUSPICIOUS_REVOKE,
    ABNORMAL_ACCESS_PATTERN,
    COMPROMISED_ACCOUNT_SUSPECTED
}

enum class HealthSecurityEventSeverity {
    INFO,
    WARNING,
    HIGH,
    CRITICAL
}

enum class HealthSecurityEventStatus {
    DETECTED,
    INVESTIGATING,
    RESOLVED,
    FALSE_POSITIVE
}

data class HealthSecurityEvent(
    val eventId: String = "hse_${UUID.randomUUID().toString().take(8)}",
    val eventType: HealthSecurityEventType,
    val actorUid: String,
    val targetUid: String,
    val organizationUid: String? = null,
    val healthAccessSessionId: String,
    val severity: HealthSecurityEventSeverity = HealthSecurityEventSeverity.HIGH,
    val country: String = "US",
    val status: HealthSecurityEventStatus = HealthSecurityEventStatus.DETECTED,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewerUid: String? = null,
    val resolution: String? = null
)

// =========================================================================
// 5. COUNTRY CONFIGURATION & SOVEREIGN BOUNDARIES
// =========================================================================

data class CentralCountryConfig(
    val countryCode: String,
    val countryName: String,
    val active: Boolean = true,
    val currencyCode: String,
    val defaultLanguage: String,
    val supportedLanguages: List<String> = listOf("en"),
    val marketplaceEnabled: Boolean = true,
    val paymentEnabled: Boolean = true,
    val deliveryEnabled: Boolean = true,
    val verificationEnabled: Boolean = true,
    val aiEnabled: Boolean = true,
    val translationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false,
    val timezone: String = "UTC",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// =========================================================================
// 6. FEATURE FLAGS & EMERGENCY CONTROLS
// =========================================================================

enum class FeatureFlagStatus {
    ON,
    OFF,
    MAINTENANCE,
    BETA,
    COMING_SOON
}

enum class FeatureFlagScope {
    GLOBAL,
    COUNTRY,
    ENVIRONMENT,
    ACCOUNT_TYPE,
    SPECIFIC_USER
}

data class FeatureFlagRecord(
    val flagId: String,
    val flagName: String,
    val status: FeatureFlagStatus = FeatureFlagStatus.ON,
    val scope: FeatureFlagScope = FeatureFlagScope.GLOBAL,
    val targetScopeValue: String = "",
    val description: String,
    val updatedBy: String = "system",
    val updatedAt: Long = System.currentTimeMillis()
)

data class EmergencyControlState(
    val controlKey: String,
    val name: String,
    val isTriggered: Boolean = false,
    val triggeredBy: String? = null,
    val triggeredAt: Long? = null,
    val reason: String? = null
)

// =========================================================================
// 7. SYSTEM HEALTH & ALERTS & INCIDENTS
// =========================================================================

enum class ServiceUptimeStatus {
    OPERATIONAL,
    DEGRADED,
    DOWN,
    MAINTENANCE
}

data class ServiceHealthRecord(
    val serviceId: String,
    val serviceName: String,
    val status: ServiceUptimeStatus = ServiceUptimeStatus.OPERATIONAL,
    val environment: String = "production",
    val country: String = "ALL",
    val uptimePercent: Double = 99.98,
    val errorRate: Double = 0.01,
    val latencyMs: Long = 45L,
    val lastSuccessAt: Long = System.currentTimeMillis(),
    val lastFailureAt: Long? = null,
    val checkedAt: Long = System.currentTimeMillis(),
    val incidentId: String? = null
)

enum class AdminAlertSeverity {
    INFO,
    WARNING,
    HIGH,
    CRITICAL
}

data class AdminAlert(
    val alertId: String = "alt_${UUID.randomUUID().toString().take(8)}",
    val severity: AdminAlertSeverity,
    val service: String,
    val category: String,
    val message: String,
    val referenceId: String? = null,
    val isAcknowledged: Boolean = false,
    val assignedAdmin: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val resolvedAt: Long? = null
)

enum class IncidentStatus {
    DETECTED,
    INVESTIGATING,
    IDENTIFIED,
    MITIGATING,
    RESOLVED,
    CLOSED
}

data class IncidentRecord(
    val incidentId: String = "inc_${UUID.randomUUID().toString().take(8)}",
    val severity: AdminAlertSeverity,
    val service: String,
    val country: String,
    val title: String,
    val description: String,
    val status: IncidentStatus = IncidentStatus.DETECTED,
    val owner: String,
    val createdAt: Long = System.currentTimeMillis(),
    val acknowledgedAt: Long? = null,
    val resolvedAt: Long? = null,
    val resolutionSummary: String? = null
)

// =========================================================================
// 8. AUDIT LOGGING & SECURITY EVENTS
// =========================================================================

data class AdminAuditLog(
    val logId: String = "aud_${UUID.randomUUID().toString().take(10)}",
    val actorUid: String,
    val actorRole: String,
    val permissionUsed: String,
    val module: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val previousStateReference: String = "",
    val newStateReference: String = "",
    val reason: String,
    val requestId: String = UUID.randomUUID().toString().take(8),
    val ipReference: String = "192.168.1.100",
    val deviceReference: String = "Admin Console Workstation",
    val timestamp: Long = System.currentTimeMillis(),
    val result: String = "SUCCESS"
)

data class SecurityEvent(
    val eventId: String = "sec_${UUID.randomUUID().toString().take(8)}",
    val eventType: String,
    val severity: String,
    val description: String,
    val actorUid: String,
    val ipReference: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

// =========================================================================
// 9. NOTIFICATIONS & SUPPORT & EXPORTS
// =========================================================================

data class NotificationTemplateRecord(
    val templateId: String,
    val category: String,
    val eventType: String,
    val language: String = "en",
    val country: String = "ALL",
    val titleTemplate: String,
    val bodyTemplate: String,
    val deepLink: String = "",
    val priority: String = "HIGH",
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SupportTicketCategory {
    CUSTOMER,
    SELLER,
    DELIVERY,
    PAYMENT,
    ACCOUNT,
    VERIFICATION,
    TECHNICAL,
    APPEAL
}

enum class SupportTicketStatus {
    OPEN,
    IN_PROGRESS,
    WAITING_FOR_USER,
    RESOLVED,
    CLOSED
}

data class SupportTicketMessage(
    val senderUid: String,
    val senderName: String,
    val isAdmin: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SupportTicket(
    val ticketId: String = "tkt_${UUID.randomUUID().toString().take(8)}",
    val requesterUid: String,
    val requesterName: String,
    val category: SupportTicketCategory,
    val priority: ModerationPriority = ModerationPriority.NORMAL,
    val status: SupportTicketStatus = SupportTicketStatus.OPEN,
    val assignedAdmin: String? = null,
    val subject: String,
    val description: String,
    val messages: List<SupportTicketMessage> = emptyList(),
    val attachments: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

data class AdminExportJob(
    val jobId: String = "exp_${UUID.randomUUID().toString().take(8)}",
    val adminUid: String,
    val reportType: String,
    val format: String = "CSV",
    val status: String = "COMPLETED",
    val rowCount: Int = 120,
    val fileUrl: String = "admin_private/exports/report_${System.currentTimeMillis()}.csv",
    val expiresAt: Long = System.currentTimeMillis() + (86400000L * 7),
    val createdAt: Long = System.currentTimeMillis()
)

data class AdminAuditRetentionConfig(
    val retentionDays: Int = 365,
    val archiveEnabled: Boolean = true,
    val archiveLocation: String = "gs://healthogram-audit-archive/",
    val deletionPolicy: String = "LEGAL_HOLD_COMPLIANT",
    val legalHoldEnabled: Boolean = true
)

// =========================================================================
// 10. AGGREGATED METRICS & SEARCH
// =========================================================================

data class AdminDashboardMetrics(
    val totalUsers: Int = 24850,
    val activeUsers: Int = 18420,
    val pendingVerifications: Int = 14,
    val openReports: Int = 6,
    val totalMarketplaceOrders: Int = 3120,
    val totalPaymentsMinor: Long = 98450000L, // USD 984,500.00
    val totalRefundsMinor: Long = 1250000L,
    val activeShipments: Int = 142,
    val deliveryIssues: Int = 3,
    val healthSecurityEvents: Int = 2,
    val totalAiJobs: Int = 5410,
    val translationRequests: Int = 18940,
    val openSupportTickets: Int = 9
)

data class AdminSearchResultItem(
    val entityType: String,
    val entityId: String,
    val title: String,
    val subtitle: String,
    val status: String,
    val countryCode: String? = null,
    val targetModule: String
)
