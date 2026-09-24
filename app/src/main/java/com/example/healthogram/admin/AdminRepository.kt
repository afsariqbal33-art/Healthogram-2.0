package com.example.healthogram.admin

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * Master State Repository for Healthogram Admin Control Panel & Platform Operations Center.
 * Implements centralized in-memory state with complete production seed data.
 */
class AdminRepository private constructor() {

    companion object {
        @Volatile
        private var instance: AdminRepository? = null

        fun getInstance(): AdminRepository {
            return instance ?: synchronized(this) {
                instance ?: AdminRepository().also { instance = it }
            }
        }
    }

    // 1. Roles & Permissions & Users
    private val _adminRoles = MutableStateFlow<Map<String, AdminRole>>(emptyMap())
    val adminRoles: StateFlow<Map<String, AdminRole>> = _adminRoles.asStateFlow()

    private val _adminPermissions = MutableStateFlow<Map<String, AdminPermission>>(emptyMap())
    val adminPermissions: StateFlow<Map<String, AdminPermission>> = _adminPermissions.asStateFlow()

    private val _adminUsers = MutableStateFlow<Map<String, AdminUser>>(emptyMap())
    val adminUsers: StateFlow<Map<String, AdminUser>> = _adminUsers.asStateFlow()

    // 2. Account Actions & Users Directory
    private val _platformUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    val platformUsers: StateFlow<Map<String, User>> = _platformUsers.asStateFlow()

    private val _accountActions = MutableStateFlow<Map<String, AccountAction>>(emptyMap())
    val accountActions: StateFlow<Map<String, AccountAction>> = _accountActions.asStateFlow()

    // 3. Social Moderation & Appeals
    private val _moderationReports = MutableStateFlow<Map<String, ModerationReport>>(emptyMap())
    val moderationReports: StateFlow<Map<String, ModerationReport>> = _moderationReports.asStateFlow()

    private val _moderationAppeals = MutableStateFlow<Map<String, ModerationAppeal>>(emptyMap())
    val moderationAppeals: StateFlow<Map<String, ModerationAppeal>> = _moderationAppeals.asStateFlow()

    // 4. Health Security Events
    private val _healthSecurityEvents = MutableStateFlow<Map<String, HealthSecurityEvent>>(emptyMap())
    val healthSecurityEvents: StateFlow<Map<String, HealthSecurityEvent>> = _healthSecurityEvents.asStateFlow()

    // 5. Central Country Configurations
    private val _countryConfigs = MutableStateFlow<Map<String, CentralCountryConfig>>(emptyMap())
    val countryConfigs: StateFlow<Map<String, CentralCountryConfig>> = _countryConfigs.asStateFlow()

    // 6. Feature Flags & Emergency Controls
    private val _featureFlags = MutableStateFlow<Map<String, FeatureFlagRecord>>(emptyMap())
    val featureFlags: StateFlow<Map<String, FeatureFlagRecord>> = _featureFlags.asStateFlow()

    private val _emergencyControls = MutableStateFlow<Map<String, EmergencyControlState>>(emptyMap())
    val emergencyControls: StateFlow<Map<String, EmergencyControlState>> = _emergencyControls.asStateFlow()

    // 7. System Health, Alerts & Incidents
    private val _serviceHealth = MutableStateFlow<Map<String, ServiceHealthRecord>>(emptyMap())
    val serviceHealth: StateFlow<Map<String, ServiceHealthRecord>> = _serviceHealth.asStateFlow()

    private val _adminAlerts = MutableStateFlow<Map<String, AdminAlert>>(emptyMap())
    val adminAlerts: StateFlow<Map<String, AdminAlert>> = _adminAlerts.asStateFlow()

    private val _incidents = MutableStateFlow<Map<String, IncidentRecord>>(emptyMap())
    val incidents: StateFlow<Map<String, IncidentRecord>> = _incidents.asStateFlow()

    // 8. Audit Logs & Security Events
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    private val _securityEvents = MutableStateFlow<List<SecurityEvent>>(emptyList())
    val securityEvents: StateFlow<List<SecurityEvent>> = _securityEvents.asStateFlow()

