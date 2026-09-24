package com.example.healthogram.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Step 19: Production Security Hardening & Zero-Trust Architecture Test Suite.
 */
class ProductionSecurityHardeningTest {

    private lateinit var engine: SecurityHardeningEngine

    @Before
    fun setUp() {
        engine = SecurityHardeningEngine.getInstance()
        engine.resetForTesting()
    }

    @Test
    fun testRateLimitingEnforcesThresholdsAndBlocksExcessiveAttempts() {
        val userKey = "attacker_uid_001"
        val endpoint = "LOGIN"

        // Max 5 attempts allowed in 60 seconds
        for (i in 1..5) {
            val res = engine.checkRateLimit(userKey, endpoint)
            assertTrue("Attempt $i should be allowed", res.isAllowed)
        }

        // 6th attempt must be rejected
        val blockedRes = engine.checkRateLimit(userKey, endpoint)
        assertFalse("6th attempt must be blocked by rate limit", blockedRes.isAllowed)
        assertTrue(blockedRes.retryAfterSeconds > 0)
        assertNotNull(blockedRes.reason)
    }

    @Test
    fun testSessionManagementEnforcesFourDeviceLimit() {
        val userId = "user_multi_device_01"

        // Register 4 devices successfully
        for (i in 1..4) {
            val result = engine.registerOrUpdateSession(
                uid = userId,
                deviceId = "device_id_$i",
                deviceName = "Phone $i"
            )
            assertTrue("Device $i registration should succeed", result.isSuccess)
        }

        assertEquals(4, engine.getActiveSessions(userId).size)

        // 5th device registration must fail
        val fifthResult = engine.registerOrUpdateSession(
            uid = userId,
            deviceId = "device_id_5",
            deviceName = "Tablet 5"
        )
        assertTrue("5th session must be rejected due to 4-device limit", fifthResult.isFailure)

        // Revoking 1 session allows adding a new device
        val revoked = engine.revokeUserSession(userId, "device_id_1")
        assertTrue(revoked)
        assertEquals(3, engine.getActiveSessions(userId).size)

        val retryFifth = engine.registerOrUpdateSession(
            uid = userId,
            deviceId = "device_id_5",
            deviceName = "Tablet 5"
        )
        assertTrue("After revoking an old device, 5th session registration must succeed", retryFifth.isSuccess)
        assertEquals(4, engine.getActiveSessions(userId).size)
    }

    @Test
    fun testReauthChallengeLifecycleAndExpiration() {
        val uid = "user_sensitive_01"
        val challenge = engine.issueReauthChallenge(uid, "CHANGE_PASSWORD")

        assertNotNull(challenge.challengeId)
        assertFalse(challenge.isVerified)
        assertFalse(challenge.isExpired)

        // Unverified operation should fail
        val unverifiedAttempt = engine.validateSensitiveOperation(
            uid = uid,
            operation = "CHANGE_PASSWORD",
            challengeId = challenge.challengeId
        )
        assertTrue("Unverified challenge must fail", unverifiedAttempt.isFailure)

        // Successful reauth verification
        val verified = engine.verifyReauthChallenge(challenge.challengeId, authProofValid = true)
        assertTrue(verified)

        val verifiedAttempt = engine.validateSensitiveOperation(
            uid = uid,
            operation = "CHANGE_PASSWORD",
            challengeId = challenge.challengeId
        )
        assertTrue("Verified challenge must allow operation", verifiedAttempt.isSuccess)
    }

    @Test
    fun testHealthPassportZeroTrustAccessGuardRequiresPatientConsent() {
        val patientUid = "patient_alice_001"
        val doctorUid = "doctor_bob_002"
        val requestedScopes = setOf(HealthAccessScope.ALLERGIES, HealthAccessScope.MEDICATIONS)

        // Doctor attempting access without patient grant must be denied (even if verified)
        val unauthorizedCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Access without patient consent grant must be rejected", unauthorizedCheck.isFailure)

        // Patient creates grant
        val grant = engine.requestHealthAccessGrant(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterAccountType = "DOCTOR",
            purpose = "Cardiology consultation",
            scopes = requestedScopes
        )
        assertEquals(HealthGrantStatus.REQUESTED, grant.status)

        // Unapproved grant is not sufficient
        val pendingCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Pending grant must not grant data access", pendingCheck.isFailure)

        // Patient approves grant
        val approveResult = engine.approveHealthAccessGrant(grant.grantId, patientUid)
        assertTrue(approveResult.isSuccess)

        // Authorized access now succeeds
        val authorizedCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Approved grant must permit access", authorizedCheck.isSuccess)

        // If doctor requests scopes not covered by grant (e.g. VISITS), access must be denied
        val partialScopeCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes + HealthAccessScope.VISITS
        )
        assertTrue("Requesting scopes outside patient consent grant must be denied", partialScopeCheck.isFailure)

