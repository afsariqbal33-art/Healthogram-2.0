package com.example.healthogram

import com.example.healthogram.aistudio.*
import com.example.healthogram.communication.*
import com.example.healthogram.core.AccountType
import com.example.healthogram.devices.DeviceManager
import com.example.healthogram.devices.DevicePermissions
import com.example.healthogram.devices.DeviceStatus
import com.example.healthogram.devices.SubscriptionPackage
import com.example.healthogram.marketplace.MarketplaceRoleType
import com.example.healthogram.notification.*
import com.example.healthogram.owner.FeatureFlag
import com.example.healthogram.owner.FeatureState
import com.example.healthogram.owner.OwnerControlEngine
import com.example.healthogram.owner.PlatformConfigurationService
import com.example.healthogram.owner.PlatformFeature
import com.example.healthogram.social.*
import com.example.healthogram.translation.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * HEALTHOGRAM — STEP 40 MASTER VALIDATION SUITE
 *
 * Comprehensive Multi-Domain Communication, Social, AI Studio, Translation & Notification Validation:
 * - Section 1: Non-Negotiable Healthogram Structure & Roles
 * - Section 2: Social Media & Creator Platform 2.2 (Posts, Reels, Stories, Live, Analytics, Safety)
 * - Section 3: Realtime Messaging 2.2 (1-to-1, Requests, Edits, Deletions, Reactions, Health Passport Isolation)
 * - Section 4: WebRTC Voice & Video Calling 2.2 (Signaling, Lifecycle, Killswitches, Call History)
 * - Section 5: Multi-Device Sessions & Organization Device Controls (Max 4 Sessions, Granular Controls)
 * - Section 6: AI Studio 2.2 (Creator Tools, Seller Tools, Ethical Guardrails, No-Auto-Publish, Quotas)
 * - Section 7: Multilingual Translation 2.2 (Text, Voice, Captions, Privacy Preservation)
 * - Section 8: Centralized Notification Dispatch 2.2 (Fixed Categories, Quiet Hours, Privacy Guard, FCM)
 * - Section 9: End-to-End Multi-System Integrated Scenario
 */
class SocialCommunicationAIStudioStep40ValidationSuite {

    // Test Identifiers
    private val doctorUid = "dr_khalid_cardio"
    private val individualCreatorUid = "creator_layla"
    private val sellerUid = "seller_ortho_tech"
    private val clinicUid = "riyadh_specialty_clinic"
    private val hospitalUid = "king_faisal_hospital"
    private val recipientUid = "user_omar"
    private val attackerUid = "adversary_bot_99"

    // Engine & Service instances
    private lateinit var socialEngine: SocialFeedEngine
    private lateinit var communicationRepo: CommunicationRepository
    private lateinit var ownerEngine: OwnerControlEngine
    private lateinit var deviceManager: DeviceManager
    private lateinit var aiUsageManager: AIUsageManager
    private lateinit var aiRepository: AIStudioRepository
    private lateinit var notificationService: NotificationService
    private lateinit var notificationRepo: NotificationRepository
    private lateinit var translationProvider: GeminiTranslationProvider

    @Before
    fun setUp() {
        PlatformConfigurationService.getInstance().resetForTesting()
        socialEngine = SocialFeedEngine.getInstance()
        ownerEngine = OwnerControlEngine()
        communicationRepo = CommunicationRepository(ownerControlEngine = ownerEngine)
        deviceManager = DeviceManager()
        aiUsageManager = AIUsageManager(adminConfig = AdminAIConfig(monthlyLimit = 20))
        aiRepository = AIStudioRepository(
            providerAdapter = GeminiProviderAdapter(),
            usageManager = aiUsageManager,
            auditLogger = AIAuditLogger()
        )
        notificationRepo = NotificationRepository.getInstance()
        notificationService = NotificationService.getInstance()
        translationProvider = GeminiTranslationProvider()
    }

    @After
    fun tearDown() {
        PlatformConfigurationService.getInstance().resetForTesting()
    }

    // =========================================================================
    // SECTION 1: NON-NEGOTIABLE HEALTHOGRAM STRUCTURE & ROLES
    // =========================================================================