    // 9. Templates & Support & Exports
    private val _notificationTemplates = MutableStateFlow<Map<String, NotificationTemplateRecord>>(emptyMap())
    val notificationTemplates: StateFlow<Map<String, NotificationTemplateRecord>> = _notificationTemplates.asStateFlow()

    private val _supportTickets = MutableStateFlow<Map<String, SupportTicket>>(emptyMap())
    val supportTickets: StateFlow<Map<String, SupportTicket>> = _supportTickets.asStateFlow()

    private val _exportJobs = MutableStateFlow<Map<String, AdminExportJob>>(emptyMap())
    val exportJobs: StateFlow<Map<String, AdminExportJob>> = _exportJobs.asStateFlow()

    private val _retentionConfig = MutableStateFlow(AdminAuditRetentionConfig())
    val retentionConfig: StateFlow<AdminAuditRetentionConfig> = _retentionConfig.asStateFlow()

    // 10. Dashboard Metrics
    private val _dashboardMetrics = MutableStateFlow(AdminDashboardMetrics())
    val dashboardMetrics: StateFlow<AdminDashboardMetrics> = _dashboardMetrics.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        // ==========================================
        // 1. PERMISSIONS
        // ==========================================
        val allPermissions = listOf(
            // Dashboard
            AdminPermission("dashboard.read", "View Admin Dashboard", "dashboard", "view", PermissionSensitivity.LOW),
            // Users
            AdminPermission("users.read", "View User Directory", "users", "view", PermissionSensitivity.LOW),
            AdminPermission("users.update", "Update User Profile", "users", "update", PermissionSensitivity.NORMAL),
            AdminPermission("users.suspend", "Suspend or Restrict Account", "users", "suspend", PermissionSensitivity.HIGH),
            AdminPermission("users.restore", "Restore Suspended Account", "users", "restore", PermissionSensitivity.HIGH),
            // Verification
            AdminPermission("verification.read", "View Verification Queue", "verification", "view", PermissionSensitivity.LOW),
            AdminPermission("verification.review", "Review Verification Application", "verification", "investigate", PermissionSensitivity.NORMAL),
            AdminPermission("verification.approve", "Approve Professional Verification", "verification", "approve", PermissionSensitivity.HIGH),
            AdminPermission("verification.reject", "Reject Verification Application", "verification", "reject", PermissionSensitivity.HIGH),
            // Moderation
            AdminPermission("moderation.read", "View Moderation Queue", "moderation", "view", PermissionSensitivity.LOW),
            AdminPermission("moderation.review", "Investigate Content Reports", "moderation", "investigate", PermissionSensitivity.NORMAL),
            AdminPermission("moderation.remove", "Remove Harmful Content", "moderation", "delete", PermissionSensitivity.NORMAL),
            AdminPermission("moderation.action", "Take Moderation Action on User", "moderation", "suspend", PermissionSensitivity.HIGH),
            AdminPermission("moderation.appeal", "Review & Resolve Appeals", "moderation", "resolve", PermissionSensitivity.NORMAL),
            // Marketplace
            AdminPermission("marketplace.read", "View Marketplace Records", "marketplace", "view", PermissionSensitivity.LOW),
            AdminPermission("marketplace.seller_approve", "Approve Marketplace Sellers", "marketplace", "approve", PermissionSensitivity.NORMAL),
            AdminPermission("marketplace.seller_freeze", "Freeze Seller Operations", "marketplace", "suspend", PermissionSensitivity.HIGH),
            AdminPermission("marketplace.product_review", "Approve or Reject Products", "marketplace", "update", PermissionSensitivity.NORMAL),
            // Payments & Finance
            AdminPermission("payments.read", "Inspect Financial Transactions", "payments", "view", PermissionSensitivity.NORMAL),
            AdminPermission("payments.refund", "Process Customer Refund", "payments", "refund", PermissionSensitivity.CRITICAL),
            AdminPermission("payments.payout_freeze", "Freeze Seller Payouts", "payments", "suspend", PermissionSensitivity.CRITICAL),
            AdminPermission("payments.payout_release", "Release Seller Payouts", "payments", "payout", PermissionSensitivity.CRITICAL),
            // Delivery
            AdminPermission("delivery.read", "View Shipments & Fulfillment", "delivery", "view", PermissionSensitivity.LOW),
            AdminPermission("delivery.update", "Update Delivery Providers & Rates", "delivery", "configure", PermissionSensitivity.NORMAL),
            AdminPermission("delivery.emergency", "Control Emergency Delivery Stop", "delivery", "suspend", PermissionSensitivity.CRITICAL),
            // Health Security
            AdminPermission("health_security.read", "Inspect Security Access Events", "health_security", "view", PermissionSensitivity.NORMAL),
            AdminPermission("health_security.audit", "Audit Access Attempts", "health_security", "investigate", PermissionSensitivity.HIGH),
            // AI & Translation
            AdminPermission("ai.read", "View AI Studio Metrics", "ai", "view", PermissionSensitivity.LOW),
            AdminPermission("ai.configure", "Configure AI Providers & Limits", "ai", "configure", PermissionSensitivity.NORMAL),
            AdminPermission("translation.read", "View Translation Metrics", "translation", "view", PermissionSensitivity.LOW),
            AdminPermission("translation.configure", "Configure Translation Settings", "translation", "configure", PermissionSensitivity.NORMAL),
            // Notifications & Support
            AdminPermission("notifications.manage", "Manage Push Notification Templates", "notifications", "configure", PermissionSensitivity.NORMAL),
            AdminPermission("support.manage", "Manage Customer Support Tickets", "support", "resolve", PermissionSensitivity.LOW),
            // Platform Governance & Owner
            AdminPermission("countries.manage", "Configure Sovereign Country Parameters", "countries", "configure", PermissionSensitivity.HIGH),
            AdminPermission("feature_flags.manage", "Toggle Platform Feature Flags", "feature_flags", "configure", PermissionSensitivity.HIGH),
            AdminPermission("emergency.manage", "Execute Emergency Kill Switches", "emergency", "suspend", PermissionSensitivity.CRITICAL),
            AdminPermission("audit_logs.read", "View Audit & Compliance Logs", "audit", "view", PermissionSensitivity.NORMAL),
            AdminPermission("system_health.read", "Monitor System Health & Incidents", "system_health", "view", PermissionSensitivity.LOW),
            AdminPermission("owner.finance", "Inspect Platform Net Earnings & Fees", "owner", "view", PermissionSensitivity.CRITICAL)
        )
        _adminPermissions.value = allPermissions.associateBy { it.permissionId }

