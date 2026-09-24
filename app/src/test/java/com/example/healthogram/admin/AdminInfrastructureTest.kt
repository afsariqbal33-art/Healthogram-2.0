package com.example.healthogram.admin

import com.example.healthogram.core.AccountStatus
import com.example.healthogram.core.AccountType
import com.example.healthogram.core.User
import com.example.healthogram.core.VerificationBadgeType
import com.example.healthogram.core.VerificationStatus
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit & Integration Test Suite for Healthogram Admin Control Panel & Operations Center (Step 16).
 */
class AdminInfrastructureTest {

    private lateinit var repository: AdminRepository
    private lateinit var engine: AdminControlEngine

    @Before
    fun setup() {
        repository = AdminRepository.getInstance()
        engine = AdminControlEngine(repository)
    }

    @Test
    fun testRoleAndPermissionResolution() {
        // 1. Owner has all permissions including owner.finance
        val ownerPermissions = engine.resolvePermissions("usr_owner_root")
        assertTrue("Owner must possess dashboard.read", ownerPermissions.contains("dashboard.read"))
        assertTrue("Owner must possess emergency.manage", ownerPermissions.contains("emergency.manage"))
        assertTrue("Owner must possess owner.finance", ownerPermissions.contains("owner.finance"))

        // 2. Moderation admin has moderation permissions, but not finance or emergency
        val modPermissions = engine.resolvePermissions("usr_admin_mod")
        assertTrue("Moderator must possess moderation.read", modPermissions.contains("moderation.read"))
        assertTrue("Moderator must possess moderation.action", modPermissions.contains("moderation.action"))
        assertFalse("Moderator must NOT possess payments.refund", modPermissions.contains("payments.refund"))
        assertFalse("Moderator must NOT possess emergency.manage", modPermissions.contains("emergency.manage"))

        // 3. Finance admin has payment permissions, but not moderation or medical verification
        val finPermissions = engine.resolvePermissions("usr_admin_fin")
        assertTrue("Finance admin must possess payments.read", finPermissions.contains("payments.read"))
        assertTrue("Finance admin must possess payments.refund", finPermissions.contains("payments.refund"))
        assertFalse("Finance admin must NOT possess moderation.remove", finPermissions.contains("moderation.remove"))
        assertFalse("Finance admin must NOT possess verification.approve", finPermissions.contains("verification.approve"))

        // 4. Read-only auditor has read-only permissions and cannot mutate
        val auditPermissions = engine.resolvePermissions("usr_admin_audit")
        assertTrue("Auditor must possess audit_logs.read", auditPermissions.contains("audit_logs.read"))
        assertFalse("Auditor must NOT possess users.suspend", auditPermissions.contains("users.suspend"))
        assertFalse("Auditor must NOT possess verification.approve", auditPermissions.contains("verification.approve"))
        assertFalse("Auditor must NOT possess emergency.manage", auditPermissions.contains("emergency.manage"))
    }

