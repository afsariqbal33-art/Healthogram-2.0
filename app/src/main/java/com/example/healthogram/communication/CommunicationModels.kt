package com.example.healthogram.communication

import java.util.UUID

/**
 * HEALTHOGRAM STEP 11: COMMUNICATION DATA MODELS
 * 
 * Supports:
 * - One-to-one text messaging, rich media, voice notes, replies, reactions, editing, deletion
 * - Privacy controls (communication_settings/{uid})
 * - Real-time WebRTC audio & video calling sessions (call_sessions/{callId})
 * - Multi-device management (user_devices/{deviceId} up to 4 sessions)
 * - Health Passport isolation guarantees
 * - Step 12 translation preparation fields
 */

enum class ConversationType {
    DIRECT,
    GROUP_FUTURE,
    SUPPORT_FUTURE
}

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    AUDIO,
    VOICE,
    DOCUMENT,
    SYSTEM,
    CALL,
    SHARED_POST,
    SHARED_REEL,
    SHARED_PRODUCT
}

enum class DeliveryStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class MessageRequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    BLOCKED,
    EXPIRED
}

enum class ReportStatus {
    OPEN,
    UNDER_REVIEW,
    RESOLVED,
    DISMISSED
}

enum class PresenceState {
    ONLINE,
    AWAY,
    OFFLINE,
    BUSY
}

