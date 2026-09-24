package com.example.healthogram.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized Notification Dispatch & Security Service.
 * Implements preference validation, privacy sanitization, quiet hours check, and multi-device dispatch.
 */
class NotificationService private constructor(
    private val repository: NotificationRepository = NotificationRepository.getInstance()
) {

    companion object {
        @Volatile
        private var INSTANCE: NotificationService? = null

        fun getInstance(): NotificationService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationService().also { INSTANCE = it }
            }
        }
    }

    // Heads-up in-app notification banner toast
    private val _inAppBanner = MutableStateFlow<NotificationItem?>(null)
    val inAppBanner: StateFlow<NotificationItem?> = _inAppBanner.asStateFlow()

    fun dismissInAppBanner() {
        _inAppBanner.value = null
    }

    /**
     * Dispatches an event through the strict security and preference pipeline.
     */
    fun dispatchEvent(
        recipientUid: String,
        type: NotificationType,
        variables: Map<String, String> = emptyMap(),
        actorUid: String? = null,
        targetType: NotificationTargetType = NotificationTargetType.NONE,
        targetId: String? = null,
        deepLinkOverride: String? = null,
        dataReference: Map<String, String> = emptyMap(),
        priorityOverride: NotificationPriority? = null,
        deduplicationKey: String? = null,
        groupKey: String? = null
    ): NotificationItem? {
        val prefs = repository.preferences.value

        // 1. Preference & Category Check
        if (!isCategoryAllowedByPreferences(type.category, prefs)) {
            return null
        }

        // 2. Abuse / Rate Limit Check
        if (!repository.checkRateLimit(recipientUid, type)) {
            return null
        }

        // 3. Quiet Hours Check
        val priority = priorityOverride ?: type.defaultPriority
        if (isQuietHoursActive(prefs)) {
            // Critical alerts and incoming calls bypass quiet hours if configured
            if (!(prefs.allowCriticalBypass && (priority == NotificationPriority.CRITICAL || type == NotificationType.INCOMING_AUDIO_CALL || type == NotificationType.INCOMING_VIDEO_CALL))) {
                // Delayed / suppressed during quiet hours
                if (type.category == NotificationCategory.PROMOTIONS || type.category == NotificationCategory.SOCIAL) {
                    return null
                }
            }
        }

        // 4. Localized Rendering & Privacy Sanitization
        val (renderedTitle, renderedBody, defaultDeepLink) = NotificationTemplates.render(
            type = type,
            language = "en",
            variables = variables
        )

        // 5. Strict Health Passport Privacy Guard
        assertNoHealthPassportClinicalData(renderedTitle, renderedBody)

        val finalDeepLink = deepLinkOverride ?: defaultDeepLink

        // 6. Build Notification Document
        val item = NotificationItem(
            recipientUid = recipientUid,
            actorUid = actorUid,
            notificationType = type,
            category = type.category,
            title = renderedTitle,
            body = renderedBody,
            targetType = targetType,
            targetId = targetId,
            deepLink = finalDeepLink,
            dataReference = dataReference,
            priority = priority,
            status = NotificationStatus.CREATED,
            groupKey = groupKey,
            deduplicationKey = deduplicationKey ?: if (type == NotificationType.INCOMING_AUDIO_CALL || type == NotificationType.INCOMING_VIDEO_CALL) "call:$targetId" else null
        )

        // 7. Store in Notification Center
        val inserted = repository.insertNotification(item)
        if (!inserted) return null

        // 8. Push Delivery Simulation to Active Registered Devices
        if (prefs.pushEnabled) {
            val activeDevices = repository.devices.value.filter { it.isActive && it.pushEnabled }
            activeDevices.forEach { device ->
                simulateFCMDispatch(item, device)
            }
        }

        // 9. In-App Heads-Up Banner (if enabled and high/critical)
        if (prefs.inAppEnabled && (priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL)) {
            _inAppBanner.value = item
        }

        return item
    }

    /**
     * Simulates Firebase Cloud Messaging (FCM) push payload dispatch.
     * Keeps payloads strictly under 4KB with ID references and zero clinical/credential data.
     */
    private fun simulateFCMDispatch(item: NotificationItem, device: UserDeviceItem) {
        val safePayload = FCMPayload(
            title = item.title,
            body = item.body,
            data = mapOf(
                "notification_id" to item.notificationId,
                "notification_type" to item.notificationType.name,
                "category" to item.category.key,
                "target_type" to item.targetType.name,
                "target_id" to (item.targetId ?: ""),
                "deep_link" to item.deepLink
            )
        )

        repository.recordAnalytics(
            NotificationAnalyticsEvent(
                notificationId = item.notificationId,
                uid = item.recipientUid,
                eventType = "sent",
                category = item.category.key,
                notificationType = item.notificationType.name,
                deviceId = device.deviceId,
                platform = device.platform
            )
        )
    }

    /**
     * Checks if current time is inside Quiet Hours interval.
     */
    fun isQuietHoursActive(prefs: NotificationPreferences): Boolean {
        if (!prefs.quietHoursEnabled) return false
        try {
            val calendar = Calendar.getInstance()
            val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)

            val startParts = prefs.quietHoursStart.split(":").map { it.toInt() }
            val endParts = prefs.quietHoursEnd.split(":").map { it.toInt() }

            val startMinutes = startParts[0] * 60 + startParts[1]
            val endMinutes = endParts[0] * 60 + endParts[1]

            return if (startMinutes < endMinutes) {
                currentMinutes in startMinutes..endMinutes
            } else {
                // Spans midnight (e.g. 22:00 to 07:00)
                currentMinutes >= startMinutes || currentMinutes <= endMinutes
            }
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Asserts zero clinical details in push notifications.
     */
    private fun assertNoHealthPassportClinicalData(title: String, body: String) {
        val clinicalKeywords = listOf("diabetes", "hypertension", "hiv", "cancer", "prescription", "dosage", "mg", "insulin", "biopsy", "glucose")
        val combined = "$title $body".lowercase()
        for (kw in clinicalKeywords) {
            if (combined.contains(kw)) {
                throw SecurityException("CRITICAL SAFETY VIOLATION: Push notification contains sensitive clinical content ($kw).")
            }
        }
    }

    private fun isCategoryAllowedByPreferences(category: NotificationCategory, prefs: NotificationPreferences): Boolean {
        return when (category) {
            NotificationCategory.SOCIAL -> prefs.socialEnabled
            NotificationCategory.MESSAGES -> prefs.messagesEnabled
            NotificationCategory.CALLS -> prefs.callsEnabled
            NotificationCategory.TRANSLATION -> prefs.translationEnabled
            NotificationCategory.MARKETPLACE -> prefs.marketplaceEnabled
            NotificationCategory.SELLER -> prefs.sellerEnabled
            NotificationCategory.HEALTH_SECURITY -> true // Non-negotiable security notification
            NotificationCategory.VERIFICATION -> prefs.verificationEnabled
            NotificationCategory.APPOINTMENTS -> prefs.appointmentsEnabled
            NotificationCategory.ORGANIZATION -> prefs.organizationEnabled
            NotificationCategory.AI_STUDIO -> prefs.aiEnabled
            NotificationCategory.SYSTEM -> prefs.systemEnabled
            NotificationCategory.PROMOTIONS -> prefs.promotionsEnabled
        }
    }

    // Delegation to repository for convenience
    fun markNotificationRead(id: String) = repository.markNotificationRead(id)
    fun markNotificationSeen(id: String) = repository.markNotificationSeen(id)
    fun markAllRead(category: NotificationCategory? = null) = repository.markAllRead(category)
    fun deleteNotification(id: String) = repository.deleteNotification(id)
    fun clearCategory(category: NotificationCategory) = repository.clearCategory(category)
}