        // ==========================================
        // 2. ROLES
        // ==========================================
        val pIds = allPermissions.map { it.permissionId }.toSet()

        val roleOwner = AdminRole(
            roleId = AdminRoleIds.OWNER,
            roleName = "Platform Owner",
            description = "Unrestricted sovereign platform governance, financial, and emergency authority.",
            permissionIds = pIds
        )
        val roleSuperAdmin = AdminRole(
            roleId = AdminRoleIds.SUPER_ADMIN,
            roleName = "Super Administrator",
            description = "High-level platform operations, system configuration, and administrative oversight.",
            permissionIds = pIds - setOf("owner.finance")
        )
        val roleModerationAdmin = AdminRole(
            roleId = AdminRoleIds.MODERATION_ADMIN,
            roleName = "Moderation Administrator",
            description = "Social feed, reels, stories, live streams, comments moderation and appeals.",
            permissionIds = setOf("dashboard.read", "moderation.read", "moderation.review", "moderation.remove", "moderation.action", "moderation.appeal", "audit_logs.read")
        )
        val roleVerificationAdmin = AdminRole(
            roleId = AdminRoleIds.VERIFICATION_ADMIN,
            roleName = "Verification Administrator",
            description = "Medical license and organization credential verification.",
            permissionIds = setOf("dashboard.read", "users.read", "verification.read", "verification.review", "verification.approve", "verification.reject", "audit_logs.read")
        )
        val roleFinanceAdmin = AdminRole(
            roleId = AdminRoleIds.FINANCE_ADMIN,
            roleName = "Finance Administrator",
            description = "Payment transactions, refunds, chargebacks, seller payout controls.",
            permissionIds = setOf("dashboard.read", "payments.read", "payments.refund", "payments.payout_freeze", "payments.payout_release", "audit_logs.read")
        )
        val roleDeliveryAdmin = AdminRole(
            roleId = AdminRoleIds.DELIVERY_ADMIN,
            roleName = "Delivery Administrator",
            description = "Shipment tracking, provider logistics, zones, rate tables.",
            permissionIds = setOf("dashboard.read", "delivery.read", "delivery.update", "delivery.emergency", "audit_logs.read")
        )
        val roleHealthSecurityAdmin = AdminRole(
            roleId = AdminRoleIds.HEALTH_SECURITY_ADMIN,
            roleName = "Health Security Administrator",
            description = "Auditing Health Passport QR sessions and suspicious access events without exposing medical records.",
            permissionIds = setOf("dashboard.read", "health_security.read", "health_security.audit", "audit_logs.read")
        )
        val roleCountryAdmin = AdminRole(
            roleId = AdminRoleIds.COUNTRY_ADMIN,
            roleName = "Country-Scoped Administrator",
            description = "Regional oversight bounded strictly by assigned country jurisdiction.",
            permissionIds = setOf("dashboard.read", "users.read", "users.suspend", "verification.read", "verification.review", "verification.approve", "moderation.read", "delivery.read", "audit_logs.read")
        )
        val roleReadOnlyAdmin = AdminRole(
            roleId = AdminRoleIds.READ_ONLY_ADMIN,
            roleName = "Compliance Auditor (Read-Only)",
            description = "Non-interactive audit observation of metrics, reports, and compliance logs.",
            permissionIds = setOf("dashboard.read", "reports.read", "audit_logs.read", "system_health.read")
        )

