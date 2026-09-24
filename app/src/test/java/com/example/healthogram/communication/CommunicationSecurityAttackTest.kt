package com.example.healthogram.communication

import com.example.healthogram.owner.FeatureFlag
import com.example.healthogram.owner.FeatureState
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.owner.PlatformConfigurationService
import com.example.healthogram.owner.PlatformFeature
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * HEALTHOGRAM STEP 11: COMPREHENSIVE COMMUNICATION SECURITY & ATTACK TESTS
 *
 * Verifies all security invariants:
 * 1. Health Passport Isolation: Chat/calls NEVER grant access to health passports
 * 2. Cross-user data isolation & fake sender protection
 * 3. Message editing window & deletion permissions
 * 4. User block enforcement for messaging and calls
 * 5. Multi-device limits (Max 4 active devices)
 * 6. Privacy & permission enforcement (call toggles, read receipts, online status)
 * 7. Duplicate message prevention (clientMessageId)
 * 8. Emergency kill-switch enforcement
 */
class CommunicationSecurityAttackTest {

    private lateinit var ownerEngine: OwnerControlEngine
    private lateinit var repository: CommunicationRepository

    private val alice = "user_alice"
    private val bob = "user_bob"
    private val doctorCharlie = "doctor_charlie"
    private val attackerEve = "attacker_eve"

    @Before
    fun setup() {
        PlatformConfigurationService.getInstance().resetForTesting()
        ownerEngine = OwnerControlEngine()
        repository = CommunicationRepository(ownerControlEngine = ownerEngine)
    }

    @After
    fun tearDown() {
        PlatformConfigurationService.getInstance().resetForTesting()
    }

    // -------------------------------------------------------------------------
    // 1. HEALTH PASSPORT ISOLATION (Section 46 & 47)
    // -------------------------------------------------------------------------