    @Test
    fun testCountryScopedAdminRestrictions() {
        // admin.sa is restricted strictly to "SA"
        val saAdminUid = "usr_admin_sa"

        // Checking scope directly
        assertTrue("Saudi admin must be authorized for SA", engine.checkCountryScope(saAdminUid, "SA"))
        assertFalse("Saudi admin must NOT be authorized for AE", engine.checkCountryScope(saAdminUid, "AE"))
        assertFalse("Saudi admin must NOT be authorized for US", engine.checkCountryScope(saAdminUid, "US"))

        // Global owner has empty country scope, representing global authority
        assertTrue("Owner must have authority for SA", engine.checkCountryScope("usr_owner_root", "SA"))
        assertTrue("Owner must have authority for AE", engine.checkCountryScope("usr_owner_root", "AE"))
        assertTrue("Owner must have authority for US", engine.checkCountryScope("usr_owner_root", "US"))

        // Attempting to suspend a UAE user using Saudi Admin credentials must fail
        val uaeUser = repository.platformUsers.value["usr_patient_01"] // UAE user
        assertNotNull("UAE test user must exist", uaeUser)
        assertEquals("AE", uaeUser?.countryCode)

        try {
            engine.suspendUser(
                actorUid = saAdminUid,
                targetUid = uaeUser!!.uid,
                actionType = AccountActionType.TEMPORARY_SUSPEND,
                reasonCode = "cross_border_test",
                reason = "Testing cross-border boundary",
                pin = "1234"
            )
            fail("Expected SecurityException when Saudi admin acts on UAE user")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("not authorized for country AE") == true)
        }
    }

    @Test
    fun testUserSuspensionAndRestorationWorkflow() {
        val ownerUid = "usr_owner_root"
        val targetUid = "usr_doc_01"

        val initialUser = repository.platformUsers.value[targetUid]
        assertNotNull(initialUser)
        assertFalse(initialUser!!.isSuspended)

        // Suspend user
        val action = engine.suspendUser(
            actorUid = ownerUid,
            targetUid = targetUid,
            actionType = AccountActionType.TEMPORARY_SUSPEND,
            reasonCode = "suspected_credential_misuse",
            reason = "Security review pending",
            durationHours = 48,
            pin = "1234"
        )
        assertNotNull(action)
        assertEquals(AccountActionType.TEMPORARY_SUSPEND, action.actionType)

        // Verify user is updated in repository
        val suspendedUser = repository.platformUsers.value[targetUid]
        assertTrue(suspendedUser!!.isSuspended)
        assertEquals(AccountStatus.SUSPENDED, suspendedUser.accountStatus)

        // Verify audit log entry
        val latestLog = repository.auditLogs.value.first()
        assertEquals(ownerUid, latestLog.actorUid)
        assertEquals("users.suspend", latestLog.permissionUsed)
        assertEquals(targetUid, latestLog.targetId)

        // Restore user
        val restored = engine.restoreUser(ownerUid, targetUid, "Investigation concluded with clean record", pin = "1234")
        assertTrue(restored)

        val restoredUser = repository.platformUsers.value[targetUid]
        assertFalse(restoredUser!!.isSuspended)
        assertEquals(AccountStatus.ACTIVE, restoredUser.accountStatus)
    }

    @Test
    fun testVerificationApprovalAndRejection() {
        val ownerUid = "usr_owner_root"
        val labUid = "usr_lab_01"

        // Approve lab verification
        val approved = engine.approveVerification(
            actorUid = ownerUid,
            targetUid = labUid,
            badgeType = VerificationBadgeType.GREEN_CROSS,
            notes = "Validated accreditation with Central Diagnostic Board",
            pin = "1234"
        )
        assertTrue(approved)

        val verifiedUser = repository.platformUsers.value[labUid]
        assertEquals(VerificationStatus.VERIFIED, verifiedUser!!.verificationStatus)
        assertTrue(verifiedUser.isVerified)
        assertEquals(VerificationBadgeType.GREEN_CROSS, verifiedUser.verificationBadgeType)
        assertNotNull(verifiedUser.verificationExpiresAt)

        // Reject verification for another test user
        val testDoctor = User(
            uid = "usr_doc_pending_reject",
            displayName = "Dr. Test Fake",
            accountType = AccountType.DOCTOR,
            countryCode = "US",
            verificationStatus = VerificationStatus.UNDER_REVIEW
        )
        repository.updatePlatformUser(testDoctor)

        val rejected = engine.rejectVerification(
            actorUid = ownerUid,
            targetUid = testDoctor.uid,
            internalReason = "Forged MOH license number detected",
            customerReason = "The submitted documentation does not match official state registry records.",
            pin = "1234"
        )
        assertTrue(rejected)

        val rejectedUser = repository.platformUsers.value[testDoctor.uid]
        assertEquals(VerificationStatus.REJECTED, rejectedUser!!.verificationStatus)
        assertFalse(rejectedUser.isVerified)
    }

    @Test
    fun testSocialModerationAndAppeals() {
        val modUid = "usr_admin_mod"
        val report = repository.moderationReports.value["rep_001"]
        assertNotNull(report)

        // Resolve report with action
        val resolved = engine.resolveModerationReport(
            actorUid = modUid,
            reportId = report!!.reportId,
            resolution = "Content removed and user warned for medical misinformation.",
            actionTaken = true,
            targetUidToWarn = report.reportedUid
        )
        assertTrue(resolved)

        val updatedReport = repository.moderationReports.value[report.reportId]
        assertEquals(ModerationReportStatus.ACTION_TAKEN, updatedReport!!.status)
        assertEquals(modUid, updatedReport.assignedAdmin)

        // User submits appeal
        val appeal = ModerationAppeal(
            uid = report.reportedUid,
            originalActionId = report.reportId,
            reason = "The study was cited from a peer-reviewed publication."
        )
        repository.recordModerationAppeal(appeal)

        // Moderator reviews appeal and rejects
        val appealProcessed = engine.reviewAppeal(
            actorUid = modUid,
            appealId = appeal.appealId,
            approved = false,
            resolution = "Publication was retracted by publisher."
        )
        assertTrue(appealProcessed)

        val updatedAppeal = repository.moderationAppeals.value[appeal.appealId]
        assertEquals(AppealStatus.REJECTED, updatedAppeal!!.status)
    }

    @Test
    fun testEmergencyKillSwitchControls() {
        val ownerUid = "usr_owner_root"
        val controlKey = "emergency_marketplace_stop"

        val initialControl = repository.emergencyControls.value[controlKey]
        assertNotNull(initialControl)
        assertFalse(initialControl!!.isTriggered)

        // 1. Non-authorized role cannot trigger kill switch
        val modUid = "usr_admin_mod"
        try {
            engine.triggerEmergencyStop(modUid, controlKey, "Attempt by mod", "1234")
            fail("Expected IllegalArgumentException or SecurityException when non-authorized admin attempts emergency stop")
        } catch (e: Exception) {
            // Expected
        }

        // 2. Owner triggers kill switch
        val triggered = engine.triggerEmergencyStop(
            actorUid = ownerUid,
            controlKey = controlKey,
            reason = "Payment provider reporting systemic double-billing glitch.",
            pin = "1234"
        )
        assertTrue(triggered)

        val activeControl = repository.emergencyControls.value[controlKey]
        assertTrue(activeControl!!.isTriggered)
        assertEquals(ownerUid, activeControl.triggeredBy)

        // 3. Clear kill switch
        val cleared = engine.clearEmergencyStop(
            actorUid = ownerUid,
            controlKey = controlKey,
            reason = "Payment provider hotfix verified.",
            pin = "1234"
        )
        assertTrue(cleared)

        val restoredControl = repository.emergencyControls.value[controlKey]
        assertFalse(restoredControl!!.isTriggered)
    }

    @Test
    fun testReauthenticationAndMfaValidation() {
        val ownerUid = "usr_owner_root"

        // Invalid credentials fail
        assertFalse(engine.verifyReauth(ownerUid, "wrong_pin"))

        // Valid credentials succeed and update lastReauthAt
        assertTrue(engine.verifyReauth(ownerUid, "1234"))
        assertTrue(engine.isReauthFresh(ownerUid))
    }

    @Test
    fun testHealthSecurityDataIsolation() {
        val hsecUid = "usr_admin_hsec"
        val event = repository.healthSecurityEvents.value["hse_001"]
        assertNotNull(event)

        val resolved = engine.reviewHealthSecurityEvent(
            actorUid = hsecUid,
            eventId = event!!.eventId,
            resolution = "Rogue IP blocked from QR access endpoint.",
            markFalsePositive = false
        )
        assertTrue(resolved)

        val updatedEvent = repository.healthSecurityEvents.value[event.eventId]
        assertEquals(HealthSecurityEventStatus.RESOLVED, updatedEvent!!.status)

        // Verify search does not leak health passport medical content
        val results = engine.searchEntities(hsecUid, "diabetes")
        // No medical diagnosis content should be returned by search
        val containsDiagnosis = results.any { it.title.contains("diabetes", ignoreCase = true) || it.subtitle.contains("diagnosis", ignoreCase = true) }
        assertFalse("Medical diagnoses must never be exposed to admin search", containsDiagnosis)
    }
}
