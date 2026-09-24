package com.example.healthogram.communication

import java.text.SimpleDateFormat
import java.util.*

/**
 * HEALTHOGRAM STEP 11: FLUTTERFLOW CUSTOM ACTIONS & CUSTOM FUNCTIONS
 *
 * Exposes declarative, typed bridges for all messaging and real-time calling operations.
 */
object CommunicationCustomActions {

    var repository: CommunicationRepository = CommunicationRepository()

    // -------------------------------------------------------------------------
    // CUSTOM ACTIONS (Section 42)
    // -------------------------------------------------------------------------

    suspend fun SendMessage(
        callerUid: String,
        conversationId: String,
        text: String
    ): Message {
        return repository.sendMessage(
            callerUid = callerUid,
            conversationId = conversationId,
            text = text,
            messageType = MessageType.TEXT
        )
    }

    suspend fun SendMediaMessage(
        callerUid: String,
        conversationId: String,
        mediaUrl: String,
        messageType: MessageType,
        fileName: String? = null,
        fileSizeBytes: Long? = null
    ): Message {
        return repository.sendMessage(
            callerUid = callerUid,
            conversationId = conversationId,
            text = fileName ?: "[Media]",
            messageType = messageType,
            mediaReference = mediaUrl,
            fileName = fileName,
            fileSizeBytes = fileSizeBytes
        )
    }

    suspend fun SendVoiceMessage(
        callerUid: String,
        conversationId: String,
        audioUrl: String,
        durationSeconds: Int
    ): Message {
        return repository.sendMessage(
            callerUid = callerUid,
            conversationId = conversationId,
            text = "Voice message (${durationSeconds}s)",
            messageType = MessageType.VOICE,
            mediaReference = audioUrl,
            mediaDurationSeconds = durationSeconds
        )
    }

    suspend fun ReplyToMessage(
        callerUid: String,
        conversationId: String,
        replyToMessageId: String,
        text: String
    ): Message {
        return repository.sendMessage(
            callerUid = callerUid,
            conversationId = conversationId,
            text = text,
            replyToMessageId = replyToMessageId
        )
    }

    fun EditMessage(
        callerUid: String,
        messageId: String,
        newText: String
    ): Message {
        return repository.editMessage(messageId, callerUid, newText)
    }

    fun DeleteMessage(
        callerUid: String,
        messageId: String,
        deleteForEveryone: Boolean
    ): Boolean {
        return repository.deleteMessage(messageId, callerUid, deleteForEveryone)
    }

    fun ReactToMessage(
        callerUid: String,
        messageId: String,
        reaction: String
    ): MessageReaction {
        return repository.reactToMessage(messageId, callerUid, reaction)
    }

    fun MarkMessageRead(
        callerUid: String,
        conversationId: String
    ) {
        repository.markConversationRead(conversationId, callerUid)
    }

    fun SearchMessages(
        callerUid: String,
        query: String,
        conversationId: String? = null
    ): List<Message> {
        val allMessages = if (conversationId != null) {
            repository.getMessagesForConversation(conversationId, callerUid)
        } else {
            val userConvs = repository.getConversationsForUser(callerUid).map { it.conversationId }
            repository.messages.value.filter { userConvs.contains(it.conversationId) }
        }
        return allMessages.filter { it.text.contains(query, ignoreCase = true) }
    }

    suspend fun StartAudioCall(
        callerUid: String,
        receiverUid: String
    ): CallSession {
        return repository.initiateCall(callerUid, receiverUid, CallType.AUDIO)
    }

    suspend fun StartVideoCall(
        callerUid: String,
        receiverUid: String
    ): CallSession {
        return repository.initiateCall(callerUid, receiverUid, CallType.VIDEO)
    }

    suspend fun AcceptCall(
        callerUid: String,
        callId: String
    ): CallSession {
        return repository.acceptCall(callId, callerUid)
    }

    suspend fun DeclineCall(
        callerUid: String,
        callId: String
    ): CallSession {
        return repository.declineCall(callId, callerUid)
    }

    suspend fun EndCall(
        callerUid: String,
        callId: String
    ): CallSession {
        return repository.endCall(callId, callerUid)
    }

    fun ToggleMicrophone(): Boolean {
        return repository.webRTCService.toggleMicrophone()
    }

    fun ToggleCamera(): Boolean {
        return repository.webRTCService.toggleCamera()
    }

    fun SwitchCamera(): Boolean {
        return repository.webRTCService.switchCamera()
    }

    fun ToggleSpeaker(): Boolean {
        return repository.webRTCService.toggleSpeaker()
    }

    fun GetCallHistory(callerUid: String): List<CallHistoryEntry> {
        return repository.getCallHistoryForUser(callerUid)
    }

    fun BlockCommunicationUser(
        blockerUid: String,
        blockedUid: String,
        reason: String? = null
    ) {
        repository.blockUser(blockerUid, blockedUid, reason)
    }

