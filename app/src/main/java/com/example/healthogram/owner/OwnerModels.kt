package com.example.healthogram.owner

import com.example.healthogram.core.AccountType
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import java.util.UUID

/**
 * Healthogram Step 17: Owner Control Panel & Global Feature Flags Models.
 * Provides types, enums, configuration entities, and audit structures.
 */

// =========================================================================
// 1. OWNER ACCOUNT & DELEGATES
// =========================================================================

enum class OwnerStatus {
    ACTIVE,
    SUSPENDED,
    LOCKED
}

enum class OwnerRole {
    PLATFORM_OWNER,
    PLATFORM_OPERATOR,
    FINANCE_OPERATOR,
    TECHNICAL_OPERATOR,
    COUNTRY_OPERATOR,
    READ_ONLY_OWNER_DELEGATE
}

data class OwnerProfile(
    val ownerUid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val role: OwnerRole = OwnerRole.PLATFORM_OWNER,
    val status: OwnerStatus = OwnerStatus.ACTIVE,
    val mfaEnabled: Boolean = true,
    val reauthRequired: Boolean = true,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val lastSensitiveActionAt: Long = System.currentTimeMillis(),
    val lastReauthAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "system_root",
    val securityLevel: String = "TIER_1_SOVEREIGN_ROOT"
)