enum class NotificationPreviewType {
    FULL,
    NAME_ONLY,
    NO_PREVIEW
}

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallStatus {
    INITIATED,
    RINGING,
    ACCEPTED,
    CONNECTING,
    CONNECTED,
    DECLINED,
    MISSED,
    CANCELLED,
    ENDED,
    FAILED,
    BUSY
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

/**
 * Communication settings: communication_settings/{uid}
 */
data class CommunicationSettings(
    val uid: String,
    val allowTextMessages: Boolean = true,
    val allowAudioCalls: Boolean = true,
    val allowVideoCalls: Boolean = true,
    val allowVoiceMessages: Boolean = true,
    val allowMediaMessages: Boolean = true,
    val allowMessageRequests: Boolean = true,
    val allowCallsFromFollowers: Boolean = true,
    val allowCallsFromFollowing: Boolean = true,
    val allowCallsFromVerifiedAccounts: Boolean = true,
    val allowCallsFromAnyone: Boolean = false,
    val showOnlineStatus: Boolean = true,
    val showLastSeen: Boolean = true,
    val showReadReceipts: Boolean = true,
    val showTypingIndicator: Boolean = true,
    val allowGroupInvites: Boolean = true,
    val allowForwarding: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Conversations collection: conversations/{conversationId}
 */
data class Conversation(
    val conversationId: String = UUID.randomUUID().toString(),
    val conversationType: ConversationType = ConversationType.DIRECT,
    val createdBy: String,
    val participantIds: List<String>,
    val participantCount: Int = participantIds.size,
    val lastMessageId: String? = null,
    val lastMessagePreview: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageSenderId: String? = null,
    val lastMessageAt: Long = System.currentTimeMillis(),
    val unreadCountMap: Map<String, Int> = emptyMap(),
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Conversation Participants: conversation_participants/{participantId}
 */
data class ConversationParticipant(
    val participantId: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val uid: String,
    val role: String = "member",
    val joinedAt: Long = System.currentTimeMillis(),
    val lastReadMessageId: String? = null,
    val lastReadAt: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val isBlocked: Boolean = false,
    val notificationEnabled: Boolean = true
)

/**
 * Messages collection: messages/{messageId}
 */
data class Message(
    val messageId: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderUid: String,
    val messageType: MessageType = MessageType.TEXT,
    val text: String,
    val mediaReference: String? = null,
    val mediaThumbnail: String? = null,
    val mediaDurationSeconds: Int? = null,
    val fileName: String? = null,
    val fileSizeBytes: Long? = null,
    val replyToMessageId: String? = null,
    val replyPreview: String? = null,
    val forwardedFromMessageId: String? = null,
    val reactionSummary: Map<String, Int> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isForwarded: Boolean = false,
    val deliveryStatus: DeliveryStatus = DeliveryStatus.SENT,
    val clientMessageId: String = UUID.randomUUID().toString(),

    // Shared Reference Metadata (Step 11 Section 48)
    val sharedContentType: String? = null,
    val sharedContentId: String? = null,

    // Translation Foundation (Step 11 Section 49 / Step 12 prep)
    val translationEnabled: Boolean = false,
    val sourceLanguage: String = "en",
    val targetLanguage: String? = null,
    val autoDetectLanguage: Boolean = true,
    val translatedText: String? = null,
    val translationStatus: String = "none",
    val translationProvider: String? = null,
    val translationVersion: String? = null
)

/**
 * Message Requests: message_requests/{requestId}
 */
data class MessageRequest(
    val requestId: String = UUID.randomUUID().toString(),
    val senderUid: String,
    val recipientUid: String,
    val conversationId: String,
    val status: MessageRequestStatus = MessageRequestStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * User Blocks: user_blocks/{blockId}
 */
data class UserBlock(
    val blockId: String = UUID.randomUUID().toString(),
    val blockerUid: String,
    val blockedUid: String,
    val createdAt: Long = System.currentTimeMillis(),
    val reasonOptional: String? = null
)

/**
 * Communication Reports: communication_reports/{reportId}
 */
data class CommunicationReport(
    val reportId: String = UUID.randomUUID().toString(),
    val reporterUid: String,
    val reportedUid: String,
    val conversationId: String? = null,
    val messageId: String? = null,
    val callId: String? = null,
    val reason: String,
    val description: String,
    val status: ReportStatus = ReportStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null
)

/**
 * Message Reactions: message_reactions/{reactionId}
 */
data class MessageReaction(
    val reactionId: String = UUID.randomUUID().toString(),
    val messageId: String,
    val conversationId: String,
    val uid: String,
    val reaction: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * User Presence: user_presence/{uid}
 */
data class UserPresence(
    val uid: String,
    val state: PresenceState = PresenceState.OFFLINE,
    val lastSeenAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * User Devices: user_devices/{deviceId}
 * Max 4 concurrent authorized sessions per user.
 */
data class UserDevice(
    val deviceId: String,
    val uid: String,
    val platform: String = "Android",
    val deviceName: String = "Pixel Device",
    val appVersion: String = "1.0.0",
    val fcmToken: String = "",
    val lastActiveAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val revokedAt: Long? = null,
    val isActive: Boolean = true,
    // Organization device granular permission controls (Section 52)
    val isMessagingEnabled: Boolean = true,
    val isAudioCallsEnabled: Boolean = true,
    val isVideoCallsEnabled: Boolean = true
)

/**
 * Notification Settings: notification_settings/{uid}
 */
data class NotificationSettings(
    val uid: String,
    val messagesEnabled: Boolean = true,
    val messageRequestsEnabled: Boolean = true,
    val callNotificationsEnabled: Boolean = true,
    val missedCallNotificationsEnabled: Boolean = true,
    val reactionNotificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val previewEnabled: NotificationPreviewType = NotificationPreviewType.FULL,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Call Sessions: call_sessions/{callId}
 */
data class CallSession(
    val callId: String = UUID.randomUUID().toString(),
    val callerUid: String,
    val receiverUid: String,
    val callType: CallType = CallType.AUDIO,
    val status: CallStatus = CallStatus.INITIATED,
    val createdAt: Long = System.currentTimeMillis(),
    val ringingAt: Long? = null,
    val acceptedAt: Long? = null,
    val connectedAt: Long? = null,
    val endedAt: Long? = null,
    val durationSeconds: Int = 0,
    val endedBy: String? = null,
    val failureReason: String? = null,
    val rtcProvider: String = "WebRTC_Standard",
    val country: String = "US",
    val metadataVersion: String = "v1",

    // Live Call Translation Foundation (Section 49)
    val liveTranslationEnabled: Boolean = false,
    val captionTranslationEnabled: Boolean = false,
    val audioTranslationEnabled: Boolean = false
)

/**
 * Call History: call_history/{callId}
 */
data class CallHistoryEntry(
    val callId: String,
    val uid: String,
    val otherPartyUid: String,
    val otherPartyName: String = "User",
    val callType: CallType,
    val direction: CallDirection,
    val status: CallStatus,
    val durationSeconds: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
)

/**
 * Communication Retention Config
 */
data class CommunicationRetentionConfig(
    val messageRetentionEnabled: Boolean = false,
    val defaultRetentionDays: Int = 365,
    val mediaRetentionDays: Int = 90,
    val voiceRetentionDays: Int = 90,
    val deletedMessageRetentionDays: Int = 30,
    val countryRules: Map<String, Int> = emptyMap(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Communication Audit Log: communication_audit_logs/{logId}
 */
data class CommunicationAuditLog(
    val logId: String = UUID.randomUUID().toString(),
    val actorUid: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val conversationId: String? = null,
    val callId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val result: String = "SUCCESS",
    val reason: String? = null,
    val country: String = "US",
    val deviceId: String? = null
)