    fun ReportCommunicationUser(
        reporterUid: String,
        reportedUid: String,
        conversationId: String? = null,
        messageId: String? = null,
        callId: String? = null,
        reason: String,
        description: String
    ): CommunicationReport {
        return repository.reportCommunication(
            reporterUid = reporterUid,
            reportedUid = reportedUid,
            conversationId = conversationId,
            messageId = messageId,
            callId = callId,
            reason = reason,
            description = description
        )
    }
}

/**
 * CUSTOM FUNCTIONS (Section 43)
 */
object CommunicationCustomFunctions {

    fun getMessagePreview(message: Message): String {
        return when (message.messageType) {
            MessageType.TEXT -> if (message.isDeleted) "This message was deleted." else message.text
            MessageType.IMAGE -> "📷 Photo"
            MessageType.VIDEO -> "📹 Video"
            MessageType.VOICE -> "🎤 Voice message"
            MessageType.AUDIO -> "🎵 Audio file"
            MessageType.DOCUMENT -> "📄 ${message.fileName ?: "Document"}"
            MessageType.CALL -> "📞 Call"
            MessageType.SHARED_POST -> "📌 Shared Post"
            MessageType.SHARED_REEL -> "🎬 Shared Reel"
            MessageType.SHARED_PRODUCT -> "🛍️ Shared Product"
            MessageType.SYSTEM -> "ℹ️ ${message.text}"
        }
    }

    fun getMessageTypeLabel(type: MessageType): String {
        return when (type) {
            MessageType.TEXT -> "Text"
            MessageType.IMAGE -> "Image"
            MessageType.VIDEO -> "Video"
            MessageType.AUDIO -> "Audio"
            MessageType.VOICE -> "Voice Note"
            MessageType.DOCUMENT -> "Document"
            MessageType.SYSTEM -> "System"
            MessageType.CALL -> "Call"
            MessageType.SHARED_POST -> "Post"
            MessageType.SHARED_REEL -> "Reel"
            MessageType.SHARED_PRODUCT -> "Marketplace Product"
        }
    }

    fun getMessageStatusIcon(status: DeliveryStatus): String {
        return when (status) {
            DeliveryStatus.SENDING -> "🕒"
            DeliveryStatus.SENT -> "✓"
            DeliveryStatus.DELIVERED -> "✓✓"
            DeliveryStatus.READ -> "✓✓" // Blue double-tick
            DeliveryStatus.FAILED -> "⚠️"
        }
    }

    fun getCallStatusLabel(status: CallStatus): String {
        return when (status) {
            CallStatus.INITIATED -> "Connecting..."
            CallStatus.RINGING -> "Ringing..."
            CallStatus.ACCEPTED -> "Accepted"
            CallStatus.CONNECTING -> "Establishing connection..."
            CallStatus.CONNECTED -> "In Call"
            CallStatus.DECLINED -> "Call Declined"
            CallStatus.MISSED -> "Missed Call"
            CallStatus.CANCELLED -> "Cancelled"
            CallStatus.ENDED -> "Call Ended"
            CallStatus.FAILED -> "Call Failed"
            CallStatus.BUSY -> "Line Busy"
        }
    }

    fun getCallDurationLabel(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    fun getPresenceLabel(presence: UserPresence): String {
        return when (presence.state) {
            PresenceState.ONLINE -> "Online"
            PresenceState.AWAY -> "Away"
            PresenceState.BUSY -> "Busy"
            PresenceState.OFFLINE -> getLastSeenLabel(presence.lastSeenAt)
        }
    }

    fun getLastSeenLabel(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            minutes < 2 -> "Active recently"
            minutes < 60 -> "Active ${minutes}m ago"
            hours < 24 -> "Last seen today at " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
            days < 7 -> "Last seen ${days}d ago"
            else -> "Last seen recently"
        }
    }

    fun getConversationUnreadCount(conversation: Conversation, uid: String): Int {
        return conversation.unreadCountMap[uid] ?: 0
    }

    fun isMessagingAllowed(recipientSettings: CommunicationSettings): Boolean {
        return recipientSettings.allowTextMessages
    }

    fun isAudioCallAllowed(recipientSettings: CommunicationSettings): Boolean {
        return recipientSettings.allowAudioCalls
    }

    fun isVideoCallAllowed(recipientSettings: CommunicationSettings): Boolean {
        return recipientSettings.allowVideoCalls
    }

    fun isBlocked(repository: CommunicationRepository, uidA: String, uidB: String): Boolean {
        return repository.isCommunicationBlocked(uidA, uidB)
    }

    fun getCommunicationPermissionLabel(isAllowed: Boolean, featureName: String): String {
        return if (isAllowed) "$featureName Available" else "$featureName Disabled by User Privacy"
    }
}