        _adminRoles.value = mapOf(
            roleOwner.roleId to roleOwner,
            roleSuperAdmin.roleId to roleSuperAdmin,
            roleModerationAdmin.roleId to roleModerationAdmin,
            roleVerificationAdmin.roleId to roleVerificationAdmin,
            roleFinanceAdmin.roleId to roleFinanceAdmin,
            roleDeliveryAdmin.roleId to roleDeliveryAdmin,
            roleHealthSecurityAdmin.roleId to roleHealthSecurityAdmin,
            roleCountryAdmin.roleId to roleCountryAdmin,
            roleReadOnlyAdmin.roleId to roleReadOnlyAdmin
        )

        // ==========================================
        // 3. ADMIN USERS
        // ==========================================
        val adminOwner = AdminUser(
            uid = "usr_owner_root",
            email = "owner@healthogram.com",
            displayName = "Dr. Tariq Al-Mansoor (Owner)",
            roleId = AdminRoleIds.OWNER,
            department = "Executive Governance",
            countryScope = emptyList(), // Global
            mfaEnabled = true
        )
        val adminSaudi = AdminUser(
            uid = "usr_admin_sa",
            email = "admin.sa@healthogram.com",
            displayName = "Noura Al-Dosari",
            roleId = AdminRoleIds.COUNTRY_ADMIN,
            department = "Saudi Operations",
            countryScope = listOf("SA"),
            mfaEnabled = true
        )
        val adminMod = AdminUser(
            uid = "usr_admin_mod",
            email = "moderation@healthogram.com",
            displayName = "Zaid Hassan",
            roleId = AdminRoleIds.MODERATION_ADMIN,
            department = "Trust & Safety",
            countryScope = emptyList(),
            mfaEnabled = true
        )
        val adminFinance = AdminUser(
            uid = "usr_admin_fin",
            email = "finance@healthogram.com",
            displayName = "Amira Selim",
            roleId = AdminRoleIds.FINANCE_ADMIN,
            department = "Treasury & Payments",
            countryScope = emptyList(),
            mfaEnabled = true
        )
        val adminHealthSec = AdminUser(
            uid = "usr_admin_hsec",
            email = "security@healthogram.com",
            displayName = "Marcus Vance",
            roleId = AdminRoleIds.HEALTH_SECURITY_ADMIN,
            department = "HIPAA/GDPR Compliance",
            countryScope = emptyList(),
            mfaEnabled = true
        )
        val adminAuditor = AdminUser(
            uid = "usr_admin_audit",
            email = "auditor@healthogram.com",
            displayName = "Sophia Schmidt",
            roleId = AdminRoleIds.READ_ONLY_ADMIN,
            department = "External Regulatory Audit",
            countryScope = emptyList(),
            mfaEnabled = true
        )