        // Revoking grant immediately terminates access
        val revoked = engine.revokeHealthAccessGrant(grant.grantId, patientUid)
        assertTrue(revoked)

        val revokedCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Revoked grant must deny access immediately", revokedCheck.isFailure)
    }

    @Test
    fun testHealthQrSessionSingleUseTokenConsumption() {
        val patientUid = "patient_charlie_003"
        val qrSession = engine.createHealthQrSession(patientUid, isSingleUse = true)

        assertTrue(qrSession.isUsable)
        assertFalse(qrSession.isConsumed)

        // First scan succeeds
        val firstConsume = engine.consumeHealthQrSession(qrSession.opaqueToken, "doctor_001")
        assertTrue(firstConsume.isSuccess)

        // Second scan of single-use token fails
        val secondConsume = engine.consumeHealthQrSession(qrSession.opaqueToken, "doctor_002")
        assertTrue("Re-use of single-use QR token must be rejected", secondConsume.isFailure)
    }

    @Test
    fun testWebhookReplayProtectionRejectsDuplicateDeliveries() {
        val eventId = "evt_stripe_payment_success_12345"
        val payload = """{"id": "$eventId", "type": "payment_intent.succeeded", "amount": 5000}"""

        val firstAttempt = engine.checkWebhookReplay(
            eventId = eventId,
            provider = "STRIPE",
            resourceId = "pi_12345",
            payloadBody = payload
        )
        assertTrue("First webhook delivery must be accepted", firstAttempt.isSuccess)

        // Duplicate replay attack or network redelivery
        val duplicateAttempt = engine.checkWebhookReplay(
            eventId = eventId,
            provider = "STRIPE",
            resourceId = "pi_12345",
            payloadBody = payload
        )
        assertTrue("Duplicate webhook delivery must be rejected to prevent replay", duplicateAttempt.isFailure)
    }

    @Test
    fun testFileUploadSecurityValidatesMimeTypesAndExecutableBlocks() {
        // Dangerous executable must be blocked immediately
        val dangerousFile = engine.validateFileUpload(
            filename = "malicious_payload.exe",
            contentType = "application/x-msdownload",
            sizeBytes = 1024,
            targetVault = "HEALTH_PRIVATE",
            callerUid = "user_001"
        )
        assertTrue("Executable files must be blocked", dangerousFile.isFailure)

        // Prohibited MIME type for health vault
        val invalidMime = engine.validateFileUpload(
            filename = "archive.zip",
            contentType = "application/zip",
            sizeBytes = 2048,
            targetVault = "HEALTH_PRIVATE",
            callerUid = "user_001"
        )
        assertTrue("ZIP archives not permitted in HEALTH_PRIVATE vault", invalidMime.isFailure)

        // Valid medical PDF within size limit
        val validPdf = engine.validateFileUpload(
            filename = "blood_test_results.pdf",
            contentType = "application/pdf",
            sizeBytes = 2 * 1024 * 1024L, // 2MB
            targetVault = "HEALTH_PRIVATE",
            callerUid = "user_001"
        )
        assertTrue("Valid PDF must be accepted in HEALTH_PRIVATE vault", validPdf.isSuccess)
    }

    @Test
    fun testSecurityScorecardReportsCompletePass() {
        val scorecard = engine.generateSecurityScorecard()
        assertEquals(100, scorecard.overallScore)
        assertEquals(ScoreStatus.PASS, scorecard.overallStatus)
        assertTrue(scorecard.categories.isNotEmpty())
        assertEquals(ScoreStatus.PASS, scorecard.categories["Authentication"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Health Data"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Financial Data"]?.status)
    }

    @Test
    fun testEmergencyControlsLifecycle() {
        val initialToggles = engine.emergencyToggles.value
        assertFalse(initialToggles.emergencyModeActive)
        assertFalse(initialToggles.registrationDisabled)

        val updated = initialToggles.copy(
            emergencyModeActive = true,
            registrationDisabled = true,
            marketplaceDisabled = true,
            lastUpdatedBy = "owner_001"
        )
        engine.updateEmergencyToggles(updated)

        val current = engine.emergencyToggles.value
        assertTrue(current.emergencyModeActive)
        assertTrue(current.registrationDisabled)
        assertTrue(current.marketplaceDisabled)

        // Verify alert was generated
        val alerts = engine.securityAlerts.value
        assertTrue(alerts.any { it.title.contains("Emergency Security Controls Updated") })
    }
}