data class OwnerDelegate(
    val delegateId: String = "del_${UUID.randomUUID().toString().take(8)}",
    val uid: String,
    val displayName: String,
    val email: String,
    val role: OwnerRole,
    val permissions: Set<String> = emptySet(),
    val countryScope: Set<String> = emptySet(), // Empty = all authorized countries
    val active: Boolean = true,
    val assignedBy: String,
    val assignedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

// =========================================================================
// 2. FEATURE FLAGS & PRECEDENCE
// =========================================================================

enum class FeatureFlagStatus {
    ON,
    OFF,
    MAINTENANCE,
    BETA,
    COMING_SOON
}

enum class FeatureCategory {
    ACCOUNT,
    SOCIAL,
    HEALTH_PASSPORT,
    MARKETPLACE,
    PAYMENT,
    DELIVERY,
    AI,
    TRANSLATION,
    COMMUNICATION,
    NOTIFICATIONS,
    VERIFICATION,
    PLATFORM
}

enum class MarketplaceRole {
    CUSTOMER,
    SELLER
}

enum class AppEnvironment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

data class GlobalFeatureFlag(
    val flagId: String,
    val featureKey: String,
    val displayName: String,
    val description: String,
    val category: FeatureCategory,
    val status: FeatureFlagStatus = FeatureFlagStatus.ON,
    val globalEnabled: Boolean = true,
    val countryOverrides: Map<String, FeatureFlagStatus> = emptyMap(),
    val accountTypeOverrides: Map<AccountType, FeatureFlagStatus> = emptyMap(),
    val marketplaceRoleOverrides: Map<MarketplaceRole, FeatureFlagStatus> = emptyMap(),
    val userTargetingOverrides: Map<String, FeatureFlagStatus> = emptyMap(),
    val environmentOverrides: Map<AppEnvironment, FeatureFlagStatus> = emptyMap(),
    val rolloutPercentage: Int = 100, // 0 to 100%
    val priority: Int = 100,
    val startAt: Long? = null,
    val endAt: Long? = null,
    val maintenanceMessage: String? = null,
    val comingSoonMessage: String? = null,
    val betaLabel: String? = null,
    val dependencies: List<String> = emptyList(),
    val conflicts: List<String> = emptyList(),
    val requiresUpdate: Boolean = false,
    val serverEnforced: Boolean = true,
    val clientVisible: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "platform_owner",
    val updatedBy: String = "platform_owner",
    val version: Int = 1,
    val previousVersion: Int = 0,
    val changeReason: String = "Initial deployment"
)

data class FeatureEvaluationContext(
    val uid: String? = null,
    val country: String = "US",
    val language: String = "en",
    val accountCategory: AccountType = AccountType.INDIVIDUAL,
    val marketplaceRole: MarketplaceRole? = MarketplaceRole.CUSTOMER,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val appVersion: String = "1.0.0",
    val platform: String = "android",
    val deviceId: String? = null,
    val verificationStatus: VerificationStatus? = VerificationStatus.NOT_STARTED,
    val sellerStatus: String? = null,
    val ownerOverride: Boolean = false,
    val betaUser: Boolean = false,
    val maintenanceState: Boolean = false
)

data class FeatureEvaluationResult(
    val featureKey: String,
    val effectiveStatus: FeatureFlagStatus,
    val isEnabled: Boolean,
    val precedenceReason: String,
    val displayMessage: String? = null
)

// =========================================================================
// 3. COUNTRY CONFIGURATION
// =========================================================================

data class SovereignCountryConfig(
    val countryCode: String, // e.g. "SA", "AE", "US", "GB", "IN"
    val countryName: String,
    val active: Boolean = true,
    val registrationEnabled: Boolean = true,
    val marketplaceEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false, // Strictly initially OFF
    val paymentsEnabled: Boolean = true,
    val deliveryEnabled: Boolean = true,
    val aiEnabled: Boolean = true,
    val translationEnabled: Boolean = true,
    val messagingEnabled: Boolean = true,
    val callingEnabled: Boolean = true,
    val healthPassportEnabled: Boolean = true,
    val verificationEnabled: Boolean = true,
    val socialEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val defaultCurrency: String = "USD",
    val supportedCurrencies: List<String> = listOf("USD"),
    val defaultLanguage: String = "en",
    val supportedLanguages: List<String> = listOf("en"),
    val timezone: String = "UTC",
    val taxConfigurationReference: String? = null,
    val paymentConfigurationReference: String? = null,
    val deliveryConfigurationReference: String? = null,
    val verificationConfigurationReference: String? = null,
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "platform_owner"
)

// =========================================================================
// 4. ACCOUNT CATEGORY CONTROLS
// =========================================================================

data class AccountCategoryFeatureControls(
    val category: AccountType,
    val enabled: Boolean = true,
    val healthPassportAccessEnabled: Boolean = true,
    val qrScanningEnabled: Boolean = true,
    val appointmentsEnabled: Boolean = true,
    val messagingEnabled: Boolean = true,
    val callingEnabled: Boolean = true,
    val socialPostingEnabled: Boolean = true,
    val liveStreamingEnabled: Boolean = true,
    val verificationRequired: Boolean = false,
    val maxConcurrentDevices: Int = 4,
    val organizationManagementEnabled: Boolean = false,
    val labReportSubmissionEnabled: Boolean = false,
    val updatedBy: String = "platform_owner",
    val updatedAt: Long = System.currentTimeMillis()
)

// =========================================================================
// 5. GLOBAL PLATFORM CONFIGURATION & DEVICE LIMITS
// =========================================================================

data class PlatformConfigGlobal(
    val appName: String = "Healthogram",
    val appVersion: String = "1.0.0",
    val minimumSupportedVersion: String = "1.0.0",
    val latestRecommendedVersion: String = "1.0.0",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Healthogram is undergoing scheduled platform maintenance. Services will resume shortly.",
    val supportEmail: String = "support@healthogram.com",
    val supportPhone: String = "+1-800-HEALTHO",
    val defaultLanguage: String = "en",
    val defaultCountry: String = "US",
    val defaultCurrency: String = "USD",
    val maxSessionsDefault: Int = 4, // Strict Healthogram limit: 4 devices
    val maxSessionsOrganizationTier1: Int = 4,
    val maxSessionsOrganizationTier2: Int = 8, // Enabled only if premium tier authorized
    val maxUploadSizeBytes: Long = 25 * 1024 * 1024L, // 25MB
    val maxVideoDurationSec: Int = 300,
    val maxReelDurationSec: Int = 90,
    val maxStoryDurationSec: Int = 30,
    val maxPostMediaCount: Int = 10,
    val paginationLimit: Int = 20,
    val searchLimit: Int = 50,
    val notificationRetentionDays: Int = 90,
    val marketplaceEnabled: Boolean = true,
    val internationalMarketplaceEnabled: Boolean = false,
    val aiEnabled: Boolean = true,
    val translationEnabled: Boolean = true,
    val callingEnabled: Boolean = true,
    val messagingEnabled: Boolean = true,
    val healthPassportEnabled: Boolean = true,
    val socialEnabled: Boolean = true,
    val verificationEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "platform_owner",
    val version: Int = 1
)

// =========================================================================
// 6. EMERGENCY KILL SWITCHES
// =========================================================================

enum class EmergencySwitchKey(val displayName: String, val typedConfirmationText: String) {
    GLOBAL_APP_DISABLE("Global App Disable", "DISABLE ALL SERVICES"),
    REGISTRATION_DISABLE("Registration Disable", "DISABLE REGISTRATION"),
    LOGIN_DISABLE("Login Disable", "DISABLE LOGIN"),
    SOCIAL_UPLOAD_DISABLE("Social Upload Disable", "DISABLE UPLOADS"),
    LIVE_DISABLE("Live Streaming Disable", "DISABLE LIVE STREAMING"),
    MESSAGING_DISABLE("Messaging Disable", "DISABLE MESSAGING"),
    CALLING_DISABLE("Audio/Video Calling Disable", "DISABLE CALLING"),
    MARKETPLACE_DISABLE("Marketplace Disable", "DISABLE MARKETPLACE"),
    CHECKOUT_DISABLE("Checkout Disable", "DISABLE CHECKOUT"),
    PAYMENTS_DISABLE("Payments Disable", "DISABLE PAYMENTS"),
    SELLER_PAYOUT_DISABLE("Seller Payout Disable", "DISABLE PAYOUTS"),
    DELIVERY_DISABLE("Delivery Dispatch Disable", "DISABLE DELIVERY"),
    AI_DISABLE("AI Studio Disable", "DISABLE AI"),
    TRANSLATION_DISABLE("Translation Disable", "DISABLE TRANSLATION"),
    HEALTH_QR_DISABLE("Health QR Access Disable", "DISABLE HEALTH QR"),
    HEALTH_ACCESS_REQUEST_DISABLE("Health Access Request Disable", "DISABLE HEALTH ACCESS"),
    NOTIFICATIONS_DISABLE("Push Notifications Disable", "DISABLE NOTIFICATIONS")
}

data class EmergencyKillSwitchState(
    val key: EmergencySwitchKey,
    val isTriggered: Boolean = false,
    val triggeredBy: String? = null,
    val triggeredAt: Long? = null,
    val reason: String? = null,
    val typedConfirmationSupplied: String? = null
)

// =========================================================================
// 7. SCHEDULED CHANGES & VERSIONING & ROLLBACK
// =========================================================================

enum class ScheduledChangeStatus {
    PENDING,
    EXECUTED,
    CANCELLED,
    FAILED
}

data class ScheduledConfigurationChange(
    val changeId: String = "sch_${UUID.randomUUID().toString().take(8)}",
    val changeType: String, // e.g. "FEATURE_FLAG_STATUS", "COUNTRY_ACTIVATION", "MAINTENANCE_WINDOW"
    val target: String,
    val oldValue: String,
    val newValue: String,
    val scope: String = "GLOBAL",
    val country: String? = null,
    val accountCategory: String? = null,
    val userScope: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val scheduledStart: Long,
    val scheduledEnd: Long? = null,
    val status: ScheduledChangeStatus = ScheduledChangeStatus.PENDING,
    val createdBy: String,
    val approvedBy: String? = null,
    val reason: String,
    val createdAt: Long = System.currentTimeMillis(),
    val executedAt: Long? = null,
    val rollbackValue: String? = null
)

data class ConfigurationVersion(
    val versionId: String = "ver_${UUID.randomUUID().toString().take(8)}",
    val versionNumber: Int,
    val configurationType: String, // "FEATURE_FLAGS", "GLOBAL_CONFIG", "COUNTRY_CONFIG"
    val previousVersion: Int,
    val changedFields: List<String>,
    val oldValues: Map<String, String>,
    val newValues: Map<String, String>,
    val reason: String,
    val createdBy: String,
    val approvedBy: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val createdAt: Long = System.currentTimeMillis(),
    val publishedAt: Long = System.currentTimeMillis(),
    val rollbackAvailable: Boolean = true
)

// =========================================================================
// 8. AUDIT LOGGING & SECURITY
// =========================================================================

data class OwnerAuditLog(
    val logId: String = "oal_${UUID.randomUUID().toString().take(8)}",
    val ownerUid: String,
    val ownerRole: String,
    val action: String,
    val actionCategory: String, // "FEATURE_FLAG", "COUNTRY", "PAYMENT", "EMERGENCY", "MAINTENANCE", "SECURITY"
    val targetType: String,
    val targetId: String,
    val country: String? = null,
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val oldValue: String = "",
    val newValue: String = "",
    val reason: String,
    val ipHashOrReference: String = "127.0.0.1",
    val deviceReference: String = "Healthogram Admin Workstation",
    val reauthenticated: Boolean = true,
    val mfaVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = true,
    val failureReason: String? = null,
    val configurationVersion: Int = 1
)

// =========================================================================
// 9. PLATFORM ANNOUNCEMENTS & PRICING & PROVIDERS
// =========================================================================

enum class AnnouncementType {
    SYSTEM,
    MAINTENANCE,
    SECURITY,
    MARKETPLACE,
    PAYMENT,
    DELIVERY,
    FEATURE,
    PROMOTION
}

data class PlatformAnnouncement(
    val announcementId: String = "anc_${UUID.randomUUID().toString().take(8)}",
    val title: String,
    val message: String,
    val imageUrl: String? = null,
    val priority: String = "NORMAL", // "LOW", "NORMAL", "HIGH", "CRITICAL"
    val type: AnnouncementType = AnnouncementType.SYSTEM,
    val status: String = "ACTIVE", // "ACTIVE", "SCHEDULED", "EXPIRED", "CANCELLED"
    val startAt: Long = System.currentTimeMillis(),
    val endAt: Long = System.currentTimeMillis() + (7L * 86400000L),
    val country: String? = null,
    val language: String? = null,
    val accountCategory: String? = null,
    val targetAudience: String = "ALL_USERS",
    val deepLink: String? = null,
    val dismissible: Boolean = true,
    val createdBy: String = "platform_owner",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ServiceProviderConfig(
    val providerId: String,
    val providerName: String,
    val country: String = "GLOBAL",
    val serviceType: String, // "PAYMENT", "DELIVERY", "AI", "TRANSLATION", "RTC_CALLING", "PUSH_NOTIFICATION"
    val environment: AppEnvironment = AppEnvironment.PRODUCTION,
    val status: String = "ONLINE", // "ONLINE", "DEGRADED", "OFFLINE"
    val priority: Int = 1,
    val fallbackProviderId: String? = null,
    val enabled: Boolean = true,
    val latencyMs: Long = 120,
    val successRatePercent: Double = 99.8,
    val failureRatePercent: Double = 0.2,
    val estimatedCostMinor: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PricingConfig(
    val sellerCommissionPercent: Double = 8.5,
    val telehealthCommissionPercent: Double = 10.0,
    val platformServiceFeeMinor: Long = 150, // $1.50 or 1.50 SAR in minor units
    val defaultShippingRateMinor: Long = 500, // $5.00
    val freeShippingThresholdMinor: Long = 5000, // $50.00
    val organizationTier1MonthlyMinor: Long = 4900, // $49.00
    val organizationTier2MonthlyMinor: Long = 9900, // $99.00 (up to 8 devices)
    val aiDailyFreeTokens: Long = 100000,
    val aiMonthlyPaidCapMinor: Long = 2500,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "platform_owner"
)
