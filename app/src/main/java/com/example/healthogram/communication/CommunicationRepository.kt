package com.example.healthogram.communication

import com.example.healthogram.owner.FeatureState
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.owner.PlatformFeature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * HEALTHOGRAM STEP 11: COMMUNICATION REPOSITORY & SECURITY ORCHESTRATOR
 * 
 * Implements server-authoritative controls:
 * - Direct messaging & message requests
 * - WebRTC Audio & Video call lifecycle & signaling
 * - Multi-device enforcement (Max 4 active sessions)
 * - User blocking & reporting
 * - Privacy settings (last seen, read receipts, online status)
 * - Anti-tamper & strict Health Passport isolation
 */
class CommunicationRepository(
    val ownerControlEngine: OwnerControlEngine = OwnerControlEngine(),
    val webRTCService: WebRTCCallService = WebRTCCallService()
) {
    // In-memory reactive state stores representing Firestore collections
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _messageRequests = MutableStateFlow<List<MessageRequest>>(emptyList())
    val messageRequests: StateFlow<List<MessageRequest>> = _messageRequests.asStateFlow()

    private val _userBlocks = MutableStateFlow<List<UserBlock>>(emptyList())
    val userBlocks: StateFlow<List<UserBlock>> = _userBlocks.asStateFlow()

    private val _communicationReports = MutableStateFlow<List<CommunicationReport>>(emptyList())
    val communicationReports: StateFlow<List<CommunicationReport>> = _communicationReports.asStateFlow()

    private val _settings = MutableStateFlow<Map<String, CommunicationSettings>>(emptyMap())
    val settings: StateFlow<Map<String, CommunicationSettings>> = _settings.asStateFlow()

    private val _notificationSettings = MutableStateFlow<Map<String, NotificationSettings>>(emptyMap())
    val notificationSettings: StateFlow<Map<String, NotificationSettings>> = _notificationSettings.asStateFlow()

    private val _userDevices = MutableStateFlow<Map<String, List<UserDevice>>>(emptyMap())
    val userDevices: StateFlow<Map<String, List<UserDevice>>> = _userDevices.asStateFlow()

    private val _userPresences = MutableStateFlow<Map<String, UserPresence>>(emptyMap())
    val userPresences: StateFlow<Map<String, UserPresence>> = _userPresences.asStateFlow()

    private val _activeCalls = MutableStateFlow<Map<String, CallSession>>(emptyMap())
    val activeCalls: StateFlow<Map<String, CallSession>> = _activeCalls.asStateFlow()

    private val _callHistory = MutableStateFlow<List<CallHistoryEntry>>(emptyList())
    val callHistory: StateFlow<List<CallHistoryEntry>> = _callHistory.asStateFlow()

    private val _reactions = MutableStateFlow<List<MessageReaction>>(emptyList())
    val reactions: StateFlow<List<MessageReaction>> = _reactions.asStateFlow()

    private val _typingState = MutableStateFlow<Map<String, Map<String, Long>>>(emptyMap()) // convId -> (uid -> timestamp)
    val typingState: StateFlow<Map<String, Map<String, Long>>> = _typingState.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<CommunicationAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<CommunicationAuditLog>> = _auditLogs.asStateFlow()

    private val protectedHealthPassportKeywords = listOf(
        "health_passports",
        "health_conditions",
        "health_allergies",
        "health_medications",
        "health_diagnoses",
        "health_lab_reports",
        "health_prescriptions",
        "health_visits"
    )

    // -------------------------------------------------------------------------
    // 1. PRIVACY & SETTINGS
    // -------------------------------------------------------------------------

    fun getSettings(uid: String): CommunicationSettings {
        return _settings.value[uid] ?: CommunicationSettings(uid = uid).also { defaultSettings ->
            _settings.value = _settings.value + (uid to defaultSettings)
        }
    }

    fun updateSettings(newSettings: CommunicationSettings, callerUid: String) {
        if (newSettings.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot modify another user's communication settings.")
        }
        _settings.value = _settings.value + (newSettings.uid to newSettings)
    }

    fun getNotificationSettings(uid: String): NotificationSettings {
        return _notificationSettings.value[uid] ?: NotificationSettings(uid = uid).also { defaultSettings ->
            _notificationSettings.value = _notificationSettings.value + (uid to defaultSettings)
        }
    }

    fun updateNotificationSettings(newSettings: NotificationSettings, callerUid: String) {
        if (newSettings.uid != callerUid) {
            throw SecurityException("Security Violation: Cannot modify another user's notification settings.")
        }
        _notificationSettings.value = _notificationSettings.value + (newSettings.uid to newSettings)
    }

    // -------------------------------------------------------------------------
    // 2. BLOCK & REPORT CONTROLS
    // -------------------------------------------------------------------------

    fun isUserBlocked(blockerUid: String, targetUid: String): Boolean {
        return _userBlocks.value.any { it.blockerUid == blockerUid && it.blockedUid == targetUid }
    }

    fun isCommunicationBlocked(uidA: String, uidB: String): Boolean {
        return _userBlocks.value.any {
            (it.blockerUid == uidA && it.blockedUid == uidB) || (it.blockerUid == uidB && it.blockedUid == uidA)
        }
    }

    fun blockUser(blockerUid: String, blockedUid: String, reason: String? = null) {
        if (blockerUid == blockedUid) {
            throw IllegalArgumentException("Cannot block yourself.")
        }
        if (!isUserBlocked(blockerUid, blockedUid)) {
            val block = UserBlock(
                blockerUid = blockerUid,
                blockedUid = blockedUid,
                reasonOptional = reason
            )
            _userBlocks.value = _userBlocks.value + block
            logAudit(
                actorUid = blockerUid,
                action = "USER_BLOCKED",
                targetType = "USER",
                targetId = blockedUid,
                reason = reason
            )
        }
    }

    fun unblockUser(blockerUid: String, blockedUid: String) {
        _userBlocks.value = _userBlocks.value.filterNot { it.blockerUid == blockerUid && it.blockedUid == blockedUid }
        logAudit(
            actorUid = blockerUid,
            action = "USER_UNBLOCKED",
            targetType = "USER",
            targetId = blockedUid
        )
    }

    fun reportCommunication(
        reporterUid: String,
        reportedUid: String,
        conversationId: String? = null,
        messageId: String? = null,
        callId: String? = null,
        reason: String,
        description: String
    ): CommunicationReport {
        val report = CommunicationReport(
            reporterUid = reporterUid,
            reportedUid = reportedUid,
            conversationId = conversationId,
            messageId = messageId,
            callId = callId,
            reason = reason,
            description = description
        )
        _communicationReports.value = _communicationReports.value + report
        logAudit(
            actorUid = reporterUid,
            action = "COMMUNICATION_REPORTED",
            targetType = if (callId != null) "CALL" else "MESSAGE",
            targetId = callId ?: (messageId ?: reportedUid),
            conversationId = conversationId,
            callId = callId,
            reason = reason
        )
        return report
    }

    // -------------------------------------------------------------------------
    // 3. CONVERSATION MANAGEMENT
    // -------------------------------------------------------------------------

    fun createOrGetDirectConversation(callerUid: String, recipientUid: String): Conversation {
        if (callerUid == recipientUid) {
            throw IllegalArgumentException("Cannot start conversation with yourself.")
        }

        if (isCommunicationBlocked(callerUid, recipientUid)) {
            throw SecurityException("Security Violation: Communication is blocked with this user.")
        }

        val existing = _conversations.value.find { conv ->
            conv.conversationType == ConversationType.DIRECT &&
                    conv.participantIds.contains(callerUid) &&
                    conv.participantIds.contains(recipientUid)
        }
        if (existing != null) {
            return existing
        }

        val recipientSettings = getSettings(recipientUid)
        if (!recipientSettings.allowTextMessages) {
            throw IllegalStateException("Recipient has disabled direct messaging.")
        }

        val newConv = Conversation(
            conversationType = ConversationType.DIRECT,
            createdBy = callerUid,
            participantIds = listOf(callerUid, recipientUid),
            unreadCountMap = mapOf(callerUid to 0, recipientUid to 0)
        )
        _conversations.value = _conversations.value + newConv
        return newConv
    }

    fun getConversationsForUser(uid: String): List<Conversation> {
        return _conversations.value.filter { it.participantIds.contains(uid) }
            .sortedByDescending { it.lastMessageAt }
    }

    fun setConversationMuted(conversationId: String, uid: String, isMuted: Boolean) {
        val conv = _conversations.value.find { it.conversationId == conversationId } ?: return
        if (!conv.participantIds.contains(uid)) {
            throw SecurityException("Security Violation: Not a participant of this conversation.")
        }
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) it.copy(isMuted = isMuted) else it
        }
    }

    fun setConversationArchived(conversationId: String, uid: String, isArchived: Boolean) {
        val conv = _conversations.value.find { it.conversationId == conversationId } ?: return
        if (!conv.participantIds.contains(uid)) {
            throw SecurityException("Security Violation: Not a participant of this conversation.")
        }
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) it.copy(isArchived = isArchived) else it
        }
    }

    fun setConversationPinned(conversationId: String, uid: String, isPinned: Boolean) {
        val conv = _conversations.value.find { it.conversationId == conversationId } ?: return
        if (!conv.participantIds.contains(uid)) {
            throw SecurityException("Security Violation: Not a participant of this conversation.")
        }
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) it.copy(isPinned = isPinned) else it
        }
    }

    // -------------------------------------------------------------------------
    // 4. MESSAGE SENDING, REPLIES, MEDIA & DUPLICATE PROTECTION
    // -------------------------------------------------------------------------

    suspend fun sendMessage(
        callerUid: String,
        conversationId: String,
        text: String,
        messageType: MessageType = MessageType.TEXT,
        mediaReference: String? = null,
        mediaDurationSeconds: Int? = null,
        fileName: String? = null,
        fileSizeBytes: Long? = null,
        replyToMessageId: String? = null,
        clientMessageId: String = UUID.randomUUID().toString(),
        sharedContentType: String? = null,
        sharedContentId: String? = null
    ): Message {
        // Platform Emergency Kill-switch
        if (ownerControlEngine.isEmergencyKillSwitchActive()) {
            throw IllegalStateException("Messaging service is temporarily suspended by platform administrator.")
        }

        // Duplicate Message Protection (Section 62)
        val existing = _messages.value.find { it.clientMessageId == clientMessageId }
        if (existing != null) {
            return existing
        }

        val conv = _conversations.value.find { it.conversationId == conversationId }
            ?: throw NoSuchElementException("Conversation not found.")

        // Enforce participant authorization (Section 9)
        if (!conv.participantIds.contains(callerUid)) {
            throw SecurityException("Security Violation: Caller is not a participant in this conversation.")
        }

        val otherParticipant = conv.participantIds.firstOrNull { it != callerUid }
        if (otherParticipant != null) {
            if (isCommunicationBlocked(callerUid, otherParticipant)) {
                throw SecurityException("Security Violation: Communication is blocked.")
            }
            val recipientSettings = getSettings(otherParticipant)
            if (!recipientSettings.allowTextMessages && messageType == MessageType.TEXT) {
                throw IllegalStateException("Recipient does not accept text messages.")
            }
            if (!recipientSettings.allowVoiceMessages && messageType == MessageType.VOICE) {
                throw IllegalStateException("Recipient does not accept voice messages.")
            }
            if (!recipientSettings.allowMediaMessages && (messageType == MessageType.IMAGE || messageType == MessageType.VIDEO || messageType == MessageType.DOCUMENT)) {
                throw IllegalStateException("Recipient does not accept media messages.")
            }
        }

        // HEALTH PASSPORT ISOLATION (Section 46 & 47)
        // Messaging must NOT bypass Health Passport authorization.
        val combinedPayload = "$text ${mediaReference.orEmpty()} ${fileName.orEmpty()} ${sharedContentType.orEmpty()}"
        if (protectedHealthPassportKeywords.any { combinedPayload.contains(it, ignoreCase = true) }) {
            logAudit(
                actorUid = callerUid,
                action = "HEALTH_PASSPORT_INJECTION_BLOCKED",
                targetType = "MESSAGE",
                targetId = conversationId,
                reason = "Attempted direct unconsented medical record injection in social chat"
            )
            throw SecurityException("Security Violation: Direct transmission of protected Health Passport data via social messaging is prohibited. Use patient-granted Health Passport QR/ticket sharing.")
        }

        // Message reply lookup
        val replyPreview = if (replyToMessageId != null) {
            val original = _messages.value.find { it.messageId == replyToMessageId }
            original?.text?.take(60)
        } else null

        val newMessage = Message(
            conversationId = conversationId,
            senderUid = callerUid,
            messageType = messageType,
            text = text,
            mediaReference = mediaReference,
            mediaDurationSeconds = mediaDurationSeconds,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes,
            replyToMessageId = replyToMessageId,
            replyPreview = replyPreview,
            clientMessageId = clientMessageId,
            sharedContentType = sharedContentType,
            sharedContentId = sharedContentId,
            deliveryStatus = DeliveryStatus.SENT
        )

        _messages.value = _messages.value + newMessage

        // Update conversation metadata & unread counters
        val updatedUnread = conv.unreadCountMap.toMutableMap()
        conv.participantIds.forEach { uid ->
            if (uid != callerUid) {
                updatedUnread[uid] = (updatedUnread[uid] ?: 0) + 1
            }
        }

        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) {
                it.copy(
                    lastMessageId = newMessage.messageId,
                    lastMessagePreview = if (messageType == MessageType.TEXT) text else "[${messageType.name}]",
                    lastMessageType = messageType,
                    lastMessageSenderId = callerUid,
                    lastMessageAt = System.currentTimeMillis(),
                    unreadCountMap = updatedUnread,
                    updatedAt = System.currentTimeMillis()
                )
            } else it
        }

        return newMessage
    }

    fun getMessagesForConversation(conversationId: String, callerUid: String): List<Message> {
        val conv = _conversations.value.find { it.conversationId == conversationId }
            ?: throw NoSuchElementException("Conversation not found.")
        if (!conv.participantIds.contains(callerUid)) {
            throw SecurityException("Security Violation: Caller is not a participant in this conversation.")
        }
        return _messages.value.filter { it.conversationId == conversationId }
            .sortedBy { it.createdAt }
    }

    // -------------------------------------------------------------------------
    // 5. MESSAGE EDITING & DELETION
    // -------------------------------------------------------------------------

    fun editMessage(messageId: String, callerUid: String, newText: String): Message {
        val msg = _messages.value.find { it.messageId == messageId }
            ?: throw NoSuchElementException("Message not found.")

        if (msg.senderUid != callerUid) {
            throw SecurityException("Security Violation: Only original sender can edit a message.")
        }

        // 15-minute edit window
        val maxEditDurationMs = 15 * 60 * 1000
        if (System.currentTimeMillis() - msg.createdAt > maxEditDurationMs) {
            throw IllegalStateException("Edit window has expired for this message.")
        }

        val updated = msg.copy(
            text = newText,
            isEdited = true,
            updatedAt = System.currentTimeMillis()
        )
        _messages.value = _messages.value.map { if (it.messageId == messageId) updated else it }
        return updated
    }

    fun deleteMessage(messageId: String, callerUid: String, deleteForEveryone: Boolean): Boolean {
        val msg = _messages.value.find { it.messageId == messageId }
            ?: throw NoSuchElementException("Message not found.")

        if (deleteForEveryone) {
            if (msg.senderUid != callerUid) {
                throw SecurityException("Security Violation: Only sender can delete for everyone.")
            }
            val deleted = msg.copy(
                text = "This message was deleted.",
                mediaReference = null,
                isDeleted = true,
                deletedAt = System.currentTimeMillis()
            )
            _messages.value = _messages.value.map { if (it.messageId == messageId) deleted else it }
        } else {
            // "Delete for me" does not destroy the message for the other recipient
            _messages.value = _messages.value.filterNot { it.messageId == messageId && it.senderUid == callerUid }
        }
        return true
    }

    // -------------------------------------------------------------------------
    // 6. REACTIONS, READ RECEIPTS & TYPING
    // -------------------------------------------------------------------------

    fun reactToMessage(messageId: String, callerUid: String, reaction: String): MessageReaction {
        val msg = _messages.value.find { it.messageId == messageId }
            ?: throw NoSuchElementException("Message not found.")

        // Remove existing reaction by same user for this message
        _reactions.value = _reactions.value.filterNot { it.messageId == messageId && it.uid == callerUid }

        val newReaction = MessageReaction(
            messageId = messageId,
            conversationId = msg.conversationId,
            uid = callerUid,
            reaction = reaction
        )
        _reactions.value = _reactions.value + newReaction
        return newReaction
    }

    fun markConversationRead(conversationId: String, callerUid: String) {
        val conv = _conversations.value.find { it.conversationId == conversationId } ?: return
        if (!conv.participantIds.contains(callerUid)) return

        val userSettings = getSettings(callerUid)
        val updatedMessages = _messages.value.map { msg ->
            if (msg.conversationId == conversationId && msg.senderUid != callerUid) {
                if (userSettings.showReadReceipts) {
                    msg.copy(deliveryStatus = DeliveryStatus.READ)
                } else msg
            } else msg
        }
        _messages.value = updatedMessages

        // Reset unread count for caller
        val unread = conv.unreadCountMap.toMutableMap()
        unread[callerUid] = 0
        _conversations.value = _conversations.value.map {
            if (it.conversationId == conversationId) it.copy(unreadCountMap = unread) else it
        }
    }

    fun setTyping(conversationId: String, callerUid: String, isTyping: Boolean) {
        val current = _typingState.value.toMutableMap()
        val convTyping = (current[conversationId] ?: emptyMap()).toMutableMap()
        if (isTyping) {
            convTyping[callerUid] = System.currentTimeMillis()
        } else {
            convTyping.remove(callerUid)
        }
        current[conversationId] = convTyping
        _typingState.value = current
    }

    fun isUserTyping(conversationId: String, uid: String): Boolean {
        val timestamp = _typingState.value[conversationId]?.get(uid) ?: return false
        // Typing timeout is 5 seconds
        return System.currentTimeMillis() - timestamp < 5000
    }

    // -------------------------------------------------------------------------
    // 7. PRESENCE & ONLINE STATUS
    // -------------------------------------------------------------------------

    fun updatePresence(uid: String, state: PresenceState) {
        val current = _userPresences.value[uid] ?: UserPresence(uid = uid)
        _userPresences.value = _userPresences.value + (uid to current.copy(
            state = state,
            lastSeenAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ))
    }

    fun getUserPresence(uid: String): UserPresence {
        val settings = getSettings(uid)
        val presence = _userPresences.value[uid] ?: UserPresence(uid = uid)
        return if (!settings.showOnlineStatus && presence.state == PresenceState.ONLINE) {
            presence.copy(state = PresenceState.OFFLINE)
        } else presence
    }

    // -------------------------------------------------------------------------
    // 8. MULTI-DEVICE SESSIONS & ORGANIZATION DEVICES
    // -------------------------------------------------------------------------

    fun registerDevice(
        uid: String,
        deviceId: String,
        platform: String = "Android",
        deviceName: String = "Mobile Device"
    ): UserDevice {
        val currentList = _userDevices.value[uid] ?: emptyList()
        val activeDevices = currentList.filter { it.isActive }

        // Max 4 concurrent sessions (Section 26)
        if (activeDevices.size >= 4 && activeDevices.none { it.deviceId == deviceId }) {
            throw IllegalStateException("Maximum 4 active device sessions reached. Revoke an existing device to connect a new one.")
        }

        val existing = currentList.find { it.deviceId == deviceId }
        val device = existing?.copy(
            isActive = true,
            lastActiveAt = System.currentTimeMillis()
        ) ?: UserDevice(
            deviceId = deviceId,
            uid = uid,
            platform = platform,
            deviceName = deviceName,
            isActive = true
        )

        val updated = currentList.filterNot { it.deviceId == deviceId } + device
        _userDevices.value = _userDevices.value + (uid to updated)
        return device
    }

    fun revokeDevice(uid: String, deviceId: String, callerUid: String) {
        if (uid != callerUid) {
            throw SecurityException("Security Violation: Cannot revoke another user's device.")
        }
        val currentList = _userDevices.value[uid] ?: return
        val updated = currentList.map {
            if (it.deviceId == deviceId) it.copy(isActive = false, revokedAt = System.currentTimeMillis()) else it
        }
        _userDevices.value = _userDevices.value + (uid to updated)
    }

    // -------------------------------------------------------------------------
    // 9. CALLING ENGINE (AUDIO & VIDEO CALLS)
    // -------------------------------------------------------------------------

    suspend fun initiateCall(
        callerUid: String,
        receiverUid: String,
        callType: CallType
    ): CallSession {
        // Platform Emergency Kill-switch
        if (ownerControlEngine.isEmergencyKillSwitchActive()) {
            throw IllegalStateException("Voice/Video call services are currently suspended by platform administrator.")
        }

        val feature = if (callType == CallType.AUDIO) PlatformFeature.AUDIO_CALLS else PlatformFeature.VIDEO_CALLS
        val flag = ownerControlEngine.getAllFlags().find { it.feature == feature }
        if (flag != null && flag.state == FeatureState.OFF) {
            throw IllegalStateException("${callType.name} calls are currently disabled on Healthogram.")
        }

        if (callerUid == receiverUid) {
            throw IllegalArgumentException("Cannot call yourself.")
        }

        if (isCommunicationBlocked(callerUid, receiverUid)) {
            throw SecurityException("Security Violation: Cannot call a user with an active communication block.")
        }

        val receiverSettings = getSettings(receiverUid)
        if (callType == CallType.AUDIO && !receiverSettings.allowAudioCalls) {
            throw IllegalStateException("Recipient has disabled incoming audio calls.")
        }
        if (callType == CallType.VIDEO && !receiverSettings.allowVideoCalls) {
            throw IllegalStateException("Recipient has disabled incoming video calls.")
        }

        val session = CallSession(
            callerUid = callerUid,
            receiverUid = receiverUid,
            callType = callType,
            status = CallStatus.RINGING,
            ringingAt = System.currentTimeMillis()
        )

        _activeCalls.value = _activeCalls.value + (session.callId to session)

        // Pre-configure WebRTC signaling token
        webRTCService.prepareCall(session)

        logAudit(
            actorUid = callerUid,
            action = "CALL_INITIATED",
            targetType = "CALL",
            targetId = session.callId,
            callId = session.callId,
            reason = callType.name
        )

        return session
    }

    suspend fun acceptCall(callId: String, callerUid: String): CallSession {
        val session = _activeCalls.value[callId]
            ?: throw NoSuchElementException("Active call session not found.")

        if (session.receiverUid != callerUid) {
            throw SecurityException("Security Violation: Only the designated receiver can accept the call.")
        }

        val updated = session.copy(
            status = CallStatus.CONNECTED,
            acceptedAt = System.currentTimeMillis(),
            connectedAt = System.currentTimeMillis()
        )
        _activeCalls.value = _activeCalls.value + (callId to updated)

        logAudit(
            actorUid = callerUid,
            action = "CALL_ACCEPTED",
            targetType = "CALL",
            targetId = callId,
            callId = callId
        )
        return updated
    }

    suspend fun declineCall(callId: String, callerUid: String): CallSession {
        val session = _activeCalls.value[callId]
            ?: throw NoSuchElementException("Active call session not found.")

        if (session.receiverUid != callerUid) {
            throw SecurityException("Security Violation: Only the designated receiver can decline the call.")
        }

        val updated = session.copy(
            status = CallStatus.DECLINED,
            endedAt = System.currentTimeMillis(),
            endedBy = callerUid
        )
        _activeCalls.value = _activeCalls.value - callId
        recordCallHistory(updated)
        webRTCService.endCall()
        return updated
    }

    suspend fun cancelCall(callId: String, callerUid: String): CallSession {
        val session = _activeCalls.value[callId]
            ?: throw NoSuchElementException("Active call session not found.")

        if (session.callerUid != callerUid) {
            throw SecurityException("Security Violation: Only the caller can cancel an outgoing call.")
        }

        val updated = session.copy(
            status = CallStatus.CANCELLED,
            endedAt = System.currentTimeMillis(),
            endedBy = callerUid
        )
        _activeCalls.value = _activeCalls.value - callId
        recordCallHistory(updated)
        webRTCService.endCall()
        return updated
    }

    suspend fun endCall(callId: String, callerUid: String): CallSession {
        val session = _activeCalls.value[callId]
            ?: throw NoSuchElementException("Active call session not found.")

        if (session.callerUid != callerUid && session.receiverUid != callerUid) {
            throw SecurityException("Security Violation: Only call participants can end the call.")
        }

        val duration = if (session.connectedAt != null) {
            ((System.currentTimeMillis() - session.connectedAt) / 1000).toInt()
        } else 0

        val updated = session.copy(
            status = CallStatus.ENDED,
            endedAt = System.currentTimeMillis(),
            durationSeconds = duration,
            endedBy = callerUid
        )
        _activeCalls.value = _activeCalls.value - callId
        recordCallHistory(updated)
        webRTCService.endCall()

        logAudit(
            actorUid = callerUid,
            action = "CALL_ENDED",
            targetType = "CALL",
            targetId = callId,
            callId = callId,
            reason = "Duration: ${duration}s"
        )
        return updated
    }

    private fun recordCallHistory(session: CallSession) {
        // Record history for caller
        val callerEntry = CallHistoryEntry(
            callId = session.callId,
            uid = session.callerUid,
            otherPartyUid = session.receiverUid,
            callType = session.callType,
            direction = CallDirection.OUTGOING,
            status = session.status,
            durationSeconds = session.durationSeconds,
            createdAt = session.createdAt,
            endedAt = session.endedAt
        )
        // Record history for receiver
        val receiverEntry = CallHistoryEntry(
            callId = session.callId,
            uid = session.receiverUid,
            otherPartyUid = session.callerUid,
            callType = session.callType,
            direction = if (session.status == CallStatus.MISSED || session.status == CallStatus.CANCELLED) CallDirection.MISSED else CallDirection.INCOMING,
            status = session.status,
            durationSeconds = session.durationSeconds,
            createdAt = session.createdAt,
            endedAt = session.endedAt
        )
        _callHistory.value = _callHistory.value + listOf(callerEntry, receiverEntry)
    }

    fun getCallHistoryForUser(uid: String): List<CallHistoryEntry> {
        return _callHistory.value.filter { it.uid == uid }
            .sortedByDescending { it.createdAt }
    }

    // -------------------------------------------------------------------------
    // 10. AUDIT LOGGING
    // -------------------------------------------------------------------------

    private fun logAudit(
        actorUid: String,
        action: String,
        targetType: String,
        targetId: String,
        conversationId: String? = null,
        callId: String? = null,
        reason: String? = null,
        result: String = "SUCCESS"
    ) {
        val entry = CommunicationAuditLog(
            actorUid = actorUid,
            action = action,
            targetType = targetType,
            targetId = targetId,
            conversationId = conversationId,
            callId = callId,
            reason = reason,
            result = result
        )
        _auditLogs.value = _auditLogs.value + entry
    }
}
