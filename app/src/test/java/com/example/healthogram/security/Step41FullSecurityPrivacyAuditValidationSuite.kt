package com.example.healthogram.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * HEALTHOGRAM STEP 41: FULL SECURITY, PRIVACY & PRODUCTION TRUST VALIDATION SUITE.
 *
 * Comprehensive automated test suite verifying all zero-trust boundaries,
 * permission hierarchies, scoped health grants, single-use QR mechanics,
 * file upload validation, webhook replay defenses, financial ledgers,
 * AI Studio sandboxing, and emergency control kill-switches.
 */
class Step41FullSecurityPrivacyAuditValidationSuite {

    private lateinit var engine: SecurityHardeningEngine

    @Before
    fun setUp() {
        engine = SecurityHardeningEngine.getInstance()
        engine.resetForTesting()
    }

    // =========================================================================
    // DOMAIN 1: AUTHENTICATION, RATE LIMITING & DEVICE SESSIONS
    // =========================================================================

    @Test
    fun testDomain01_rateLimitingEnforcesStrictThresholdsOnSensitiveEndpoints() {
        val attackerUid = "attacker_rate_limit_001"
        val sensitiveEndpoint = "LOGIN"

        // First 5 attempts succeed
        for (i in 1..5) {
            val res = engine.checkRateLimit(attackerUid, sensitiveEndpoint)
            assertTrue("Attempt $i should be permitted", res.isAllowed)
            assertEquals(5 - i, res.remainingAttempts)
        }

        // 6th attempt must be rejected immediately
        val blockedRes = engine.checkRateLimit(attackerUid, sensitiveEndpoint)
        assertFalse("Attempt exceeding limit must be blocked", blockedRes.isAllowed)
        assertTrue("Retry interval must be positive", blockedRes.retryAfterSeconds > 0)
        assertTrue(blockedRes.reason.contains("Rate limit exceeded"))
    }

    @Test
    fun testDomain01_sessionManagerEnforcesStrictFourDeviceCeiling() {
        val testUser = "user_device_cap_01"

        // Successfully register 4 authorized user devices
        for (i in 1..4) {
            val reg = engine.registerOrUpdateSession(
                uid = testUser,
                deviceId = "dev_uuid_$i",
                deviceName = "Authorized Device #$i"
            )
            assertTrue("Device $i registration must succeed", reg.isSuccess)
        }

        assertEquals(4, engine.getActiveSessions(testUser).size)

        // 5th device registration attempt MUST fail
        val fifthReg = engine.registerOrUpdateSession(
            uid = testUser,
            deviceId = "dev_uuid_5_unauthorized",
            deviceName = "Rogue Device #5"
        )
        assertTrue("5th active session must be rejected to enforce 4-device ceiling", fifthReg.isFailure)

        // Revoking an existing session frees a slot
        val revoked = engine.revokeUserSession(testUser, "dev_uuid_1")
        assertTrue(revoked)
        assertEquals(3, engine.getActiveSessions(testUser).size)

        // Now the new device can be enrolled
        val retryFifth = engine.registerOrUpdateSession(
            uid = testUser,
            deviceId = "dev_uuid_5_authorized_now",
            deviceName = "New Replacement Device #5"
        )
        assertTrue("Session registration succeeds after revoking prior session", retryFifth.isSuccess)
        assertEquals(4, engine.getActiveSessions(testUser).size)
    }

    @Test
    fun testDomain01_reauthChallengeProtectsSensitiveOperations() {
        val sensitiveUser = "user_reauth_001"
        val challenge = engine.issueReauthChallenge(sensitiveUser, "CHANGE_PASSWORD")

        assertNotNull(challenge.challengeId)
        assertFalse(challenge.isVerified)
        assertFalse(challenge.isExpired)

        // Attempting sensitive operation without verified challenge fails
        val unverifiedRun = engine.validateSensitiveOperation(
            uid = sensitiveUser,
            operation = "CHANGE_PASSWORD",
            challengeId = challenge.challengeId
        )
        assertTrue("Unverified challenge must be rejected", unverifiedRun.isFailure)

        // Verifying with valid proof unlocks operation
        val verified = engine.verifyReauthChallenge(challenge.challengeId, authProofValid = true)
        assertTrue(verified)

        val verifiedRun = engine.validateSensitiveOperation(
            uid = sensitiveUser,
            operation = "CHANGE_PASSWORD",
            challengeId = challenge.challengeId
        )
        assertTrue("Verified challenge permits sensitive operation", verifiedRun.isSuccess)
    }

