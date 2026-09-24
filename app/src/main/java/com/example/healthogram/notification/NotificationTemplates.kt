package com.example.healthogram.notification

/**
 * Standardized, safe Notification Template Engine.
 *
 * CRITICAL SAFETY RULES:
 * 1. Health Passport notifications MUST NOT expose diagnosis, medication, lab values, or medical reports.
 * 2. Messaging notifications MUST NOT expose full private chat messages in lock-screen notifications.
 * 3. Marketplace/Seller notifications MUST NOT expose payment card credentials, OTPs, or another seller's sales.
 * 4. Only pre-approved safe template variables are substituted.
 */
object NotificationTemplates {

    data class TemplateDefinition(
        val titleTemplate: String,
        val bodyTemplate: String,
        val deepLinkTemplate: String
    )

    private val safeTemplates = mapOf(
        // Health Passport Security (ZERO CLINICAL DATA EXPOSURE)
        NotificationType.HEALTH_ACCESS_REQUEST to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Health Passport Access Request",
                bodyTemplate = "{organization_name} requested temporary access to your Health Passport. Review now to approve or deny.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            ),
            "ar" to TemplateDefinition(
                titleTemplate = "طلب وصول إلى جوازك الصحي",
                bodyTemplate = "طلب {organization_name} الوصول المؤقت إلى جوازك الصحي. يرجى المراجعة للموافقة أو الرفض.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            ),
            "es" to TemplateDefinition(
                titleTemplate = "Solicitud de acceso al Pasaporte de Salud",
                bodyTemplate = "{organization_name} solicitó acceso temporal a su Pasaporte de Salud. Revise ahora para aprobar o rechazar.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            )
        ),
        NotificationType.HEALTH_ACCESS_APPROVED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Health Passport Access Granted",
                bodyTemplate = "You granted temporary Health Passport access to {organization_name}.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            )
        ),
        NotificationType.HEALTH_ACCESS_REVOKED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Health Passport Access Revoked",
                bodyTemplate = "Access to your Health Passport for {organization_name} was revoked.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            )
        ),
        NotificationType.HEALTH_QR_ACCESS_ATTEMPT to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Security Alert: QR Scan Detected",
                bodyTemplate = "Your dynamic Health Passport QR was scanned. Tap to verify this session.",
                deepLinkTemplate = "healthogram://health-access/{request_id}"
            )
        ),
        NotificationType.HEALTH_SECURITY_EVENT to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Health Passport Security Notice",
                bodyTemplate = "A security-sensitive event occurred regarding your health credentials. Check your audit log.",
                deepLinkTemplate = "healthogram://health-access/audit"
            )
        ),

        // Messaging (Privacy-protected lockscreen notifications)
        NotificationType.NEW_MESSAGE to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "{sender_name}",
                bodyTemplate = "Sent you a message.",
                deepLinkTemplate = "healthogram://message/{conversation_id}"
            ),
            "ar" to TemplateDefinition(
                titleTemplate = "{sender_name}",
                bodyTemplate = "أرسل لك رسالة جديدة.",
                deepLinkTemplate = "healthogram://message/{conversation_id}"
            )
        ),
        NotificationType.MESSAGE_MEDIA to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "{sender_name}",
                bodyTemplate = "Sent a voice note or media file.",
                deepLinkTemplate = "healthogram://message/{conversation_id}"
            )
        ),

        // Calls
        NotificationType.INCOMING_AUDIO_CALL to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Incoming Audio Call",
                bodyTemplate = "{caller_name} is calling you via secure HD voice...",
                deepLinkTemplate = "healthogram://call/{call_id}"
            ),
            "ar" to TemplateDefinition(
                titleTemplate = "مكالمة صوتية واردة",
                bodyTemplate = "{caller_name} يتصل بك عبر اتصال صوتي مشفر...",
                deepLinkTemplate = "healthogram://call/{call_id}"
            )
        ),
        NotificationType.INCOMING_VIDEO_CALL to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Incoming Video Call",
                bodyTemplate = "{caller_name} is calling you via secure video...",
                deepLinkTemplate = "healthogram://call/{call_id}"
            )
        ),
        NotificationType.MISSED_AUDIO_CALL to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Missed Call",
                bodyTemplate = "You missed a secure audio call from {caller_name}.",
                deepLinkTemplate = "healthogram://call/{call_id}"
            )
        ),
        NotificationType.MISSED_VIDEO_CALL to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Missed Video Call",
                bodyTemplate = "You missed a video call from {caller_name}.",
                deepLinkTemplate = "healthogram://call/{call_id}"
            )
        ),

        // Social
        NotificationType.NEW_FOLLOWER to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "New Follower",
                bodyTemplate = "{actor_name} started following you.",
                deepLinkTemplate = "healthogram://profile/{actor_uid}"
            ),
            "ar" to TemplateDefinition(
                titleTemplate = "متابع جديد",
                bodyTemplate = "بدأ {actor_name} بمتابعتك.",
                deepLinkTemplate = "healthogram://profile/{actor_uid}"
            )
        ),
        NotificationType.POST_LIKE to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Post Liked",
                bodyTemplate = "{actor_name} liked your health update.",
                deepLinkTemplate = "healthogram://post/{post_id}"
            )
        ),
        NotificationType.POST_COMMENT to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "New Comment",
                bodyTemplate = "{actor_name} commented on your post.",
                deepLinkTemplate = "healthogram://post/{post_id}"
            )
        ),
        NotificationType.MENTION to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Mention",
                bodyTemplate = "{actor_name} mentioned you in a comment.",
                deepLinkTemplate = "healthogram://post/{post_id}"
            )
        ),
        NotificationType.LIVE_STARTED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Live Stream",
                bodyTemplate = "{creator_name} is now live hosting a health Q&A.",
                deepLinkTemplate = "healthogram://live/{live_id}"
            )
        ),

        // Marketplace Customer
        NotificationType.ORDER_CONFIRMED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Order Confirmed",
                bodyTemplate = "Your order #{order_number} for {product_name} is confirmed.",
                deepLinkTemplate = "healthogram://order/{order_id}"
            )
        ),
        NotificationType.ORDER_SHIPPED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Order Shipped",
                bodyTemplate = "Order #{order_number} has been dispatched with tracking.",
                deepLinkTemplate = "healthogram://order/{order_id}"
            )
        ),
        NotificationType.ORDER_DELIVERED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Order Delivered",
                bodyTemplate = "Package #{order_number} has been delivered successfully.",
                deepLinkTemplate = "healthogram://order/{order_id}"
            )
        ),
        NotificationType.REFUND_COMPLETED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Refund Processed",
                bodyTemplate = "A refund for order #{order_number} was successfully credited.",
                deepLinkTemplate = "healthogram://order/{order_id}"
            )
        ),

        // Seller
        NotificationType.NEW_ORDER to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "New Seller Order",
                bodyTemplate = "You received a new order #{order_number} for {product_name}.",
                deepLinkTemplate = "healthogram://seller/orders/{order_id}"
            )
        ),
        NotificationType.LOW_INVENTORY to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Inventory Alert",
                bodyTemplate = "Inventory is low for {product_name} ({count} items left).",
                deepLinkTemplate = "healthogram://seller/inventory/{product_id}"
            )
        ),
        NotificationType.PAYOUT_COMPLETED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Payout Transferred",
                bodyTemplate = "Your scheduled payout has been transferred to your registered bank account.",
                deepLinkTemplate = "healthogram://seller/payouts"
            )
        ),

        // Verification
        NotificationType.VERIFICATION_APPROVED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Verification Approved",
                bodyTemplate = "Your professional healthcare verification has been approved.",
                deepLinkTemplate = "healthogram://verification/{request_id}"
            )
        ),
        NotificationType.VERIFICATION_UNDER_REVIEW to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Verification Under Review",
                bodyTemplate = "Your identity and credential submission is under review by compliance.",
                deepLinkTemplate = "healthogram://verification/{request_id}"
            )
        ),

        // Appointments
        NotificationType.APPOINTMENT_CONFIRMED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Appointment Confirmed",
                bodyTemplate = "Your consultation with {practitioner_name} is confirmed for {appointment_date}.",
                deepLinkTemplate = "healthogram://appointment/{appointment_id}"
            )
        ),
        NotificationType.APPOINTMENT_REMINDER to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Upcoming Consultation",
                bodyTemplate = "Reminder: Consultation with {practitioner_name} starts at {appointment_date}.",
                deepLinkTemplate = "healthogram://appointment/{appointment_id}"
            )
        ),

        // Translation
        NotificationType.TRANSLATION_COMPLETED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Translation Complete",
                bodyTemplate = "Your requested document translation is ready to view securely in Healthogram.",
                deepLinkTemplate = "healthogram://translation/{job_id}"
            )
        ),

        // AI Studio
        NotificationType.AI_JOB_COMPLETED to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "AI Studio Processing Finished",
                bodyTemplate = "Your AI generation job {job_name} has completed successfully.",
                deepLinkTemplate = "healthogram://aistudio/{job_id}"
            )
        ),

        // System
        NotificationType.MAINTENANCE to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Scheduled System Maintenance",
                bodyTemplate = "Healthogram services will undergo brief maintenance on {date_time}.",
                deepLinkTemplate = "healthogram://system/status"
            )
        ),
        NotificationType.SECURITY_ALERT to mapOf(
            "en" to TemplateDefinition(
                titleTemplate = "Account Security Alert",
                bodyTemplate = "A new login was detected from {device_name}. Review your active sessions.",
                deepLinkTemplate = "healthogram://devices"
            )
        )
    )

    /**
     * Sanitizes template variables to prevent medical leaks and injection.
     */
    fun sanitizeVariables(variables: Map<String, String>): Map<String, String> {
        val forbiddenKeys = listOf("diagnosis", "medication", "prescription", "lab_value", "password", "token", "credit_card", "cvv")
        return variables.filterKeys { key ->
            forbiddenKeys.none { forbidden -> key.lowercase().contains(forbidden) }
        }.mapValues { (_, value) ->
            // Truncate long texts
            if (value.length > 80) value.take(77) + "..." else value
        }
    }

    /**
     * Resolves localized title, body, and deepLink safely.
     */
    fun render(
        type: NotificationType,
        language: String,
        variables: Map<String, String>
    ): Triple<String, String, String> {
        val sanitized = sanitizeVariables(variables)
        val langKey = if (language.startsWith("ar")) "ar" else if (language.startsWith("es")) "es" else "en"
        val templateMap = safeTemplates[type] ?: safeTemplates[NotificationType.SECURITY_ALERT]!!
        val template = templateMap[langKey] ?: templateMap["en"] ?: TemplateDefinition(
            titleTemplate = "Healthogram Notification",
            bodyTemplate = "You have a new update in Healthogram.",
            deepLinkTemplate = "healthogram://notifications"
        )

        var title = template.titleTemplate
        var body = template.bodyTemplate
        var deepLink = template.deepLinkTemplate

        sanitized.forEach { (k, v) ->
            title = title.replace("{$k}", v)
            body = body.replace("{$k}", v)
            deepLink = deepLink.replace("{$k}", v)
        }

        return Triple(title, body, deepLink)
    }
}
