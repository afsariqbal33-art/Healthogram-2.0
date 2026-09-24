package com.example.healthogram.notification

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationInfrastructureTest {

    private lateinit var repository: NotificationRepository
    private lateinit var service: NotificationService

    @Before
    fun setUp() {
        repository = NotificationRepository.getInstance()
        service = NotificationService.getInstance()
    }

    @Test
    fun testNotificationUnreadCountsAndCategorization() {
        val initialCounts = repository.unreadCount.value
        assertTrue("Total unread count should be non-negative", initialCounts.total >= 0)

        // Dispatch a new message notification
        val item = service.dispatchEvent(
            recipientUid = "current_user",
            type = NotificationType.NEW_MESSAGE,
            variables = mapOf("sender_name" to "Dr. Sarah"),
            targetType = NotificationTargetType.CONVERSATION,
            targetId = "conv_test_1"
        )

        assertNotNull("Notification item should be created", item)
        assertEquals(NotificationCategory.MESSAGES, item?.category)
        assertFalse(item?.isRead ?: true)

        // Mark as read
        item?.let { repository.markNotificationRead(it.notificationId) }
        val updated = repository.notifications.value.find { it.notificationId == item?.notificationId }
        assertTrue("Notification should be marked read", updated?.isRead ?: false)
    }

    @Test
    fun testHealthPassportPrivacyInvariant() {
        // Safe access request should render without clinical data
        val safeItem = service.dispatchEvent(
            recipientUid = "current_user",
            type = NotificationType.HEALTH_ACCESS_REQUEST,
            variables = mapOf("organization_name" to "City Clinic", "request_id" to "req_123"),
            targetType = NotificationTargetType.HEALTH_ACCESS,
            targetId = "req_123"
        )

        assertNotNull("Safe health notification should be generated", safeItem)
        assertFalse("Push notification must not contain prescription info", safeItem?.body?.contains("prescription") ?: false)
        assertFalse("Push notification must not contain insulin info", safeItem?.body?.contains("insulin") ?: false)
        assertEquals(NotificationCategory.HEALTH_SECURITY, safeItem?.category)
    }

    @Test
    fun testDeduplicationAndRateLimiting() {
        val deduplicationKey = "call_test_dedup_1"
        val firstCall = service.dispatchEvent(
            recipientUid = "current_user",
            type = NotificationType.INCOMING_AUDIO_CALL,
            variables = mapOf("caller_name" to "Dr. Sarah", "call_id" to "c_1"),
            deduplicationKey = deduplicationKey
        )
        assertNotNull("First event should succeed", firstCall)

        // Duplicate event within 10 seconds should be rejected
        val duplicateCall = service.dispatchEvent(
            recipientUid = "current_user",
            type = NotificationType.INCOMING_AUDIO_CALL,
            variables = mapOf("caller_name" to "Dr. Sarah", "call_id" to "c_1"),
            deduplicationKey = deduplicationKey
        )
        assertNull("Duplicate event within 10s window must be deduplicated", duplicateCall)
    }

    @Test
    fun testMultiDeviceRegistrationAndRevocation() {
        val device = UserDeviceItem(
            deviceId = "dev_unit_test_phone",
            uid = "current_user",
            platform = "Android",
            deviceName = "Pixel 8 Pro",
            fcmTokenOrInstallationId = "fcm_token_unit_test",
            notificationPermission = true,
            pushEnabled = true
        )

        repository.registerDevice(device)
        val registered = repository.devices.value.find { it.deviceId == "dev_unit_test_phone" }
        assertNotNull("Device should be registered", registered)
        assertTrue("Device should be active", registered?.isActive ?: false)

        repository.revokeDevice("dev_unit_test_phone")
        val revoked = repository.devices.value.find { it.deviceId == "dev_unit_test_phone" }
        assertFalse("Device should be inactive after revocation", revoked?.isActive ?: true)
    }
}
