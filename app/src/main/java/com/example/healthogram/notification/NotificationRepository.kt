package com.example.healthogram.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository for Healthogram Notifications.
 * Handles state, multi-device tokens, unread counts, grouping, deduplication, and retention.
 */
class NotificationRepository private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: NotificationRepository? = null

        fun getInstance(): NotificationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationRepository().also { INSTANCE = it }
            }
        }
    }

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(NotificationUnreadCount(uid = "current_user"))
    val unreadCount: StateFlow<NotificationUnreadCount> = _unreadCount.asStateFlow()

    private val _preferences = MutableStateFlow(NotificationPreferences(uid = "current_user"))
    val preferences: StateFlow<NotificationPreferences> = _preferences.asStateFlow()

    private val _devices = MutableStateFlow<List<UserDeviceItem>>(emptyList())
    val devices: StateFlow<List<UserDeviceItem>> = _devices.asStateFlow()

    private val _retentionConfig = MutableStateFlow(NotificationRetentionConfig())
    val retentionConfig: StateFlow<NotificationRetentionConfig> = _retentionConfig.asStateFlow()

    private val _analyticsEvents = MutableStateFlow<List<NotificationAnalyticsEvent>>(emptyList())
    val analyticsEvents: StateFlow<List<NotificationAnalyticsEvent>> = _analyticsEvents.asStateFlow()

    // Deduplication tracking: key -> timestamp
    private val deduplicationMap = mutableMapOf<String, Long>()

    // Rate limiting tracking: uid:type -> list of timestamps
    private val rateLimitMap = mutableMapOf<String, MutableList<Long>>()

    init {
        seedInitialData()
        recalculateUnreadCount()
    }

    private fun seedInitialData() {
        val now = System.currentTimeMillis()
        val seedItems = listOf(
            NotificationItem(
                notificationId = "notif_hp_01",
                recipientUid = "current_user",
                actorUid = "org_nova_clinic",
                notificationType = NotificationType.HEALTH_ACCESS_REQUEST,
                category = NotificationCategory.HEALTH_SECURITY,
                title = "Health Passport Access Request",
                body = "Nova Telehealth Center requested temporary 24-hour access to your Health Passport.",
                targetType = NotificationTargetType.HEALTH_ACCESS,
                targetId = "req_nova_9921",
                deepLink = "healthogram://health-access/req_nova_9921",
                priority = NotificationPriority.HIGH,
                createdAt = now - (12 * 60 * 1000), // 12 mins ago
                isRead = false,
                isSeen = true,
                dataReference = mapOf("org_name" to "Nova Telehealth Center", "scope" to "Emergency Allergies & Vitals")
            ),
            NotificationItem(
                notificationId = "notif_msg_01",
                recipientUid = "current_user",
                actorUid = "user_dr_sarah",
                notificationType = NotificationType.NEW_MESSAGE,
                category = NotificationCategory.MESSAGES,
                title = "Dr. Sarah Jenkins",
                body = "Sent you a message regarding your recent lab review.",
                targetType = NotificationTargetType.CONVERSATION,
                targetId = "conv_dr_sarah_101",
                deepLink = "healthogram://message/conv_dr_sarah_101",
                priority = NotificationPriority.NORMAL,
                createdAt = now - (45 * 60 * 1000), // 45 mins ago
                isRead = false,
                isSeen = true
            ),
            NotificationItem(
                notificationId = "notif_call_01",
                recipientUid = "current_user",
                actorUid = "user_dr_sarah",
                notificationType = NotificationType.MISSED_AUDIO_CALL,
                category = NotificationCategory.CALLS,
                title = "Missed Call",
                body = "You missed an encrypted HD audio consultation call from Dr. Sarah Jenkins.",
                targetType = NotificationTargetType.CALL,
                targetId = "call_audio_881",
                deepLink = "healthogram://call/call_audio_881",
                priority = NotificationPriority.HIGH,
                createdAt = now - (2 * 3600 * 1000), // 2 hours ago
                isRead = false,
                isSeen = true
            ),
            NotificationItem(
                notificationId = "notif_app_01",
                recipientUid = "current_user",
                actorUid = "org_central_hospital",
                notificationType = NotificationType.APPOINTMENT_REMINDER,
                category = NotificationCategory.APPOINTMENTS,
                title = "Appointment Reminder",
                body = "Your cardiology video follow-up with Dr. Marcus Vance starts tomorrow at 10:00 AM.",
                targetType = NotificationTargetType.APPOINTMENT,
                targetId = "apt_cardio_554",
                deepLink = "healthogram://appointment/apt_cardio_554",
                priority = NotificationPriority.HIGH,
                createdAt = now - (4 * 3600 * 1000), // 4 hours ago
                isRead = true,
                isSeen = true,
                readAt = now - (3 * 3600 * 1000)
            ),
            NotificationItem(
                notificationId = "notif_mkt_01",
                recipientUid = "current_user",
                actorUid = "seller_vital_health",
                notificationType = NotificationType.ORDER_SHIPPED,
                category = NotificationCategory.MARKETPLACE,
                title = "Order Shipped",
                body = "Order #HLTH-90214 (Smart Blood Pressure Monitor) is in transit with DHL Express.",
                targetType = NotificationTargetType.ORDER,
                targetId = "ord_90214",
                deepLink = "healthogram://order/ord_90214",
                priority = NotificationPriority.HIGH,
                createdAt = now - (6 * 3600 * 1000),
                isRead = true,
                isSeen = true,
                readAt = now - (5 * 3600 * 1000)
            ),
            NotificationItem(
                notificationId = "notif_soc_01",
                recipientUid = "current_user",
                actorUid = "user_ahmed_al_mansoor",
                notificationType = NotificationType.NEW_FOLLOWER,
                category = NotificationCategory.SOCIAL,
                title = "New Follower",
                body = "Ahmed Al-Mansoor started following your public fitness stories.",
                targetType = NotificationTargetType.POST,
                targetId = "user_ahmed",
                deepLink = "healthogram://profile/user_ahmed",
                priority = NotificationPriority.LOW,
                createdAt = now - (14 * 3600 * 1000),
                isRead = true,
                isSeen = true,
                readAt = now - (12 * 3600 * 1000)
            ),
            NotificationItem(
                notificationId = "notif_sel_01",
                recipientUid = "current_user",
                actorUid = "marketplace_engine",
                notificationType = NotificationType.NEW_ORDER,
                category = NotificationCategory.SELLER,
                title = "New Seller Order",
                body = "Customer ordered 2x Organic Electrolyte Powder from your verified storefront.",
                targetType = NotificationTargetType.SELLER_DASHBOARD,
                targetId = "seller_ord_411",
                deepLink = "healthogram://seller/orders/seller_ord_411",
                priority = NotificationPriority.HIGH,
                createdAt = now - (20 * 3600 * 1000),
                isRead = true,
                isSeen = true,
                readAt = now - (18 * 3600 * 1000)
            ),
            NotificationItem(
                notificationId = "notif_ver_01",
                recipientUid = "current_user",
                actorUid = "compliance_admin",
                notificationType = NotificationType.VERIFICATION_APPROVED,
                category = NotificationCategory.VERIFICATION,
                title = "Verification Approved",
                body = "Your Medical Practitioner identity documents were successfully verified.",
                targetType = NotificationTargetType.VERIFICATION,
                targetId = "ver_doc_991",
                deepLink = "healthogram://verification/ver_doc_991",
                priority = NotificationPriority.HIGH,
                createdAt = now - (36 * 3600 * 1000),
                isRead = true,
                isSeen = true,
                readAt = now - (30 * 3600 * 1000)
            ),
            NotificationItem(
                notificationId = "notif_sys_01",
                recipientUid = "current_user",
                actorUid = "healthogram_ops",
                notificationType = NotificationType.APP_UPDATE,
                category = NotificationCategory.SYSTEM,
                title = "Healthogram System Update",
                body = "Version 3.8.0 is active with encrypted WebRTC Calling and multilingual translation.",
                targetType = NotificationTargetType.SETTINGS,
                targetId = "update_v38",
                deepLink = "healthogram://system/status",
                priority = NotificationPriority.NORMAL,
                createdAt = now - (48 * 3600 * 1000),
                isRead = true,
                isSeen = true,
                readAt = now - (40 * 3600 * 1000)
            )
        )
        _notifications.value = seedItems

        // Seed 3 active user devices (Multi-device support: up to 4 sessions)
        val seedDevices = listOf(
            UserDeviceItem(
                deviceId = "dev_phone_s24",
                uid = "current_user",
                platform = "Android",
                deviceName = "Samsung Galaxy S24 Ultra",
                appVersion = "3.8.0",
                osVersion = "Android 14 (API 34)",
                fcmTokenOrInstallationId = "fcm_token_s24_982b_active",
                notificationPermission = true,
                pushEnabled = true,
                lastActiveAt = now
            ),
            UserDeviceItem(
                deviceId = "dev_tablet_tab9",
                uid = "current_user",
                platform = "Android",
                deviceName = "Galaxy Tab S9+ (Clinic Tablet)",
                appVersion = "3.8.0",
                osVersion = "Android 14",
                fcmTokenOrInstallationId = "fcm_token_tab9_321c_active",
                notificationPermission = true,
                pushEnabled = true,
                lastActiveAt = now - (3 * 3600 * 1000)
            ),
            UserDeviceItem(
                deviceId = "dev_macbook_pro",
                uid = "current_user",
                platform = "Web/Desktop",
                deviceName = "MacBook Pro (Chrome Desktop)",
                appVersion = "Web 3.8.0",
                osVersion = "macOS Sonoma",
                fcmTokenOrInstallationId = "fcm_web_token_77a9_active",
                notificationPermission = true,
                pushEnabled = true,
                lastActiveAt = now - (1 * 3600 * 1000)
            )
        )
        _devices.value = seedDevices
    }

    /**
     * Recalculates authoritative unread counts.
     */
    private fun recalculateUnreadCount() {
        val list = _notifications.value
        val totalUnread = list.count { !it.isRead }
        val socialUnread = list.count { it.category == NotificationCategory.SOCIAL && !it.isRead }
        val msgUnread = list.count { it.category == NotificationCategory.MESSAGES && !it.isRead }
        val callUnread = list.count { it.category == NotificationCategory.CALLS && !it.isRead }
        val mktUnread = list.count { it.category == NotificationCategory.MARKETPLACE && !it.isRead }
        val sellerUnread = list.count { it.category == NotificationCategory.SELLER && !it.isRead }
        val healthUnread = list.count { it.category == NotificationCategory.HEALTH_SECURITY && !it.isRead }
        val verUnread = list.count { it.category == NotificationCategory.VERIFICATION && !it.isRead }
        val aptUnread = list.count { it.category == NotificationCategory.APPOINTMENTS && !it.isRead }
        val sysUnread = list.count { it.category == NotificationCategory.SYSTEM && !it.isRead }

        _unreadCount.value = NotificationUnreadCount(
            uid = "current_user",
            total = totalUnread,
            social = socialUnread,
            messages = msgUnread,
            calls = callUnread,
            marketplace = mktUnread,
            seller = sellerUnread,
            healthSecurity = healthUnread,
            verification = verUnread,
            appointments = aptUnread,
            system = sysUnread,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Inserts a new notification with deduplication and grouping check.
     */
    fun insertNotification(item: NotificationItem): Boolean {
        // Deduplication check
        item.deduplicationKey?.let { key ->
            val prev = deduplicationMap[key]
            if (prev != null && System.currentTimeMillis() - prev < 10000) {
                // Ignore duplicate event
                return false
            }
            deduplicationMap[key] = System.currentTimeMillis()
        }

        // Check if groupable (e.g., social likes on same post)
        if (item.groupKey != null && item.category == NotificationCategory.SOCIAL) {
            val existingIdx = _notifications.value.indexOfFirst { it.groupKey == item.groupKey }
            if (existingIdx != -1) {
                val existing = _notifications.value[existingIdx]
                val updatedGroup = existing.copy(
                    title = "Recent Activity",
                    body = "Multiple people interacted with your post.",
                    createdAt = System.currentTimeMillis(),
                    isRead = false
                )
                val mutable = _notifications.value.toMutableList()
                mutable[existingIdx] = updatedGroup
                _notifications.value = mutable
                recalculateUnreadCount()
                return true
            }
        }

        // Normal insertion at beginning
        val current = _notifications.value.toMutableList()
        current.add(0, item)
        _notifications.value = current
        recalculateUnreadCount()

        recordAnalytics(
            NotificationAnalyticsEvent(
                notificationId = item.notificationId,
                uid = item.recipientUid,
                eventType = "created",
                category = item.category.key,
                notificationType = item.notificationType.name
            )
        )
        return true
    }

    fun markNotificationRead(notificationId: String) {
        val now = System.currentTimeMillis()
        _notifications.value = _notifications.value.map {
            if (it.notificationId == notificationId) {
                it.copy(isRead = true, isSeen = true, readAt = now, seenAt = it.seenAt ?: now)
            } else it
        }
        recalculateUnreadCount()
        recordAnalytics(
            NotificationAnalyticsEvent(
                notificationId = notificationId,
                uid = "current_user",
                eventType = "opened",
                category = "user_action",
                notificationType = "read"
            )
        )
    }

    fun markNotificationSeen(notificationId: String) {
        val now = System.currentTimeMillis()
        _notifications.value = _notifications.value.map {
            if (it.notificationId == notificationId && !it.isSeen) {
                it.copy(isSeen = true, seenAt = now)
            } else it
        }
    }

    fun markAllRead(category: NotificationCategory? = null) {
        val now = System.currentTimeMillis()
        _notifications.value = _notifications.value.map {
            if (category == null || it.category == category) {
                it.copy(isRead = true, isSeen = true, readAt = it.readAt ?: now, seenAt = it.seenAt ?: now)
            } else it
        }
        recalculateUnreadCount()
    }

    fun deleteNotification(notificationId: String) {
        _notifications.value = _notifications.value.filterNot { it.notificationId == notificationId }
        recalculateUnreadCount()
        recordAnalytics(
            NotificationAnalyticsEvent(
                notificationId = notificationId,
                uid = "current_user",
                eventType = "dismissed",
                category = "user_action",
                notificationType = "delete"
            )
        )
    }

    fun clearCategory(category: NotificationCategory) {
        _notifications.value = _notifications.value.filterNot { it.category == category }
        recalculateUnreadCount()
    }

    fun updatePreferences(newPrefs: NotificationPreferences) {
        _preferences.value = newPrefs.copy(updatedAt = System.currentTimeMillis())
    }

    fun registerDevice(device: UserDeviceItem) {
        val current = _devices.value.filterNot { it.deviceId == device.deviceId }.toMutableList()
        current.add(0, device)
        // Keep max 4 sessions
        _devices.value = current.take(4)
    }

    fun revokeDevice(deviceId: String) {
        _devices.value = _devices.value.map {
            if (it.deviceId == deviceId) {
                it.copy(isActive = false, revokedAt = System.currentTimeMillis())
            } else it
        }
    }

    fun cleanupExpiredNotifications() {
        val now = System.currentTimeMillis()
        _notifications.value = _notifications.value.filter {
            it.expiresAt == null || it.expiresAt > now
        }
        recalculateUnreadCount()
    }

    fun recordAnalytics(event: NotificationAnalyticsEvent) {
        val current = _analyticsEvents.value.toMutableList()
        current.add(0, event)
        if (current.size > 200) {
            _analyticsEvents.value = current.take(200)
        } else {
            _analyticsEvents.value = current
        }
    }

    fun checkRateLimit(uid: String, type: NotificationType): Boolean {
        val key = "$uid:${type.name}"
        val now = System.currentTimeMillis()
        val windowMs = 60 * 1000 // 1 minute
        val maxPerMinute = when (type.defaultPriority) {
            NotificationPriority.CRITICAL -> 20
            NotificationPriority.HIGH -> 10
            NotificationPriority.NORMAL -> 5
            NotificationPriority.LOW -> 3
        }

        val timestamps = rateLimitMap.getOrPut(key) { mutableListOf() }
        timestamps.removeAll { now - it > windowMs }
        if (timestamps.size >= maxPerMinute) {
            return false // Rate limit exceeded
        }
        timestamps.add(now)
        return true
    }
}