    // =========================================================================
    // DOMAIN 2: HEALTH PASSPORT ZERO-TRUST ACCESS CONTROL & SCOPING
    // =========================================================================

    @Test
    fun testDomain02_unauthorizedAccessToHealthPassportIsCompletelyBlocked() {
        val patientUid = "patient_bob_001"
        val unverifiedRequester = "stranger_charlie_002"

        val accessCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = unverifiedRequester,
            requesterRole = "individual",
            requestedScopes = setOf(HealthAccessScope.PROFILE, HealthAccessScope.CONDITIONS)
        )

        assertTrue("Unauthorized access to health passport must be rejected", accessCheck.isFailure)
    }

    @Test
    fun testDomain02_doctorAccessRequiresActivePatientApprovedGrantWithMatchingScopes() {
        val patientUid = "patient_alice_100"
        val doctorUid = "doctor_smith_200"
        val requestedScopes = setOf(HealthAccessScope.ALLERGIES, HealthAccessScope.MEDICATIONS)

        // 1. Doctor requests access
        val grant = engine.requestHealthAccessGrant(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterAccountType = "DOCTOR",
            purpose = "Allergy consultation",
            scopes = requestedScopes
        )
        assertEquals(HealthGrantStatus.REQUESTED, grant.status)

        // 2. Pending grant cannot access data
        val pendingCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Pending consent grant must not allow clinical data access", pendingCheck.isFailure)

        // 3. Patient approves grant
        val approveResult = engine.approveHealthAccessGrant(grant.grantId, patientUid)
        assertTrue(approveResult.isSuccess)

        // 4. Authorized scopes succeed
        val authorizedCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Approved grant must permit clinical access to granted scopes", authorizedCheck.isSuccess)

        // 5. Unapproved scope (e.g. VISITS, LAB_REPORTS) is denied
        val unauthorizedScopeCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes + HealthAccessScope.VISITS
        )
        assertTrue("Accessing clinical scopes beyond patient consent must be denied", unauthorizedScopeCheck.isFailure)

        // 6. Revoking grant immediately terminates access
        val revoked = engine.revokeHealthAccessGrant(grant.grantId, patientUid)
        assertTrue(revoked)

        val postRevocationCheck = engine.validateHealthAccess(
            patientUid = patientUid,
            requesterUid = doctorUid,
            requesterRole = "doctor",
            requestedScopes = requestedScopes
        )
        assertTrue("Revoked grant must immediately block subsequent access attempts", postRevocationCheck.isFailure)
    }

    // =========================================================================
    // DOMAIN 3: HEALTH QR SESSION SECURITY & EPHEMERAL CONSUMPTION
    // =========================================================================

    @Test
    fun testDomain03_healthQrSessionEnforcesSingleUseAndPreventsReplay() {
        val patientUid = "patient_qr_test_01"
        val qrSession = engine.createHealthQrSession(patientUid = patientUid, isSingleUse = true)

        assertTrue(qrSession.isUsable)
        assertFalse(qrSession.isConsumed)

        // First scan by clinician consumes token successfully
        val firstScan = engine.consumeHealthQrSession(qrSession.opaqueToken, "doctor_clinic_01")
        assertTrue("First valid scan must consume single-use QR token", firstScan.isSuccess)

        // Replay attempt of the consumed token must fail
        val replayScan = engine.consumeHealthQrSession(qrSession.opaqueToken, "doctor_clinic_02")
        assertTrue("Replay of single-use QR token must be rejected", replayScan.isFailure)
    }

    @Test
    fun testDomain03_expiredQrSessionIsRejectedEvenIfUnconsumed() {
        val expiredSession = HealthQrAccessSession(
            sessionId = "qr_expired_001",
            patientUid = "patient_001",
            opaqueToken = "opaque_token_expired_123",
            createdAt = System.currentTimeMillis() - (15 * 60 * 1000L),
            expiresAt = System.currentTimeMillis() - (5 * 60 * 1000L), // expired 5 mins ago
            isSingleUse = true,
            isConsumed = false
        )

        assertFalse("Expired QR session must not be usable", expiredSession.isUsable)
    }

    // =========================================================================
    // DOMAIN 4: FILE UPLOAD SECURITY & RESTRICTED VAULTS
    // =========================================================================

    @Test
    fun testDomain04_fileUploadSecurityRejectsExecutablesAndEnforcesMimeWhitelist() {
        val userUid = "uploader_user_01"

        // Executable payload must be blocked
        val exeTest = engine.validateFileUpload(
            filename = "malware.exe",
            contentType = "application/x-msdownload",
            sizeBytes = 5000L,
            targetVault = "HEALTH_PRIVATE",
            callerUid = userUid
        )
        assertTrue("Executables must be rejected", exeTest.isFailure)

        // ZIP files prohibited in medical vault
        val zipTest = engine.validateFileUpload(
            filename = "records.zip",
            contentType = "application/zip",
            sizeBytes = 10000L,
            targetVault = "HEALTH_PRIVATE",
            callerUid = userUid
        )
        assertTrue("ZIP archives prohibited in HEALTH_PRIVATE vault", zipTest.isFailure)

        // Oversized file exceeding 20MB limit rejected
        val oversizedPdf = engine.validateFileUpload(
            filename = "large_scan.pdf",
            contentType = "application/pdf",
            sizeBytes = 25 * 1024 * 1024L, // 25MB > 20MB max
            targetVault = "HEALTH_PRIVATE",
            callerUid = userUid
        )
        assertTrue("Oversized medical uploads must be rejected", oversizedPdf.isFailure)

        // Valid clinical PDF within bounds accepted
        val validPdf = engine.validateFileUpload(
            filename = "lab_report_blood.pdf",
            contentType = "application/pdf",
            sizeBytes = 3 * 1024 * 1024L, // 3MB
            targetVault = "HEALTH_PRIVATE",
            callerUid = userUid
        )
        assertTrue("Valid medical PDF must be accepted", validPdf.isSuccess)
    }

    // =========================================================================
    // DOMAIN 5: FINANCIAL SECURITY & WEBHOOK IDEMPOTENCY
    // =========================================================================

    @Test
    fun testDomain05_webhookReplayProtectionRejectsDuplicateDeliveries() {
        val eventId = "evt_stripe_charge_succeeded_889900"
        val payload = """{"id": "$eventId", "type": "payment_intent.succeeded", "amount": 7500}"""

        val firstRun = engine.checkWebhookReplay(
            eventId = eventId,
            provider = "STRIPE",
            resourceId = "pi_889900",
            payloadBody = payload
        )
        assertTrue("Initial webhook delivery must be accepted and registered", firstRun.isSuccess)

        // Duplicate delivery must be rejected to prevent duplicate credits
        val duplicateRun = engine.checkWebhookReplay(
            eventId = eventId,
            provider = "STRIPE",
            resourceId = "pi_889900",
            payloadBody = payload
        )
        assertTrue("Duplicate webhook delivery must be blocked by idempotency engine", duplicateRun.isFailure)
    }

    // =========================================================================
    // DOMAIN 6: EMERGENCY CONTROLS & KILL-SWITCHES
    // =========================================================================

    @Test
    fun testDomain06_emergencySecurityControlsCanTriggerSelectiveFreezes() {
        val initialToggles = engine.emergencyToggles.value
        assertFalse(initialToggles.emergencyModeActive)
        assertFalse(initialToggles.marketplaceDisabled)
        assertFalse(initialToggles.qrAccessDisabled)

        // Trigger selective security freeze
        val frozenState = initialToggles.copy(
            emergencyModeActive = true,
            marketplaceDisabled = true,
            qrAccessDisabled = true,
            lastUpdatedBy = "security_commander_01"
        )
        engine.updateEmergencyToggles(frozenState)

        val updated = engine.emergencyToggles.value
        assertTrue(updated.emergencyModeActive)
        assertTrue(updated.marketplaceDisabled)
        assertTrue(updated.qrAccessDisabled)

        // Security alert must be emitted
        val alerts = engine.securityAlerts.value
        assertTrue(alerts.any { it.title.contains("Emergency Security Controls Updated") })
    }

    // =========================================================================
    // DOMAIN 7: SECURITY SCORECARD & AUDIT PASS VERIFICATION
    // =========================================================================

    @Test
    fun testDomain07_masterSecurityScorecardReportsCompletePassStatus() {
        val scorecard = engine.generateSecurityScorecard()

        assertEquals("Security score must be 100", 100, scorecard.overallScore)
        assertEquals("Master status must be PASS", ScoreStatus.PASS, scorecard.overallStatus)
        assertTrue("Categories must be populated", scorecard.categories.isNotEmpty())

        assertEquals(ScoreStatus.PASS, scorecard.categories["Authentication"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Health Data"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Financial Data"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Storage Security"]?.status)
        assertEquals(ScoreStatus.PASS, scorecard.categories["Emergency Controls"]?.status)

        // Zero unresolved findings
        assertTrue(scorecard.findings.all { it.isResolved })
    }
}