        _adminUsers.value = mapOf(
            adminOwner.uid to adminOwner,
            adminSaudi.uid to adminSaudi,
            adminMod.uid to adminMod,
            adminFinance.uid to adminFinance,
            adminHealthSec.uid to adminHealthSec,
            adminAuditor.uid to adminAuditor
        )

        // ==========================================
        // 4. PLATFORM DIRECTORY USERS
        // ==========================================
        val demoUsers = listOf(
            User(
                uid = "usr_doc_01",
                displayName = "Dr. Sarah Al-Ahmad",
                username = "dr_sarah",
                email = "dr.sarah@riyadhclinic.sa",
                accountType = AccountType.DOCTOR,
                countryCode = "SA",
                countryName = "Saudi Arabia",
                city = "Riyadh",
                verificationStatus = VerificationStatus.UNDER_REVIEW,
                isVerified = false
            ),
            User(
                uid = "usr_hosp_01",
                displayName = "King Faisal Specialist Hospital",
                username = "kfsh_riyadh",
                email = "contact@kfsh.med.sa",
                accountType = AccountType.HOSPITAL,
                countryCode = "SA",
                countryName = "Saudi Arabia",
                city = "Riyadh",
                verificationStatus = VerificationStatus.VERIFIED,
                isVerified = true,
                verificationBadgeType = VerificationBadgeType.GOLD_SHIELD
            ),
            User(
                uid = "usr_lab_01",
                displayName = "Al-Borg Diagnostics",
                username = "alborg_labs",
                email = "support@alborg.sa",
                accountType = AccountType.LABORATORY,
                countryCode = "SA",
                countryName = "Saudi Arabia",
                city = "Jeddah",
                verificationStatus = VerificationStatus.UNDER_REVIEW,
                isVerified = false
            ),
            User(
                uid = "usr_patient_01",
                displayName = "Omar Al-Mansoor",
                username = "omar_mansoor",
                email = "omar@emirates.ae",
                accountType = AccountType.INDIVIDUAL,
                countryCode = "AE",
                countryName = "United Arab Emirates",
                city = "Dubai",
                verificationStatus = VerificationStatus.NOT_STARTED,
                isVerified = false
            )
        )
        _platformUsers.value = demoUsers.associateBy { it.uid }

        // ==========================================
        // 5. MODERATION REPORTS & APPEALS
        // ==========================================
        val rep1 = ModerationReport(
            reportId = "rep_001",
            reporterUid = "usr_patient_01",
            reportedUid = "usr_bad_actor_99",
            contentType = ModerationContentType.POST,
            contentId = "post_unverified_cure",
            reasonCode = "misinformation",
            description = "Claims unapproved herbal mixture cures diabetes in 48 hours.",
            evidenceReference = "evidence_post_screenshot_01",
            priority = ModerationPriority.HIGH,
            status = ModerationReportStatus.PENDING
        )
        val rep2 = ModerationReport(
            reportId = "rep_002",
            reporterUid = "usr_doc_01",
            reportedUid = "usr_spammer_12",
            contentType = ModerationContentType.COMMENT,
            contentId = "comment_spam_crypto",
            reasonCode = "spam",
            description = "Automated promotional bot posting fake pharmaceutical discount links.",
            priority = ModerationPriority.NORMAL,
            status = ModerationReportStatus.PENDING
        )
        _moderationReports.value = mapOf(rep1.reportId to rep1, rep2.reportId to rep2)