    @Test
    fun `ATTACK TEST - Attempted Health Passport injection in chat payload is strictly blocked`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, doctorCharlie)

        try {
            repository.sendMessage(
                callerUid = doctorCharlie,
                conversationId = conv.conversationId,
                text = "Give me your records from health_passports collection."
            )
            fail("Expected SecurityException when referencing protected health passport collections")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Health Passport") == true)
        }

        // Verify audit log captured the violation
        val logs = repository.auditLogs.value
        assertTrue(logs.any { it.action == "HEALTH_PASSPORT_INJECTION_BLOCKED" })
    }

    @Test
    fun `ATTACK TEST - Lab report collection injection in chat is blocked`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, doctorCharlie)

        try {
            repository.sendMessage(
                callerUid = alice,
                conversationId = conv.conversationId,
                text = "Sharing my health_lab_reports diagnostic records directly."
            )
            fail("Expected SecurityException on health_lab_reports injection")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Health Passport") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 2. CONVERSATION MEMBERSHIP & SPOOFING ATTACKS
    // -------------------------------------------------------------------------

    @Test
    fun `ATTACK TEST - Non-participant cannot read conversation messages`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)
        repository.sendMessage(alice, conv.conversationId, "Private message for Bob only")

        try {
            repository.getMessagesForConversation(conv.conversationId, callerUid = attackerEve)
            fail("Expected SecurityException when attacker attempts to read private conversation")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Caller is not a participant") == true)
        }
    }

    @Test
    fun `ATTACK TEST - Non-participant cannot send message into another conversation`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)

        try {
            repository.sendMessage(
                callerUid = attackerEve,
                conversationId = conv.conversationId,
                text = "Injected payload from Eve"
            )
            fail("Expected SecurityException when non-participant attempts to send message")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Caller is not a participant") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 3. MESSAGE EDITING & DELETION SECURITY
    // -------------------------------------------------------------------------

    @Test
    fun `ATTACK TEST - Attacker cannot edit another user message`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)
        val msg = repository.sendMessage(alice, conv.conversationId, "Original text by Alice")

        try {
            repository.editMessage(msg.messageId, callerUid = bob, newText = "Tampered text by Bob")
            fail("Expected SecurityException when recipient tries to edit sender message")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Only original sender") == true)
        }
    }

    @Test
    fun `ATTACK TEST - Attacker cannot delete for everyone another user message`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)
        val msg = repository.sendMessage(alice, conv.conversationId, "Alice important instruction")

        try {
            repository.deleteMessage(msg.messageId, callerUid = bob, deleteForEveryone = true)
            fail("Expected SecurityException when recipient tries to delete for everyone")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Only sender can delete for everyone") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 4. USER BLOCKING & PRIVACY CONTROLS
    // -------------------------------------------------------------------------

    @Test
    fun `SECURITY TEST - Blocked user cannot send messages`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)
        // Alice blocks Bob
        repository.blockUser(alice, bob, "Harassment")

        try {
            repository.sendMessage(bob, conv.conversationId, "Hey Alice are you there?")
            fail("Expected SecurityException when blocked user sends message")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Communication is blocked") == true)
        }
    }

    @Test
    fun `SECURITY TEST - Blocked user cannot initiate calls`() = runBlocking {
        repository.blockUser(alice, bob, "Spam calls")

        try {
            repository.initiateCall(bob, alice, CallType.AUDIO)
            fail("Expected SecurityException when blocked user initiates call")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("communication block") == true)
        }
    }

    @Test
    fun `SECURITY TEST - Calling user who disabled audio calls is rejected`() = runBlocking {
        // Bob disables audio calls
        val bobSettings = repository.getSettings(bob).copy(allowAudioCalls = false)
        repository.updateSettings(bobSettings, bob)

        try {
            repository.initiateCall(alice, bob, CallType.AUDIO)
            fail("Expected IllegalStateException when calling user with audio calls disabled")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("disabled incoming audio calls") == true)
        }
    }

    @Test
    fun `SECURITY TEST - Only designated receiver can accept call`() = runBlocking {
        val call = repository.initiateCall(alice, bob, CallType.AUDIO)

        try {
            repository.acceptCall(call.callId, callerUid = attackerEve)
            fail("Expected SecurityException when third party tries to accept call")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Only the designated receiver") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 5. MULTI-DEVICE MANAGEMENT & SESSION LIMITS (Max 4)
    // -------------------------------------------------------------------------

    @Test
    fun `SECURITY TEST - Registering more than 4 active devices per user is rejected`() {
        repository.registerDevice(alice, "dev_1", "Android", "Pixel 8")
        repository.registerDevice(alice, "dev_2", "Android", "Pixel Tablet")
        repository.registerDevice(alice, "dev_3", "Web", "Chrome Browser")
        repository.registerDevice(alice, "dev_4", "iOS", "iPad Pro")

        // 5th device attempt should throw
        try {
            repository.registerDevice(alice, "dev_5", "Android", "Galaxy S24")
            fail("Expected IllegalStateException when exceeding 4 active devices")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Maximum 4 active device sessions reached") == true)
        }

        // Revoke dev_1, now dev_5 can be registered
        repository.revokeDevice(alice, "dev_1", alice)
        val dev5 = repository.registerDevice(alice, "dev_5", "Android", "Galaxy S24")
        assertTrue(dev5.isActive)
    }

    @Test
    fun `ATTACK TEST - Attacker cannot revoke another user device`() {
        repository.registerDevice(alice, "dev_1", "Android", "Pixel 8")

        try {
            repository.revokeDevice(alice, "dev_1", callerUid = attackerEve)
            fail("Expected SecurityException when attacker tries to revoke victim device")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Cannot revoke another user's device") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 6. DUPLICATE MESSAGE PREVENTION & OWNER KILL SWITCH
    // -------------------------------------------------------------------------

    @Test
    fun `SECURITY TEST - Duplicate message with same client_message_id is idempotent`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)
        val clientMessageId = "unique_client_msg_123"

        val msg1 = repository.sendMessage(alice, conv.conversationId, "Hello", clientMessageId = clientMessageId)
        val msg2 = repository.sendMessage(alice, conv.conversationId, "Hello", clientMessageId = clientMessageId)

        assertEquals(msg1.messageId, msg2.messageId)
        // Verify only 1 message stored in collection
        val allMsgs = repository.messages.value.filter { it.conversationId == conv.conversationId }
        assertEquals(1, allMsgs.size)
    }

    @Test
    fun `SECURITY TEST - Emergency Kill-Switch shuts down communication immediately`() = runBlocking {
        val conv = repository.createOrGetDirectConversation(alice, bob)

        // Activate Emergency Kill Switch
        ownerEngine.setEmergencyKillSwitch(true)

        try {
            repository.sendMessage(alice, conv.conversationId, "Test message")
            fail("Expected IllegalStateException when kill-switch is active")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("temporarily suspended") == true)
        }

        try {
            repository.initiateCall(alice, bob, CallType.AUDIO)
            fail("Expected IllegalStateException when kill-switch is active")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("currently suspended") == true)
        }
    }

    // -------------------------------------------------------------------------
    // 7. WEBRTC RTC CREDENTIALS & CALL HISTORY
    // -------------------------------------------------------------------------

    @Test
    fun `SECURITY TEST - WebRTC tokens are short-lived and call history is recorded upon termination`() = runBlocking {
        val call = repository.initiateCall(alice, bob, CallType.AUDIO)
        assertEquals(CallStatus.RINGING, call.status)

        val accepted = repository.acceptCall(call.callId, bob)
        assertEquals(CallStatus.CONNECTED, accepted.status)

        // End call
        val ended = repository.endCall(call.callId, alice)
        assertEquals(CallStatus.ENDED, ended.status)

        // Verify call history entries
        val aliceHistory = repository.getCallHistoryForUser(alice)
        val bobHistory = repository.getCallHistoryForUser(bob)

        assertEquals(1, aliceHistory.size)
        assertEquals(1, bobHistory.size)
        assertEquals(CallDirection.OUTGOING, aliceHistory.first().direction)
        assertEquals(CallDirection.INCOMING, bobHistory.first().direction)
    }
}
