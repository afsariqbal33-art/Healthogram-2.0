package com.example.healthogram.notification

import java.util.UUID

/**
 * Fixed Notification Categories in Healthogram.
 * Ordinary users cannot invent arbitrary categories.
 */
enum class NotificationCategory(val key: String, val displayName: String) {
    SOCIAL("social", "Social"),
    MESSAGES("messages", "Messages"),
    CALLS("calls", "Calls"),
    TRANSLATION("translation", "Translation"),
    MARKETPLACE("marketplace", "Marketplace"),
    SELLER("seller", "Seller"),
    HEALTH_SECURITY("health_security", "Health & Security"),
    VERIFICATION("verification", "Verification"),
    APPOINTMENTS("appointments", "Appointments"),
    ORGANIZATION("organization", "Organization"),
    AI_STUDIO("ai_studio", "AI Studio"),
    SYSTEM("system", "System"),
    PROMOTIONS("promotions", "Promotions")
}

/**
 * Notification Types across all Healthogram subsystems.
 */
enum class NotificationType(val category: NotificationCategory, val defaultPriority: NotificationPriority) {
    // Social
    NEW_FOLLOWER(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    FOLLOW_REQUEST(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    FOLLOW_REQUEST_ACCEPTED(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    POST_LIKE(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    POST_COMMENT(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    COMMENT_REPLY(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    POST_SHARE(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    POST_SAVE(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    MENTION(NotificationCategory.SOCIAL, NotificationPriority.HIGH),
    STORY_REPLY(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    STORY_REACTION(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    REEL_LIKE(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    REEL_COMMENT(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    REEL_SHARE(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    LIVE_STARTED(NotificationCategory.SOCIAL, NotificationPriority.NORMAL),
    LIVE_ENDING_SOON(NotificationCategory.SOCIAL, NotificationPriority.LOW),
    CREATOR_UPDATE(NotificationCategory.SOCIAL, NotificationPriority.LOW),

    // Messaging
    NEW_MESSAGE(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),
    MESSAGE_REQUEST(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),
    MESSAGE_REQUEST_ACCEPTED(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),
    MESSAGE_REPLY(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),
    MESSAGE_REACTION(NotificationCategory.MESSAGES, NotificationPriority.LOW),
    MESSAGE_MENTION(NotificationCategory.MESSAGES, NotificationPriority.HIGH),
    MESSAGE_MEDIA(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),
    MESSAGE_DOCUMENT(NotificationCategory.MESSAGES, NotificationPriority.NORMAL),

    // Calling
    INCOMING_AUDIO_CALL(NotificationCategory.CALLS, NotificationPriority.HIGH),
    INCOMING_VIDEO_CALL(NotificationCategory.CALLS, NotificationPriority.HIGH),
    MISSED_AUDIO_CALL(NotificationCategory.CALLS, NotificationPriority.HIGH),
    MISSED_VIDEO_CALL(NotificationCategory.CALLS, NotificationPriority.HIGH),
    CALL_DECLINED(NotificationCategory.CALLS, NotificationPriority.LOW),
    CALL_ENDED(NotificationCategory.CALLS, NotificationPriority.LOW),
    CALL_FAILED(NotificationCategory.CALLS, NotificationPriority.NORMAL),

    // Translation
    TRANSLATION_COMPLETED(NotificationCategory.TRANSLATION, NotificationPriority.NORMAL),
    TRANSLATION_FAILED(NotificationCategory.TRANSLATION, NotificationPriority.NORMAL),
    TRANSLATION_LIMIT_REACHED(NotificationCategory.TRANSLATION, NotificationPriority.HIGH),
    TRANSLATION_FEATURE_AVAILABLE(NotificationCategory.TRANSLATION, NotificationPriority.LOW),

    // Marketplace Customer
    ORDER_CREATED(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    PAYMENT_SUCCESS(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    PAYMENT_FAILED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    ORDER_CONFIRMED(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    ORDER_PROCESSING(NotificationCategory.MARKETPLACE, NotificationPriority.LOW),
    ORDER_SHIPPED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    ORDER_OUT_FOR_DELIVERY(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    ORDER_DELIVERED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    ORDER_CANCELLED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    RETURN_REQUESTED(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    RETURN_APPROVED(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    RETURN_REJECTED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    REFUND_CREATED(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    REFUND_COMPLETED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    REFUND_FAILED(NotificationCategory.MARKETPLACE, NotificationPriority.HIGH),
    DELIVERY_UPDATE(NotificationCategory.MARKETPLACE, NotificationPriority.NORMAL),
    PRICE_DROP(NotificationCategory.MARKETPLACE, NotificationPriority.LOW),
    WISHLIST_OFFER(NotificationCategory.MARKETPLACE, NotificationPriority.LOW),
    FLASH_SALE(NotificationCategory.PROMOTIONS, NotificationPriority.LOW),
    DEAL_STARTED(NotificationCategory.PROMOTIONS, NotificationPriority.LOW),
    REVIEW_REMINDER(NotificationCategory.MARKETPLACE, NotificationPriority.LOW),

    // Seller
    NEW_ORDER(NotificationCategory.SELLER, NotificationPriority.HIGH),
    SELLER_PAYMENT_CONFIRMED(NotificationCategory.SELLER, NotificationPriority.HIGH),
    SELLER_ORDER_CANCELLED(NotificationCategory.SELLER, NotificationPriority.HIGH),
    SELLER_RETURN_REQUEST(NotificationCategory.SELLER, NotificationPriority.HIGH),
    SELLER_REFUND_REQUEST(NotificationCategory.SELLER, NotificationPriority.HIGH),
    LOW_INVENTORY(NotificationCategory.SELLER, NotificationPriority.HIGH),
    PRODUCT_APPROVED(NotificationCategory.SELLER, NotificationPriority.NORMAL),
    PRODUCT_REJECTED(NotificationCategory.SELLER, NotificationPriority.HIGH),
    PRODUCT_REVIEW_REQUIRED(NotificationCategory.SELLER, NotificationPriority.NORMAL),
    SELLER_VERIFICATION_UPDATE(NotificationCategory.SELLER, NotificationPriority.HIGH),
    PAYOUT_REQUESTED(NotificationCategory.SELLER, NotificationPriority.NORMAL),
    PAYOUT_PROCESSING(NotificationCategory.SELLER, NotificationPriority.NORMAL),
    PAYOUT_COMPLETED(NotificationCategory.SELLER, NotificationPriority.HIGH),
    PAYOUT_FAILED(NotificationCategory.SELLER, NotificationPriority.CRITICAL),
    COMMISSION_UPDATE(NotificationCategory.SELLER, NotificationPriority.NORMAL),
    STORE_SUSPENDED(NotificationCategory.SELLER, NotificationPriority.CRITICAL),
    COMPLIANCE_REQUIRED(NotificationCategory.SELLER, NotificationPriority.CRITICAL),

    // Verification
    VERIFICATION_SUBMITTED(NotificationCategory.VERIFICATION, NotificationPriority.NORMAL),
    VERIFICATION_UNDER_REVIEW(NotificationCategory.VERIFICATION, NotificationPriority.NORMAL),
    VERIFICATION_ADDITIONAL_INFO(NotificationCategory.VERIFICATION, NotificationPriority.HIGH),
    VERIFICATION_APPROVED(NotificationCategory.VERIFICATION, NotificationPriority.HIGH),
    VERIFICATION_REJECTED(NotificationCategory.VERIFICATION, NotificationPriority.HIGH),
    VERIFICATION_EXPIRING(NotificationCategory.VERIFICATION, NotificationPriority.HIGH),
    VERIFICATION_REVOKED(NotificationCategory.VERIFICATION, NotificationPriority.CRITICAL),

    // Health Passport Security (Highest privacy protection: NO medical details in text!)
    HEALTH_ACCESS_REQUEST(NotificationCategory.HEALTH_SECURITY, NotificationPriority.HIGH),
    HEALTH_ACCESS_APPROVED(NotificationCategory.HEALTH_SECURITY, NotificationPriority.HIGH),
    HEALTH_ACCESS_DENIED(NotificationCategory.HEALTH_SECURITY, NotificationPriority.NORMAL),
    HEALTH_ACCESS_REVOKED(NotificationCategory.HEALTH_SECURITY, NotificationPriority.HIGH),
    HEALTH_ACCESS_EXPIRED(NotificationCategory.HEALTH_SECURITY, NotificationPriority.NORMAL),
    HEALTH_QR_ACCESS_ATTEMPT(NotificationCategory.HEALTH_SECURITY, NotificationPriority.HIGH),
    HEALTH_SECURITY_EVENT(NotificationCategory.HEALTH_SECURITY, NotificationPriority.CRITICAL),

    // Appointments
    APPOINTMENT_REQUESTED(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    APPOINTMENT_CONFIRMED(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    APPOINTMENT_RESCHEDULED(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    APPOINTMENT_CANCELLED(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    APPOINTMENT_REMINDER(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    APPOINTMENT_STARTED(NotificationCategory.APPOINTMENTS, NotificationPriority.CRITICAL),
    APPOINTMENT_MISSED(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),
    CONSULTATION_AVAILABLE(NotificationCategory.APPOINTMENTS, NotificationPriority.HIGH),

    // Organization
    ORG_NEW_APPOINTMENT(NotificationCategory.ORGANIZATION, NotificationPriority.HIGH),
    STAFF_ACCESS_CHANGED(NotificationCategory.ORGANIZATION, NotificationPriority.HIGH),
    DEVICE_ADDED(NotificationCategory.ORGANIZATION, NotificationPriority.HIGH),
    DEVICE_REMOVED(NotificationCategory.ORGANIZATION, NotificationPriority.HIGH),
    DEVICE_REVOKED(NotificationCategory.ORGANIZATION, NotificationPriority.CRITICAL),
    ORG_VERIFICATION_UPDATE(NotificationCategory.ORGANIZATION, NotificationPriority.HIGH),
    SERVICE_UPDATE(NotificationCategory.ORGANIZATION, NotificationPriority.NORMAL),
    ORG_SYSTEM_ALERT(NotificationCategory.ORGANIZATION, NotificationPriority.CRITICAL),
    ORG_COMPLIANCE_ALERT(NotificationCategory.ORGANIZATION, NotificationPriority.CRITICAL),

    // AI Studio
    AI_JOB_COMPLETED(NotificationCategory.AI_STUDIO, NotificationPriority.NORMAL),
    AI_JOB_FAILED(NotificationCategory.AI_STUDIO, NotificationPriority.NORMAL),
    AI_USAGE_LIMIT(NotificationCategory.AI_STUDIO, NotificationPriority.HIGH),
    AI_FEATURE_UNAVAILABLE(NotificationCategory.AI_STUDIO, NotificationPriority.LOW),

    // System
    MAINTENANCE(NotificationCategory.SYSTEM, NotificationPriority.HIGH),
    SECURITY_ALERT(NotificationCategory.SYSTEM, NotificationPriority.CRITICAL),
    SERVICE_DEGRADATION(NotificationCategory.SYSTEM, NotificationPriority.HIGH),
    FEATURE_AVAILABLE(NotificationCategory.SYSTEM, NotificationPriority.LOW),
    FEATURE_DISABLED(NotificationCategory.SYSTEM, NotificationPriority.NORMAL),
    TERMS_UPDATE(NotificationCategory.SYSTEM, NotificationPriority.HIGH),
    PRIVACY_UPDATE(NotificationCategory.SYSTEM, NotificationPriority.HIGH),
    APP_UPDATE(NotificationCategory.SYSTEM, NotificationPriority.NORMAL),

    // Promotions
    PROMOTIONAL_CAMPAIGN(NotificationCategory.PROMOTIONS, NotificationPriority.LOW),
    MARKETPLACE_DEAL(NotificationCategory.PROMOTIONS, NotificationPriority.LOW),
    SELLER_OFFER(NotificationCategory.PROMOTIONS, NotificationPriority.LOW),

    // Admin & Owner
    ADMIN_ALERT(NotificationCategory.SYSTEM, NotificationPriority.HIGH),
    OWNER_ALERT(NotificationCategory.SYSTEM, NotificationPriority.CRITICAL)
}

/**
 * Notification Priority levels.
 */
enum class NotificationPriority {
    CRITICAL,
    HIGH,
    NORMAL,
    LOW
}

/**
 * Lifecycle Status of a notification item.
 */
enum class NotificationStatus {
    CREATED,
    SENT,
    DELIVERED,
    READ,
    DISMISSED,
    FAILED
}

/**
 * Target Resource Type for deep linking and navigation.
 */
enum class NotificationTargetType {
    POST,
    REEL,
    STORY,
    LIVE,
    CONVERSATION,
    CALL,
    ORDER,
    PRODUCT,
    SELLER_DASHBOARD,
    HEALTH_ACCESS,
    VERIFICATION,
    APPOINTMENT,
    AI_JOB,
    SETTINGS,
    NONE
}

/**
 * Primary Notification Data Model (notifications/{notificationId}).
 */
data class NotificationItem(
    val notificationId: String = UUID.randomUUID().toString(),
    val recipientUid: String,
    val actorUid: String? = null,
    val notificationType: NotificationType,
    val category: NotificationCategory = notificationType.category,
    val title: String,
    val body: String,
    val imageReference: String? = null,
    val targetType: NotificationTargetType = NotificationTargetType.NONE,
    val targetId: String? = null,
    val deepLink: String = "healthogram://notifications/$notificationId",
    val dataReference: Map<String, String> = emptyMap(),
    val priority: NotificationPriority = notificationType.defaultPriority,
    val status: NotificationStatus = NotificationStatus.CREATED,
    val isRead: Boolean = false,
    val isSeen: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val readAt: Long? = null,
    val seenAt: Long? = null,
    val expiresAt: Long? = null,
    val groupKey: String? = null,
    val deduplicationKey: String? = null,
    val country: String = "US",
    val language: String = "en",
    val metadataVersion: Int = 1
)

/**
 * User Notification Preferences (notification_preferences/{uid}).
 */
data class NotificationPreferences(
    val uid: String,
    val pushEnabled: Boolean = true,
    val inAppEnabled: Boolean = true,
    val emailEnabledFuture: Boolean = false,
    val smsEnabledFuture: Boolean = false,

    // Category Toggles
    val socialEnabled: Boolean = true,
    val messagesEnabled: Boolean = true,
    val callsEnabled: Boolean = true,
    val translationEnabled: Boolean = true,
    val marketplaceEnabled: Boolean = true,
    val sellerEnabled: Boolean = true,
    val healthSecurityEnabled: Boolean = true, // Immutable essential security notification
    val verificationEnabled: Boolean = true,
    val appointmentsEnabled: Boolean = true,
    val organizationEnabled: Boolean = true,
    val aiEnabled: Boolean = true,
    val systemEnabled: Boolean = true,
    val promotionsEnabled: Boolean = true, // Marketing separate from essential

    // Channels
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val badgeEnabled: Boolean = true,

    // Quiet Hours
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00", // 24h format HH:mm
    val quietHoursEnd: String = "07:00",
    val allowCriticalBypass: Boolean = true, // Critical security & incoming calls bypass quiet hours

    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Registered Device Record (user_devices/{deviceId}).
 */
data class UserDeviceItem(
    val deviceId: String,
    val uid: String,
    val platform: String = "Android", // Android, iOS, Web, Desktop
    val deviceName: String,
    val appVersion: String = "1.0.0",
    val osVersion: String = "Android 14",
    val fcmTokenOrInstallationId: String,
    val notificationPermission: Boolean = true,
    val pushEnabled: Boolean = true,
    val lastTokenUpdate: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val revokedAt: Long? = null,
    val isActive: Boolean = true
)

/**
 * Authoritative Server-Calculated Unread Count (notification_unread_count/{uid}).
 */
data class NotificationUnreadCount(
    val uid: String,
    val total: Int = 0,
    val social: Int = 0,
    val messages: Int = 0,
    val calls: Int = 0,
    val marketplace: Int = 0,
    val seller: Int = 0,
    val healthSecurity: Int = 0,
    val verification: Int = 0,
    val appointments: Int = 0,
    val system: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Notification Data Retention Configuration.
 */
data class NotificationRetentionConfig(
    val defaultRetentionDays: Int = 30,
    val securityNotificationRetentionDays: Int = 90,
    val marketingNotificationRetentionDays: Int = 7,
    val systemNotificationRetentionDays: Int = 60,
    val cleanupEnabled: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Notification Delivery and Interaction Analytics Event (notification_events/{eventId}).
 */
data class NotificationAnalyticsEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val notificationId: String,
    val uid: String,
    val eventType: String, // created, sent, delivered, opened, clicked, dismissed, failed
    val category: String,
    val notificationType: String,
    val deviceId: String? = null,
    val platform: String = "Android",
    val latencyMs: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Country-Specific Notification Compliance Configuration (notification_country_config/{countryCode}).
 */
data class NotificationCountryConfig(
    val countryCode: String,
    val pushEnabled: Boolean = true,
    val smsEnabledFuture: Boolean = false,
    val emailEnabledFuture: Boolean = false,
    val supportedLanguages: List<String> = listOf("en", "ar", "es", "fr"),
    val quietHourDefaults: Pair<String, String> = "22:00" to "07:00",
    val promotionalNotificationsEnabled: Boolean = true,
    val legalNotice: String = "Compliant with national communications & patient privacy regulations.",
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Standardized FCM Delivery Payload Structure.
 * CRITICAL: Zero medical data, passwords, or sensitive credentials inside payload.
 */
data class FCMPayload(
    val title: String,
    val body: String,
    val data: Map<String, String>
)