        // ==========================================
        // 6. HEALTH SECURITY EVENTS (HIPAA/GDPR)
        // ==========================================
        val hse1 = HealthSecurityEvent(
            eventId = "hse_001",
            eventType = HealthSecurityEventType.UNUSUAL_QR_SCAN,
            actorUid = "usr_clinic_unverified",
            targetUid = "usr_patient_01",
            healthAccessSessionId = "has_session_9102",
            severity = HealthSecurityEventSeverity.HIGH,
            country = "AE",
            status = HealthSecurityEventStatus.DETECTED,
            description = "Unverified clinic account attempted to scan patient QR code without valid consent token."
        )
        val hse2 = HealthSecurityEvent(
            eventId = "hse_002",
            eventType = HealthSecurityEventType.ABNORMAL_ACCESS_PATTERN,
            actorUid = "usr_doc_external",
            targetUid = "usr_patient_multiple",
            healthAccessSessionId = "has_session_bulk",
            severity = HealthSecurityEventSeverity.WARNING,
            country = "US",
            status = HealthSecurityEventStatus.INVESTIGATING,
            description = "High rate of record access requests (42 within 10 minutes)."
        )
        _healthSecurityEvents.value = mapOf(hse1.eventId to hse1, hse2.eventId to hse2)

        // ==========================================
        // 7. CENTRAL COUNTRY CONFIGURATIONS
        // ==========================================
        val saConfig = CentralCountryConfig(
            countryCode = "SA",
            countryName = "Saudi Arabia",
            active = true,
            currencyCode = "SAR",
            defaultLanguage = "ar",
            supportedLanguages = listOf("ar", "en"),
            timezone = "Asia/Riyadh"
        )
        val aeConfig = CentralCountryConfig(
            countryCode = "AE",
            countryName = "United Arab Emirates",
            active = true,
            currencyCode = "AED",
            defaultLanguage = "ar",
            supportedLanguages = listOf("ar", "en"),
            timezone = "Asia/Dubai"
        )
        val usConfig = CentralCountryConfig(
            countryCode = "US",
            countryName = "United States",
            active = true,
            currencyCode = "USD",
            defaultLanguage = "en",
            supportedLanguages = listOf("en", "es"),
            timezone = "America/New_York"
        )
        _countryConfigs.value = mapOf("SA" to saConfig, "AE" to aeConfig, "US" to usConfig)

        // ==========================================
        // 8. FEATURE FLAGS
        // ==========================================
        val defaultFlags = listOf(
            FeatureFlagRecord("flag_marketplace", "Marketplace Operations", FeatureFlagStatus.ON, FeatureFlagScope.GLOBAL, "", "Global marketplace and suborders"),
            FeatureFlagRecord("flag_payments", "Payment Processing Engine", FeatureFlagStatus.ON, FeatureFlagScope.GLOBAL, "", "Multi-gateway sovereign payment processing"),
            FeatureFlagRecord("flag_delivery", "Delivery & Logistics Fulfillment", FeatureFlagStatus.ON, FeatureFlagScope.GLOBAL, "", "Fleet and third-party delivery dispatch"),
            FeatureFlagRecord("flag_health_passport", "Health Passport QR Access", FeatureFlagStatus.ON, FeatureFlagScope.GLOBAL, "", "Dynamic QR and consent security"),
            FeatureFlagRecord("flag_ai_studio", "AI Medical & Creative Assistant", FeatureFlagStatus.ON, FeatureFlagScope.GLOBAL, "", "Gemini API intelligent clinical assistant"),
            FeatureFlagRecord("flag_social_live", "Live Video Streaming", FeatureFlagStatus.BETA, FeatureFlagScope.GLOBAL, "", "Live streaming broadcasts for verified entities")
        )
        _featureFlags.value = defaultFlags.associateBy { it.flagId }

        // ==========================================
        // 9. EMERGENCY CONTROLS
        // ==========================================
        val defaultEmergency = listOf(
            EmergencyControlState("emergency_app_maintenance", "Global App Maintenance Mode"),
            EmergencyControlState("emergency_marketplace_stop", "Halt Marketplace Checkout"),
            EmergencyControlState("emergency_payment_stop", "Halt All Payment Ingestion"),
            EmergencyControlState("emergency_payout_stop", "Freeze All Seller & Provider Payouts"),
            EmergencyControlState("emergency_delivery_stop", "Suspend Delivery & Shipping Dispatch"),
            EmergencyControlState("emergency_ai_stop", "Immediate AI Infrastructure Shutdown"),
            EmergencyControlState("emergency_live_stop", "Halt All Active Live Streams")
        )
        _emergencyControls.value = defaultEmergency.associateBy { it.controlKey }