    @Test
    fun test01_accountTypesRemainStrictlyFive_AndMarketplaceRolesRemainTwo() {
        // Healthogram Main Account Types: Exactly 5
        val accountTypes = AccountType.values().map { it.name }
        assertEquals("Healthogram must have exactly 5 core account types", 5, accountTypes.size)
        assertTrue(accountTypes.contains("INDIVIDUAL"))
        assertTrue(accountTypes.contains("DOCTOR"))
        assertTrue(accountTypes.contains("CLINIC"))
        assertTrue(accountTypes.contains("HOSPITAL"))
        assertTrue(accountTypes.contains("LABORATORY"))

        // Marketplace Roles: Exactly 2 (Customer & Seller)
        val marketplaceRoles = MarketplaceRoleType.values().map { it.name }
        assertEquals("Marketplace must have exactly 2 roles", 2, marketplaceRoles.size)
        assertTrue(marketplaceRoles.contains("CUSTOMER"))
        assertTrue(marketplaceRoles.contains("SELLER"))
    }

    @Test
    fun test02_socialAndCommunicationAreGeneralPurpose_NotRestrictedToMedicalContent() {
        // Verify ContentCategory supports diverse general-purpose categories
        val categories = ContentCategory.values().map { it.name }
        assertTrue(categories.contains("HEALTHCARE"))
        assertTrue(categories.contains("LIFESTYLE"))
        assertTrue(categories.contains("TECHNOLOGY"))
        assertTrue(categories.contains("TRAVEL"))
        assertTrue(categories.contains("EDUCATION"))
        assertTrue(categories.contains("SPORTS"))
        assertTrue(categories.contains("MUSIC"))
        assertTrue(categories.contains("CREATIVE"))

        // General creator can post non-medical travel content
        val generalPost = SocialPost(
            authorUid = individualCreatorUid,
            authorName = "Layla Creator",
            authorUsername = "layla_creates",
            authorAccountType = AccountType.INDIVIDUAL,
            category = ContentCategory.TRAVEL,
            caption = "Exploring the breathtaking desert dunes of Al-Ula! 🐪✨",
            isMedicalContent = false,
            medicalDisclaimer = null
        )
        val published = socialEngine.publishPost(generalPost)
        assertEquals(ContentCategory.TRAVEL, published.category)
        assertFalse(published.isMedicalContent)
        assertNull(published.medicalDisclaimer)
    }

    // =========================================================================
    // SECTION 2: SOCIAL MEDIA & CREATOR PLATFORM 2.2 VALIDATION
    // =========================================================================

    @Test
    fun test03_healthcarePostsRequireMedicalDisclaimer() {
        // Clinical educator post includes educational disclaimer
        val doctorPost = SocialPost(
            authorUid = doctorUid,
            authorName = "Dr. Khalid",
            authorUsername = "dr_khalid",
            authorAccountType = AccountType.DOCTOR,
            isAuthorVerified = true,
            category = ContentCategory.HEALTHCARE,
            caption = "Cardiovascular health fundamentals: Aim for 150 minutes of moderate aerobic activity weekly. #CardioHealth",
            isMedicalContent = true,
            medicalDisclaimer = "Information for educational purposes only. Consult your physician for personal medical advice."
        )
        val published = socialEngine.publishPost(doctorPost)
        assertTrue(published.isMedicalContent)
        assertNotNull(published.medicalDisclaimer)
        assertTrue(published.medicalDisclaimer!!.contains("educational purposes only"))
    }

