package com.example.healthogram.notification

/**
 * Cloud Functions & Backend Custom Actions for Healthogram Notifications.
 * Provides idempotent, event-driven hooks connecting subsystems to notifications.
 */
object NotificationCustomActions {

    private val service = NotificationService.getInstance()
    private val repository = NotificationRepository.getInstance()

    // -------------------------------------------------------------------------
    // Backend Cloud Function Wrappers
    // -------------------------------------------------------------------------

    fun createNotification(
        recipientUid: String,
        type: NotificationType,
        variables: Map<String, String> = emptyMap(),
        actorUid: String? = null,
        targetType: NotificationTargetType = NotificationTargetType.NONE,
        targetId: String? = null,
        deepLink: String? = null,
        dataReference: Map<String, String> = emptyMap(),
        priority: NotificationPriority? = null
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = variables,
            actorUid = actorUid,
            targetType = targetType,
            targetId = targetId,
            deepLinkOverride = deepLink,
            dataReference = dataReference,
            priorityOverride = priority
        )
    }

    fun sendSocialNotification(
        recipientUid: String,
        actorUid: String,
        actorName: String,
        type: NotificationType,
        targetId: String
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("actor_name" to actorName, "actor_uid" to actorUid),
            actorUid = actorUid,
            targetType = NotificationTargetType.POST,
            targetId = targetId,
            groupKey = if (type == NotificationType.POST_LIKE) "post_like:$targetId" else null
        )
    }

    fun sendMessageNotification(
        recipientUid: String,
        senderUid: String,
        senderName: String,
        conversationId: String,
        isMedia: Boolean = false
    ): NotificationItem? {
        val type = if (isMedia) NotificationType.MESSAGE_MEDIA else NotificationType.NEW_MESSAGE
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("sender_name" to senderName, "conversation_id" to conversationId),
            actorUid = senderUid,
            targetType = NotificationTargetType.CONVERSATION,
            targetId = conversationId
        )
    }

    fun sendCallNotification(
        recipientUid: String,
        callerUid: String,
        callerName: String,
        callId: String,
        isVideo: Boolean,
        isMissed: Boolean = false
    ): NotificationItem? {
        val type = when {
            isMissed && isVideo -> NotificationType.MISSED_VIDEO_CALL
            isMissed && !isVideo -> NotificationType.MISSED_AUDIO_CALL
            !isMissed && isVideo -> NotificationType.INCOMING_VIDEO_CALL
            else -> NotificationType.INCOMING_AUDIO_CALL
        }
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("caller_name" to callerName, "call_id" to callId),
            actorUid = callerUid,
            targetType = NotificationTargetType.CALL,
            targetId = callId,
            deduplicationKey = "call:$callId:$type"
        )
    }

    fun sendHealthSecurityNotification(
        recipientUid: String,
        orgName: String,
        requestId: String,
        type: NotificationType = NotificationType.HEALTH_ACCESS_REQUEST
    ): NotificationItem? {
        // Enforces ZERO clinical data
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("organization_name" to orgName, "request_id" to requestId),
            targetType = NotificationTargetType.HEALTH_ACCESS,
            targetId = requestId,
            priorityOverride = NotificationPriority.HIGH
        )
    }

    fun sendMarketplaceNotification(
        recipientUid: String,
        orderId: String,
        orderNumber: String,
        productName: String,
        type: NotificationType
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("order_number" to orderNumber, "product_name" to productName, "order_id" to orderId),
            targetType = NotificationTargetType.ORDER,
            targetId = orderId
        )
    }

    fun sendSellerNotification(
        sellerUid: String,
        orderId: String,
        orderNumber: String,
        productName: String,
        type: NotificationType = NotificationType.NEW_ORDER,
        itemCount: Int = 1
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = sellerUid,
            type = type,
            variables = mapOf("order_number" to orderNumber, "product_name" to productName, "order_id" to orderId, "count" to itemCount.toString()),
            targetType = NotificationTargetType.SELLER_DASHBOARD,
            targetId = orderId,
            priorityOverride = NotificationPriority.HIGH
        )
    }

    fun sendAppointmentNotification(
        recipientUid: String,
        practitionerName: String,
        appointmentId: String,
        appointmentDate: String,
        type: NotificationType = NotificationType.APPOINTMENT_REMINDER
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("practitioner_name" to practitionerName, "appointment_id" to appointmentId, "appointment_date" to appointmentDate),
            targetType = NotificationTargetType.APPOINTMENT,
            targetId = appointmentId,
            priorityOverride = NotificationPriority.HIGH
        )
    }

    fun sendVerificationNotification(
        recipientUid: String,
        requestId: String,
        type: NotificationType
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = type,
            variables = mapOf("request_id" to requestId),
            targetType = NotificationTargetType.VERIFICATION,
            targetId = requestId,
            priorityOverride = NotificationPriority.HIGH
        )
    }

    fun sendAIJobNotification(
        recipientUid: String,
        jobId: String,
        jobName: String
    ): NotificationItem? {
        return service.dispatchEvent(
            recipientUid = recipientUid,
            type = NotificationType.AI_JOB_COMPLETED,
            variables = mapOf("job_id" to jobId, "job_name" to jobName),
            targetType = NotificationTargetType.AI_JOB,
            targetId = jobId
        )
    }

    fun markNotificationRead(id: String) = repository.markNotificationRead(id)
    fun markNotificationSeen(id: String) = repository.markNotificationSeen(id)
    fun markAllNotificationsRead(category: NotificationCategory? = null) = repository.markAllRead(category)
    fun deleteNotification(id: String) = repository.deleteNotification(id)
    fun cleanupExpiredNotifications() = repository.cleanupExpiredNotifications()
}