        // ==========================================
        // 10. SYSTEM HEALTH SERVICES
        // ==========================================
        val services = listOf(
            ServiceHealthRecord("srv_firebase_auth", "Firebase Authentication", ServiceUptimeStatus.OPERATIONAL, latencyMs = 28L),
            ServiceHealthRecord("srv_firestore", "Firestore Distributed Database", ServiceUptimeStatus.OPERATIONAL, latencyMs = 34L),
            ServiceHealthRecord("srv_payments", "Sovereign Payment Gateway Pipeline", ServiceUptimeStatus.OPERATIONAL, latencyMs = 82L),
            ServiceHealthRecord("srv_delivery", "Logistics & Fleet Dispatch Engine", ServiceUptimeStatus.OPERATIONAL, latencyMs = 55L),
            ServiceHealthRecord("srv_fcm", "FCM & Country Push Notification Hub", ServiceUptimeStatus.OPERATIONAL, latencyMs = 40L),
            ServiceHealthRecord("srv_ai_gemini", "Gemini Server-Side Clinical Engine", ServiceUptimeStatus.OPERATIONAL, latencyMs = 120L),
            ServiceHealthRecord("srv_translation", "Universal Translation Service", ServiceUptimeStatus.OPERATIONAL, latencyMs = 60L)
        )
        _serviceHealth.value = services.associateBy { it.serviceId }

        // Initial Audit Log
        _auditLogs.value = listOf(
            AdminAuditLog(
                actorUid = "usr_owner_root",
                actorRole = "owner",
                permissionUsed = "system.init",
                module = "admin",
                action = "initialize_system",
                targetType = "platform",
                targetId = "healthogram_global",
                reason = "System initialization and sovereign security verification."
            )
        )
    }

    // ==========================================
    // MUTATION FUNCTIONS
    // ==========================================

    fun updateAdminUser(user: AdminUser) {
        _adminUsers.update { it + (user.uid to user) }
    }

    fun updatePlatformUser(user: User) {
        _platformUsers.update { it + (user.uid to user) }
    }

    fun recordAccountAction(action: AccountAction) {
        _accountActions.update { it + (action.actionId to action) }
    }

    fun recordModerationReport(report: ModerationReport) {
        _moderationReports.update { it + (report.reportId to report) }
    }

    fun recordModerationAppeal(appeal: ModerationAppeal) {
        _moderationAppeals.update { it + (appeal.appealId to appeal) }
    }

    fun recordHealthSecurityEvent(event: HealthSecurityEvent) {
        _healthSecurityEvents.update { it + (event.eventId to event) }
    }

    fun updateCountryConfig(config: CentralCountryConfig) {
        _countryConfigs.update { it + (config.countryCode to config) }
    }

    fun updateFeatureFlag(flag: FeatureFlagRecord) {
        _featureFlags.update { it + (flag.flagId to flag) }
    }

    fun updateEmergencyControl(control: EmergencyControlState) {
        _emergencyControls.update { it + (control.controlKey to control) }
    }

    fun updateServiceHealth(record: ServiceHealthRecord) {
        _serviceHealth.update { it + (record.serviceId to record) }
    }

    fun recordAdminAlert(alert: AdminAlert) {
        _adminAlerts.update { it + (alert.alertId to alert) }
    }

    fun recordIncident(incident: IncidentRecord) {
        _incidents.update { it + (incident.incidentId to incident) }
    }

    fun appendAuditLog(log: AdminAuditLog) {
        _auditLogs.update { listOf(log) + it }
    }

    fun recordSecurityEvent(event: SecurityEvent) {
        _securityEvents.update { listOf(event) + it }
    }

    fun updateNotificationTemplate(template: NotificationTemplateRecord) {
        _notificationTemplates.update { it + (template.templateId to template) }
    }

    fun updateSupportTicket(ticket: SupportTicket) {
        _supportTickets.update { it + (ticket.ticketId to ticket) }
    }

    fun recordExportJob(job: AdminExportJob) {
        _exportJobs.update { it + (job.jobId to job) }
    }
}