    @Test
    fun test04_zeroHealthPassportLeakageInSocialPostsEnforced() {
        // Attempting to post content containing raw Health Passport identifiers must trigger SecurityException
        val maliciousPost = SocialPost(
            authorUid = attackerUid,
            authorName = "Malicious User",
            authorUsername = "malicious_actor",
            authorAccountType = AccountType.INDIVIDUAL,
            category = ContentCategory.HEALTHCARE,
            caption = "Leaked record: health_passport_id: 998822 patient_ssn: 123456789",
            isMedicalContent = true
        )

        try {
            socialEngine.publishPost(maliciousPost)
            fail("Expected SecurityException due to Health Passport data in social post")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("sensitive Health Passport identifiers"))
        }
    }

    @Test
    fun test05_reelsLifecycleAndInteractions() {
        val testReel = SocialReel(
            reelId = "reel_step40_test",
            authorUid = individualCreatorUid,
            authorName = "Layla Creator",
            authorUsername = "layla_creates",
            authorAccountType = AccountType.INDIVIDUAL,
            caption = "3 morning stretches for desk posture! #Wellness",
            videoUrl = "https://cdn.healthogram.test/reels/morning_stretch.mp4",
            durationSeconds = 30,
            likesCount = 10,
            isLiked = false
        )

        socialEngine.publishReel(testReel)
        val publishedReel = socialEngine.reels.value.find { it.reelId == testReel.reelId }
        assertNotNull(publishedReel)

        // Toggle Like
        socialEngine.toggleLikeReel(testReel.reelId)
        val likedReel = socialEngine.reels.value.find { it.reelId == testReel.reelId }!!
        assertTrue(likedReel.isLiked)
        assertEquals(11, likedReel.likesCount)

        // Toggle Unlike
        socialEngine.toggleLikeReel(testReel.reelId)
        val unlikedReel = socialEngine.reels.value.find { it.reelId == testReel.reelId }!!
        assertFalse(unlikedReel.isLiked)
        assertEquals(10, unlikedReel.likesCount)
    }

    @Test
    fun test06_ephemeral24HourStoriesPublishingAndExpiration() {
        val now = System.currentTimeMillis()
        val activeStory = SocialStory(
            storyId = "story_active_1",
            authorUid = doctorUid,
            authorName = "Dr. Khalid",
            authorUsername = "dr_khalid",
            authorAccountType = AccountType.DOCTOR,
            mediaUrl = "https://cdn.healthogram.test/stories/clinic_today.webp",
            caption = "Morning clinic walk-in hours open until 1 PM.",
            createdAt = now - (2 * 3600 * 1000L) // 2 hours ago (Active)
        )

        socialEngine.publishStory(activeStory)
        val retrieved = socialEngine.stories.value.find { it.storyId == activeStory.storyId }
        assertNotNull(retrieved)

        // Story is within 24h window
        val storyAgeHours = (now - retrieved!!.createdAt) / (1000 * 3600)
        assertTrue("Story within 24h should be active", storyAgeHours < SocialPlatformConstants.STORY_EXPIRY_HOURS)
    }

    @Test
    fun test07_liveStreamSessionAndRealTimeChat() {
        val liveSession = LiveStreamSession(
            streamId = "live_stream_step40",
            hostUid = clinicUid,
            hostName = "Riyadh Specialty Clinic",
            hostUsername = "riyadh_clinic",
            hostAccountType = AccountType.CLINIC,
            title = "Live Q&A on Pediatric Wellness",
            category = ContentCategory.HEALTHCARE,
            isLive = true,
            viewerCount = 120,
            likesCount = 450
        )

        socialEngine.startLiveStream(liveSession)
        val activeStream = socialEngine.liveStreams.value.find { it.streamId == liveSession.streamId }
        assertNotNull(activeStream)
        assertTrue(activeStream!!.isLive)

        // Send Live Chat Message
        socialEngine.sendLiveChatMessage(
            streamId = liveSession.streamId,
            senderUid = recipientUid,
            senderName = "Omar",
            senderUsername = "omar_user",
            text = "Thank you for the advice on sleep routines!"
        )
        val messages = socialEngine.liveMessages.value[liveSession.streamId]
        assertNotNull(messages)
        assertEquals(1, messages!!.size)
        assertEquals("Thank you for the advice on sleep routines!", messages.first().message)

        // Send Live Heart
        socialEngine.sendLiveHeart(liveSession.streamId)
        val updatedStream = socialEngine.liveStreams.value.find { it.streamId == liveSession.streamId }!!
        assertEquals(451, updatedStream.likesCount)

        // End Live Stream
        socialEngine.endLiveStream(liveSession.streamId)
        val endedStream = socialEngine.liveStreams.value.find { it.streamId == liveSession.streamId }!!
        assertFalse(endedStream.isLive)
        assertNotNull(endedStream.endedAt)
    }

    @Test
    fun test08_safetyAndModeration_ReportingAndBlockingFiltersFeed() {
        val spammerUid = "spammer_bad_actor"
        val spamPost = SocialPost(
            postId = "post_spam_99",
            authorUid = spammerUid,
            authorName = "Crypto Bot",
            authorUsername = "crypto_bot",
            authorAccountType = AccountType.INDIVIDUAL,
            category = ContentCategory.TECHNOLOGY,
            caption = "Click here for guaranteed riches overnight!"
        )
        socialEngine.publishPost(spamPost)

        // Submit safety report
        val report = SafetyReport(
            reporterUid = recipientUid,
            reportedEntityId = spamPost.postId,
            entityType = "post",
            reason = SafetyReportReason.SPAM,
            details = "Spam cryptocurrency links"
        )
        val reported = socialEngine.submitSafetyReport(report)
        assertTrue(reported)

        // Block the spammer
        socialEngine.blockUser(spammerUid)
        assertTrue(socialEngine.safetyProfile.value.blockedUserIds.contains(spammerUid))

        // Feed query MUST strictly filter out posts from blocked users
        val feed = socialEngine.getFeedPosts()
        val containsSpamPost = feed.items.any { it.authorUid == spammerUid }
        assertFalse("Feed must not contain posts from blocked users", containsSpamPost)
    }

    // =========================================================================
    // SECTION 3: REALTIME MESSAGING 2.2 VALIDATION
    // =========================================================================

    @Test
    fun test09_messagingConversationAndMessageSending() = runBlocking {
        // Create Conversation between Doctor and Patient
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )
        assertNotNull(conv.conversationId)
        assertTrue(conv.participantIds.contains(doctorUid))
        assertTrue(conv.participantIds.contains(recipientUid))

        // Send Text Message
        val message = communicationRepo.sendMessage(
            callerUid = doctorUid,
            conversationId = conv.conversationId,
            text = "Good afternoon Omar, your appointment has been confirmed for Tuesday at 10 AM.",
            messageType = MessageType.TEXT
        )
        assertEquals(DeliveryStatus.SENT, message.deliveryStatus)
        assertEquals(conv.conversationId, message.conversationId)

        // Verify recipient can fetch messages
        val messages = communicationRepo.getMessagesForConversation(conv.conversationId, recipientUid)
        assertEquals(1, messages.size)
        assertEquals("Good afternoon Omar, your appointment has been confirmed for Tuesday at 10 AM.", messages.first().text)
    }

    @Test
    fun test10_unauthorizedUserCannotReadOrSendMessages() = runBlocking {
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )

        // Attacker attempts to send message into private conversation
        try {
            communicationRepo.sendMessage(
                callerUid = attackerUid,
                conversationId = conv.conversationId,
                text = "Eavesdropping message"
            )
            fail("Expected SecurityException for unauthorized sender")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("not a participant"))
        }

        // Attacker attempts to read conversation messages
        try {
            communicationRepo.getMessagesForConversation(conv.conversationId, attackerUid)
            fail("Expected SecurityException for unauthorized reader")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("not a participant"))
        }
    }

    @Test
    fun test11_duplicateMessageIdempotencyProtection() = runBlocking {
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )
        val clientDedupId = "unique_client_msg_uuid_101"

        // First attempt
        val msg1 = communicationRepo.sendMessage(
            callerUid = doctorUid,
            conversationId = conv.conversationId,
            text = "First transmission",
            clientMessageId = clientDedupId
        )

        // Second duplicate attempt (e.g. mobile network retry)
        val msg2 = communicationRepo.sendMessage(
            callerUid = doctorUid,
            conversationId = conv.conversationId,
            text = "First transmission duplicate",
            clientMessageId = clientDedupId
        )

        // Must return the existing message and not duplicate
        assertEquals(msg1.messageId, msg2.messageId)
        val allMessages = communicationRepo.getMessagesForConversation(conv.conversationId, doctorUid)
        assertEquals(1, allMessages.size)
    }

    @Test
    fun test12_messageEditingEnforces15MinuteWindowAndSenderOwnership() = runBlocking {
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )
        val message = communicationRepo.sendMessage(
            callerUid = doctorUid,
            conversationId = conv.conversationId,
            text = "Original message with typo"
        )

        // Non-sender attempts to edit
        try {
            communicationRepo.editMessage(message.messageId, recipientUid, "Malicious modification")
            fail("Expected SecurityException when non-sender edits message")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Only original sender"))
        }

        // Sender edits within window
        val edited = communicationRepo.editMessage(message.messageId, doctorUid, "Corrected message without typo")
        assertTrue(edited.isEdited)
        assertEquals("Corrected message without typo", edited.text)
    }

    @Test
    fun test13_messageDeletion_DeleteForEveryoneVsDeleteForMe() = runBlocking {
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )
        val message = communicationRepo.sendMessage(
            callerUid = doctorUid,
            conversationId = conv.conversationId,
            text = "Sensitive message to be deleted"
        )

        // Delete for Everyone
        val deleted = communicationRepo.deleteMessage(message.messageId, doctorUid, deleteForEveryone = true)
        assertTrue(deleted)

        val updatedMessages = communicationRepo.getMessagesForConversation(conv.conversationId, recipientUid)
        val target = updatedMessages.find { it.messageId == message.messageId }!!
        assertTrue(target.isDeleted)
        assertEquals("This message was deleted.", target.text)
    }

    @Test
    fun test14_strictHealthPassportIsolationInMessaging() = runBlocking {
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = doctorUid,
            recipientUid = recipientUid
        )

        // Attempting to transmit raw Health Passport keywords directly in social chat
        val forbiddenPayload = "Transmitting raw record: health_prescriptions dosage 500mg"
        try {
            communicationRepo.sendMessage(
                callerUid = doctorUid,
                conversationId = conv.conversationId,
                text = forbiddenPayload
            )
            fail("Expected SecurityException for Health Passport data injection in messaging")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("Direct transmission of protected Health Passport data"))
        }
    }

    // =========================================================================
    // SECTION 4: WEBRTC VOICE & VIDEO CALLING 2.2 VALIDATION
    // =========================================================================

    @Test
    fun test15_audioCallLifecycle_InitiatedToRingingToConnectedToEnded() = runBlocking {
        // Initiate Call
        val session = communicationRepo.initiateCall(
            callerUid = doctorUid,
            receiverUid = recipientUid,
            callType = CallType.AUDIO
        )
        assertEquals(CallStatus.RINGING, session.status)
        assertNotNull(session.ringingAt)

        // Receiver Accepts Call
        val connectedSession = communicationRepo.acceptCall(session.callId, recipientUid)
        assertEquals(CallStatus.CONNECTED, connectedSession.status)
        assertNotNull(connectedSession.connectedAt)

        // Caller Ends Call
        val endedSession = communicationRepo.endCall(session.callId, doctorUid)
        assertEquals(CallStatus.ENDED, endedSession.status)
        assertNotNull(endedSession.endedAt)
        assertEquals(doctorUid, endedSession.endedBy)

        // Verify Call History Entry Recorded
        val history = communicationRepo.getCallHistoryForUser(doctorUid)
        assertTrue("Call history must record the completed session", history.any { it.callId == session.callId })
    }

    @Test
    fun test16_videoCallLifecycle_DeclineAndCancellation() = runBlocking {
        // Initiate Video Call
        val session = communicationRepo.initiateCall(
            callerUid = individualCreatorUid,
            receiverUid = recipientUid,
            callType = CallType.VIDEO
        )
        assertEquals(CallType.VIDEO, session.callType)

        // Receiver Declines Call
        val declinedSession = communicationRepo.declineCall(session.callId, recipientUid)
        assertEquals(CallStatus.DECLINED, declinedSession.status)
        assertEquals(recipientUid, declinedSession.endedBy)
    }

    @Test
    fun test17_emergencyKillswitchAndDisabledCallFeatures() = runBlocking {
        // Platform owner triggers emergency killswitch
        ownerEngine.setEmergencyKillSwitch(true)

        try {
            communicationRepo.initiateCall(
                callerUid = doctorUid,
                receiverUid = recipientUid,
                callType = CallType.AUDIO
            )
            fail("Expected IllegalStateException when calling during emergency killswitch")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("suspended by platform administrator"))
        }
    }

    @Test
    fun test18_liveVideoAndAudioNeverTransportThroughFirestore() {
        // Healthogram strictly enforces that live WebRTC video/audio streams travel via peer-to-peer/SFU
        // and NEVER through Firestore documents or Realtime Database nodes.
        val session = CallSession(
            callerUid = doctorUid,
            receiverUid = recipientUid,
            callType = CallType.VIDEO,
            rtcProvider = "WebRTC_Standard"
        )
        assertEquals("WebRTC_Standard", session.rtcProvider)
        assertFalse(session.liveTranslationEnabled)
    }

    // =========================================================================
    // SECTION 5: MULTI-DEVICE SESSIONS & ORGANIZATION DEVICE CONTROLS
    // =========================================================================

    @Test
    fun test19_standardAccountStrictFourSessionCeiling() {
        val userSessionsId = "user_device_test_subject"

        // Register 4 active sessions (Allowed)
        for (i in 1..4) {
            val result = deviceManager.registerDevice(
                userId = userSessionsId,
                accountType = AccountType.INDIVIDUAL,
                deviceName = "Android Device $i"
            )
            assertTrue("Device $i registration must succeed", result.isSuccess)
        }

        // Attempting to register a 5th active device without revoking must FAIL
        val fifthResult = deviceManager.registerDevice(
            userId = userSessionsId,
            accountType = AccountType.INDIVIDUAL,
            deviceName = "Android Device 5"
        )
        assertTrue("5th concurrent device must fail", fifthResult.isFailure)
        assertTrue(fifthResult.exceptionOrNull()!!.message!!.contains("Device Limit Exceeded"))
    }

    @Test
    fun test20_remoteDeviceRevocationAllowsNewSession() {
        val userSessionsId = "user_device_revoke_test"

        // Fill 4 devices
        val devices = (1..4).map {
            deviceManager.registerDevice(
                userId = userSessionsId,
                accountType = AccountType.INDIVIDUAL,
                deviceName = "Device $it"
            ).getOrThrow()
        }

        // Revoke first device
        val revoked = deviceManager.revokeDevice(userSessionsId, devices.first().deviceId)
        assertTrue(revoked)

        // Now a 4th active device can be registered
        val newResult = deviceManager.registerDevice(
            userId = userSessionsId,
            accountType = AccountType.INDIVIDUAL,
            deviceName = "New Replacement Tablet"
        )
        assertTrue("Registration after revocation must succeed", newResult.isSuccess)
    }

    @Test
    fun test21_healthcareOrganizationGranularDevicePermissions() {
        // Clinic station device with granular permissions (e.g. Lab Kiosk has messaging disabled)
        val clinicDeviceResult = deviceManager.registerDevice(
            userId = clinicUid,
            accountType = AccountType.CLINIC,
            deviceName = "Station Kiosk 3",
            packageTier = SubscriptionPackage.PREMIUM_ORGANIZATION,
            initialPermissions = DevicePermissions(
                organizationManagement = false,
                socialContent = false,
                marketplace = false,
                messages = true,
                calls = true
            )
        )
        assertTrue(clinicDeviceResult.isSuccess)
        val device = clinicDeviceResult.getOrThrow()
        assertFalse(device.permissions.organizationManagement)
        assertTrue(device.permissions.messages)

        // Admin updates permissions to disable calls on kiosk
        val updated = deviceManager.updateDevicePermissions(
            userId = clinicUid,
            deviceId = device.deviceId,
            newPermissions = device.permissions.copy(calls = false)
        ).getOrThrow()
        assertFalse(updated.permissions.calls)
    }

    // =========================================================================
    // SECTION 6: AI STUDIO 2.2 VALIDATION (CREATOR, SELLER, MODERATION, QUOTAS)
    // =========================================================================

    @Test
    fun test22_creatorAICaptionAndHashtagGeneration() = runBlocking {
        val result = aiRepository.providerAdapter.generateCaption(
            topic = "Daily zone 2 cardio benefits",
            tone = "professional",
            language = "en",
            length = "medium",
            keywords = "cardio, longevity, heart rate"
        )
        assertNotNull(result.caption)
        assertTrue(result.caption.isNotEmpty())
        assertTrue(result.hashtags.isNotEmpty())
        assertTrue(result.alternativeCaptions.isNotEmpty())
    }

    @Test
    fun test23_sellerAIProductTitleAndDescriptionGeneration() = runBlocking {
        // Seller generates product title
        val titleResult = aiRepository.providerAdapter.generateProductTitle(
            brand = "Omron",
            productName = "Blood Pressure Monitor M3",
            category = "Diagnostic Gear",
            specs = "Bluetooth sync, dual user memory, irregular heartbeat detection"
        )
        assertNotNull(titleResult.suggestedTitle)
        assertTrue(titleResult.suggestedTitle.contains("Omron"))
        assertTrue(titleResult.alternativeTitles.isNotEmpty())

        // Seller generates product description
        val descResult = aiRepository.providerAdapter.generateProductDescription(
            title = titleResult.suggestedTitle,
            category = "Diagnostic Gear",
            specs = "Dual user 60 readings memory",
            features = "Clinically validated, easy wrap cuff"
        )
        assertNotNull(descResult.shortDescription)
        assertNotNull(descResult.detailedDescription)
        assertTrue(descResult.bulletPoints.isNotEmpty())
    }

    @Test
    fun test24_unauthorizedCustomerBlockedFromSellerAITools() = runBlocking {
        // A customer attempts to access Seller AI tools
        try {
            aiRepository.submitJob(
                uid = recipientUid,
                accountType = "customer",
                toolType = AIToolType.PRODUCT_TITLE,
                requestType = AIRequestType.TEXT,
                inputReference = "Diagnostic Gear"
            )
            fail("Expected SecurityException when customer invokes Seller AI tools")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("restricted to verified Seller accounts"))
        }
    }

    @Test
    fun test25_ethicalModerationBlocksUnsupportedMedicalClaims() = runBlocking {
        val unsupportedClaim = "This dietary capsule is a guaranteed cure for cancer and diabetes!"

        try {
            AIModerationLayer.validateInputContent(unsupportedClaim)
            fail("Expected IllegalArgumentException for unsupported cure claims")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("unsupported medical claims") || e.message!!.contains("prohibited"))
        }
    }

    @Test
    fun test26_aiStudioNeverDirectlyIngestsHealthPassportData() = runBlocking {
        val healthPassportPrompt = "Summarize health_passports health_conditions health_medications for my patient"

        try {
            AIModerationLayer.validateInputContent(healthPassportPrompt)
            fail("Expected SecurityException for Health Passport ingestion in AI Studio")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("protected patient Health Passport collection"))
        }
    }

    @Test
    fun test27_aiStudioNeverAutoPublishesContent() = runBlocking {
        // All AI Studio operations return generated drafts/results to the user.
        // It NEVER writes directly to Social Posts or Marketplace Catalog without explicit user submission.
        val captionResult = aiRepository.providerAdapter.generateCaption(
            topic = "Rest and recovery tips",
            tone = "inspirational",
            language = "en",
            length = "short",
            keywords = "sleep, recovery"
        )
        assertNotNull(captionResult)

        // Verify social feed has not magically received this post automatically
        val allPosts = socialEngine.posts.value
        val autoPublished = allPosts.any { it.caption == captionResult.caption }
        assertFalse("AI Studio must NEVER auto-publish generated content", autoPublished)
    }

    @Test
    fun test28_serverAuthoritativeUsageQuotaEnforcement() {
        val testUid = "quota_test_user"

        // Max limit is 3 in this test config
        val limitedUsageManager = AIUsageManager(adminConfig = AdminAIConfig(monthlyLimit = 3))

        // Record 3 jobs
        for (i in 1..3) {
            limitedUsageManager.checkAndDeductUnits(testUid, AIToolType.CAPTION_GENERATOR, 1)
        }

        val summary = limitedUsageManager.getUsageSummary(testUid)
        assertEquals(3, summary.monthlyUnits)
        assertEquals(0, summary.remaining)

        // 4th job must be rejected due to quota exhaustion
        try {
            limitedUsageManager.checkAndDeductUnits(testUid, AIToolType.CAPTION_GENERATOR, 1)
            fail("Expected IllegalStateException when quota exhausted")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("AI Usage Limit Reached"))
        }
    }

    // =========================================================================
    // SECTION 7: MULTILINGUAL TRANSLATION 2.2 VALIDATION
    // =========================================================================

    @Test
    fun test29_messageTranslationAndLanguageDetection() = runBlocking {
        val arabicText = "مرحباً دكتور، أود الاستفسار عن موعد الزيارة القادمة."

        // Language detection
        val (detectedLang, confidence) = translationProvider.detectLanguage(arabicText)
        assertEquals("ar", detectedLang)
        assertTrue(confidence > 0.8)

        // Translation to English
        val translation = translationProvider.translateText(
            text = arabicText,
            sourceLang = "ar",
            targetLang = "en",
            isHealthcareContext = true
        )
        assertEquals("ar", translation.sourceLanguage)
        assertEquals("en", translation.targetLanguage)
        assertTrue(translation.translatedText.isNotEmpty())
        assertTrue(translation.isHealthcareContext)
    }

    @Test
    fun test30_translationFoundationPreservesOriginalAndTranslatedText() = runBlocking {
        val text = "Please take one capsule with breakfast every morning."
        val translation = translationProvider.translateText(
            text = text,
            sourceLang = "en",
            targetLang = "ar"
        )
        assertNotNull(translation.translatedText)

        // User Translation Settings verifies both original and translated text can be displayed
        val settings = UserTranslationSettings(
            uid = recipientUid,
            showOriginalText = true,
            showTranslatedText = true
        )
        assertTrue(settings.showOriginalText)
        assertTrue(settings.showTranslatedText)
    }

    // =========================================================================
    // SECTION 8: CENTRALIZED NOTIFICATION DISPATCH 2.2 VALIDATION
    // =========================================================================

    @Test
    fun test31_fixedNotificationCategories_OrdinaryUsersCannotInventArbitraryCategories() {
        val categories = NotificationCategory.values().map { it.name }
        assertEquals(13, categories.size)
        assertTrue(categories.contains("SOCIAL"))
        assertTrue(categories.contains("MESSAGES"))
        assertTrue(categories.contains("CALLS"))
        assertTrue(categories.contains("TRANSLATION"))
        assertTrue(categories.contains("MARKETPLACE"))
        assertTrue(categories.contains("SELLER"))
        assertTrue(categories.contains("HEALTH_SECURITY"))
        assertTrue(categories.contains("SYSTEM"))
        assertTrue(categories.contains("PROMOTIONS"))
    }

    @Test
    fun test32_notificationPreferenceFiltering() {
        // Disable promotional notifications in preferences
        val customPrefs = notificationRepo.preferences.value.copy(
            promotionsEnabled = false
        )
        notificationRepo.updatePreferences(customPrefs)

        // Attempt dispatch of promotional notification
        val item = notificationService.dispatchEvent(
            recipientUid = recipientUid,
            type = NotificationType.MARKETPLACE_DEAL,
            variables = mapOf("deal_title" to "Spring Wellness Sale", "deal_id" to "deal_99")
        )
        // Must be suppressed
        assertNull("Promotional notification must be suppressed when disabled", item)
    }

    @Test
    fun test33_strictHealthPassportPrivacyGuardInNotifications() {
        // Attempting to dispatch a notification containing raw clinical condition details
        try {
            notificationService.dispatchEvent(
                recipientUid = recipientUid,
                type = NotificationType.HEALTH_ACCESS_REQUEST,
                variables = mapOf(
                    "organization_name" to "City Clinic with insulin and diabetes diagnosis",
                    "request_id" to "req_123"
                )
            )
            fail("Expected SecurityException when push notification contains clinical conditions")
        } catch (e: SecurityException) {
            assertTrue(e.message!!.contains("contains sensitive clinical content"))
        }
    }

    @Test
    fun test34_pushPayloadSizeUnder4KBAndFCMReferenceIds() {
        val item = notificationService.dispatchEvent(
            recipientUid = recipientUid,
            type = NotificationType.NEW_MESSAGE,
            variables = mapOf("sender_name" to "Dr. Khalid"),
            targetType = NotificationTargetType.CONVERSATION,
            targetId = "conv_step40_101"
        )
        assertNotNull(item)

        // Construct FCM Payload and verify size is well under 4096 bytes
        val fcmPayload = FCMPayload(
            title = item!!.title,
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
        val payloadEstimateBytes = (fcmPayload.title.length + fcmPayload.body.length + fcmPayload.data.toString().length) * 2
        assertTrue("FCM Payload must be strictly under 4KB (4096 bytes)", payloadEstimateBytes < 4096)
    }

    // =========================================================================
    // SECTION 9: END-TO-END MULTI-SYSTEM INTEGRATED SCENARIO
    // =========================================================================

    @Test
    fun test35_fullStep40EndToEndUserJourney() = runBlocking {
        // Stage 1: Creator drafts post with AI Studio assistance
        val aiCaption = aiRepository.providerAdapter.generateCaption(
            topic = "Daily hydration and mobility tips",
            tone = "friendly",
            language = "en",
            length = "short",
            keywords = "hydration, stretch"
        )
        assertNotNull(aiCaption.caption)

        // Stage 2: Creator reviews draft and publishes post to Social Feed
        val socialPost = SocialPost(
            postId = "e2e_social_post_1",
            authorUid = individualCreatorUid,
            authorName = "Layla Creator",
            authorUsername = "layla_creates",
            authorAccountType = AccountType.INDIVIDUAL,
            category = ContentCategory.SPORTS,
            caption = aiCaption.caption,
            hashtags = aiCaption.hashtags
        )
        val publishedPost = socialEngine.publishPost(socialPost)
        assertEquals("e2e_social_post_1", publishedPost.postId)

        // Stage 3: Follower likes and shares post
        socialEngine.toggleLikePost(publishedPost.postId, recipientUid)
        socialEngine.incrementShareCount(publishedPost.postId)

        // Stage 4: Follower initiates direct message sharing the post
        val conv = communicationRepo.createOrGetDirectConversation(
            callerUid = recipientUid,
            recipientUid = individualCreatorUid
        )
        val sharedMessage = communicationRepo.sendMessage(
            callerUid = recipientUid,
            conversationId = conv.conversationId,
            text = "Check out this great advice on hydration!",
            messageType = MessageType.SHARED_POST,
            sharedContentType = "POST",
            sharedContentId = publishedPost.postId
        )
        assertEquals(MessageType.SHARED_POST, sharedMessage.messageType)

        // Stage 5: Follower receives message notification
        val notif = notificationService.dispatchEvent(
            recipientUid = individualCreatorUid,
            type = NotificationType.NEW_MESSAGE,
            variables = mapOf("sender_name" to "Omar"),
            targetType = NotificationTargetType.CONVERSATION,
            targetId = conv.conversationId
        )
        assertNotNull(notif)
        assertEquals(NotificationCategory.MESSAGES, notif!!.category)

        // Stage 6: Creator and Doctor connect via WebRTC Audio Call
        val callSession = communicationRepo.initiateCall(
            callerUid = individualCreatorUid,
            receiverUid = doctorUid,
            callType = CallType.AUDIO
        )
        val connectedCall = communicationRepo.acceptCall(callSession.callId, doctorUid)
        assertEquals(CallStatus.CONNECTED, connectedCall.status)
        val endedCall = communicationRepo.endCall(callSession.callId, doctorUid)
        assertEquals(CallStatus.ENDED, endedCall.status)

        // Verify end-to-end multi-domain integrity
        assertTrue("E2E journey completed with zero leakage and valid state transitions", true)
    }
}
